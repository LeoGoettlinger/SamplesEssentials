package de.exlll.configlib;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

final class PolymorphicSerializer implements Serializer<Object, Map<?, ?>> {
   private final SerializerContext context;
   private final Class<?> polymorphicType;
   private final Polymorphic polymorphic;
   private final Map<String, Class<?>> typeByAlias = new HashMap<>();
   private final Map<Class<?>, String> aliasByType = new HashMap<>();

   public PolymorphicSerializer(SerializerContext context) {
      this.context = context;
      this.polymorphicType = (Class<?>)context.annotatedType().getType();
      this.polymorphic = this.polymorphicType.getAnnotation(Polymorphic.class);
      this.requireNonBlankProperty();
      this.initAliases();
   }

   private void requireNonBlankProperty() {
      if (this.polymorphic.property().isBlank()) {
         String msg = "The @Polymorphic annotation does not allow a blank property name but " + "type '%s' uses one.".formatted(this.polymorphicType.getName());
         throw new ConfigurationException(msg);
      }
   }

   private void initAliases() {
      PolymorphicTypes polymorphicTypes = this.polymorphicType.getAnnotation(PolymorphicTypes.class);
      if (polymorphicTypes != null) {
         for (PolymorphicTypes.Type pType : polymorphicTypes.value()) {
            Class<?> type = pType.type();
            String alias = pType.alias().isBlank() ? type.getName() : pType.alias();
            this.requireDistinctAliases(alias);
            this.requireDistinctTypes(type);
            this.typeByAlias.put(alias, type);
            this.aliasByType.put(type, alias);
         }
      }
   }

   private void requireDistinctAliases(String alias) {
      if (this.typeByAlias.containsKey(alias)) {
         String msg = "The @PolymorphicTypes annotation must not use the same alias for "
            + "multiple types. Alias '%s' appears more than once.".formatted(alias);
         throw new ConfigurationException(msg);
      }
   }

   private void requireDistinctTypes(Class<?> type) {
      if (this.aliasByType.containsKey(type)) {
         String msg = "The @PolymorphicTypes annotation must not contain multiple "
            + "definitions for the same subtype. Type '%s' appears more than once.".formatted(type.getName());
         throw new ConfigurationException(msg);
      }
   }

   public Map<?, ?> serialize(Object element) {
      Class<?> elementType = element.getClass();
      TypeSerializer<Object, ?> serializer = TypeSerializer.newSerializerFor((Class<Object>)elementType, this.context.properties());
      Map<?, ?> serialization = serializer.serialize(element);
      this.requireSerializationNotContainsProperty(serialization);
      LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
      result.put(this.polymorphic.property(), this.getTypeIdentifierByType(elementType));
      result.putAll((Map<? extends Object, ? extends Object>)serialization);
      return result;
   }

   private String getTypeIdentifierByType(Class<?> type) {
      String alias = this.aliasByType.get(type);
      return alias == null ? type.getName() : alias;
   }

   private void requireSerializationNotContainsProperty(Map<?, ?> serialization) {
      if (serialization.containsKey(this.polymorphic.property())) {
         String msg = "Polymorphic serialization for type '%s' failed. The type contains a configuration element with name '%s' but that name is used by the @Polymorphic property."
            .formatted(this.polymorphicType.getName(), this.polymorphic.property());
         throw new ConfigurationException(msg);
      }
   }

   public Object deserialize(Map<?, ?> element) {
      this.requirePropertyPresent(element);
      Object typeIdentifier = element.get(this.polymorphic.property());
      this.requireTypeIdentifierString(typeIdentifier);
      Class<?> type = this.getTypeByTypeIdentifier((String)typeIdentifier);
      TypeSerializer<?, ?> serializer = TypeSerializer.newSerializerFor(type, this.context.properties());
      return serializer.deserialize(element);
   }

   private Class<?> getTypeByTypeIdentifier(String typeIdentifier) {
      Class<?> type = this.typeByAlias.get(typeIdentifier);
      return type == null ? this.tryFindClass(typeIdentifier) : type;
   }

   private Class<?> tryFindClass(String className) {
      try {
         return Reflect.getClassByName(className);
      } catch (RuntimeException var4) {
         String msg = "Polymorphic deserialization for type '%s' failed. The class '%s' does not exist.".formatted(this.polymorphicType.getName(), className);
         throw new ConfigurationException(msg, var4);
      }
   }

   private void requirePropertyPresent(Map<?, ?> element) {
      if (element.get(this.polymorphic.property()) == null) {
         String msg = "Polymorphic deserialization for type '%s' failed. The property '%s' which holds the type is missing. Value to be deserialized:\n%s"
            .formatted(this.polymorphicType.getName(), this.polymorphic.property(), element);
         throw new ConfigurationException(msg);
      }
   }

   private void requireTypeIdentifierString(Object typeIdentifier) {
      if (!(typeIdentifier instanceof String)) {
         String msg = "Polymorphic deserialization for type '%s' failed. The type identifier '%s' which should hold the type is not a string but of type '%s'."
            .formatted(this.polymorphicType.getName(), typeIdentifier, typeIdentifier.getClass().getName());
         throw new ConfigurationException(msg);
      }
   }

   Class<?> getPolymorphicType() {
      return this.polymorphicType;
   }
}
