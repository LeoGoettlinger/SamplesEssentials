package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Executable;

@Name("Result")
@Description({"Runs something (like a function) and returns its result.", "If the thing is expected to return multiple values, use 'results' instead of 'result'."})
@Example.Examples({@Example("set {_function} to the function named \"myFunction\""), @Example("set {_result} to the result of {_function}"), @Example("set {_list::*} to the results of {_function}"), @Example("set {_result} to the result of {_function} with arguments 13 and true")})
@Since({"2.10"})
@Keywords({"run", "result", "execute", "function", "reflection"})
public class ExprResult extends PropertyExpression<Executable<Event, Object>, Object> implements ReflectionExperimentSyntax {
   private Expression<?> arguments;
   private boolean hasArguments;
   private boolean isPlural;
   private DynamicFunctionReference.Input input;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      this.setExpr((Expression<? extends Executable<Event, Object>>)expressions[0]);
      this.hasArguments = result.hasTag("arguments");
      this.isPlural = result.hasTag("plural");
      if (this.hasArguments) {
         this.arguments = LiteralUtils.defendExpression(expressions[1]);
         Expression<?>[] arguments;
         if (this.arguments instanceof ExpressionList<?> list) {
            arguments = list.getExpressions();
         } else {
            arguments = new Expression[]{this.arguments};
         }

         this.input = new DynamicFunctionReference.Input(arguments);
         return LiteralUtils.canInitSafely(this.arguments);
      } else {
         this.input = new DynamicFunctionReference.Input();
         return true;
      }
   }

   protected Object[] get(Event event, Executable<Event, Object>[] source) {
      int var4 = source.length;
      byte var5 = 0;
      if (var5 < var4) {
         Executable<Event, Object> task = source[var5];
         Object[] arguments;
         if (task instanceof DynamicFunctionReference reference) {
            Expression<?> validated = reference.validate(this.input);
            if (validated == null) {
               return new Object[0];
            }

            arguments = validated.getArray(event);
         } else if (this.hasArguments) {
            arguments = this.arguments.getArray(event);
         } else {
            arguments = new Object[0];
         }

         Object execute = task.execute(event, arguments);
         return execute instanceof Object[] results ? results : new Object[]{execute};
      } else {
         return new Object[0];
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return null;
   }

   @Override
   public Class<Object> getReturnType() {
      return Object.class;
   }

   @Override
   public boolean isSingle() {
      return !this.isPlural;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String text = "the result" + (this.isPlural ? "s" : "") + " of " + this.getExpr().toString(event, debug);
      if (this.hasArguments) {
         text = text + " with arguments " + this.arguments.toString(event, debug);
      }

      return text;
   }

   static {
      Skript.registerExpression(
         ExprResult.class,
         Object.class,
         ExpressionType.COMBINED,
         "[the] result[plural:s] of [running|executing] %executable% [arguments:with arg[ument]s %-objects%]"
      );
   }
}
