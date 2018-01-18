package com.mantuliu.retry.core.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringCommons {
	private static ApplicationContext context;

	public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		SpringCommons.context = context;
	}
	
    /**
     * 获取对象   
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
     public static Object getBean(String name) throws BeansException {
         return context.getBean(name);
     }
     
     /**
      * 获取对象   
      * @param name
      * @return Object 一个以所给类注册的bean的实例
      * @throws BeansException
      */
      public static Object getBean(Class name) throws BeansException {
          return context.getBean(name);
      }
     
     /**
      * 获取类型为requiredType的对象
      * 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
      * @param name       bean注册名
      * @param requiredType 返回对象类型
      * @return Object 返回requiredType类型对象
      * @throws BeansException
      */
      public static Object getBean(String name, Class requiredType) throws BeansException {
          return context.getBean(name, requiredType);
      }
     
      /**
      * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true 
      * @param name
      * @return boolean
      */
      public static boolean containsBean(String name) {
          return context.containsBean(name);
      }
}
