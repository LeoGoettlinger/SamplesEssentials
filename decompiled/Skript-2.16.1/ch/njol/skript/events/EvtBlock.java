package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Relation;

public class EvtBlock extends SkriptEvent {
   @Nullable
   private Literal<Object> types;
   private boolean mine = false;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
      this.types = (Literal<Object>)args[0];
      this.mine = parser.mark == 1;
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (this.mine
         && event instanceof BlockBreakEvent
         && ((BlockBreakEvent)event).getBlock().getDrops(((BlockBreakEvent)event).getPlayer().getItemInHand()).isEmpty()) {
         return false;
      } else if (this.types == null) {
         return true;
      } else {
         BlockData blockData = null;
         ItemType item;
         if (event instanceof BlockFormEvent blockFormEvent) {
            BlockState newState = blockFormEvent.getNewState();
            item = new ItemType(newState.getBlockData());
            blockData = newState.getBlockData();
         } else if (event instanceof BlockDropItemEvent blockDropItemEvent) {
            Block block = blockDropItemEvent.getBlock();
            item = new ItemType(block);
            blockData = block.getBlockData();
         } else if (event instanceof BlockEvent blockEvent) {
            Block block = blockEvent.getBlock();
            item = new ItemType(block);
            blockData = block.getBlockData();
         } else if (event instanceof PlayerBucketFillEvent playerBucketFillEvent) {
            Block block = playerBucketFillEvent.getBlockClicked();
            item = new ItemType(block);
            blockData = block.getBlockData();
         } else {
            if (!(event instanceof PlayerBucketEmptyEvent playerBucketEmptyEvent)) {
               if (event instanceof HangingEvent hangingEvent) {
                  EntityData<?> d = EntityData.fromEntity(hangingEvent.getEntity());
                  return this.types
                     .check(
                        event, o -> o instanceof ItemType ? Relation.EQUAL.isImpliedBy(DefaultComparators.entityItemComparator.compare(d, (ItemType)o)) : false
                     );
               }

               assert false;

               return false;
            }

            item = new ItemType(playerBucketEmptyEvent.getItemStack());
         }

         ItemType itemF = item;
         BlockData finalBlockData = blockData;
         return this.types.check(event, o -> {
            if (o instanceof ItemType) {
               return ((ItemType)o).isSupertypeOf(itemF);
            } else {
               return o instanceof BlockData && finalBlockData != null ? finalBlockData.matches((BlockData)o) : false;
            }
         });
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "break/place/burn/fade/form/drop of " + Classes.toString(this.types);
   }

   static {
      Skript.registerEvent(
            "Break / Mine",
            EvtBlock.class,
            new Class[]{BlockBreakEvent.class, PlayerBucketFillEvent.class, HangingBreakEvent.class},
            "[block] (break[ing]|1¦min(e|ing)) [[of] %-itemtypes/blockdatas%]"
         )
         .description(
            "Called when a block is broken by a player. If you use 'on mine', only events where the broken block dropped something will call the trigger."
         )
         .examples("on mine:", "on break of stone:", "on break of chest[facing=north]:", "on break of potatoes[age=7]:")
         .since("1.0 (break), unknown (mine), 2.6 (BlockData support)");
      Skript.registerEvent("Burn", EvtBlock.class, BlockBurnEvent.class, "[block] burn[ing] [[of] %-itemtypes/blockdatas%]")
         .description("Called when a block is destroyed by fire.")
         .examples("on burn:", "on burn of oak wood, oak fences, or chests:", "on burn of oak_log[axis=y]:")
         .since("1.0, 2.6 (BlockData support)");
      Skript.registerEvent(
            "Place",
            EvtBlock.class,
            new Class[]{BlockPlaceEvent.class, PlayerBucketEmptyEvent.class, HangingPlaceEvent.class},
            "[block] (plac(e|ing)|build[ing]) [[of] %-itemtypes/blockdatas%]"
         )
         .description("Called when a player places a block.")
         .examples("on place:", "on place of a furnace, crafting table or chest:", "on break of chest[type=right] or chest[type=left]")
         .since("1.0, 2.6 (BlockData support)");
      Skript.registerEvent("Fade", EvtBlock.class, BlockFadeEvent.class, "[block] fad(e|ing) [[of] %-itemtypes/blockdatas%]")
         .description("Called when a block 'fades away', e.g. ice or snow melts.")
         .examples("on fade of snow or blue ice:", "on fade of snow[layers=2]")
         .since("1.0, 2.6 (BlockData support)");
      Skript.registerEvent("Form", EvtBlock.class, BlockFormEvent.class, "[block] form[ing] [[of] %-itemtypes/blockdatas%]")
         .description(
            "Called when a block is created, but not by a player, e.g. snow forms due to snowfall, water freezes in cold biomes. This isn't called when block spreads (mushroom growth, water physics etc.), as it has its own event (see <a href='#spread'>spread event</a>)."
         )
         .examples("on form of snow:")
         .since("1.0, 2.6 (BlockData support)");
      Skript.registerEvent("Block Drop", EvtBlock.class, BlockDropItemEvent.class, "block drop[ping] [[of] %-itemtypes/blockdatas%]")
         .description(
            "Called when a block broken by a player drops something.",
            "<ul>",
            "<li>event-player: The player that broke the block</li>",
            "<li>past event-block: The block that was broken</li>",
            "<li>event-block: The block after being broken</li>",
            "<li>event-items (or drops): The drops of the block</li>",
            "<li>event-entities: The entities of the dropped items</li>",
            "</ul>",
            "",
            "If the breaking of the block leads to others being broken, such as torches, they will appearin \"event-items\" and \"event-entities\"."
         )
         .examples(
            "on block drop:",
            "\tbroadcast event-player",
            "\tbroadcast past event-block",
            "\tbroadcast event-block",
            "\tbroadcast event-items",
            "\tbroadcast event-entities",
            "on block drop of oak log:"
         )
         .since("2.10");
   }
}
