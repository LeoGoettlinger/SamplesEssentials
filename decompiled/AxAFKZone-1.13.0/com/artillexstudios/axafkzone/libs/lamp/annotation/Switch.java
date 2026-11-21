package com.artillexstudios.axafkzone.libs.lamp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotSender.ImpliesNotSender
public @interface Switch {
   String value() default "";

   boolean defaultValue() default false;
}
