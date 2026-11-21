package de.theredend2000.advancedhunt.lib.cronutils.converter;

public class CronToCalendarTransformer extends BaseCronTransformer {
   @Override
   protected void transform() {
      LOGGER.debug(
         "Setting field '{}' from cron found at position '{}' to calendar field '{}'", this.cronFieldValue, this.cronFieldPosition, this.calendarField
      );
      this.calendarInstance.set(this.calendarField, this.cronFieldValue);
   }
}
