package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Inverse Boolean")
@Description({"An expression to obtain the inverse value of a boolean"})
@Example("set {_gravity} to inverse of player's flight mode")
@Since({"2.12"})
public class ExprInverse extends SimpleExpression<Boolean> {
   private Expression<Boolean> booleans;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.booleans = (Expression<Boolean>)exprs[0];
      return true;
   }

   @Nullable
   protected Boolean[] get(Event event) {
      Boolean[] original = this.booleans.getArray(event);
      Boolean[] toggled = new Boolean[original.length];

      for (int i = 0; i < original.length; i++) {
         toggled[i] = !original[i];
      }

      return toggled;
   }

   @Override
   public boolean isSingle() {
      return this.booleans.isSingle();
   }

   @Override
   public Class<? extends Boolean> getReturnType() {
      return Boolean.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "inverse of " + this.booleans.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprInverse.class, Boolean.class, ExpressionType.COMBINED, "[the] (inverse|opposite)[s] of %booleans%");
   }
}
