package de.exlll.configlib;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

abstract sealed class TypeSerializer<T, E extends ConfigurationElement<?>> implements Serializer<T, Map<?, ?>> permits ConfigurationSerializer, RecordSerializer {
   protected final Class<T> type;
   protected final ConfigurationProperties properties;
   protected final NameFormatter formatter;
   protected final Map<String, Serializer<?, ?>> serializers;
   protected final UnaryOperator<T> postProcessor;

   protected TypeSerializer(Class<T> type, ConfigurationProperties properties) {
      this.type = Validator.requireNonNull(type, "type");
      this.properties = Validator.requireNonNull(properties, "configuration properties");
      this.formatter = properties.getNameFormatter();
      this.serializers = this.buildSerializerMap();
      this.postProcessor = this.createPostProcessorFromAnnotatedMethod();
      this.requireSerializableElements();
   }

   static <T> TypeSerializer<T, ?> newSerializerFor(Class<T> type, ConfigurationProperties properties) {
      return (TypeSerializer<T, ?>)(type.isRecord() ? new RecordSerializer<>(type, properties) : new ConfigurationSerializer<>(type, properties));
   }

   Map<String, Serializer<?, ?>> buildSerializerMap() {
      SerializerSelector selector = new SerializerSelector(this.properties);

      try {
         return this.elements().stream().collect(Collectors.toMap(ConfigurationElement::name, selector::select));
      } catch (StackOverflowError var4) {
         String msg = "Recursive type definitions are not supported.";
         throw new ConfigurationException(msg, var4);
      }
   }

   public final Map<?, ?> serialize(T configuration) {
      Map<String, Object> result = new LinkedHashMap<>();

      for (E element : this.elements()) {
         Object elementValue = element.value(configuration);
         if (elementValue != null || this.properties.outputNulls()) {
            Object serializedValue = this.serializeElement(element, elementValue);
            String formattedName = this.formatter.format(element.name());
            result.put(formattedName, serializedValue);
         }
      }

      return result;
   }

   protected final Object serializeElement(E element, Object value) {
      if (value == null) {
         return null;
      } else {
         Serializer<Object, Object> serializer = (Serializer<Object, Object>)this.serializers.get(element.name());

         try {
            Object serialized = serializer.serialize(value);
            validateTargetType(element, value, serialized);
            return serialized;
         } catch (ClassCastException var6) {
            String msg = "Serialization of value '%s' for element '%s' of type '%s' failed.\nThe type of the object to be serialized does not match the type the custom serializer of type '%s' expects."
               .formatted(value, element.element(), element.declaringType(), serializer.getClass());
            throw new ConfigurationException(msg, var6);
         }
      }
   }

   private static void validateTargetType(ConfigurationElement<?> element, Object value, Object serialized) {
      try {
         Validator.requireTargetType(serialized);
      } catch (ConfigurationException var5) {
         String msg = "Serialization of value '%s' for element '%s' of type '%s' failed. The serializer produced an invalid target type."
            .formatted(value, element.element(), element.declaringType());
         throw new ConfigurationException(msg, var5);
      }
   }

   protected final Object deserialize(E element, Object value) {
      Serializer<Object, Object> serializer = (Serializer<Object, Object>)this.serializers.get(element.name());

      try {
         return serializer.deserialize(value);
      } catch (ClassCastException var7) {
         String msg = this.baseDeserializeExceptionMessage(element, value)
            + "\nThe type of the object to be deserialized does not match the type the deserializer expects.";
         throw new ConfigurationException(msg, var7);
      } catch (RuntimeException var8) {
         String msgx = this.baseDeserializeExceptionMessage(element, value);
         throw new ConfigurationException(msgx, var8);
      }
   }

   protected final Object[] deserializeConfigurationElements(Map<?, ?> serializedConfiguration) {
      List<E> elements = this.elements();
      Object[] result = new Object[elements.size()];
      int i = 0;

      for (int size = elements.size(); i < size; i++) {
         E element = elements.get(i);
         String formattedName = this.formatter.format(element.name());
         if (!serializedConfiguration.containsKey(formattedName)) {
            Object defaultValue = this.getDefaultValueOf(element);
            result[i] = this.applyPostProcessorForElement(element, defaultValue);
         } else {
            Object serializedValue = serializedConfiguration.get(formattedName);
            if (serializedValue == null && this.properties.inputNulls()) {
               result[i] = null;
            } else if (serializedValue == null) {
               result[i] = this.getDefaultValueOf(element);
            } else {
               result[i] = this.deserialize(element, serializedValue);
            }

            result[i] = this.applyPostProcessorForElement(element, result[i]);
         }
      }

      return result;
   }

   private Object applyPostProcessorForElement(ConfigurationElement<?> element, Object deserializeValue) {
      Object result = deserializeValue;
      boolean postProcessed = false;

      for (Entry<Predicate<? super ConfigurationElement<?>>, UnaryOperator<?>> entry : this.properties.getPostProcessorsByCondition().entrySet()) {
         Predicate<? super ConfigurationElement<?>> condition = entry.getKey();
         if (condition.test(element)) {
            UnaryOperator<?> postProcessor = entry.getValue();
            result = tryApplyPostProcessorForElement(element, postProcessor, result);
            postProcessed = true;
         }
      }

      if (result == null && postProcessed) {
         requirePostProcessorDoesNotReturnNullForPrimitiveElement(element);
      } else if (result == null) {
         requireNonPrimitiveType(element);
      }

      return result;
   }

