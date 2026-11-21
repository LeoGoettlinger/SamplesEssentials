package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Map;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class ExprKeyValueSet extends SimpleExpression<Object> implements KeyProviderExpression<Object> {
   private static final Map<String, String> testSet;
   private Variable<?> variable;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0) {
         Expression<?> expression = expressions[0];
         if (!(expression instanceof Variable<?> variable) || !variable.isList()) {
            Skript.error("The expression '" + expression + "' is not a list variable.");
            return false;
         }

         this.variable = variable;
      }

      return true;
   }

   @NotNull
   @Override
   public String[] getArrayKeys(Event event) throws IllegalStateException {
      return this.variable == null ? testSet.keySet().toArray(new String[0]) : this.variable.getArrayKeys(event);
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      return this.variable == null ? testSet.values().toArray() : this.variable.getArray(event);
   }

   @Override
   public Class<?> getReturnType() {
      return this.variable == null ? String.class : this.variable.getReturnType();
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.variable == null ? super.possibleReturnTypes() : this.variable.possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return this.variable == null ? super.canReturn(returnType) : this.variable.canReturn(returnType);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.variable != null ? "test key values of " + this.variable.toString(event, debug) : "test key values";
   }

   static {
      if (TestMode.ENABLED) {
         Skript.registerExpression(ExprKeyValueSet.class, Object.class, ExpressionType.SIMPLE, "test key values of %~objects%", "test key values");
      }

      testSet = Map.of("hello", "there", "foo", "bar", "a", "b");
   }
}
