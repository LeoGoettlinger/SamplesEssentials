package com.fastasyncworldedit.core.jnbt;

public class NBTException extends RuntimeException {
   public NBTException(String message) {
      super(message);
   }

   @Override
   public Throwable fillInStackTrace() {
      return this;
   }
}
