package de.theredend2000.advancedhunt.lib.cronutils.parser;

import de.theredend2000.advancedhunt.lib.cronutils.builder.CronBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.CompositeCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.SingleCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronNicknames;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CronParser {
   private final Map<Integer, List<CronParserField>> expressions = new HashMap<>();
   private final CronDefinition cronDefinition;

   public CronParser(CronDefinition cronDefinition) {
      this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
      this.buildPossibleExpressions(cronDefinition);
   }

   private void buildPossibleExpressions(CronDefinition cronDefinition) {
      List<CronParserField> sortedExpression = cronDefinition.getFieldDefinitions()
         .stream()
         .map(this::toCronParserField)
         .sorted(CronParserField.createFieldTypeComparator())
         .collect(Collectors.toList());
      List<CronParserField> tempExpression = sortedExpression;

      while (this.lastFieldIsOptional(tempExpression)) {
         int expressionLength = tempExpression.size() - 1;
         ArrayList<CronParserField> possibleExpression = new ArrayList<>(tempExpression.subList(0, expressionLength));
         this.expressions.put(expressionLength, possibleExpression);
         tempExpression = possibleExpression;
      }

      this.expressions.put(sortedExpression.size(), sortedExpression);
   }

   private CronParserField toCronParserField(FieldDefinition fieldDefinition) {
      return new CronParserField(fieldDefinition.getFieldName(), fieldDefinition.getConstraints(), fieldDefinition.isOptional());
   }

   private boolean lastFieldIsOptional(List<CronParserField> fields) {
      return !fields.isEmpty() && fields.get(fields.size() - 1).isOptional();
   }

   private Cron validateAndReturnSupportedCronNickname(String nickname, Set<CronNicknames> cronNicknames, CronNicknames cronNickname, Cron cron) {
      if (cronNicknames.contains(cronNickname)) {
         return cron;
      } else {
         throw new IllegalArgumentException(String.format("Nickname %s not supported!", nickname));
      }
   }

   public Cron parse(String expression) {
      Preconditions.checkNotNull(expression, "Expression must not be null");
      String replaced = expression.replaceAll("\\s+", " ").trim();
      if (StringUtils.isEmpty(replaced)) {
         throw new IllegalArgumentException("Empty expression!");
      } else {
         Set<CronNicknames> cronNicknames = this.cronDefinition.getCronNicknames();
         if (expression.startsWith("@")) {
            if (cronNicknames.isEmpty()) {
               throw new IllegalArgumentException("Nicknames not supported!");
            }

            switch (expression) {
               case "@yearly":
                  return this.validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.YEARLY, CronBuilder.yearly(this.cronDefinition));
               case "@annually":
                  return this.validateAndReturnSupportedCronNickname(
                     expression, cronNicknames, CronNicknames.ANNUALLY, CronBuilder.annually(this.cronDefinition)
                  );
               case "@monthly":
                  return this.validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.MONTHLY, CronBuilder.monthly(this.cronDefinition));
               case "@weekly":
                  return this.validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.WEEKLY, CronBuilder.weekly(this.cronDefinition));
               case "@daily":
                  return this.validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.DAILY, CronBuilder.daily(this.cronDefinition));
               case "@midnight":
                  return this.validateAndReturnSupportedCronNickname(
                     expression, cronNicknames, CronNicknames.MIDNIGHT, CronBuilder.midnight(this.cronDefinition)
                  );
               case "@hourly":
                  return this.validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.HOURLY, CronBuilder.hourly(this.cronDefinition));
               case "@reboot":
                  return this.validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.REBOOT, CronBuilder.reboot(this.cronDefinition));
            }
         }

         if (expression.contains("||")) {
            List<Cron> crons = Arrays.stream(expression.split("\\|\\|")).map(this::parse).collect(Collectors.toList());
            return new CompositeCron(crons);
         } else if (expression.contains("|")) {
            List<String> crons = new ArrayList<>();
            int cronscount = Arrays.stream(expression.split("\\s+")).mapToInt(sx -> sx.split("\\|").length).max().orElse(0);

            for (int j = 0; j < cronscount; j++) {
               StringBuilder builder = new StringBuilder();

               for (String s : expression.split("\\s+")) {
                  if (s.contains("|")) {
                     builder.append(String.format("%s ", s.split("\\|")[j]));
                  } else {
                     builder.append(String.format("%s ", s));
                  }
               }

               crons.add(builder.toString().trim());
            }

            return new CompositeCron(crons.stream().map(this::parse).collect(Collectors.toList()));
         } else {
            String[] expressionParts = replaced.toUpperCase().split(" ");
            int expressionLength = expressionParts.length;
            String fieldWithTrailingCommas = Arrays.stream(expressionParts).filter(x -> x.endsWith(",")).findAny().orElse(null);
            if (fieldWithTrailingCommas != null) {
               throw new IllegalArgumentException(String.format("Invalid field value! Trailing commas not permitted! '%s'", fieldWithTrailingCommas));
            } else {
               List<CronParserField> fields = this.expressions.get(expressionLength);
               if (fields == null) {
                  throw new IllegalArgumentException(
                     String.format("Cron expression contains %s parts but we expect one of %s", expressionLength, this.expressions.keySet())
                  );
               } else {
                  try {
                     int size = expressionParts.length;
                     List<CronField> results = new ArrayList<>(size + 1);

                     for (int j = 0; j < size; j++) {
                        results.add(fields.get(j).parse(expressionParts[j]));
                     }

                     return new SingleCron(this.cronDefinition, results).validate();
                  } catch (IllegalArgumentException var12) {
                     throw new IllegalArgumentException(String.format("Failed to parse cron expression. %s", var12.getMessage()), var12);
                  }
               }
            }
         }
      }
   }
}
