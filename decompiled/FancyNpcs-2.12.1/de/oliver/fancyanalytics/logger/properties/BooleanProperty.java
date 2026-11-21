package de.oliver.fancyanalytics.logger.properties;

public class BooleanProperty extends Property<Boolean> {
   public BooleanProperty(String key, Boolean value) {
      super(key, value);
   }

   public static BooleanProperty of(String key, Boolean value) {
      return new BooleanProperty(key, value);
   }
}
