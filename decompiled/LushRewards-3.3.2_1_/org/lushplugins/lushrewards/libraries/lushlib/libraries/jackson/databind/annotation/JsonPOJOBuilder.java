package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JacksonAnnotation;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonPOJOBuilder {
   String DEFAULT_BUILD_METHOD = "build";
   String DEFAULT_WITH_PREFIX = "with";

   String buildMethodName() default "build";

   String withPrefix() default "with";

   public static class Value {
      public final String buildMethodName;
      public final String withPrefix;

      public Value(JsonPOJOBuilder ann) {
         this(ann.buildMethodName(), ann.withPrefix());
      }

      public Value(String buildMethodName, String withPrefix) {
         this.buildMethodName = buildMethodName;
         this.withPrefix = withPrefix;
      }
   }
}
