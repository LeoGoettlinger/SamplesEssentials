package com.viaversion.viaversion.libs.mcstructs.converter.model;

import lombok.Generated;

public class CodecException extends RuntimeException {
   @Override
   public void printStackTrace() {
   }

   @Override
   public Throwable fillInStackTrace() {
      return this;
   }

   @Generated
   public CodecException() {
      this((String)null, (Throwable)null);
   }

   @Generated
   public CodecException(String message) {
      this(message, (Throwable)null);
   }

   @Generated
   public CodecException(Throwable cause) {
      this(cause != null ? cause.getMessage() : null, cause);
   }

   @Generated
   public CodecException(String message, Throwable cause) {
      super(message);
      if (cause != null) {
         super.initCause(cause);
      }
   }
}
