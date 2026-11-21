package org.jdbi.v3.sqlobject.customizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jdbi.v3.meta.Beta;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Definitions.class)
@Beta
public @interface Definition {
   String UNDEFINED = "__definition_undefined__";

   String key() default "__definition_undefined__";

   String value() default "__definition_undefined__";
}
