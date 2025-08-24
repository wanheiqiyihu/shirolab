package com.example.shirolab;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyUtilsTest {

    @Test
    public void testPropertyUtils() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        PersonBean personBean = new PersonBean("asd", 123);
        
        // 测试 PropertyUtils.getProperty
        Object age = PropertyUtils.getProperty(personBean, "age");
        Object name = PropertyUtils.getProperty(personBean, "name");
        
        // 验证结果
        assertEquals(123, age);
        assertEquals("asd", name);
        
        System.out.println("Age: " + age);
        System.out.println("Name: " + name);
    }
}
