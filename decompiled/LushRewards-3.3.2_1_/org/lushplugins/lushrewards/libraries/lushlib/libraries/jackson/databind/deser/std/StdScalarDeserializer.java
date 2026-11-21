package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.LogicalType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.AccessPattern;

public abstract class StdScalarDeserializer<T> extends StdDeserializer<T> {
   private static final long serialVersionUID = 1L;

   protected StdScalarDeserializer(Class<?> vc) {
      super(vc);
   }

   protected StdScalarDeserializer(JavaType valueType) {
      super(valueType);
   }

   protected StdScalarDeserializer(StdScalarDeserializer<?> src) {
      super(src);
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.OtherScalar;
   }

   @Override
   public Boolean supportsUpdate(DeserializationConfig config) {
      return Boolean.FALSE;
   }

   @Override
   public AccessPattern getNullAccessPattern() {
      return AccessPattern.ALWAYS_NULL;
   }

   @Override
   public AccessPattern getEmptyAccessPattern() {
      return AccessPattern.CONSTANT;
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return typeDeserializer.deserializeTypedFromScalar(p, ctxt);
   }

   @Override
   public T deserialize(JsonParser p, DeserializationContext ctxt, T intoValue) throws IOException {
      ctxt.handleBadMerge(this);
      return this.deserialize(p, ctxt);
   }
}
