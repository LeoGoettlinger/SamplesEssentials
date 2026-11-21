package org.skriptlang.skript.bukkit.entity.displays.item.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item Display Transform")
@Description({"Returns or changes the <a href='#itemdisplaytransform'>item display transform</a> of <a href='#display'>item displays</a>."})
@Example.Examples({@Example("set the item transform of the last spawned item display to first person left handed"), @Example("set the item transform of the last spawned item display to no transform # Reset to default")})
@Since({"2.10"})
public class ExprItemDisplayTransform extends SimplePropertyExpression<Display, ItemDisplayTransform> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprItemDisplayTransform.class, ItemDisplayTransform.class, "item [display] transform", "displays", true
               )
               .supplier(ExprItemDisplayTransform::new))
            .build()
      );
   }

   @Nullable
   public ItemDisplayTransform convert(Display display) {
      return display instanceof ItemDisplay itemDisplay ? itemDisplay.getItemDisplayTransform() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case RESET -> (Class[])CollectionUtils.array();
         case SET -> (Class[])CollectionUtils.array(ItemDisplayTransform.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ItemDisplayTransform transform = mode == Changer.ChangeMode.SET ? (ItemDisplayTransform)delta[0] : ItemDisplayTransform.NONE;

      for (Display display : this.getExpr().getArray(event)) {
         if (display instanceof ItemDisplay itemDisplay) {
            itemDisplay.setItemDisplayTransform(transform);
         }
      }
   }

   @Override
   public Class<? extends ItemDisplayTransform> getReturnType() {
      return ItemDisplayTransform.class;
   }

   @Override
   protected String getPropertyName() {
      return "item display transform";
   }
}
