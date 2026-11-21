package de.theredend2000.advancedhunt.lib.cronutils.model.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;

public class CronConstraintsFactory {
   private CronConstraintsFactory() {
   }

   public static CronConstraint ensureEitherDayOfYearOrMonth() {
      return new CronConstraint("Both, a day-of-year AND a day-of-month or day-of-week, are not supported.") {
         private static final long serialVersionUID = 520379111876897579L;

         @Override
         public boolean validate(Cron cron) {
            CronField dayOfYearField = cron.retrieve(CronFieldName.DAY_OF_YEAR);
            return dayOfYearField != null && !(dayOfYearField.getExpression() instanceof QuestionMark)
               ? cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark
                  && cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() instanceof QuestionMark
               : true;
         }
      };
   }

   public static CronConstraint ensureEitherDayOfWeekOrDayOfMonth() {
      return new CronConstraint("Both, a day-of-week AND a day-of-month parameter, are not supported.") {
         private static final long serialVersionUID = -4423693913868081656L;

         @Override
         public boolean validate(Cron cron) {
            CronField dayOfYearField = cron.retrieve(CronFieldName.DAY_OF_YEAR);
            CronField dayOfMonthField = cron.retrieve(CronFieldName.DAY_OF_MONTH);
            CronField dayOfWeekField = cron.retrieve(CronFieldName.DAY_OF_WEEK);
            if (dayOfYearField != null && !(dayOfYearField.getExpression() instanceof QuestionMark)) {
               return true;
            } else {
               return dayOfMonthField != null && !(dayOfMonthField.getExpression() instanceof QuestionMark)
                  ? dayOfWeekField != null && dayOfWeekField.getExpression() instanceof QuestionMark
                  : dayOfWeekField != null && !(dayOfWeekField.getExpression() instanceof QuestionMark);
            }
         }
      };
   }
}
