package de.exlll.configlib;

import java.util.List;
import java.util.Map;

final class ConfigurationSerializer<T> extends TypeSerializer<T, ConfigurationElements.FieldElement> {
   private final T defaultInstance = this.newDefaultInstance();

   ConfigurationSerializer(Class<T> configurationType, ConfigurationProperties properties) {
      super(Validator.requireConfigurationClass(configurationType), properties);
   }

   public T deserialize(Map<?, ?> serializedConfiguration) {
      Object[] deserializedElements = this.deserializeConfigurationElements(serializedConfiguration);
      List<ConfigurationElements.FieldElement> elements = this.elements();
      T result = this.newDefaultInstance();

      for (int i = 0; i < deserializedElements.length; i++) {
         ConfigurationElements.FieldElement fieldElement = elements.get(i);
         Reflect.setValue(fieldElement.element(), result, deserializedElements[i]);
      }

      return this.postProcessor.apply(result);
   }

   @Override
   protected void requireSerializableElements() {
      if (this.serializers.isEmpty()) {
         String msg = "Configuration class '" + this.type.getSimpleName() + "' does not contain any (de-)serializable fields.";
         throw new ConfigurationException(msg);
      }
   }

   protected String baseDeserializeExceptionMessage(ConfigurationElements.FieldElement element, Object value) {
      return "Deserialization of value '%s' with type '%s' for field '%s' failed.".formatted(value, value.getClass(), element.element());
   }

   @Override
   protected List<ConfigurationElements.FieldElement> elements() {
      return FieldExtractors.CONFIGURATION.extract(this.type).filter(this.properties.getFieldFilter()).map(ConfigurationElements.FieldElement::new).toList();
   }

   @Override
   T newDefaultInstance() {
      return Reflect.callNoParamConstructor(this.type);
   }

   Class<T> getConfigurationType() {
      return this.type;
   }

   protected Object getDefaultValueOf(ConfigurationElements.FieldElement element) {
      return Reflect.getValue(element.element(), this.defaultInstance);
   }
}
