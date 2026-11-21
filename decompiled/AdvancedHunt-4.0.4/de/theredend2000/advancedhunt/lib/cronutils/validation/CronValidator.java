package de.theredend2000.advancedhunt.lib.cronutils.validation;

import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.parser.CronParser;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CronValidator implements ConstraintValidator<Cron, String> {
   private CronType type;

   public void initialize(Cron constraintAnnotation) {
      this.type = constraintAnnotation.type();
   }

   public boolean isValid(String value, ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      } else {
         CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(this.type);
         CronParser cronParser = new CronParser(cronDefinition);

         try {
            cronParser.parse(value).validate();
            return true;
         } catch (IllegalArgumentException var6) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(var6.getMessage()).addConstraintViolation();
            return false;
         }
      }
   }
}
