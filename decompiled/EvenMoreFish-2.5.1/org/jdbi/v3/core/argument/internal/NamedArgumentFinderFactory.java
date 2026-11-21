package org.jdbi.v3.core.argument.internal;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jdbi.v3.core.annotation.internal.JdbiAnnotations;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.BeanPropertyArguments;
import org.jdbi.v3.core.argument.ObjectFieldArguments;
import org.jdbi.v3.core.argument.ObjectMethodArguments;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.reflect.internal.PojoProperties;
import org.jdbi.v3.core.qualifier.QualifiedType;

public interface NamedArgumentFinderFactory {
   NamedArgumentFinderFactory BEAN = new NamedArgumentFinderFactory.Bean();
   NamedArgumentFinderFactory POJO = new NamedArgumentFinderFactory.Pojo();
   NamedArgumentFinderFactory FIELDS = new NamedArgumentFinderFactory.Fields();
   NamedArgumentFinderFactory METHODS = new NamedArgumentFinderFactory.Methods();

   NamedArgumentFinderFactory.PrepareKey keyFor(String prefix, Object value);

   Function<String, Optional<Function<Object, Argument>>> prepareFor(
      ConfigRegistry config, Function<QualifiedType<?>, Function<Object, Argument>> argumentFactoryLookup, String prefix, Object example, Type type
   );

   public abstract static class Base implements NamedArgumentFinderFactory {
      @Override
      public NamedArgumentFinderFactory.PrepareKey keyFor(String prefix, Object value) {
         return new NamedArgumentFinderFactory.PrepareKey(this.getClass(), value.getClass(), prefix);
      }
   }

   public static class Bean extends NamedArgumentFinderFactory.Pojo {
      @Override
      public Function<String, Optional<Function<Object, Argument>>> prepareFor(
         ConfigRegistry config, Function<QualifiedType<?>, Function<Object, Argument>> argumentFactoryLookup, String prefix, Object example, Type type
      ) {
         return this.forPojoProps(argumentFactoryLookup, new BeanPropertyArguments(prefix, example, config));
      }
   }

   public static class Fields extends NamedArgumentFinderFactory.ReflectionBase {
      @Override
      BiFunction<String, Object, BiFunction<String, ConfigRegistry, Optional<Function<Object, TypedValue>>>> create() {
         return (prefix, example) -> new ObjectFieldArguments(prefix, example)::getter;
      }
   }

   public static class Methods extends NamedArgumentFinderFactory.ReflectionBase {
      @Override
      BiFunction<String, Object, BiFunction<String, ConfigRegistry, Optional<Function<Object, TypedValue>>>> create() {
         return (prefix, example) -> new ObjectMethodArguments(prefix, example)::getter;
      }
   }

   public static class Pojo extends NamedArgumentFinderFactory.Base {
      @Override
      public Function<String, Optional<Function<Object, Argument>>> prepareFor(
         ConfigRegistry config, Function<QualifiedType<?>, Function<Object, Argument>> argumentFactoryLookup, String prefix, Object example, Type type
      ) {
         return this.forPojoProps(argumentFactoryLookup, new PojoPropertyArguments(prefix, example, type, config));
      }

      Function<String, Optional<Function<Object, Argument>>> forPojoProps(
         Function<QualifiedType<?>, Function<Object, Argument>> argumentFactoryLookup, PojoPropertyArguments ppa
      ) {
         return name -> Optional.ofNullable(ppa.properties.getProperties().get(name))
            .map(PojoProperties.PojoProperty.class::cast)
            .filter(JdbiAnnotations::isBound)
            .map(property -> {
               Function<Object, Argument> arg = argumentFactoryLookup.apply(property.getQualifiedType());
               return pojo -> arg.apply(property.get(pojo));
            });
      }
   }

   public static final class PrepareKey {
      final Class<?> factoryClass;
      final Type type;
      final String prefix;

      PrepareKey(Class<?> factoryClass, Type type, String prefix) {
         this.factoryClass = factoryClass;
         this.type = type;
         this.prefix = prefix;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.factoryClass, this.prefix, this.type);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            NamedArgumentFinderFactory.PrepareKey other = (NamedArgumentFinderFactory.PrepareKey)obj;
            return Objects.equals(this.factoryClass, other.factoryClass) && Objects.equals(this.prefix, other.prefix) && Objects.equals(this.type, other.type);
         }
      }
   }

   public abstract static class ReflectionBase extends NamedArgumentFinderFactory.Base {
      @Override
      public Function<String, Optional<Function<Object, Argument>>> prepareFor(
         ConfigRegistry config, Function<QualifiedType<?>, Function<Object, Argument>> argumentFactoryLookup, String prefix, Object example, Type type
      ) {
         return name -> this.create().apply(prefix, example).apply(name, config).map(getter -> {
            Function<Object, Argument> arg = argumentFactoryLookup.apply(getter.apply(example).getType());
            return obj -> arg.apply(((TypedValue)getter.apply(obj)).getValue());
         });
      }

      abstract BiFunction<String, Object, BiFunction<String, ConfigRegistry, Optional<Function<Object, TypedValue>>>> create();
   }
}
