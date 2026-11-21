package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.NumberOutput;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;

public class ShortNode extends NumericNode {
   protected final short _value;

   public ShortNode(short v) {
      this._value = v;
   }

   public static ShortNode valueOf(short l) {
      return new ShortNode(l);
   }

   @Override
   public JsonToken asToken() {
      return JsonToken.VALUE_NUMBER_INT;
   }

   @Override
   public JsonParser.NumberType numberType() {
      return JsonParser.NumberType.INT;
   }

   @Override
   public boolean isIntegralNumber() {
      return true;
   }

   @Override
   public boolean isShort() {
      return true;
   }

   @Override
   public boolean canConvertToInt() {
      return true;
   }

   @Override
   public boolean canConvertToLong() {
      return true;
   }

   @Override
   public Number numberValue() {
      return this._value;
   }

   @Override
   public short shortValue() {
      return this._value;
   }

   @Override
   public int intValue() {
      return this._value;
   }

   @Override
   public long longValue() {
      return this._value;
   }

   @Override
   public float floatValue() {
      return this._value;
   }

   @Override
   public double doubleValue() {
      return this._value;
   }

   @Override
   public BigDecimal decimalValue() {
      return BigDecimal.valueOf((long)this._value);
   }

   @Override
   public BigInteger bigIntegerValue() {
      return BigInteger.valueOf(this._value);
   }

   @Override
   public String asText() {
      return NumberOutput.toString(this._value);
   }

   @Override
   public boolean asBoolean(boolean defaultValue) {
      return this._value != 0;
   }

   @Override
   public final void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeNumber(this._value);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else {
         return o instanceof ShortNode ? ((ShortNode)o)._value == this._value : false;
      }
   }

   @Override
   public int hashCode() {
      return this._value;
   }
}
