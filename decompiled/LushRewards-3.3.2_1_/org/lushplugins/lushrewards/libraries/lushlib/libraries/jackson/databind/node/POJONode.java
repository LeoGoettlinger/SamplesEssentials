package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node;

import java.io.IOException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public class POJONode extends ValueNode {
   private static final long serialVersionUID = 2L;
   protected final Object _value;

   public POJONode(Object v) {
      this._value = v;
   }

   @Override
   public JsonNodeType getNodeType() {
      return JsonNodeType.POJO;
   }

   @Override
   public JsonToken asToken() {
      return JsonToken.VALUE_EMBEDDED_OBJECT;
   }

   @Override
   public byte[] binaryValue() throws IOException {
      return this._value instanceof byte[] ? (byte[])this._value : super.binaryValue();
   }

   @Override
   public String asText() {
      return this._value == null ? "null" : this._value.toString();
   }

   @Override
   public String asText(String defaultValue) {
      return this._value == null ? defaultValue : this._value.toString();
   }

   @Override
   public boolean asBoolean(boolean defaultValue) {
      return this._value != null && this._value instanceof Boolean ? (Boolean)this._value : defaultValue;
   }

   @Override
   public int asInt(int defaultValue) {
      return this._value instanceof Number ? ((Number)this._value).intValue() : defaultValue;
   }

   @Override
   public long asLong(long defaultValue) {
      return this._value instanceof Number ? ((Number)this._value).longValue() : defaultValue;
   }

   @Override
   public double asDouble(double defaultValue) {
      return this._value instanceof Number ? ((Number)this._value).doubleValue() : defaultValue;
   }

   @Override
   public final void serialize(JsonGenerator gen, SerializerProvider ctxt) throws IOException {
      if (this._value == null) {
         ctxt.defaultSerializeNull(gen);
      } else if (this._value instanceof JsonSerializable) {
         ((JsonSerializable)this._value).serialize(gen, ctxt);
      } else {
         ctxt.defaultSerializeValue(this._value, gen);
      }
   }

   public Object getPojo() {
      return this._value;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o instanceof POJONode ? this._pojoEquals((POJONode)o) : false;
      }
   }

   protected boolean _pojoEquals(POJONode other) {
      return this._value == null ? other._value == null : this._value.equals(other._value);
   }

   @Override
   public int hashCode() {
      return this._value.hashCode();
   }
}
