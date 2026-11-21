package de.oliver.fancyanalytics.api.errors;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsException extends RuntimeException {
   private final Exception exception;
   private final Map<String, String> properties;

   public AnalyticsException(RuntimeException e) {
      super(e.getClass().getSimpleName() + ": " + e.getMessage());
      this.exception = e;
      this.properties = new HashMap<>();
   }

   public Exception getException() {
      return this.exception;
   }

   public Map<String, String> getProperties() {
      return this.properties;
   }

   public AnalyticsException addProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
   }
}
