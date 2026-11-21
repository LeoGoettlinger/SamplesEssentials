package de.exlll.configlib;

import java.lang.reflect.AnnotatedType;

record SerializerContextImpl(ConfigurationProperties properties, ConfigurationElement<?> element, AnnotatedType annotatedType) implements SerializerContext {
   SerializerContextImpl(ConfigurationProperties properties, ConfigurationElement<?> element, AnnotatedType annotatedType) {
      properties = Validator.requireNonNull(properties, "configuration properties");
      element = Validator.requireNonNull(element, "configuration element");
      annotatedType = Validator.requireNonNull(annotatedType, "annotated type");
      this.properties = properties;
      this.element = element;
      this.annotatedType = annotatedType;
   }
}
