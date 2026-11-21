package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.util.concurrent.atomic.AtomicReference;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;

public class AtomicReferenceDeserializer extends ReferenceTypeDeserializer<AtomicReference<Object>> {
   private static final long serialVersionUID = 1L;

   public AtomicReferenceDeserializer(JavaType fullType, ValueInstantiator inst, TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
      super(fullType, inst, typeDeser, deser);
   }

   public AtomicReferenceDeserializer withResolved(TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
      return new AtomicReferenceDeserializer(this._fullType, this._valueInstantiator, typeDeser, valueDeser);
   }

   public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
      return new AtomicReference<>(this._valueDeserializer.getNullValue(ctxt));
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return this.getNullValue(ctxt);
   }

   @Override
   public Object getAbsentValue(DeserializationContext ctxt) throws JsonMappingException {
      return null;
   }

   public AtomicReference<Object> referenceValue(Object contents) {
      return new AtomicReference<>(contents);
   }

   public Object getReferenced(AtomicReference<Object> reference) {
      return reference.get();
   }

   public AtomicReference<Object> updateReference(AtomicReference<Object> reference, Object contents) {
      reference.set(contents);
      return reference;
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return Boolean.TRUE;
   }
}
