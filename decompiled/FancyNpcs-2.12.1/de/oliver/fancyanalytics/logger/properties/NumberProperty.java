package de.oliver.fancyanalytics.logger.properties;

public class NumberProperty extends Property<Number> {
   public NumberProperty(String key, Number value) {
      super(key, value);
   }

   public static NumberProperty of(String key, Number value) {
      return new NumberProperty(key, value);
   }
}
