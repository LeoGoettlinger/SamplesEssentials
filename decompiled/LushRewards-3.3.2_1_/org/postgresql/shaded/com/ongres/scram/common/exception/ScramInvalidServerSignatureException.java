package org.postgresql.shaded.com.ongres.scram.common.exception;

public class ScramInvalidServerSignatureException extends ScramException {
   public ScramInvalidServerSignatureException(String detail) {
      super(detail);
   }

   public ScramInvalidServerSignatureException(String detail, Throwable ex) {
      super(detail, ex);
   }
}
