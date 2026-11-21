package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.exc;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyName;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

public class InvalidNullException extends MismatchedInputException {
   private static final long serialVersionUID = 1L;
   protected final PropertyName _propertyName;

   protected InvalidNullException(DeserializationContext ctxt, String msg, PropertyName pname) {
      super(ctxt.getParser(), msg);
      this._propertyName = pname;
   }

   public static InvalidNullException from(DeserializationContext ctxt, PropertyName name, JavaType type) {
      String msg = String.format("Invalid `null` value encountered for property %s", ClassUtil.quotedOr(name, "<UNKNOWN>"));
      InvalidNullException exc = new InvalidNullException(ctxt, msg, name);
      if (type != null) {
         exc.setTargetType(type);
      }

      return exc;
   }

   public PropertyName getPropertyName() {
      return this._propertyName;
   }
}
