package org.jsoup;

import java.io.IOException;

/** @deprecated */
public class UncheckedIOException extends java.io.UncheckedIOException {
   public UncheckedIOException(IOException cause) {
      super(cause);
   }

   public UncheckedIOException(String message) {
      super(new IOException(message));
   }

   public IOException ioException() {
      return this.getCause();
   }
}
