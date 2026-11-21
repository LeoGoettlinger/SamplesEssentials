package de.theredend2000.advancedhunt.lib.cronutils.parser;

import de.theredend2000.advancedhunt.lib.cronutils.StringValidations;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialCharFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.StringUtils;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.util.regex.Pattern;

public class FieldParser {
   private static final String SLASH = "/";
   private static final String W_STRING = "W";
   private static final String EMPTY_STRING = "";
   private static final String LW_STRING = "LW";
   private static final String HASH_TAG = "#";
   private static final String L_STRING = "L";
   private static final String QUESTION_MARK_STRING = "?";
   private static final String ASTERISK = "*";
   private static final char[] SPECIAL_CHARS_MINUS_STAR = new char[]{'/', '-', ','};
   private static final Pattern L_PATTERN = Pattern.compile("[0-9]L", 2);
   private static final Pattern W_PATTERN = Pattern.compile("[0-9]W", 2);
   private static final String ASTERISK_ALWAYS_VALUE = "1";
   private final FieldConstraints fieldConstraints;

   public FieldParser(FieldConstraints constraints) {
      this.fieldConstraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
   }

   public FieldExpression parse(String expression) {
      if (!StringUtils.containsAny(expression, SPECIAL_CHARS_MINUS_STAR)) {
         if (expression.contains("?") && !this.fieldConstraints.getSpecialChars().contains(SpecialChar.QUESTION_MARK)) {
            throw new IllegalArgumentException("Invalid expression: " + expression);
         } else {
            return this.noSpecialCharsNorStar(expression);
         }
      } else {
         String[] array = expression.split(",");
         if (array.length > 1) {
            return this.commaSplitResult(array);
         } else {
            String[] splitted = expression.split("-");
            if (expression.contains("-") && splitted.length != 2) {
               throw new IllegalArgumentException("Missing values for range: " + expression);
            } else {
               return (FieldExpression)(splitted[0].equalsIgnoreCase("L")
                  ? this.parseOnWithL(splitted[0], this.mapToIntegerFieldValue(splitted[1]))
                  : this.dashSplitResult(expression, splitted));
            }
         }
      }
   }

   private FieldExpression dashSplitResult(String expression, String[] betweenArray) {
      return betweenArray.length > 1 ? this.parseBetween(betweenArray) : this.slashSplit(expression, expression.split("/"));
   }

   private FieldExpression commaSplitResult(String[] array) {
      And and = new And();

      for (String exp : array) {
         and.and(this.parse(exp));
      }

      return and;
   }

   private FieldExpression slashSplit(String expression, String[] values) {
      if (values.length == 2) {
         String start = values[0];
         String value = values[1];
         return this.asteriskOrempty(start, value);
      } else if (values.length == 1) {
         throw new IllegalArgumentException("Missing steps for expression: " + expression);
      } else {
         throw new IllegalArgumentException("Invalid expression: " + expression);
      }
   }

   private FieldExpression asteriskOrempty(String start, String value) {
      String trimmedStart = start.trim();
      if ("*".equals(trimmedStart) && value.equals("1")) {
         return this.noSpecialCharsNorStar(start);
      } else {
         return !"*".equals(trimmedStart) && !"".equals(start.trim())
            ? new Every(new On(this.mapToIntegerFieldValue(start)), new IntegerFieldValue(Integer.parseInt(value)))
            : new Every(new IntegerFieldValue(Integer.parseInt(value)));
      }
   }

   private FieldExpression noSpecialCharsNorStar(String expression) {
      if ("*".equals(expression)) {
         return FieldExpression.always();
      } else {
         return (FieldExpression)("?".equals(expression) ? FieldExpression.questionMark() : this.parseOn(expression));
      }
   }

   @VisibleForTesting
   protected FieldExpression parseBetween(String[] array) {
      if (!array[0].isEmpty() && !array[1].isEmpty()) {
         if (array[1].contains("/")) {
            String[] every = array[1].split("/");
            return new Every(new Between(this.map(array[0]), this.map(every[0])), this.mapToIntegerFieldValue(every[1]));
         } else {
            return new Between(this.map(array[0]), this.map(array[1]));
         }
      } else {
         throw new IllegalArgumentException(
            String.format("Invalid expression! Expression: %s-%s does not describe a range. Negative numbers are not allowed.", array[0], array[1])
         );
      }
   }

