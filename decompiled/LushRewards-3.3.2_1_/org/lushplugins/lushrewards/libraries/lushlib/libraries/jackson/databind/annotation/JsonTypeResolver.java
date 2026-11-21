package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JacksonAnnotation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeResolverBuilder;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonTypeResolver {
   Class<? extends TypeResolverBuilder<?>> value();
}
