package de.oliver.fancyanalytics.sdk.errors;

import java.util.Map;

public record Error(String exceptionClass, String message, String stackTrace, Map<String, String> properties) {
   public Error withProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
   }
}
