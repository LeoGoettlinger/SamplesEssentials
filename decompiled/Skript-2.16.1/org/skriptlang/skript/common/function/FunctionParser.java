package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.SequencedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class FunctionParser {
   private static final Pattern SCRIPT_PARAMETER_PATTERN = Pattern.compile(
      "^\\s*(?<name>[^:(){}\",]+?)\\s*:\\s*(?<type>[a-zA-Z ]+?)\\s*(?:\\s*=\\s*(?<def>.+))?\\s*$"
   );

   @Nullable
   public static ch.njol.skript.lang.function.Signature<?> parse(String script, String name, String args, @Nullable String returns, boolean local) {
      Parameters parameters = parseParameters(args);
      if (parameters == null) {
         return null;
      } else {
         Class<?> returnType = null;
         if (returns != null) {
            ClassInfo<?> returnClass = Classes.getClassInfoFromUserInput(returns);
            Utils.PluralResult result = Utils.isPlural(returns);
            if (returnClass == null) {
               returnClass = Classes.getClassInfoFromUserInput(result.updated());
            }

            if (returnClass == null) {
               Skript.error("Cannot recognise the type '" + returns + "'");
               return null;
            } else {
               if (result.plural()) {
                  returnType = returnClass.getC().arrayType();
               } else {
                  returnType = returnClass.getC();
               }

               return new ch.njol.skript.lang.function.Signature<>(script, name, parameters, returnType, local);
            }
         } else {
            return new ch.njol.skript.lang.function.Signature<>(script, name, parameters, returnType, local);
         }
      }
   }

   private static Parameters parseParameters(String args) {
      SequencedMap<String, Parameter<?>> params = new LinkedHashMap<>();
      boolean caseInsensitive = SkriptConfig.caseInsensitiveVariables.value();
      if (args.isEmpty()) {
         return new Parameters(params);
      } else {
         int j = 0;
         int i = 0;

         while (true) {
            label83: {
               if (i <= args.length()) {
                  if (i == -1) {
                     Skript.error("Invalid text/variables/parentheses in the arguments of this function");
                     return null;
                  }

                  if (i != args.length() && args.charAt(i) != ',') {
                     break label83;
                  }

                  String arg = args.substring(j, i);
                  Matcher matcher = SCRIPT_PARAMETER_PATTERN.matcher(arg);
                  if (!matcher.matches()) {
                     Skript.error(
                        "The "
                           + StringUtils.fancyOrderNumber(params.size() + 1)
                           + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'."
                     );
                     return null;
                  }

                  String paramName = matcher.group("name");
                  String lowerParamName = paramName.toLowerCase(Locale.ENGLISH);

                  for (String otherName : params.keySet()) {
                     otherName = caseInsensitive ? otherName.toLowerCase(Locale.ENGLISH) : otherName;
                     if (otherName.equals(caseInsensitive ? lowerParamName : paramName)) {
                        Skript.error("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
                        return null;
                     }
                  }

                  ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(matcher.group("type"));
                  Utils.PluralResult result = Utils.isPlural(matcher.group("type"));
                  if (classInfo == null) {
                     classInfo = Classes.getClassInfoFromUserInput(result.updated());
                  }

                  if (classInfo == null) {
                     Skript.error("Cannot recognise the type '%s'", matcher.group("type"));
                     return null;
                  }

                  String variableName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) + (!result.plural() ? "::1" : "") : paramName;
                  Class<?> type;
                  if (result.plural()) {
                     type = classInfo.getC().arrayType();
                  } else {
                     type = classInfo.getC();
                  }

                  Parameter<?> parameter = ScriptParameter.parse(variableName, type, matcher.group("def"));
                  if (parameter == null) {
                     return null;
                  }

                  params.put(variableName, parameter);
                  j = i + 1;
                  if (i != args.length()) {
                     break label83;
                  }
               }

               return new Parameters(params);
            }

            i = SkriptParser.next(args, i, ParseContext.DEFAULT);
         }
      }
   }
}
