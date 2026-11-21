package com.artillexstudios.axtrade.libs.axapi.nms.wrapper.exception;

public class UnknownWrappedException extends RuntimeException {
   public UnknownWrappedException() {
   }

   public UnknownWrappedException(String message) {
      super(message);
   }

   public UnknownWrappedException(Throwable cause) {
      super(cause);
   }

   public UnknownWrappedException(String message, Throwable cause) {
      super(message, cause);
   }
}
