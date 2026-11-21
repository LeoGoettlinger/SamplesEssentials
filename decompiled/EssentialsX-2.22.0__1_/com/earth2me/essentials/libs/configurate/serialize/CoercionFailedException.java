package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

public class CoercionFailedException extends SerializationException {
   private static final long serialVersionUID = 5800074754243723221L;

   public CoercionFailedException(final Object inputValue, final String typeDescription) {
      super("Failed to coerce input value of type " + inputValue.getClass() + " to " + typeDescription);
   }

   public CoercionFailedException(final Type target, final Object inputValue, final String typeDescription) {
      super(target, "Failed to coerce input value of type " + inputValue.getClass() + " to " + typeDescription);
   }

   public CoercionFailedException(final AnnotatedType target, final Object inputValue, final String typeDescription) {
      super(target, "Failed to coerce input value of type " + inputValue.getClass() + " to " + typeDescription);
   }
}
