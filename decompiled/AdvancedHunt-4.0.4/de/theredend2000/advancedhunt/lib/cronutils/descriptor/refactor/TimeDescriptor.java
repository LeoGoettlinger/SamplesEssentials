package de.theredend2000.advancedhunt.lib.cronutils.descriptor.refactor;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class TimeDescriptor {
   private final ResourceBundle resourceBundle;

   public TimeDescriptor(ResourceBundle resourceBundle) {
      this.resourceBundle = Preconditions.checkNotNull(resourceBundle, "The resource bundle must not be null");
   }

   public String describe(Cron cron) {
      return this.describe(cron.retrieveFieldsAsMap());
   }

   private String describe(Map<CronFieldName, CronField> expressions) {
      if (expressions.containsKey(CronFieldName.SECOND)) {
         CronField cronField = expressions.get(CronFieldName.SECOND);
         if (cronField.getExpression() instanceof Always) {
            return this.describeEverySecond(1);
         }

         if (cronField.getExpression() instanceof On) {
            return this.describeAtSecond(((On)cronField.getExpression()).getTime().getValue());
         }

         if (cronField.getExpression() instanceof Every) {
            return this.describeEverySecond(((Every)cronField.getExpression()).getPeriod().getValue());
         }
      }

      return "";
   }

   private String describeEverySecond(int second) {
      double[] secondsLimit = new double[]{1.0, 2.0};
      String[] secondsStrings = new String[]{this.resourceBundle.getString("oneSecond"), this.resourceBundle.getString("multipleSeconds")};
      double[] everyLimit = new double[]{1.0, 2.0};
      String[] everyStrings = new String[]{this.resourceBundle.getString("every_one"), this.resourceBundle.getString("every_multi")};
      ChoiceFormat secondsChoiceFormat = new ChoiceFormat(secondsLimit, secondsStrings);
      ChoiceFormat everyChoiceFormat = new ChoiceFormat(everyLimit, everyStrings);
      String pattern = this.resourceBundle.getString("pattern_every_seconds");
      MessageFormat messageFormat = new MessageFormat(pattern, Locale.UK);
      Format[] formats = new Format[]{everyChoiceFormat, secondsChoiceFormat, NumberFormat.getInstance()};
      messageFormat.setFormats(formats);
      Object[] messageArguments = new Object[]{second, second, second};
      return messageFormat.format(messageArguments);
   }

   private String describeAtSecond(int second) {
      String pattern = this.resourceBundle.getString("pattern_at_second");
      MessageFormat messageFormat = new MessageFormat(pattern, Locale.UK);
      Format[] formats = new Format[]{NumberFormat.getInstance()};
      messageFormat.setFormats(formats);
      Object[] messageArguments = new Object[]{second};
      return messageFormat.format(messageArguments);
   }
}
