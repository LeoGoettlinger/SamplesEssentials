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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Minecart Derailed / Flying Velocity")
@Description({"The velocity of a minecart as soon as it has been derailed or as soon as it starts flying."})
@Example("on right click on minecart:\n\tset derailed velocity of event-entity to vector 2, 10, 2\n")
@Since({"2.5.1"})
public class ExprMinecartDerailedFlyingVelocity extends SimplePropertyExpression<Entity, Vector> {
   private boolean flying;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.flying = parseResult.mark == 2;
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Vector convert(Entity entity) {
      if (entity instanceof Minecart mc) {
         return this.flying ? mc.getFlyingVelocityMod() : mc.getDerailedVelocityMod();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case ADD:
         case REMOVE:
            return CollectionUtils.array(Vector.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         if (this.flying) {
            switch (mode) {
               case SET:
                  for (Entity entityxx : this.getExpr().getArray(e)) {
                     if (entityxx instanceof Minecart) {
                        ((Minecart)entityxx).setFlyingVelocityMod((Vector)delta[0]);
                     }
                  }
                  break;
               case ADD:
                  for (Entity entityx : this.getExpr().getArray(e)) {
                     if (entityx instanceof Minecart minecart) {
                        minecart.setFlyingVelocityMod(((Vector)delta[0]).add(minecart.getFlyingVelocityMod()));
                     }
                  }
                  break;
               case REMOVE:
                  for (Entity entity : this.getExpr().getArray(e)) {
                     if (entity instanceof Minecart minecart) {
                        minecart.setFlyingVelocityMod(((Vector)delta[0]).subtract(minecart.getFlyingVelocityMod()));
                     }
                  }
                  break;
               default:
                  assert false;
            }
         } else {
            switch (mode) {
               case SET:
                  for (Entity entityxxxxx : this.getExpr().getArray(e)) {
                     if (entityxxxxx instanceof Minecart) {
                        ((Minecart)entityxxxxx).setDerailedVelocityMod((Vector)delta[0]);
                     }
                  }
                  break;
               case ADD:
                  for (Entity entityxxxx : this.getExpr().getArray(e)) {
                     if (entityxxxx instanceof Minecart minecart) {
                        minecart.setDerailedVelocityMod(((Vector)delta[0]).add(minecart.getDerailedVelocityMod()));
                     }
                  }
                  break;
               case REMOVE:
                  for (Entity entityxxx : this.getExpr().getArray(e)) {
                     if (entityxxx instanceof Minecart minecart) {
                        minecart.setDerailedVelocityMod(((Vector)delta[0]).subtract(minecart.getDerailedVelocityMod()));
                     }
                  }
                  break;
               default:
                  assert false;
            }
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return (this.flying ? "flying" : "derailed") + " velocity";
   }

   @Override
   public Class<? extends Vector> getReturnType() {
      return Vector.class;
   }

   static {
      register(ExprMinecartDerailedFlyingVelocity.class, Vector.class, "[minecart] (1¦derailed|2¦flying) velocity", "entities");
   }
}
