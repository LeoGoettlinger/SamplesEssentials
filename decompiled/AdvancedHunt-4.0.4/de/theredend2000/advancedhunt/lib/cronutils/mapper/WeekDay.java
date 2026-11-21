package de.theredend2000.advancedhunt.lib.cronutils.mapper;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.io.Serializable;

@VisibleForTesting
public class WeekDay implements Serializable {
   private static final long serialVersionUID = -1542525283511798919L;
   private final int mondayDoWValue;
   private final boolean firstDayZero;

   public WeekDay(int mondayDoWValue, boolean firstDayZero) {
      Preconditions.checkArgument(mondayDoWValue >= 0, "Monday Day of Week value must be greater or equal to zero");
      this.mondayDoWValue = mondayDoWValue;
      this.firstDayZero = firstDayZero;
   }

   public int getMondayDoWValue() {
      return this.mondayDoWValue;
   }

   public boolean isFirstDayZero() {
      return this.firstDayZero;
   }

   public int mapTo(int dayOfWeek, WeekDay targetWeekDayDefinition) {
      if (this.firstDayZero && targetWeekDayDefinition.isFirstDayZero()) {
         return this.bothSameStartOfRange(0, 6, this, targetWeekDayDefinition).apply(dayOfWeek);
      } else if (!this.firstDayZero && !targetWeekDayDefinition.isFirstDayZero()) {
         return this.bothSameStartOfRange(1, 7, this, targetWeekDayDefinition).apply(dayOfWeek);
      } else {
         return targetWeekDayDefinition.isFirstDayZero()
            ? this.mapTo(dayOfWeek, new WeekDay(targetWeekDayDefinition.getMondayDoWValue() + 1, false)) - 1
            : this.mapTo(dayOfWeek, new WeekDay(targetWeekDayDefinition.getMondayDoWValue() - 1, true)) % 7 + 1;
      }
   }

   private Function<Integer, Integer> bothSameStartOfRange(int startRange, int endRange, WeekDay source, WeekDay target) {
      return integer -> {
         int diff = target.getMondayDoWValue() - source.getMondayDoWValue();
         int result = integer;
         if (diff == 0) {
            return integer;
         } else {
            if (diff < 0) {
               result = integer + diff;
               int distanceToStartRange = startRange - result;
               if (result < startRange) {
                  result = endRange + 1 - distanceToStartRange;
               }
            }

            if (diff > 0) {
               result = integer + diff;
               if (result > endRange) {
                  result -= endRange;
               }
            }

            return result;
         }
      };
   }
}
