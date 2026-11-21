package org.jdbi.v3.core.mapper.reflect.internal;

import io.leangen.geantyref.GenericTypeReflector;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.internal.ConfigCache;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.internal.JdbiClassUtils;
import org.jdbi.v3.core.internal.exceptions.Unchecked;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;

public class BeanPropertiesFactory {
   private static final ConfigCache<Type, BeanPropertiesFactory.BeanPojoProperties.PropertiesHolder<?>> PROPERTY_CACHE = ConfigCaches.declare(
      (config, type) -> new BeanPropertiesFactory.BeanPojoProperties.PropertiesHolder(config, type)
   );

   private BeanPropertiesFactory() {
   }

   public static PojoProperties<?> propertiesFor(Type t, ConfigRegistry config) {
      return new BeanPropertiesFactory.BeanPojoProperties(t, config);
   }

   private static boolean shouldSeeProperty(PropertyDescriptor pd) {
      Method read = pd.getReadMethod();
      return read == null ? pd.getWriteMethod() != null : read.getParameterCount() == 0 && read.getDeclaringClass() != Object.class;
   }

   static class BeanPojoProperties<T> extends PojoProperties<T> {
      private final ConfigRegistry config;

      BeanPojoProperties(Type type, ConfigRegistry config) {
         super(type);
         this.config = config;
      }

      @Override
      public Map<String, BeanPropertiesFactory.BeanPojoProperties.BeanPojoProperty<T>> getProperties() {
         return (Map<String, BeanPropertiesFactory.BeanPojoProperties.BeanPojoProperty<T>>)BeanPropertiesFactory.PROPERTY_CACHE
            .get(this.getType(), this.config)
            .properties;
      }

      @Override
      public PojoProperties.PojoBuilder<T> create() {
         final BeanPropertiesFactory.BeanPojoProperties.PropertiesHolder<T> holder = (BeanPropertiesFactory.BeanPojoProperties.PropertiesHolder<T>)BeanPropertiesFactory.PROPERTY_CACHE
            .get(this.getType(), this.config);
         final T instance = holder.getInstance();
         return new PojoProperties.PojoBuilder<T>() {
            @Override
            public void set(String property, Object value) {
               holder.properties.get(property).setter().accept(instance, value);
            }

            @Override
            public T build() {
               return instance;
            }
         };
      }

      static class BeanPojoProperty<T> implements PojoProperties.PojoProperty<T> {
         final PropertyDescriptor descriptor;
         final QualifiedType<?> qualifiedType;
         final ConcurrentMap<Class<?>, Optional<Annotation>> annoCache = new ConcurrentHashMap<>();
         final Function<Object, Object> getter;
         final BiConsumer<Object, Object> setter;
         final Type actualBeanType;

         BeanPojoProperty(ConfigRegistry config, PropertyDescriptor property, Type actualBeanType) {
            this.descriptor = property;
            this.actualBeanType = actualBeanType;
            this.qualifiedType = this.determineQualifiedType();
            this.getter = Optional.ofNullable(this.descriptor.getReadMethod())
               .map(method -> JdbiClassUtils.unreflect(config, method))
               .map(mh -> mh.asType(MethodType.methodType(Object.class, Object.class)))
               .map(mh -> Unchecked.function(mh::invokeExact))
               .orElse(null);
            this.setter = Optional.ofNullable(this.descriptor.getWriteMethod())
               .map(method -> JdbiClassUtils.unreflect(config, method))
               .map(mh -> mh.asType(MethodType.methodType(void.class, Object.class, Object.class)))
               .map(mh -> Unchecked.biConsumer(mh::invokeExact))
               .orElse(null);
         }

         protected Function<Object, Object> getter() {
            if (this.getter == null) {
               throw new UnableToCreateStatementException(
                  String.format("No getter method found for bean property [%s] on [%s]", this.getName(), this.qualifiedType)
               );
            } else {
               return this.getter;
            }
         }

         protected BiConsumer<Object, Object> setter() {
            if (this.setter == null) {
               throw new UnableToCreateStatementException(
                  String.format("No setter method found for bean property [%s] on [%s]", this.getName(), this.qualifiedType)
               );
            } else {
               return this.setter;
            }
         }

         @Override
         public String getName() {
            return this.descriptor.getName();
         }

         @Override
         public QualifiedType<?> getQualifiedType() {
            return this.qualifiedType;
         }

         private QualifiedType<?> determineQualifiedType() {
            Parameter setterParam = Optional.ofNullable(this.descriptor.getWriteMethod())
               .map(m -> m.getParameterCount() > 0 ? m.getParameters()[0] : null)
               .orElse(null);
            return QualifiedType.of(
                  GenericTypeReflector.reduceBounded(
                        GenericTypeReflector.annotate(
                           GenericTypeReflector.resolveExactType(
                              Optional.ofNullable(this.descriptor.getReadMethod())
                                 .map(m -> GenericTypeReflector.getExactReturnType(m, this.actualBeanType))
                                 .orElseGet(() -> GenericTypeReflector.getExactParameterTypes(this.descriptor.getWriteMethod(), this.actualBeanType)[0]),
                              this.actualBeanType
                           )
                        )
                     )
                     .getType()
               )
               .withAnnotations(new Qualifiers().findFor(this.descriptor.getReadMethod(), this.descriptor.getWriteMethod(), setterParam));
         }

         @Override
         public <A extends Annotation> Optional<A> getAnnotation(Class<A> anno) {
            return this.annoCache
               .computeIfAbsent(
                  anno,
                  x -> Stream.of(this.descriptor.getWriteMethod(), this.descriptor.getReadMethod())
                     .filter(Objects::nonNull)
                     .map(m -> m.getAnnotation(anno))
                     .filter(Objects::nonNull)
                     .findFirst()
                     .map(Annotation.class::cast)
               )
               .map(anno::cast);
         }

         @Override
         public Object get(T pojo) {
            return this.getter().apply(pojo);
         }
      }

      static class PropertiesHolder<T> {
         final Class<?> clazz;
         final Map<String, BeanPropertiesFactory.BeanPojoProperties.BeanPojoProperty<?>> properties;
         final JdbiClassUtils.MethodHandleHolder<?> ctorHandleHolder;

         PropertiesHolder(ConfigRegistry config, Type type) {
            this.clazz = GenericTypes.getErasedType(type);

            try {
               this.properties = Arrays.stream(Introspector.getBeanInfo(this.clazz).getPropertyDescriptors())
                  .filter(BeanPropertiesFactory::shouldSeeProperty)
                  .map(p -> new BeanPropertiesFactory.BeanPojoProperties.BeanPojoProperty(config, p, this.addMissingWildcards(type)))
                  .collect(Collectors.toMap(PojoProperties.PojoProperty::getName, Function.identity()));
            } catch (IntrospectionException var4) {
               throw new IllegalArgumentException("Failed to inspect bean " + this.clazz, var4);
            }

            this.ctorHandleHolder = JdbiClassUtils.findConstructor(config, this.clazz);
         }

         public T getInstance() {
            return (T)this.ctorHandleHolder.invoke(handle -> (Object)handle.invokeExact());
         }

         private Type addMissingWildcards(Type type) {
            return GenericTypeReflector.isMissingTypeParameters(type) ? GenericTypeReflector.addWildcardParameters(GenericTypeReflector.erase(type)) : type;
         }
      }
   }
}
