package org.jdbi.v3.core.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jdbi.v3.core.extension.ExtensionConfigurer;
import org.jdbi.v3.meta.Alpha;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Alpha
public @interface UseExtensionConfigurer {
   Class<? extends ExtensionConfigurer> value();
}
