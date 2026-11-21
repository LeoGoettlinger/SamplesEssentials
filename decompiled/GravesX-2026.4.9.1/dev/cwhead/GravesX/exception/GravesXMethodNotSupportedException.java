package dev.cwhead.GravesX.exception;

public class GravesXMethodNotSupportedException extends UnsupportedOperationException {
   private static final long serialVersionUID = 1L;

   public GravesXMethodNotSupportedException(String message) {
      super(message);
   }
}
