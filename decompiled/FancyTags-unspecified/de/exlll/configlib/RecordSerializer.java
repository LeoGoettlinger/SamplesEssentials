package de.exlll.configlib;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class RecordSerializer<R> extends TypeSerializer<R, ConfigurationElements.RecordComponentElement> {
   RecordSerializer(Class<R> recordType, ConfigurationProperties properties) {
      super(Validator.requireRecord(recordType), properties);
   }

   public R deserialize(Map<?, ?> serializedConfiguration) {
      Object[] ctorArgs = this.deserializeConfigurationElements(serializedConfiguration);
      R result = Reflect.callCanonicalConstructor(this.type, ctorArgs);
      return this.postProcessor.apply(result);
   }

   @Override
   protected void requireSerializableElements() {
      if (this.serializers.isEmpty()) {
         String msg = "Record type '%s' does not define any components.".formatted(this.type.getSimpleName());
         throw new ConfigurationException(msg);
      }
   }

   protected String baseDeserializeExceptionMessage(ConfigurationElements.RecordComponentElement element, Object value) {
      return "Deserialization of value '%s' with type '%s' for component '%s' of record '%s' failed."
         .formatted(value, value.getClass(), element.element(), element.declaringType());
   }

   @Override
   protected List<ConfigurationElements.RecordComponentElement> elements() {
      return Arrays.stream(this.type.getRecordComponents()).map(ConfigurationElements.RecordComponentElement::new).toList();
   }

   @Override
   R newDefaultInstance() {
      return Reflect.hasDefaultConstructor(this.type)
         ? Reflect.callNoParamConstructor(this.type)
         : Reflect.callCanonicalConstructorWithDefaultValues(this.type);
   }

   Class<R> getRecordType() {
      return this.type;
   }

   protected Object getDefaultValueOf(ConfigurationElements.RecordComponentElement element) {
      return Reflect.getDefaultValue(element.type());
   }
}
