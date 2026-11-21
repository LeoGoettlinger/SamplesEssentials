package org.jdbi.v3.core.qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.config.internal.ConfigCache;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.internal.CollectionCollectors;

public class Qualifiers implements JdbiConfig<Qualifiers> {
   private static final ConfigCache<AnnotatedElement[], Set<Annotation>> QUALIFIER_CACHE = ConfigCaches.declare(
      elements -> elements.length == 1 ? elements[0] : new HashSet<>(Arrays.asList(elements)), Qualifiers::getQualifiers
   );
   private static final ConfigCache<AnnotatedElement, QualifiedType<?>> QUALIFIED_TYPE_CACHE = ConfigCaches.declare(
      type -> QualifiedType.of((Type)type).withAnnotations(getQualifiers(type))
   );
   private ConfigRegistry registry;

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   public <ELEM extends AnnotatedElement & Type> QualifiedType<?> qualifiedTypeOf(ELEM type) {
      return this.registry == null ? QualifiedType.of(type).withAnnotations(getQualifiers(type)) : QUALIFIED_TYPE_CACHE.get(type, this.registry);
   }

   public Set<Annotation> findFor(AnnotatedElement... elements) {
      return this.registry == null ? getQualifiers(elements) : QUALIFIER_CACHE.get(elements, this.registry);
   }

   private static Set<Annotation> getQualifiers(AnnotatedElement... elements) {
      return Arrays.stream(elements)
         .filter(Objects::nonNull)
         .map(AnnotatedElement::getAnnotations)
         .flatMap(Arrays::stream)
         .filter(anno -> anno.annotationType().isAnnotationPresent(Qualifier.class))
         .collect(CollectionCollectors.toUnmodifiableSet());
   }

   public Qualifiers createCopy() {
      return new Qualifiers();
   }
}
