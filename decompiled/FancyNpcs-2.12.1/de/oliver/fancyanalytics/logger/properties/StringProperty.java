package de.oliver.fancyanalytics.logger.properties;

public class StringProperty extends Property<String> {
   public StringProperty(String key, String value) {
      super(key, value);
   }

   public static StringProperty of(String key, String value) {
      return new StringProperty(key, value);
   }
}
