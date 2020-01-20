package com.mantuliu.retry.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionRetryUtils {

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes)
    {
        if(clazz==null) {
            throw new IllegalArgumentException("clazz must not be null");
        }
        if(name==null||"".equals(name)) {
            throw new IllegalArgumentException("Method name must not be null");
        }
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods();
            Method[] arrayOfMethod1; int j = (arrayOfMethod1 = methods).length; 
            for (int i = 0; i < j; i++) { 
                Method method = arrayOfMethod1[i];
                if ((name.equals(method.getName())) && ((paramTypes == null) || (equalsClass(paramTypes, method.getParameterTypes())))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
         return null;
    }
    
    public static boolean equalsClass(Class[] a, Class[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;
        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
        	Class o1 = a[i];
        	Class o2 = a2[i];
        	if(o2.isAssignableFrom(o1)){
                continue;
        	}
        	if(o1.equals(o2)){
        		continue;
        	}
        	return false;
        }
        return true;
    }
}
