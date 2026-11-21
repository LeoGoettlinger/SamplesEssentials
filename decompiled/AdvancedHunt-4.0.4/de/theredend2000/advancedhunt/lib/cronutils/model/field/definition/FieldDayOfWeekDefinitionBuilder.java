package de.theredend2000.advancedhunt.lib.cronutils.model.field.definition;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;

public class FieldDayOfWeekDefinitionBuilder extends FieldSpecialCharsDefinitionBuilder {
   private int mondayDoWValue = 1;

   public FieldDayOfWeekDefinitionBuilder(CronDefinitionBuilder cronDefinitionBuilder, CronFieldName fieldName) {
      super(cronDefinitionBuilder, fieldName);
      Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(fieldName), "CronFieldName must be DAY_OF_WEEK");
   }

   public FieldDayOfWeekDefinitionBuilder withMondayDoWValue(int mondayDoW) {
      this.constraints.withShiftedStringMapping(mondayDoW - this.mondayDoWValue);
      this.mondayDoWValue = mondayDoW;
      return this;
   }

   @Override
   public CronDefinitionBuilder and() {
      boolean zeroInRange = this.constraints.createConstraintsInstance().isInRange(0);
      this.cronDefinitionBuilder
         .register(
            new DayOfWeekFieldDefinition(
               this.fieldName, this.constraints.createConstraintsInstance(), this.optional, new WeekDay(this.mondayDoWValue, zeroInRange)
            )
         );
      return this.cronDefinitionBuilder;
   }

   public FieldDayOfWeekDefinitionBuilder withValidRange(int startRange, int endRange) {
      super.withValidRange(startRange, endRange);
      return this;
   }

   public FieldDayOfWeekDefinitionBuilder withIntMapping(int source, int dest) {
      super.withIntMapping(source, dest);
      return this;
   }
}
