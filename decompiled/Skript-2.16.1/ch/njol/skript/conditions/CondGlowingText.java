package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;

@Name("Has Glowing Text")
@Description({"Checks whether a sign (either a block or an item) has glowing text"})
@Example("if target block has glowing text")
@Since({"2.8.0"})
public class CondGlowingText extends PropertyCondition<Object> {
   @Override
   public boolean check(Object obj) {
      if (obj instanceof Block block) {
         return block.getState() instanceof Sign sign && sign.isGlowingText();
      } else {
         return obj instanceof ItemType itemType && itemType.getItemMeta() instanceof BlockStateMeta blockStateMeta
            ? blockStateMeta.getBlockState() instanceof Sign sign && sign.isGlowingText()
            : false;
      }
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.HAVE;
   }

   @Override
   protected String getPropertyName() {
      return "glowing text";
   }

   static {
      if (Skript.methodExists(Sign.class, "isGlowingText")) {
         register(CondGlowingText.class, PropertyCondition.PropertyType.HAVE, "glowing text", "blocks/itemtypes");
      }
   }
}
