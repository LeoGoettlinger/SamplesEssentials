package org.jdbi.v3.core.mapper.reflect.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import org.jdbi.v3.core.qualifier.QualifiedType;

public abstract class PojoProperties<T> {
   private final Type type;

   protected PojoProperties(Type type) {
      this.type = type;
   }

   public Type getType() {
      return this.type;
   }

   public abstract Map<String, ? extends PojoProperties.PojoProperty<T>> getProperties();

   public abstract PojoProperties.PojoBuilder<T> create();

   public interface PojoBuilder<T> {
      void set(String property, Object value);

      default void set(PojoProperties.PojoProperty<T> property, Object value) {
         this.set(property.getName(), value);
      }

      T build();
   }

   public interface PojoProperty<T> {
      String getName();

      QualifiedType<?> getQualifiedType();

      <A extends Annotation> Optional<A> getAnnotation(Class<A> anno);

      Object get(T pojo);
   }
}
