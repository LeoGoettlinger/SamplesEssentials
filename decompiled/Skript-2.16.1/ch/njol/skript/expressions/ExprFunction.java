package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Namespace;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Function")
@Description({"Obtain a function by name, which can be executed."})
@Example.Examples({@Example("set {_function} to the function named \"myFunction\""), @Example("run {_function} with arguments 13 and true")})
@Since({"2.10"})
public class ExprFunction extends SimpleExpression<DynamicFunctionReference> implements ReflectionExperimentSyntax {
   private Expression<String> name;
   private Expression<Script> script;
   private int mode;
   private boolean local;
   private Script here;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      this.mode = matchedPattern;
      this.local = this.mode == 2 || expressions[1] != null;
      switch (this.mode) {
         case 0:
         case 1:
            this.name = (Expression<String>)expressions[0];
            if (this.local) {
               this.script = (Expression<Script>)expressions[1];
            }
            break;
         case 2:
            this.script = (Expression<Script>)expressions[0];
      }

      this.here = this.getParser().getCurrentScript();
      return true;
   }

   protected DynamicFunctionReference<?>[] get(Event event) {
      Script script;
      if (this.local) {
         script = this.script.getSingle(event);
      } else {
         script = this.here;
      }
      return switch (this.mode) {
         case 0 -> {
            String name = this.name.getSingle(event);
            if (name == null) {
               yield CollectionUtils.array();
            } else {
               DynamicFunctionReference reference = DynamicFunctionReference.resolveFunction(name, script);
               yield reference == null ? CollectionUtils.array() : CollectionUtils.array(reference);
            }
         }
         case 1 -> (DynamicFunctionReference[])this.name
            .stream(event)
            .map(string -> DynamicFunctionReference.resolveFunction(string, script))
            .filter(Objects::nonNull)
            .toArray(DynamicFunctionReference[]::new);
         case 2 -> {
            if (script == null) {
               yield CollectionUtils.array();
            } else {
               Namespace namespace = Functions.getScriptNamespace(script.getConfig().getFileName());
               yield namespace == null
                  ? CollectionUtils.array()
                  : namespace.getFunctions().stream().map(DynamicFunctionReference::new).toArray(DynamicFunctionReference[]::new);
            }
         }
         default -> throw new IllegalStateException("Unexpected value: " + this.mode);
      };
   }

   @Override
   public boolean isSingle() {
      return this.mode != 2 && this.name.isSingle();
   }

   @Override
   public Class<? extends DynamicFunctionReference> getReturnType() {
      return DynamicFunctionReference.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.mode) {
         case 0 -> "the function named " + this.name.toString(event, debug) + (this.local ? " from " + this.script.toString(event, debug) : "");
         case 1 -> "functions named " + this.name.toString(event, debug) + (this.local ? " from " + this.script.toString(event, debug) : "");
         case 2 -> "the functions from " + this.script.toString(event, debug);
         default -> throw new IllegalStateException("Unexpected value: " + this.mode);
      };
   }

   static {
      Skript.registerExpression(
         ExprFunction.class,
         DynamicFunctionReference.class,
         ExpressionType.COMBINED,
         "[the|a] function [named] %string% [(in|from) %-script%]",
         "[the] functions [named] %strings% [(in|from) %-script%]",
         "[all [[of] the]|the] functions (in|from) %script%"
      );
   }
}
