package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Speed")
@Description({"A player's walking or flying speed. Both can be changed, but values must be between -1 and 1 (excessive values will be changed to -1 or 1 respectively). Negative values reverse directions.", "Please note that changing a player's speed will change their FOV just like potions do."})
@Example.Examples({@Example("set the player's walk speed to 1"), @Example("increase the argument's fly speed by 0.1")})
@Since({"unknown (before 2.1)"})
public class ExprSpeed extends SimplePropertyExpression<Player, Number> {
   private boolean walk;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      super.init(exprs, matchedPattern, isDelayed, parseResult);
      this.walk = parseResult.mark == 0;
      return true;
   }

   public Number convert(Player p) {
      return this.walk ? p.getWalkSpeed() : p.getFlySpeed();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET && mode != Changer.ChangeMode.ADD && mode != Changer.ChangeMode.REMOVE
         ? null
         : new Class[]{Number.class};
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      float input = delta == null ? 0.0F : ((Number)delta[0]).floatValue();

      for (Player p : this.getExpr().getArray(e)) {
         float oldSpeed = this.walk ? p.getWalkSpeed() : p.getFlySpeed();

         float d = Math2.fit(-1.0F, switch (mode) {
            case SET -> input;
            case ADD -> oldSpeed + input;
            case REMOVE -> oldSpeed - input;
            default -> this.walk ? 0.2F : 0.1F;
         }, 1.0F);
         if (this.walk) {
            p.setWalkSpeed(d);
         } else {
            p.setFlySpeed(d);
         }
      }
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   protected String getPropertyName() {
      return this.walk ? "walk speed" : "fly speed";
   }

   static {
      register(ExprSpeed.class, Number.class, "(0¦walk[ing]|1¦fl(y[ing]|ight))[( |-)]speed", "players");
   }
}
