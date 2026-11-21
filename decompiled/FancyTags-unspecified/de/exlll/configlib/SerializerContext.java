package de.exlll.configlib;

import java.lang.reflect.AnnotatedType;

public interface SerializerContext {
   ConfigurationProperties properties();

   ConfigurationElement<?> element();

   AnnotatedType annotatedType();
}
