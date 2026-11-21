package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@Deprecated(
   forRemoval = true,
   since = "2.14"
)
public final class Parameter<T> implements org.skriptlang.skript.common.function.Parameter<T> {
   public static final Pattern PARAM_PATTERN = Pattern.compile("^\\s*([^:(){}\",]+?)\\s*:\\s*([a-zA-Z ]+?)\\s*(?:\\s*=\\s*(.+))?\\s*$");
   final String name;
   final ClassInfo<T> type;
   @Nullable
   final Expression<? extends T> def;
   final boolean single;
   private final Set<org.skriptlang.skript.common.function.Parameter.Modifier> modifiers;
   /** @deprecated */
   final boolean keyed;

   @Deprecated(
      since = "2.13",
      forRemoval = true
   )
   public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def) {
      this(name, type, single, def, false);
   }

   @Deprecated(
      since = "2.13",
      forRemoval = true
   )
   public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def, boolean keyed) {
      this.name = name;
      this.type = type;
      this.def = def;
      this.single = single;
      this.keyed = keyed;
      this.modifiers = new HashSet<>();
      if (def != null) {
         this.modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL);
      }

      if (keyed) {
         this.modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED);
      }
   }

   @Deprecated(
      since = "2.13",
      forRemoval = true
   )
   public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def, boolean keyed, boolean optional) {
      this.name = name;
      this.type = type;
      this.def = def;
      this.single = single;
      this.keyed = keyed;
      this.modifiers = new HashSet<>();
      if (optional) {
         this.modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL);
      }

      if (keyed) {
         this.modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED);
      }
   }

   Parameter(
      String name,
      ClassInfo<T> type,
      boolean single,
      @Nullable Expression<? extends T> def,
      org.skriptlang.skript.common.function.Parameter.Modifier... modifiers
   ) {
      this.name = name;
      this.type = type;
      this.def = def;
      this.single = single;
      this.modifiers = Set.of(modifiers);
      this.keyed = this.modifiers.contains(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED);
   }

   public boolean isOptional() {
      return this.modifiers.contains(org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   public ClassInfo<T> getType() {
      return this.type;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   @Nullable
   public static <T> Parameter<T> newInstance(String name, ClassInfo<T> type, boolean single, @Nullable String def) {
      if (!Variable.isValidVariableName(name, true, false)) {
         Skript.error("A parameter's name must be a valid variable name.");
         return null;
      } else {
         Expression<? extends T> d = null;
         if (def != null) {
            RetainingLogHandler log = SkriptLogger.startRetainingLog();

            try {
               d = new SkriptParser(def, 3, ParseContext.DEFAULT).parseExpression(type.getC());
               if (d == null || LiteralUtils.hasUnparsedLiteral(d)) {
                  log.printErrors("Can't understand this expression: " + def);
                  return null;
               }

               log.printLog();
            } finally {
               log.stop();
            }
         }

         Set<org.skriptlang.skript.common.function.Parameter.Modifier> modifiers = new HashSet<>();
         if (d != null) {
            modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL);
         }

         if (!single) {
            modifiers.add(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED);
         }

         return new Parameter<>(name, type, single, d, modifiers.toArray(new org.skriptlang.skript.common.function.Parameter.Modifier[0]));
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   @Nullable
   public static List<Parameter<?>> parse(String args) {
      List<Parameter<?>> params = new ArrayList<>();
      boolean caseInsensitive = SkriptConfig.caseInsensitiveVariables.value();
      int j = 0;
      int i = 0;

      while (i <= args.length()) {
         if (i == -1) {
            Skript.error("Invalid text/variables/parentheses in the arguments of this function");
            return null;
         }

         if (i == args.length() || args.charAt(i) == ',') {
            String arg = args.substring(j, i);
            if (args.isEmpty()) {
               break;
            }

            Matcher n = PARAM_PATTERN.matcher(arg);
            if (!n.matches()) {
               Skript.error(
                  "The "
                     + StringUtils.fancyOrderNumber(params.size() + 1)
                     + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'."
               );
               return null;
            }

            String paramName = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(n.group(1));
            String lowerParamName = paramName.toLowerCase(Locale.ENGLISH);

            for (Parameter<?> p : params) {
               String otherName = caseInsensitive ? p.name.toLowerCase(Locale.ENGLISH) : p.name;
               if (otherName.equals(caseInsensitive ? lowerParamName : paramName)) {
                  Skript.error("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
                  return null;
               }
            }

            ClassInfo<?> c = Classes.getClassInfoFromUserInput(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(n.group(2)));
            NonNullPair<String, Boolean> pl = Utils.getEnglishPlural(
               StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(n.group(2))
            );
            if (c == null) {
               c = Classes.getClassInfoFromUserInput(pl.getFirst());
            }

            if (c == null) {
               Skript.error("Cannot recognise the type '" + n.group(2) + "'");
               return null;
            }

            String rParamName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) + (!pl.getSecond() ? "::1" : "") : paramName;
            Parameter<?> px = newInstance(rParamName, c, !pl.getSecond(), n.group(3));
            if (px == null) {
               return null;
            }

            params.add(px);
            j = i + 1;
         }

         if (i == args.length()) {
            break;
         }

         i = SkriptParser.next(args, i, ParseContext.DEFAULT);
      }

      return params;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13"
   )
   public String getName() {
      return this.name;
   }

   @Nullable
   public Expression<? extends T> getDefaultExpression() {
      return this.def;
   }

   public boolean isSingleValue() {
      return this.single;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof Parameter<?> parameter)
         ? false
         : this.modifiers.equals(parameter.modifiers)
            && this.single == parameter.single
            && this.name.equals(parameter.name)
            && this.type.equals(parameter.type)
            && Objects.equals(this.def, parameter.def);
   }

   @Override
   public String toString() {
      return this.toString(Skript.debug());
   }

   public String toString(boolean debug) {
      String result = this.name + ": " + Utils.toEnglishPlural(this.type.getCodeName(), !this.single);
      if (this.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.RANGED)) {
         org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier<?> range = this.getModifier(
            org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier.class
         );
         result = result + " between " + Classes.toString(range.getMin()) + " and " + Classes.toString(range.getMax());
      }

      return result + (this.def != null ? " = " + this.def.toString(null, debug) : "");
   }

   @NotNull
   @Override
   public String name() {
      return this.name;
   }

   @NotNull
   @Override
   public Class<T> type() {
      return (Class<T>)Signature.getReturns(this.single, this.type.getC());
   }

   @NotNull
   @Unmodifiable
   @Override
   public Set<org.skriptlang.skript.common.function.Parameter.Modifier> modifiers() {
      return Collections.unmodifiableSet(this.modifiers);
   }

   @Override
   public boolean isSingle() {
      return this.single;
   }
}
