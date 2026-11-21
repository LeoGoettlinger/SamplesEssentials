package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.exc;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;

public class ValueInstantiationException extends JsonMappingException {
   protected final JavaType _type;

   protected ValueInstantiationException(JsonParser p, String msg, JavaType type, Throwable cause) {
      super(p, msg, cause);
      this._type = type;
   }

   protected ValueInstantiationException(JsonParser p, String msg, JavaType type) {
      super(p, msg);
      this._type = type;
   }

   public static ValueInstantiationException from(JsonParser p, String msg, JavaType type) {
      return new ValueInstantiationException(p, msg, type);
   }

   public static ValueInstantiationException from(JsonParser p, String msg, JavaType type, Throwable cause) {
      return new ValueInstantiationException(p, msg, type, cause);
   }

   public JavaType getType() {
      return this._type;
   }
}
