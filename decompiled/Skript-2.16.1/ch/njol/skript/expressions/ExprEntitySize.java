package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Size")
@Description({"Changes the entity size of slimes and phantoms. This is not the same as changing the scale attribute of an entity.", "When changing the size of a slime, its health is fully resorted and will have changes done to its max health, movement speed and attack damage.", "The default minecraft size of a slime is anywhere between 0 and 2, with a maximum of 126.", "The default minecraft size of a phantom is 0 with a maximum size of 64."})
@Example("spawn a slime at player:\n\tset entity size of event-entity to 5\n\tset name of event-entity to \"King Slime Jorg\"\n")
@Since({"2.11"})
public class ExprEntitySize extends SimplePropertyExpression<LivingEntity, Integer> {
   private static final int MAXIMUM_SLIME_SIZE = 127;
   private static final int MAXIMUM_PHANTOM_SIZE = 64;

   @Nullable
   public Integer convert(LivingEntity from) {
      if (from instanceof Phantom phantom) {
         return phantom.getSize();
      } else {
         return from instanceof Slime slime ? slime.getSize() - 1 : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET -> (Class[])CollectionUtils.array(Number.class);
         case RESET -> (Class[])CollectionUtils.array();
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null || mode == Changer.ChangeMode.RESET) {
         double deltaSizeDouble = delta != null ? ((Number)delta[0]).doubleValue() : 0.0;
         if (!Double.isNaN(deltaSizeDouble) && !Double.isInfinite(deltaSizeDouble)) {
            int sizeDelta = delta != null ? ((Number)delta[0]).intValue() : 0;
            if (mode == Changer.ChangeMode.REMOVE) {
               sizeDelta = -sizeDelta;
            }

            switch (mode) {
               case ADD:
               case REMOVE:
                  for (LivingEntity entityx : this.getExpr().getArray(event)) {
                     if (entityx instanceof Phantom phantom) {
                        int newSize = Math2.fit(0, phantom.getSize() + sizeDelta, 64);
                        phantom.setSize(newSize);
                     } else if (entityx instanceof Slime slime) {
                        int newSize = Math2.fit(1, slime.getSize() + sizeDelta, 127);
                        slime.setSize(newSize);
                     }
                  }
                  break;
               case SET:
               case RESET:
                  for (LivingEntity entity : this.getExpr().getArray(event)) {
                     if (entity instanceof Phantom phantom) {
                        phantom.setSize(Math2.fit(0, sizeDelta, 64));
                     } else if (entity instanceof Slime slime) {
                        slime.setSize(Math2.fit(1, sizeDelta + 1, 127));
                     }
                  }
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
      return "entity size";
   }

   static {
      register(ExprEntitySize.class, Integer.class, "entity size", "livingentities");
   }
}
