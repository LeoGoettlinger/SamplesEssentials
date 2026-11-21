package com.earth2me.essentials.libs.configurate.reactive;

public class TransactionFailedException extends Exception {
   private static final long serialVersionUID = -3726752009864775844L;

   public TransactionFailedException() {
   }

   public TransactionFailedException(final Throwable cause) {
      super(cause);
   }
}
