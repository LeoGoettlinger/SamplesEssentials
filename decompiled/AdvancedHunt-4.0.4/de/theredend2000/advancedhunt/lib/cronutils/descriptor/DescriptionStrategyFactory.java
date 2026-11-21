package de.theredend2000.advancedhunt.lib.cronutils.descriptor;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ResourceBundle;

class DescriptionStrategyFactory {
   private DescriptionStrategyFactory() {
   }

   public static DescriptionStrategy daysOfWeekInstance(ResourceBundle bundle, FieldExpression expression, FieldDefinition definition) {
      Function<Integer, String> nominal = integer -> {
         int diff = definition instanceof DayOfWeekFieldDefinition
            ? DayOfWeek.MONDAY.getValue() - ((DayOfWeekFieldDefinition)definition).getMondayDoWValue().getMondayDoWValue()
            : 0;
         return DayOfWeek.of(integer + diff < 1 ? 7 : integer + diff).getDisplayName(TextStyle.FULL, bundle.getLocale());
      };
      NominalDescriptionStrategy dow = new NominalDescriptionStrategy(bundle, nominal, expression);
      dow.addDescription(fieldExpression -> {
         if (fieldExpression instanceof On) {
            On on = (On)fieldExpression;
            switch (on.getSpecialChar().getValue()) {
               case L:
                  return String.format("%s %s %s ", bundle.getString("last"), nominal.apply(on.getTime().getValue()), bundle.getString("of_every_month"));
               case HASH:
                  return String.format("%s %s %s ", nominal.apply(on.getTime().getValue()), on.getNth(), bundle.getString("of_every_month"));
               default:
                  return "";
            }
         } else {
            return "";
         }
      });
      return dow;
   }

   public static DescriptionStrategy daysOfMonthInstance(ResourceBundle bundle, FieldExpression expression) {
      NominalDescriptionStrategy dom = new NominalDescriptionStrategy(bundle, null, expression);
      dom.addDescription(fieldExpression -> {
         if (fieldExpression instanceof On) {
            On on = (On)fieldExpression;
            switch (on.getSpecialChar().getValue()) {
               case W:
                  return String.format("%s %s %s ", bundle.getString("the_nearest_weekday_to_the"), on.getTime().getValue(), bundle.getString("of_the_month"));
               case L:
                  Integer daysBefore = on.getNth().getValue();
                  if (daysBefore > 1) {
                     return MessageFormat.format(bundle.getString("days_before_last_day_of_month"), daysBefore);
                  } else {
                     if (daysBefore == 1) {
                        return bundle.getString("day_before_last_day_of_month");
                     }

                     return bundle.getString("last_day_of_month");
                  }
               case LW:
                  return bundle.getString("last_weekday_of_month");
               default:
                  return "";
            }
         } else {
            return "";
         }
      });
      return dom;
   }

   public static DescriptionStrategy monthsInstance(ResourceBundle bundle, FieldExpression expression) {
      Function<Integer, String> mappingFunction;
      if (expression instanceof Every) {
         mappingFunction = Object::toString;
      } else {
         mappingFunction = integer -> Month.of(integer).getDisplayName(TextStyle.FULL, bundle.getLocale());
      }

      return new NominalDescriptionStrategy(bundle, mappingFunction, expression);
   }

   public static DescriptionStrategy plainInstance(ResourceBundle bundle, FieldExpression expression) {
      return new NominalDescriptionStrategy(bundle, null, expression);
   }

   public static DescriptionStrategy hhMMssInstance(ResourceBundle bundle, FieldExpression hours, FieldExpression minutes, FieldExpression seconds) {
      return new TimeDescriptionStrategy(bundle, hours, minutes, seconds);
   }
}
