package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonschema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JacksonAnnotation;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
@Deprecated
public @interface JsonSerializableSchema {
   String NO_VALUE = "##irrelevant";

   String id() default "";

   String schemaType() default "any";

   @Deprecated
   String schemaObjectPropertiesDefinition() default "##irrelevant";

   @Deprecated
   String schemaItemDefinition() default "##irrelevant";
}
