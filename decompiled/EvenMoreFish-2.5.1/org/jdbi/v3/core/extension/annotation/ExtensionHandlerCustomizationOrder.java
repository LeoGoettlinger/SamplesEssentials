package org.jdbi.v3.core.extension.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jdbi.v3.meta.Alpha;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Alpha
public @interface ExtensionHandlerCustomizationOrder {
   Class<? extends Annotation>[] value();
}
