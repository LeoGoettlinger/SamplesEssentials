package com.earth2me.essentials.libs.configurate;

final class VisitorSafeNoopException extends Exception {
   private static final long serialVersionUID = 3031624503762201856L;

   VisitorSafeNoopException() {
      throw new AssertionError();
   }
}
