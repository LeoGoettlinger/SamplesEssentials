package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class RecordFieldDiscoverer implements FieldDiscoverer<Object[]> {
   static final RecordFieldDiscoverer INSTANCE = new RecordFieldDiscoverer();
   private static final Lookup LOOKUP = MethodHandles.lookup();
   @Nullable
   private static final MethodHandle CLASS_IS_RECORD;
   @Nullable
   private static final MethodHandle CLASS_GET_RECORD_COMPONENTS;
   @Nullable
   private static final MethodHandle RECORD_COMPONENT_GET_ANNOTATED_TYPE;
   @Nullable
   private static final MethodHandle RECORD_COMPONENT_GET_NAME;
   @Nullable
   private static final MethodHandle RECORD_COMPONENT_GET_ACCESSOR;

   private RecordFieldDiscoverer() {
   }

   @Nullable
   @Override
   public <V> FieldDiscoverer.InstanceFactory<Object[]> discover(final AnnotatedType target, final FieldDiscoverer.FieldCollector<Object[], V> collector) throws SerializationException {
      if (CLASS_IS_RECORD != null
         && CLASS_GET_RECORD_COMPONENTS != null
         && RECORD_COMPONENT_GET_ANNOTATED_TYPE != null
         && RECORD_COMPONENT_GET_NAME != null
         && RECORD_COMPONENT_GET_ACCESSOR != null) {
         Class<?> clazz = GenericTypeReflector.erase(target.getType());

         try {
            if ((boolean)CLASS_IS_RECORD.invoke((Class)clazz)) {
               final AnnotatedElement[] recordComponents = (AnnotatedElement[])CLASS_GET_RECORD_COMPONENTS.invoke((Class)clazz);
               final Class<?>[] constructorParams = new Class[recordComponents.length];
               int i = 0;

               for (int recordComponentsLength = recordComponents.length; i < recordComponentsLength; i++) {
                  AnnotatedElement component = recordComponents[i];
                  Method accessor = (Method)RECORD_COMPONENT_GET_ACCESSOR.invoke((AnnotatedElement)component);
                  accessor.setAccessible(true);
                  String name = (String)RECORD_COMPONENT_GET_NAME.invoke((AnnotatedElement)component);
                  AnnotatedType genericType = (AnnotatedType)RECORD_COMPONENT_GET_ANNOTATED_TYPE.invoke((AnnotatedElement)component);
                  constructorParams[i] = GenericTypeReflector.erase(genericType.getType());
                  Field backingField = clazz.getDeclaredField(name);
                  backingField.setAccessible(true);
                  AnnotatedType resolvedType = GenericTypeReflector.resolveExactType(genericType, target);
                  AnnotatedElement annotationContainer = Types.combinedAnnotations(component, backingField, accessor);
                  int targetIdx = i;
                  collector.accept(name, resolvedType, annotationContainer, (intermediate, el, implicitSupplier) -> {
                     if (el != null) {
                        intermediate[targetIdx] = el;
                     } else {
                        intermediate[targetIdx] = implicitSupplier.get();
                     }
                  }, x$0 -> accessor.invoke(x$0));
               }

               final Constructor<?> clazzConstructor = clazz.getDeclaredConstructor(constructorParams);
               clazzConstructor.setAccessible(true);
               return new FieldDiscoverer.InstanceFactory<Object[]>() {
                  public Object[] begin() {
                     return new Object[recordComponents.length];
                  }

                  public Object complete(final Object[] intermediate) throws SerializationException {
                     int ix = 0;

                     for (int length = intermediate.length; ix < length; ix++) {
                        if (intermediate[ix] == null && constructorParams[ix].isPrimitive()) {
                           intermediate[ix] = Types.defaultValue(constructorParams[ix]);
                        }
                     }

                     try {
                        return clazzConstructor.newInstance(intermediate);
                     } catch (IllegalAccessException | InvocationTargetException | InstantiationException var4) {
                        throw new SerializationException(target.getType(), var4);
                     }
                  }

                  @Override
                  public boolean canCreateInstances() {
                     return true;
                  }
               };
            }
         } catch (SerializationException var16) {
            throw var16;
         } catch (Throwable var17) {
         }
      }

      return null;
   }

   static {
      MethodHandle classIsRecord = null;
      MethodHandle classGetRecordComponents = null;
      MethodHandle recordComponentGetAnnotatedType = null;
      MethodHandle recordComponentGetName = null;
      MethodHandle recordComponentGetAccessor = null;

      try {
         classIsRecord = LOOKUP.findVirtual(Class.class, "isRecord", MethodType.methodType(boolean.class));
         Class<?> recordComponent = Class.forName("java.lang.reflect.RecordComponent");
         Class<?> recordComponentArray = Array.newInstance(recordComponent, 0).getClass();
         classGetRecordComponents = LOOKUP.findVirtual(Class.class, "getRecordComponents", MethodType.methodType(recordComponentArray));
         recordComponentGetAnnotatedType = LOOKUP.findVirtual(recordComponent, "getAnnotatedType", MethodType.methodType(AnnotatedType.class));
         recordComponentGetAccessor = LOOKUP.findVirtual(recordComponent, "getAccessor", MethodType.methodType(Method.class));
         recordComponentGetName = LOOKUP.findVirtual(recordComponent, "getName", MethodType.methodType(String.class));
      } catch (Exception var7) {
      }

      CLASS_IS_RECORD = classIsRecord;
      CLASS_GET_RECORD_COMPONENTS = classGetRecordComponents;
      RECORD_COMPONENT_GET_ANNOTATED_TYPE = recordComponentGetAnnotatedType;
      RECORD_COMPONENT_GET_NAME = recordComponentGetName;
      RECORD_COMPONENT_GET_ACCESSOR = recordComponentGetAccessor;
   }
}