   private static Object tryApplyPostProcessorForElement(ConfigurationElement<?> element, UnaryOperator<?> postProcessor, Object value) {
      try {
         return ((UnaryOperator<Object>)postProcessor).apply(value);
      } catch (ClassCastException var5) {
         String msg = "Deserialization of value '%s' for element '%s' of type '%s' failed.\nThe type of the object to be deserialized does not match the type post-processor '%s' expects."
            .formatted(value, element.element(), element.declaringType(), postProcessor);
         throw new ConfigurationException(msg, var5);
      }
   }

   private static void requirePostProcessorDoesNotReturnNullForPrimitiveElement(ConfigurationElement<?> element) {
      if (element.type().isPrimitive()) {
         if (element instanceof ConfigurationElements.RecordComponentElement recordComponentElement) {
            RecordComponent component = recordComponentElement.element();
            String msg = "Post-processors must not return null for primitive record components but some post-processor of component '%s' of record type '%s' does."
               .formatted(component, component.getDeclaringRecord());
            throw new ConfigurationException(msg);
         } else if (element instanceof ConfigurationElements.FieldElement fieldElement) {
            Field field = fieldElement.element();
            String msg = "Post-processors must not return null for primitive fields but some post-processor of field '%s' does.".formatted(field);
            throw new ConfigurationException(msg);
         } else {
            throw new ConfigurationException("Unhandled ConfigurationElement: " + element);
         }
      }
   }

   private static void requireNonPrimitiveType(ConfigurationElement<?> element) {
      if (element.type().isPrimitive()) {
         if (element instanceof ConfigurationElements.RecordComponentElement recordComponentElement) {
            RecordComponent component = recordComponentElement.element();
            String msg = "Cannot set component '%s' of record type '%s' to null. Primitive types cannot be assigned null values."
               .formatted(component, component.getDeclaringRecord());
            throw new ConfigurationException(msg);
         } else if (element instanceof ConfigurationElements.FieldElement fieldElement) {
            Field field = fieldElement.element();
            String msg = "Cannot set field '%s' to null value. Primitive types cannot be assigned null.".formatted(field);
            throw new ConfigurationException(msg);
         } else {
            throw new ConfigurationException("Unhandled ConfigurationElement: " + element);
         }
      }
   }

   final UnaryOperator<T> createPostProcessorFromAnnotatedMethod() {
      List<Method> list = Arrays.stream(this.type.getDeclaredMethods())
         .filter(methodx -> methodx.isAnnotationPresent(PostProcess.class))
         .filter(Predicate.not(Method::isSynthetic))
         .filter(Predicate.not(this::isAccessorMethod))
         .toList();
      if (list.isEmpty()) {
         return UnaryOperator.identity();
      } else if (list.size() > 1) {
         String methodNames = String.join("\n  ", list.stream().map(Method::toString).toList());
         String msg = "Configuration types must not define more than one method for "
            + "post-processing but type '%s' defines %d:\n  %s".formatted(this.type, list.size(), methodNames);
         throw new ConfigurationException(msg);
      } else {
         Method method = list.get(0);
         int modifiers = method.getModifiers();
         if (!Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers)) {
            int parameterCount = method.getParameterCount();
            if (parameterCount > 0) {
               String msg = "Post-processing methods must not define any parameters but "
                  + "post-processing method '%s' of type '%s' defines %d.".formatted(method, this.type, parameterCount);
               throw new ConfigurationException(msg);
            } else {
               Class<?> returnType = method.getReturnType();
               if (returnType != void.class && returnType != this.type) {
                  String msg = "The return type of post-processing methods must either be 'void' or the same type as the configuration type in which the post-processing method is defined. The return type of the post-processing method of "
                     + "type '%s' is neither 'void' nor '%s'.".formatted(this.type, this.type.getSimpleName());
                  throw new ConfigurationException(msg);
               } else {
                  return object -> {
                     if (method.getReturnType() == void.class) {
                        Reflect.invoke(method, object);
                        return object;
                     } else {
                        return Reflect.invoke(method, object);
                     }
                  };
               }
            }
         } else {
            String msg = "Post-processing methods must be neither abstract nor static, "
               + "but post-processing method '%s' of type '%s' is.".formatted(method, this.type);
            throw new ConfigurationException(msg);
         }
      }
   }

   final boolean isAccessorMethod(Method method) {
      if (!this.type.isRecord()) {
         return false;
      } else if (!method.getDeclaringClass().equals(this.type)) {
         return false;
      } else {
         return method.getParameterCount() > 0
            ? false
            : Arrays.stream(this.type.getRecordComponents()).map(RecordComponent::getName).anyMatch(s -> s.equals(method.getName()));
      }
   }

   protected abstract void requireSerializableElements();

   protected abstract String baseDeserializeExceptionMessage(E var1, Object var2);

   protected abstract List<E> elements();

   protected abstract Object getDefaultValueOf(E var1);

   abstract T newDefaultInstance();
}
