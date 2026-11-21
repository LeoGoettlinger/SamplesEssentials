package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Method Exists")
@Description({"Checks if a method exists"})
@Example("if method \"org.bukkit.Bukkit#getPluginCommand(java.lang.String)\" exists")
@Since({"2.7"})
@NoDoc
public class CondMethodExists extends PropertyCondition<String> {
   private static final Pattern SIGNATURE_PATTERN = Pattern.compile("(?<class>.+)#(?<name>.+)\\((?<params>.*)\\)");
   private Expression<String> signatures;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.signatures = (Expression<String>)exprs[0];
      this.setExpr(this.signatures);
      this.setNegated(parseResult.hasTag("dont"));
      return true;
   }

   public boolean check(String signature) {
      Matcher sigMatcher = SIGNATURE_PATTERN.matcher(signature);
      if (!sigMatcher.matches()) {
         return false;
      } else {
         try {
            Class<?> clazz = Class.forName(sigMatcher.group("class"));
            List<Class<?>> parameters = new ArrayList<>();
            String rawParameters = sigMatcher.group("params");
            if (!StringUtils.isBlank(rawParameters)) {
               for (String parameter : rawParameters.split(",")) {
                  parameters.add(parseClass(parameter.trim()));
               }
            }

            return Skript.methodExists(clazz, sigMatcher.group("name"), parameters.toArray(new Class[0]));
         } catch (ClassNotFoundException var10) {
            return false;
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "method exists";
   }

   @Override
   public Condition simplify() {
      return (Condition)(!(this.signatures instanceof Literal) ? this : SimplifiedCondition.fromCondition(this));
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "method " + this.signatures.toString(event, debug) + " exists";
   }

   private static Class<?> parseClass(String clazz) throws ClassNotFoundException {
      if (clazz.endsWith("[]")) {
         Class<?> baseClass = parseClass(clazz.substring(0, clazz.length() - 2));
         return Array.newInstance(baseClass, 0).getClass();
      } else {
         switch (clazz) {
            case "byte":
               return byte.class;
            case "short":
               return short.class;
            case "int":
               return int.class;
            case "long":
               return long.class;
            case "float":
               return float.class;
            case "double":
               return double.class;
            case "boolean":
               return boolean.class;
            case "char":
               return char.class;
            default:
               return Class.forName(clazz);
         }
      }
   }

   static {
      Skript.registerCondition(CondMethodExists.class, "method[s] %strings% [dont:do(esn't|n't)] exist[s]");
   }
}
