package de.oliver.fancyanalytics.logger.properties;

public class Property<T> {
   public T value;
   private String key;

   public Property(String key, T value) {
      this.key = key;
      this.value = value;
   }

   public String getKey() {
      return this.key;
   }

   public T getValue() {
      return this.value;
   }
}
