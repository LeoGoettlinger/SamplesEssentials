package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

class ObjectFieldDiscoverer implements FieldDiscoverer<Map<Field, Object>> {
   static final ObjectFieldDiscoverer EMPTY_CONSTRUCTOR_INSTANCE = new ObjectFieldDiscoverer(type -> {
      try {
         Constructor<?> constructor = GenericTypeReflector.erase(type.getType()).getDeclaredConstructor();
         constructor.setAccessible(true);
         return () -> {
            try {
               return constructor.newInstance();
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException var2x) {
               throw new RuntimeException(var2x);
            }
         };
      } catch (NoSuchMethodException var2) {
         return null;
      }
   }, "Objects must have a zero-argument constructor to be able to create new instances", false);
   private final CheckedFunction<AnnotatedType, Supplier<Object>, SerializationException> instanceFactory;
   private final String instanceUnavailableErrorMessage;
   private final boolean requiresInstanceCreation;

   ObjectFieldDiscoverer(
      final CheckedFunction<AnnotatedType, Supplier<Object>, SerializationException> instanceFactory,
      @Nullable final String instanceUnavailableErrorMessage,
      final boolean requiresInstanceCreation
   ) {
      this.instanceFactory = instanceFactory;
      if (instanceUnavailableErrorMessage == null) {
         this.instanceUnavailableErrorMessage = "Unable to create instances for this type!";
      } else {
         this.instanceUnavailableErrorMessage = instanceUnavailableErrorMessage;
      }

      this.requiresInstanceCreation = requiresInstanceCreation;
   }

   @Override
   public <V> FieldDiscoverer.InstanceFactory<Map<Field, Object>> discover(
      final AnnotatedType target, final FieldDiscoverer.FieldCollector<Map<Field, Object>, V> collector
   ) throws SerializationException {
      Class<?> clazz = GenericTypeReflector.erase(target.getType());
      if (clazz.isInterface()) {
         throw new SerializationException(target.getType(), "ObjectMapper can only work with concrete types");
      } else {
         final Supplier<Object> maker = this.instanceFactory.apply(target);
         if (maker == null && this.requiresInstanceCreation) {
            return null;
         } else {
            AnnotatedType collectType = target;
            Class<?> collectClass = clazz;

            while (true) {
               this.collectFields(collectType, collector);
               collectClass = collectClass.getSuperclass();
               if (collectClass.equals(Object.class)) {
                  return new FieldDiscoverer.MutableInstanceFactory<Map<Field, Object>>() {
                     public Map<Field, Object> begin() {
                        return new HashMap<>();
                     }

                     public void complete(final Object instance, final Map<Field, Object> intermediate) throws SerializationException {
                        for (Entry<Field, Object> entry : intermediate.entrySet()) {
                           try {
                              if (entry.getValue() instanceof ObjectFieldDiscoverer.ImplicitProvider) {
                                 Object implicit = ((ObjectFieldDiscoverer.ImplicitProvider)entry.getValue()).provider.get();
                                 if (implicit != null && entry.getKey().get(instance) == null) {
                                    entry.getKey().set(instance, implicit);
                                 }
                              } else {
                                 entry.getKey().set(instance, entry.getValue());
                              }
                           } catch (IllegalAccessException var6) {
                              throw new SerializationException(target.getType(), var6);
                           }
                        }
                     }

                     public Object complete(final Map<Field, Object> intermediate) throws SerializationException {
                        Object instance = maker == null ? null : maker.get();
                        if (instance == null) {
                           throw new SerializationException(target.getType(), ObjectFieldDiscoverer.this.instanceUnavailableErrorMessage);
                        } else {
                           this.complete(instance, intermediate);
                           return instance;
                        }
                     }

                     @Override
                     public boolean canCreateInstances() {
                        return maker != null;
                     }
                  };
               }

               collectType = GenericTypeReflector.getExactSuperType(collectType, collectClass);
            }
         }
      }
   }

   private void collectFields(final AnnotatedType clazz, final FieldDiscoverer.FieldCollector<Map<Field, Object>, ?> fieldMaker) {
      for (Field field : GenericTypeReflector.erase(clazz.getType()).getDeclaredFields()) {
         if ((field.getModifiers() & 136) == 0) {
            field.setAccessible(true);
            AnnotatedType fieldType = GenericTypeReflector.getFieldType(field, clazz);
            fieldMaker.accept(field.getName(), fieldType, Types.combinedAnnotations(fieldType, field), (intermediate, val, implicitProvider) -> {
               if (val != null) {
                  intermediate.put(field, val);
               } else {
                  intermediate.put(field, new ObjectFieldDiscoverer.ImplicitProvider(implicitProvider));
               }
            }, field::get);
         }
      }
   }

   static class ImplicitProvider {
      final Supplier<Object> provider;

      ImplicitProvider(final Supplier<Object> provider) {
         this.provider = provider;
      }
   }
}
