package de.theredend2000.advancedhunt.lib.cronutils.model.field.definition;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;

public class DayOfWeekFieldDefinition extends FieldDefinition {
   private static final long serialVersionUID = 8684844402711204711L;
   private final WeekDay mondayDoWValue;

   public DayOfWeekFieldDefinition(CronFieldName fieldName, FieldConstraints constraints, boolean optional, WeekDay mondayDoWValue) {
      super(fieldName, constraints, optional);
      this.mondayDoWValue = mondayDoWValue;
   }

   public WeekDay getMondayDoWValue() {
      return this.mondayDoWValue;
   }
}
