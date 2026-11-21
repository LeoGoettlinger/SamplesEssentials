package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Total Experience")
@Description({"The total experience, in points, of players or experience orbs.", "Adding to a player's experience will trigger Mending, but setting their experience will not."})
@Example.Examples({@Example("set total experience of player to 100"), @Example("add 100 to player's experience"), @Example("if player's total experience is greater than 100:\n\tset player's total experience to 0\n\tgive player 1 diamond\n")})
@Since({"2.7"})
public class ExprTotalExperience extends SimplePropertyExpression<Entity, Integer> {
   @Nullable
   public Integer convert(Entity entity) {
      if (entity instanceof ExperienceOrb) {
         return ((ExperienceOrb)entity).getExperience();
      } else {
         return entity instanceof Player ? PlayerUtils.getTotalXP(((Player)entity).getLevel(), ((Player)entity).getExp()) : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case ADD:
         case REMOVE:
         case SET:
         case DELETE:
         case RESET:
            return new Class[]{Number.class};
         case REMOVE_ALL:
         default:
            return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int change = delta == null ? 0 : ((Number)delta[0]).intValue();
      switch (mode) {
         case REMOVE:
            change = -change;
         case ADD:
            for (Entity entity : this.getExpr().getArray(event)) {
               if (entity instanceof ExperienceOrb) {
                  int xp = ((ExperienceOrb)entity).getExperience() + change;
                  ((ExperienceOrb)entity).setExperience(Math.max(xp, 0));
               } else if (entity instanceof Player) {
                  if (change < 0) {
                     int xp = PlayerUtils.getTotalXP((Player)entity) + change;
                     PlayerUtils.setTotalXP((Player)entity, Math.max(xp, 0));
                  } else {
                     ((Player)entity).giveExp(change);
                  }
               }
            }
            break;
         case SET:
         case DELETE:
         case RESET:
            if (change < 0) {
               change = 0;
            }

            for (Entity entityx : this.getExpr().getArray(event)) {
               if (entityx instanceof ExperienceOrb) {
                  ((ExperienceOrb)entityx).setExperience(change);
               } else if (entityx instanceof Player) {
                  PlayerUtils.setTotalXP((Player)entityx, change);
               }
            }
      }
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return "total experience";
   }

   static {
      register(ExprTotalExperience.class, Integer.class, "[total] experience", "entities");
   }
}
