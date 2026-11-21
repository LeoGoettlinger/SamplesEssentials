package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Arrow Pierce Level")
@Description({"An arrow's pierce level."})
@Example("on shoot:\n\tevent-projectile is an arrow\n\tset arrow pierce level of event-projectile to 5\n")
@RequiredPlugins({"Minecraft 1.14+"})
@Since({"2.5.1"})
public class ExprArrowPierceLevel extends SimplePropertyExpression<Projectile, Long> {
   private static final boolean CAN_USE_PIERCE = Skript.methodExists(Arrow.class, "getPierceLevel");

   @Nullable
   public Long convert(Projectile arrow) {
      return (long)((Arrow)arrow).getPierceLevel();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case RESET:
         case REMOVE:
         case ADD:
            return CollectionUtils.array(Number.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int strength = delta != null ? Math.max(((Number)delta[0]).intValue(), 0) : 0;
      int mod = 1;
      switch (mode) {
         case SET:
         case RESET:
            for (Projectile entity : this.getExpr().getArray(e)) {
               ((Arrow)entity).setPierceLevel(strength);
            }
            break;
         case REMOVE:
            mod = -1;
         case ADD:
            for (Projectile entity : this.getExpr().getArray(e)) {
               if (entity instanceof Arrow arrow) {
                  int dmg = Math.round((float)(arrow.getPierceLevel() + strength * mod));
                  if (dmg < 0) {
                     dmg = 0;
                  }

                  arrow.setPierceLevel(dmg);
               }
            }
            break;
         default:
            assert false;
      }
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "arrow pierce level";
   }

   static {
      if (CAN_USE_PIERCE) {
         register(ExprArrowPierceLevel.class, Long.class, "arrow pierce level", "projectiles");
      }
   }
}
