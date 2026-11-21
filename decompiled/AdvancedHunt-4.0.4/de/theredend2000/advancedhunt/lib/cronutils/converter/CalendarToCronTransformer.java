package de.theredend2000.advancedhunt.lib.cronutils.converter;

public class CalendarToCronTransformer extends BaseCronTransformer {
   @Override
   protected void transform() {
      String updatedValue = String.valueOf(this.calendarInstance.get(this.calendarField));
      LOGGER.debug("Updating cron field at position {} with {}, using calendar field {}", this.cronFieldPosition, updatedValue, this.calendarField);
      this.cronParts[this.cronFieldPosition] = updatedValue;
   }
}
