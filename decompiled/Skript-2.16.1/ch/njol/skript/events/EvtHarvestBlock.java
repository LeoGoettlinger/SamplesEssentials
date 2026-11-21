package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EvtHarvestBlock extends SkriptEvent {
   private Literal<?> types = null;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.types = args[0];
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (event instanceof PlayerHarvestBlockEvent harvestBlockEvent) {
         if (this.types == null) {
            return true;
         } else {
            Block block = harvestBlockEvent.getHarvestedBlock();
            BlockData sourceData = block.getBlockData();
            return SimpleExpression.check(this.types.getAll(), object -> {
               if (object instanceof ItemType itemType) {
                  return itemType.isOfType(block);
               } else {
                  return object instanceof BlockData blockData ? blockData.matches(sourceData) : false;
               }
            }, false, false);
         }
      } else {
         return false;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("player block harvest");
      if (this.types != null) {
         builder.append("of", this.types);
      }

      return builder.toString();
   }

   static {
      Skript.registerEvent(
            "Harvest Block", EvtHarvestBlock.class, PlayerHarvestBlockEvent.class, "[player] [block|crop] harvest[ing] [of %-itemtypes/blockdatas%]"
         )
         .description(
            "Called when a player harvests a block.\nA block being harvested is when a block drops items and the state of the block is changed, but the block is not broken.\nAn example is harvesting berries from a berry bush.\n"
         )
         .examples(
            "on player block harvest:\n\tsend \"You have harvested %event-block% that dropped %event-items% using your %item of event-slot% in your %event-equipment slot%\"\n\non crop harvesting of sweet berry bush:\n\tchance 5%:\n\t\tset drops to a diamond\n\tchance 1%\n\t\tcancel the drops\n"
         )
         .since("2.12");
      EventValues.registerEventValue(PlayerHarvestBlockEvent.class, Block.class, PlayerHarvestBlockEvent::getHarvestedBlock);
      EventValues.registerEventValue(PlayerHarvestBlockEvent.class, ItemStack[].class, event -> event.getItemsHarvested().toArray(ItemStack[]::new));
      EventValues.registerEventValue(PlayerHarvestBlockEvent.class, EquipmentSlot.class, PlayerHarvestBlockEvent::getHand);
      EventValues.registerEventValue(
         PlayerHarvestBlockEvent.class, Slot.class, event -> new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer(), event.getHand())
      );
   }
}
