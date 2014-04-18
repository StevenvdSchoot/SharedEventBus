package com.rushteamc.lib.SharedEventBus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe
{
	public enum Priority
	{
		HIGHEST,
		HIGH,
		MEDIUM,
		LOW,
		LOWEST,
		MONITORING
	}
	
	Priority priority() default Priority.MEDIUM;
	boolean instanceOf() default false;
}
