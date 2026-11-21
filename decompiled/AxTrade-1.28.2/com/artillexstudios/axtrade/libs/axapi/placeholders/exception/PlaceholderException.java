package com.artillexstudios.axtrade.libs.axapi.placeholders.exception;

public abstract class PlaceholderException extends Exception {
   public PlaceholderException() {
   }

   public PlaceholderException(String message) {
      super(message);
   }
}
