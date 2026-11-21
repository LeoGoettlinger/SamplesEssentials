package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JacksonAnnotation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.EnumNamingStrategy;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface EnumNaming {
   Class<? extends EnumNamingStrategy> value();
}
