package de.exlll.configlib;

public final class ConfigurationException extends RuntimeException {
   ConfigurationException(String message) {
      super(message);
   }

   ConfigurationException(String message, Throwable cause) {
      super(message, cause);
   }
}
