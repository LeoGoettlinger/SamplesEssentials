package de.oliver.fancyanalytics.sdk.events;

import java.util.Map;

public record Event(String name, Map<String, String> properties) {
   public Event withProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
   }
}
