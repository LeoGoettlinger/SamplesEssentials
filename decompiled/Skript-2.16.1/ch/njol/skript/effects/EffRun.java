package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Executable;

@Name("Run")
@Description({"Executes a task (a function). Any returned result is discarded."})
@Example("set {_function} to the function named \"myFunction\"\nrun {_function}\nrun {_function} with arguments {_things::*}\n")
@Since({"2.10"})
@Keywords({"run", "execute", "reflection", "function"})
public class EffRun extends Effect implements ReflectionExperimentSyntax {
   private Expression<Executable> executable;
   private Expression<?> arguments;
   private DynamicFunctionReference.Input input;
   private boolean hasArguments;

   @Override
   public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      this.executable = (Expression<Executable>)expressions[0];
      this.hasArguments = result.hasTag("arguments");
      if (this.hasArguments) {
         this.arguments = LiteralUtils.defendExpression(expressions[1]);
         Expression<?>[] arguments;
         if (this.arguments instanceof ExpressionList) {
            arguments = ((ExpressionList)this.arguments).getExpressions();
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

   @Override
   protected void execute(Event event) {
      Executable task = this.executable.getSingle(event);
      if (task != null) {
         Object[] arguments;
         if (task instanceof DynamicFunctionReference<?> reference) {
            Expression<?> validated = reference.validate(this.input);
            if (validated == null) {
               return;
            }

            arguments = validated.getArray(event);
         } else if (this.hasArguments) {
            arguments = this.arguments.getArray(event);
         } else {
            arguments = new Object[0];
         }

         task.execute(event, arguments);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.hasArguments
         ? "run " + this.executable.toString(event, debug) + " with arguments " + this.arguments.toString(event, debug)
         : "run " + this.executable.toString(event, debug);
   }

   static {
      Skript.registerEffect(
         EffRun.class, "run %executable% [arguments:with arg[ument]s %-objects%]", "execute %executable% [arguments:with arg[ument]s %-objects%]"
      );
   }
}
