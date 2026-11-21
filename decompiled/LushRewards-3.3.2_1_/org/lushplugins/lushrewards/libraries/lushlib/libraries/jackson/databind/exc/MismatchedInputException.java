package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.exc;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonLocation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;

public class MismatchedInputException extends JsonMappingException {
   protected Class<?> _targetType;

   protected MismatchedInputException(JsonParser p, String msg) {
      this(p, msg, (JavaType)null);
   }

   protected MismatchedInputException(JsonParser p, String msg, JsonLocation loc) {
      super(p, msg, loc);
   }

   protected MismatchedInputException(JsonParser p, String msg, Class<?> targetType) {
      super(p, msg);
      this._targetType = targetType;
   }

   protected MismatchedInputException(JsonParser p, String msg, JavaType targetType) {
      super(p, msg);
      this._targetType = ClassUtil.rawClass(targetType);
   }

   @Deprecated
   public static MismatchedInputException from(JsonParser p, String msg) {
      return from(p, (Class<?>)null, msg);
   }

   public static MismatchedInputException from(JsonParser p, JavaType targetType, String msg) {
      return new MismatchedInputException(p, msg, targetType);
   }

   public static MismatchedInputException from(JsonParser p, Class<?> targetType, String msg) {
      return new MismatchedInputException(p, msg, targetType);
   }

   public MismatchedInputException setTargetType(JavaType t) {
      this._targetType = t.getRawClass();
      return this;
   }

   public Class<?> getTargetType() {
      return this._targetType;
   }
}
