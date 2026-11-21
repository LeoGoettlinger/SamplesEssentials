package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.AbstractArrowSlot;
import ch.njol.skript.util.slot.DisplayEntitySlot;
import ch.njol.skript.util.slot.DroppedItemSlot;
import ch.njol.skript.util.slot.ItemFrameSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.ThrowableProjectileSlot;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ThrowableProjectile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item of an Entity")
@Description({"An item associated with an entity. For dropped item entities, it gets the item that was dropped.", "For item frames, the item inside the frame is returned.", "For throwable projectiles (snowballs, enderpearls etc.) or item displays, it gets the displayed item.", "For arrows, it gets the item that will be picked up when retrieving the arrow. Note that setting the item may not change the displayed model, and that setting a spectral arrow to a non-spectral arrow or vice-versa will not affect the effects of the projectile.", "Other entities do not have items associated with them."})
@Example.Examples({@Example("item of event-entity"), @Example("set the item inside of event-entity to a diamond sword named \"Example\"")})
@Since({"2.2-dev35, 2.2-dev36 (improved), 2.5.2 (throwable projectiles), 2.10 (item displays), 2.14.1 (arrows)"})
public class ExprItemOfEntity extends SimplePropertyExpression<Entity, Slot> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprItemOfEntity.class, Slot.class, "item [inside]", "entities", false)
               .supplier(ExprItemOfEntity::new))
            .build()
      );
   }

   @Nullable
   public Slot convert(Entity entity) {
      if (entity instanceof ItemFrame itemFrame) {
         return new ItemFrameSlot(itemFrame);
      } else if (entity instanceof Item item) {
         return new DroppedItemSlot(item);
      } else if (entity instanceof ThrowableProjectile throwableProjectile) {
         return new ThrowableProjectileSlot(throwableProjectile);
      } else if (entity instanceof AbstractArrow arrow) {
         return new AbstractArrowSlot(arrow);
      } else {
         return entity instanceof ItemDisplay itemDisplay ? new DisplayEntitySlot(itemDisplay) : null;
      }
   }

   @Override
   public Class<? extends Slot> getReturnType() {
      return Slot.class;
   }

   @Override
   protected String getPropertyName() {
      return "item inside";
   }
}
