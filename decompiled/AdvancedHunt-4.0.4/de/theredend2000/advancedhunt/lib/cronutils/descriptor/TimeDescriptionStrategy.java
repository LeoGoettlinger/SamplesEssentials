package de.theredend2000.advancedhunt.lib.cronutils.descriptor;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

class TimeDescriptionStrategy extends DescriptionStrategy {
   private final FieldExpression hours;
   private final FieldExpression minutes;
   private final FieldExpression seconds;
   private final Set<Function<TimeDescriptionStrategy.TimeFields, String>> descriptions;
   private static final int DEFAULTSECONDS = 0;
   private static final String EVERY = "every";
   private static final String SECOND = "second";
   private static final String MINUTE = "minute";
   private static final String HOUR = "hour";
   private static final String EVERY_MINUTE_FORMAT = "%s %s ";

   TimeDescriptionStrategy(ResourceBundle bundle, FieldExpression hours, FieldExpression minutes, FieldExpression seconds) {
      super(bundle);
      this.hours = this.ensureInstance(hours, FieldExpression.always());
      this.minutes = this.ensureInstance(minutes, FieldExpression.always());
      this.seconds = this.ensureInstance(seconds, new On(new IntegerFieldValue(0)));
      this.descriptions = new HashSet<>();
      this.registerFunctions();
   }

   private FieldExpression ensureInstance(FieldExpression expression, FieldExpression defaultExpression) {
      Preconditions.checkNotNull(defaultExpression, "Default expression must not be null");
      return expression != null ? expression : defaultExpression;
   }

   @Override
   public String describe() {
      TimeDescriptionStrategy.TimeFields fields = new TimeDescriptionStrategy.TimeFields(this.hours, this.minutes, this.seconds);

      for (Function<TimeDescriptionStrategy.TimeFields, String> function : this.descriptions) {
         if (!"".equals(function.apply(fields))) {
            return function.apply(fields);
         }
      }

      String secondsDesc = "";
      String minutesDesc = "";
      String hoursDesc = "";
      if (!(this.hours instanceof Always)) {
         hoursDesc = this.addTimeExpressions(this.describe(this.hours), this.bundle.getString("hour"), this.bundle.getString("hours"));
      }

      if ((!(this.minutes instanceof On) || !this.isDefault((On)this.minutes)) && (!(this.minutes instanceof Always) || !(this.hours instanceof Always))) {
         minutesDesc = this.addTimeExpressions(this.describe(this.minutes), this.bundle.getString("minute"), this.bundle.getString("minutes"));
      }

      if (!(this.seconds instanceof On) || !this.isDefault((On)this.seconds)) {
         secondsDesc = this.addTimeExpressions(this.describe(this.seconds), this.bundle.getString("second"), this.bundle.getString("seconds"));
      }

      return String.format("%s %s %s", secondsDesc, minutesDesc, hoursDesc);
   }

   @Override
   protected String describe(Always always, boolean and) {
      return this.describe(new Every(new IntegerFieldValue(1)), and);
   }

   private String addTimeExpressions(String description, String singular, String plural) {
      return description.replaceAll("%s", singular).replaceAll("replace_plural", plural);
   }