   @VisibleForTesting
   protected On parseOn(String exp) {
      if ("?".equals(exp)) {
         return this.parseOnWithQuestionMark(exp);
      } else if (exp.contains("#")) {
         return this.parseOnWithHash(exp);
      } else if (exp.contains("LW")) {
         return this.parseOnWithLW(exp);
      } else if (L_PATTERN.matcher(exp).find() || exp.equalsIgnoreCase("L")) {
         return this.parseOnWithL(exp);
      } else {
         return W_PATTERN.matcher(exp).find()
            ? this.parseOnWithW(exp)
            : new On(this.mapToIntegerFieldValue(exp), new SpecialCharFieldValue(SpecialChar.NONE), new IntegerFieldValue(-1));
      }
   }

   @VisibleForTesting
   protected On parseOnWithHash(String exp) {
      if (!this.fieldConstraints.getSpecialChars().contains(SpecialChar.HASH)) {
         throw new IllegalArgumentException("Invalid expression: " + exp);
      } else {
         SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.HASH);
         String[] array = exp.split("#");
         if (array.length == 0) {
            throw new IllegalArgumentException("Invalid Position of # Character!");
         } else {
            IntegerFieldValue nth = this.mapToIntegerFieldValue(array[1]);
            if (array[0].isEmpty()) {
               throw new IllegalArgumentException("Time should be specified!");
            } else {
               return new On(this.mapToIntegerFieldValue(array[0]), specialChar, nth);
            }
         }
      }
   }

   @VisibleForTesting
   protected On parseOnWithQuestionMark(String exp) {
      SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.QUESTION_MARK);
      String questionMarkExpression = exp.replace("?", "");
      if ("".equals(questionMarkExpression)) {
         return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
      } else {
         throw new IllegalArgumentException(String.format("Expected: '?', found: %s", questionMarkExpression));
      }
   }

   @VisibleForTesting
   protected On parseOnWithLW(String exp) {
      SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.LW);
      String lwExpression = exp.replace("LW", "");
      if ("".equals(lwExpression)) {
         return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
      } else {
         throw new IllegalArgumentException(String.format("Expected: LW, found: %s", lwExpression));
      }
   }

   @VisibleForTesting
   protected On parseOnWithL(String exp) {
      return this.parseOnWithL(exp, new IntegerFieldValue(-1));
   }

   protected On parseOnWithL(String exp, IntegerFieldValue daysBefore) {
      SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.L);
      String expression = exp.replace("L", "");
      IntegerFieldValue time = new IntegerFieldValue(-1);
      if (!"".equals(expression)) {
         time = this.mapToIntegerFieldValue(expression);
      }

      return new On(time, specialChar, daysBefore);
   }

   @VisibleForTesting
   protected On parseOnWithW(String exp) {
      return new On(this.mapToIntegerFieldValue(exp.replace("W", "")), new SpecialCharFieldValue(SpecialChar.W), new IntegerFieldValue(-1));
   }

   @VisibleForTesting
   protected IntegerFieldValue mapToIntegerFieldValue(String string) {
      try {
         return new IntegerFieldValue(this.intToInt(this.stringToInt(string)));
      } catch (NumberFormatException var3) {
         throw new IllegalArgumentException(String.format("Invalid value. Expected some integer, found %s", string));
      }
   }

   @VisibleForTesting
   protected FieldValue<?> map(String string) {
      for (SpecialChar sc : SpecialChar.values()) {
         if (sc.toString().equals(string)) {
            return new SpecialCharFieldValue(sc);
         }
      }

      return new IntegerFieldValue(this.stringToInt(string));
   }

   @VisibleForTesting
   protected int stringToInt(String exp) {
      Integer value = this.fieldConstraints.getStringMappingValue(exp);
      if (value != null) {
         return value;
      } else {
         try {
            return Integer.parseInt(exp);
         } catch (NumberFormatException var5) {
            String invalidChars = new StringValidations(this.fieldConstraints).removeValidChars(exp);
            throw new IllegalArgumentException(String.format("Invalid chars in expression! Expression: %s Invalid chars: %s", exp, invalidChars));
         }
      }
   }

   @VisibleForTesting
   protected int intToInt(Integer exp) {
      Integer value = this.fieldConstraints.getIntMappingValue(exp);
      return value != null ? value : exp;
   }
}
