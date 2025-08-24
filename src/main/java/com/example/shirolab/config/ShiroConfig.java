package com.example.shirolab.config;

import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro 安全配置（基于 Shiro 1.2.4 + shiro-spring）。
 *
 * 关键点概览：
 * 1) Realm：使用 IniRealm 从 classpath:shiro.ini 读取用户/角色/权限数据（适合无数据库演示）。
 * 2) SecurityManager：Web 场景的核心组件，挂接 Realm 与 RememberMeManager 等，实现 Subject 的创建与管理。
 * 3) RememberMe：通过 Cookie 记住主体信息；在未登录会话下仍可恢复身份（匹配 user 过滤器时放行）。
 * 4) Web 过滤器：ShiroFilterFactoryBean 生成的 shiroFilter 拦截所有请求并按 URL 过滤链判定访问控制。
 * 5) 过滤器接入：DelegatingFilterProxy("shiroFilter") 将 Servlet 请求交给 Spring 容器中的同名 Bean 处理。
 * 6) 注解支持：AuthorizationAttributeSourceAdvisor 使 @RequiresRoles 等注解生效。
 *
 * 请求流转简述：
 * Browser -> DelegatingFilterProxy(shiroFilter) -> AbstractShiroFilter ->
 *   创建/恢复 Subject(会话/RememberMe) -> 匹配过滤链(anon/user/roles/...) ->
 *   通过后进入 Spring MVC(Interceptor -> Controller)。
 */
@Configuration
public class ShiroConfig {

    /**
     * Realm 数据源：从 shiro.ini 读取 [users]/[roles]/[urls] 等配置。
     * 适用于演示与小型场景；接数据库时可改为 JdbcRealm 或自定义 Realm。
     */
    @Bean
    public IniRealm iniRealm() {
        return new IniRealm("classpath:shiro.ini");
    }

    /**
     * Web 安全管理器：承载认证/授权、会话与 RememberMe 管理。
     *
     * 注：此处启用了 RememberMeManager，使得使用 user 过滤器的路径允许“被记住的主体”访问。
     */
    @Bean
    public DefaultWebSecurityManager securityManager(IniRealm iniRealm, CookieRememberMeManager rememberMeManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(iniRealm);
        securityManager.setRememberMeManager(rememberMeManager);
        return securityManager;
    }

    /**
     * RememberMe Cookie 定义。
     * - 名称与前端勾选“记住我”的功能绑定（Shiro 默认也是 rememberMe）。
     * - HttpOnly 提升安全性；MaxAge 控制记住周期（单位秒）。
     */
    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(2592000); // 30 天
        return cookie;
    }

    /**
     * RememberMe 管理器：负责将主体序列化到 Cookie 及后续恢复。
     *
     * 反序列化触发点（请求带有 rememberMe Cookie 且当前未登录时，会在构建 Subject 阶段触发）：
     * - org.apache.shiro.web.servlet.AbstractShiroFilter#doFilterInternal(..) 进入请求处理
     * - -> SecurityManager.createSubject(..) / RememberMeManager#getRememberedPrincipals(..)
     * - -> org.apache.shiro.web.mgt.CookieRememberMeManager#getRememberedSerializedIdentity(..)
     * - -> org.apache.shiro.mgt.AbstractRememberMeManager#convertBytesToPrincipals(..)
     * - -> org.apache.shiro.io.DefaultSerializer#deserialize(byte[])   // Java 反序列化
     *
     * 风险提示（Shiro 1.2.4）：若使用默认密钥或密钥泄露，存在被伪造 Cookie 的风险。
     * 生产环境应显式设置随机 cipherKey 并妥善保密与轮换。
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie) {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        manager.setCookie(rememberMeCookie);
        return manager;
    }

    /**
     * Shiro Web 过滤器工厂：生成核心 Filter（AbstractShiroFilter）。
     * - loginUrl/successUrl：未通过 authc 时的跳转与登录成功后的默认页。
     * - filterChainDefinitionMap：URL 匹配顺序很重要，按定义先后逐条匹配。
     *   这里使用 user 而非 authc，使“被记住的用户”也能访问受保护资源；若需强制实时登录改为 authc。
     *
     * 与反序列化的关系：当路径命中 user/roles 等需要主体的过滤器时，若当前会话未登录，
     * AbstractShiroFilter 会尝试通过 RememberMe 从 Cookie 恢复主体（见上方 rememberMeManager 注释中的调用链），
     * 恢复过程最终在 DefaultSerializer#deserialize(byte[]) 发生 Java 反序列化。
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        factoryBean.setLoginUrl("/login");
        factoryBean.setSuccessUrl("/");

        Map<String, String> chain = new LinkedHashMap<String, String>();
        chain.put("/login", "anon");
        chain.put("/login/form", "anon");
        chain.put("/logout", "logout");
        chain.put("/inject", "anon");  // 允许匿名访问注入接口

        chain.put("/css/**", "anon");
        chain.put("/js/**", "anon");
        chain.put("/images/**", "anon");
        // 使用 user 过滤器，允许已登录或被记住的用户访问
        // 命中以下规则且当前未登录时，会走 RememberMe 恢复主体，从而触发反序列化（见上方调用链说明）
        chain.put("/admin/**", "user, roles[admin]");
        chain.put("/user/**", "user, roles[user]");
        chain.put("/**", "user");

        factoryBean.setFilterChainDefinitionMap(chain);
        return factoryBean;
    }

    /**
     * 将 Shiro 过滤器接入到 Servlet Filter 链。
     * DelegatingFilterProxy 会按名称定位 Spring 容器中的 "shiroFilter" Bean 并把请求转交给它。
     */
    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> delegatingFilterProxy() {
        FilterRegistrationBean<DelegatingFilterProxy> registration = new FilterRegistrationBean<DelegatingFilterProxy>();
        DelegatingFilterProxy proxy = new DelegatingFilterProxy("shiroFilter");
        registration.setFilter(proxy);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }

    /**
     * 启用 Shiro 注解支持：@RequiresRoles、@RequiresPermissions 等。
     * 需与 Spring AOP 协同工作，使注解在方法调用时参与授权判定。
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}


