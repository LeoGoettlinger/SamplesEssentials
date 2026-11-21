package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;

public final class TypeWrappedDeserializer extends JsonDeserializer<Object> implements Serializable {
   private static final long serialVersionUID = 1L;
   protected final TypeDeserializer _typeDeserializer;
   protected final JsonDeserializer<Object> _deserializer;

   public TypeWrappedDeserializer(TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
      this._typeDeserializer = typeDeser;
      this._deserializer = (JsonDeserializer<Object>)deser;
   }

   @Override
   public LogicalType logicalType() {
      return this._deserializer.logicalType();
   }

   @Override
   public Class<?> handledType() {
      return this._deserializer.handledType();
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return this._deserializer.supportsUpdate(config);
   }

   @Override
   public JsonDeserializer<?> getDelegatee() {
      return this._deserializer.getDelegatee();
   }

   @Override
   public Collection<Object> getKnownPropertyNames() {
      return this._deserializer.getKnownPropertyNames();
   }

   @Override
   public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
      return this._deserializer.getNullValue(ctxt);
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return this._deserializer.getEmptyValue(ctxt);
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return this._deserializer.deserializeWithType(p, ctxt, this._typeDeserializer);
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      throw new IllegalStateException("Type-wrapped deserializer's deserializeWithType should never get called");
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
      return this._deserializer.deserialize(p, ctxt, intoValue);
   }
}
