package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Experience Pickup Cooldown")
@Description({"The experience cooldown of a player.", "Experience cooldown is how long until a player can pick up another orb of experience.", "The cooldown of a player must be 0 to pick up another orb of experience."})
@Example.Examples({@Example("send experience cooldown of player"), @Example("set the xp pickup cooldown of player to 1 hour"), @Example("if exp collection cooldown of player >= 10 minutes:\n\tclear the experience pickup cooldown of player\n")})
@Since({"2.10"})
public class ExprExperienceCooldown extends SimplePropertyExpression<Player, Timespan> {
   private static final int maxTicks = Integer.MAX_VALUE;

   public Timespan convert(Player player) {
      return new Timespan(Timespan.TimePeriod.TICK, player.getExpCooldown());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, SET, RESET, DELETE -> (Class[])CollectionUtils.array(Timespan.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int providedTime = 0;
      if (delta[0] != null) {
         providedTime = (int)((Timespan)delta[0]).get(Timespan.TimePeriod.TICK);
      }

      switch (mode) {
         case ADD:
            for (Player player : this.getExpr().getArray(event)) {
               player.setExpCooldown(Math2.fit(-1, player.getExpCooldown() + providedTime, Integer.MAX_VALUE));
            }
            break;
         case SET:
            for (Player player : this.getExpr().getArray(event)) {
               player.setExpCooldown(Math2.fit(-1, providedTime, Integer.MAX_VALUE));
            }
            break;
         case RESET:
         case DELETE:
            for (Player player : this.getExpr().getArray(event)) {
               player.setExpCooldown(0);
            }
            break;
         case REMOVE:
            for (Player player : this.getExpr().getArray(event)) {
               player.setExpCooldown(Math2.fit(-1, player.getExpCooldown() - providedTime, Integer.MAX_VALUE));
            }
      }
   }

   @Override
   protected String getPropertyName() {
      return "experience cooldown";
   }

   @Override
   public Class<? extends Timespan> getReturnType() {
      return Timespan.class;
   }

   static {
      register(ExprExperienceCooldown.class, Timespan.class, "(experience|[e]xp) [pickup|collection] cooldown", "players");
   }
}
