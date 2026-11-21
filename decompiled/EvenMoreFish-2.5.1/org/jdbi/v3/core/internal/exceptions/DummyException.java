package org.jdbi.v3.core.internal.exceptions;

public class DummyException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   private DummyException() {
      throw new UnsupportedOperationException("this exception should never actually be instantiated or thrown");
   }
}