   private void registerFunctions() {
      this.descriptions
         .add(
            timeFields -> {
               if (timeFields.hours instanceof Always && timeFields.minutes instanceof Always) {
                  if (timeFields.seconds instanceof Always) {
                     return String.format("%s %s ", this.bundle.getString("every"), this.bundle.getString("second"));
                  }

                  if (timeFields.seconds instanceof On) {
                     if (this.isDefault((On)timeFields.seconds)) {
                        return String.format("%s %s ", this.bundle.getString("every"), this.bundle.getString("minute"));
                     }

                     return String.format(
                        "%s %s %s %s %02d",
                        this.bundle.getString("every"),
                        this.bundle.getString("minute"),
                        this.bundle.getString("at"),
                        this.bundle.getString("second"),
                        ((On)timeFields.seconds).getTime().getValue()
                     );
                  }
               }

               return "";
            }
         );
      this.descriptions
         .add(
            timeFields -> {
               if (!(timeFields.hours instanceof Always) || !(timeFields.minutes instanceof On) || !(timeFields.seconds instanceof On)) {
                  return "";
               } else if (!this.isDefault((On)timeFields.seconds)) {
                  return String.format(
                     "%s %s %s %s %s %s %s %s",
                     this.bundle.getString("every"),
                     this.bundle.getString("hour"),
                     this.bundle.getString("at"),
                     this.bundle.getString("minute"),
                     ((On)timeFields.minutes).getTime().getValue(),
                     this.bundle.getString("and"),
                     this.bundle.getString("second"),
                     ((On)timeFields.seconds).getTime().getValue()
                  );
               } else {
                  return this.isDefault((On)timeFields.minutes)
                     ? String.format("%s %s ", this.bundle.getString("every"), this.bundle.getString("hour"))
                     : String.format(
                        "%s %s %s %s %s",
                        this.bundle.getString("every"),
                        this.bundle.getString("hour"),
                        this.bundle.getString("at"),
                        this.bundle.getString("minute"),
                        ((On)timeFields.minutes).getTime().getValue()
                     );
               }
            }
         );
      this.descriptions
         .add(
            timeFields -> timeFields.hours instanceof On && timeFields.minutes instanceof On && timeFields.seconds instanceof Always
               ? String.format(
                  "%s %s %s %02d:%02d",
                  this.bundle.getString("every"),
                  this.bundle.getString("second"),
                  this.bundle.getString("at"),
                  ((On)this.hours).getTime().getValue(),
                  ((On)this.minutes).getTime().getValue()
               )
               : ""
         );
      this.descriptions
         .add(
            timeFields -> {
               if (!(timeFields.hours instanceof On) || !(timeFields.minutes instanceof On) || !(timeFields.seconds instanceof On)) {
                  return "";
               } else {
                  return this.isDefault((On)timeFields.seconds)
                     ? String.format(
                        "%s %02d:%02d", this.bundle.getString("at"), ((On)this.hours).getTime().getValue(), ((On)this.minutes).getTime().getValue()
                     )
                     : String.format(
                        "%s %02d:%02d:%02d",
                        this.bundle.getString("at"),
                        ((On)this.hours).getTime().getValue(),
                        ((On)this.minutes).getTime().getValue(),
                        ((On)this.seconds).getTime().getValue()
                     );
               }
            }
         );
      this.descriptions
         .add(
            timeFields -> timeFields.hours instanceof On && timeFields.minutes instanceof Always && timeFields.seconds instanceof Always
               ? String.format("%s %02d:00", this.bundle.getString("at"), ((On)this.hours).getTime().getValue())
               : ""
         );
      this.descriptions
         .add(
            timeFields -> {
               if (timeFields.hours instanceof On && timeFields.minutes instanceof Between) {
                  if (timeFields.seconds instanceof On) {
                     return String.format(
                        "%s %s %s %02d:%02d %s %02d:%02d",
                        this.bundle.getString("every"),
                        this.bundle.getString("minute"),
                        this.bundle.getString("between"),
                        ((On)timeFields.hours).getTime().getValue(),
                        ((Between)timeFields.minutes).getFrom().getValue(),
                        this.bundle.getString("and"),
                        ((On)timeFields.hours).getTime().getValue(),
                        ((Between)timeFields.minutes).getTo().getValue()
                     );
                  }

                  if (timeFields.seconds instanceof Always) {
                     return String.format(
                        "%s %s %s %02d:%02d %s %02d:%02d",
                        this.bundle.getString("every"),
                        this.bundle.getString("second"),
                        this.bundle.getString("between"),
                        ((On)timeFields.hours).getTime().getValue(),
                        ((Between)timeFields.minutes).getFrom().getValue(),
                        this.bundle.getString("and"),
                        ((On)timeFields.hours).getTime().getValue(),
                        ((Between)timeFields.minutes).getTo().getValue()
                     );
                  }
               }

               return "";
            }
         );
      this.descriptions.add(timeFields -> {
         if (timeFields.hours instanceof Always && timeFields.minutes instanceof Every && timeFields.seconds instanceof On) {
            Every minute = (Every)timeFields.minutes;
            String desc;
            if (minute.getPeriod().getValue() == 1 && this.isDefault((On)timeFields.seconds)) {
               desc = String.format("%s %s ", this.bundle.getString("every"), this.bundle.getString("minute"));
            } else {
               desc = String.format("%s %s %s ", this.bundle.getString("every"), minute.getPeriod().getValue(), this.bundle.getString("minutes"));
            }

            return minute.getExpression() instanceof Between ? "" : desc;
         } else {
            return "";
         }
      });
      this.descriptions
         .add(
            timeFields -> {
               if (timeFields.hours instanceof Every && timeFields.minutes instanceof On && timeFields.seconds instanceof On) {
                  if (((On)timeFields.minutes).getTime().getValue() == 0 && ((On)timeFields.seconds).getTime().getValue() == 0) {
                     Integer period = ((Every)timeFields.hours).getPeriod().getValue();
                     if (period == null || period == 1) {
                        return String.format("%s %s ", this.bundle.getString("every"), this.bundle.getString("hour"));
                     }
                  }

                  String result = String.format(
                     "%s %s %s %s %s %s ",
                     this.bundle.getString("every"),
                     ((Every)this.hours).getPeriod().getValue(),
                     this.bundle.getString("hours"),
                     this.bundle.getString("at"),
                     this.bundle.getString("minute"),
                     ((On)this.minutes).getTime().getValue()
                  );
                  return this.isDefault((On)timeFields.seconds)
                     ? result
                     : String.format("%s %s %s", this.bundle.getString("and"), this.bundle.getString("second"), ((On)this.seconds).getTime().getValue());
               } else {
                  return "";
               }
            }
         );
      this.descriptions
         .add(
            timeFields -> timeFields.hours instanceof Always && timeFields.seconds instanceof Always && timeFields.minutes instanceof On
               ? String.format(
                  "%s %s %s %s %02d %s",
                  this.bundle.getString("every"),
                  this.bundle.getString("second"),
                  this.bundle.getString("at"),
                  this.bundle.getString("minute"),
                  ((On)timeFields.minutes).getTime().getValue(),
                  this.bundle.getString("of_every_hour")
               )
               : ""
         );
      this.descriptions.add(timeFields -> {
         if (timeFields.hours instanceof Always && timeFields.seconds instanceof Always && timeFields.minutes instanceof And) {
            String minutesDesc = this.addTimeExpressions(this.describe(timeFields.minutes), this.bundle.getString("minute"), this.bundle.getString("minutes"));
            return String.format("%s %s %s", this.bundle.getString("every"), this.bundle.getString("second"), minutesDesc);
         } else {
            return "";
         }
      });
      this.descriptions.add(timeFields -> {
         if (timeFields.hours instanceof And && timeFields.minutes instanceof Always && timeFields.seconds instanceof Always) {
            String hoursDesc = this.addTimeExpressions(this.describe(timeFields.hours), this.bundle.getString("hour"), this.bundle.getString("hours"));
            return String.format("%s %s %s", this.bundle.getString("every"), this.bundle.getString("second"), hoursDesc);
         } else {
            return "";
         }
      });
   }

   private boolean isDefault(On on) {
      return on.getTime().getValue() == 0;
   }

   class TimeFields {
      private final FieldExpression seconds;
      private final FieldExpression minutes;
      private final FieldExpression hours;

      public TimeFields(FieldExpression hours, FieldExpression minutes, FieldExpression seconds) {
         this.hours = hours;
         this.minutes = minutes;
         this.seconds = seconds;
      }
   }
}
