package com.mantuliu.retry.core.common;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;


@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RetryStrategy {
	TimeUnit unit() default TimeUnit.SECONDS;
	int interval() default 1;
	int counts() default 3;
}
