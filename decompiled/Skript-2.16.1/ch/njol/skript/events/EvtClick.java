package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ClickEventTracker;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Relation;

public class EvtClick extends SkriptEvent {
   private static final boolean USE_OLD_PIAEE_BEHAVIOR = !Skript.isRunningMinecraft(26, 1, 1);
   private static final int RIGHT = 1;
   private static final int LEFT = 2;
   private static final int ANY = 3;
   public static final ClickEventTracker interactTracker = new ClickEventTracker(Skript.getInstance());
   @Nullable
   private Literal<?> type;
   @Nullable
   private Literal<ItemType> tools;
   private int click = 3;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.click = parseResult.mark == 0 ? 3 : parseResult.mark;
      this.type = args[matchedPattern];
      if (this.type != null && !this.type.canReturn(ItemType.class) && !this.type.canReturn(BlockData.class)) {
         Literal<EntityData<?>> entitydata = (Literal<EntityData<?>>)this.type;
         if (this.click == 2) {
            if (Vehicle.class.isAssignableFrom(entitydata.getSingle().getType())) {
               Skript.error("A leftclick on a vehicle entity is an attack and thus not covered by the 'click' event, but the 'vehicle damage' event.");
            } else {
               Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event.");
            }

            return false;
         }

         if (this.click == 3) {
            if (Vehicle.class.isAssignableFrom(entitydata.getSingle().getType())) {
               Skript.error(
                  "A leftclick on a vehicle entity is an attack and thus not covered by the 'click' event, but the 'vehicle damage' event. Change this event to a rightclick to fix this warning message."
               );
            } else {
               Skript.error(
                  "A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event. Change this event to a rightclick to fix this warning message."
               );
            }
         }
      }

      this.tools = (Literal<ItemType>)args[1 - matchedPattern];
      return true;
   }

   @Override
   public boolean check(Event event) {
      Block block;
      Entity entity;
      if (event instanceof PlayerInteractEntityEvent interactEntityEvent) {
         Entity clicked = interactEntityEvent.getRightClicked();
         if (USE_OLD_PIAEE_BEHAVIOR && interactEntityEvent instanceof PlayerInteractAtEntityEvent && !(clicked instanceof ArmorStand)) {
            return false;
         }

         if (this.click == 2) {
            return false;
         }

         if (USE_OLD_PIAEE_BEHAVIOR) {
            if (!(event instanceof PlayerInteractAtEntityEvent)
               && !interactTracker.checkEvent(interactEntityEvent.getPlayer(), interactEntityEvent, interactEntityEvent.getHand())) {
               return false;
            }
         } else if (!interactTracker.checkEvent(interactEntityEvent.getPlayer(), interactEntityEvent, interactEntityEvent.getHand())) {
            return false;
         }

         entity = clicked;
         block = null;
      } else {
         if (!(event instanceof PlayerInteractEvent interactEvent)) {
            assert false;

            return false;
         }

         Action action = interactEvent.getAction();
         int click;
         switch (action) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
               click = 2;
               break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
               click = 1;
               break;
            default:
               return false;
         }

         if ((this.click & click) == 0) {
            return false;
         }

         EquipmentSlot hand = interactEvent.getHand();

         assert hand != null;

         if (!interactTracker.checkEvent(interactEvent.getPlayer(), interactEvent, hand)) {
            return false;
         }

         block = interactEvent.getClickedBlock();
         entity = null;
      }

      Predicate<ItemType> checker = itemType -> {
         if (event instanceof PlayerInteractEvent interactEventx) {
            return itemType.isOfType(interactEventx.getItem());
         } else {
            PlayerInventory invi = ((PlayerInteractEntityEvent)event).getPlayer().getInventory();
            ItemStack item = ((PlayerInteractEntityEvent)event).getHand() == EquipmentSlot.HAND ? invi.getItemInMainHand() : invi.getItemInOffHand();
            return itemType.isOfType(item);
         }
      };
      if (this.tools != null && !this.tools.check(event, checker)) {
         return false;
      } else if (this.type != null) {
         BlockData blockDataCheck = block != null ? block.getBlockData() : null;
         return this.type.check(event, object -> {
            if (entity != null) {
               if (object instanceof EntityData<?> entityData) {
                  return entityData.isInstance(entity);
               }

               if (object instanceof ItemType itemType) {
                  Relation compare = DefaultComparators.entityItemComparator.compare(EntityData.fromEntity(entity), itemType);
                  return Relation.EQUAL.isImpliedBy(compare);
               }
            } else {
               if (object instanceof ItemType itemType) {
                  return itemType.isOfType(block);
               }

               if (object instanceof BlockData blockData) {
                  return blockDataCheck != null && blockDataCheck.matches(blockData);
               }
            }

            return false;
         });
      } else {
         return true;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.click) {
            case 1 -> "right";
            case 2 -> "left";
            default -> "";
         }
         + "click"
         + (this.type != null ? " on " + this.type.toString(event, debug) : "")
         + (this.tools != null ? " holding " + this.tools.toString(event, debug) : "");
   }

   static {
      Class<? extends PlayerEvent>[] eventTypes = CollectionUtils.array(
         PlayerInteractEvent.class, PlayerInteractEntityEvent.class, PlayerInteractAtEntityEvent.class
      );
      Skript.registerEvent(
            "Click",
            EvtClick.class,
            eventTypes,
            "[(1:right|2:left)(| |-)][mouse(| |-)]click[ing] [on %-entitydata/itemtype/blockdata%] [(with|using|holding) %-itemtype%]",
            "[(1:right|2:left)(| |-)][mouse(| |-)]click[ing] (with|using|holding) %itemtype% on %entitydata/itemtype/blockdata%"
         )
         .description(
            "Called when a user clicks on a block, an entity or air with or without an item in their hand.",
            "Please note that rightclick events with an empty hand while not looking at a block are not sent to the server, so there's no way to detect them.",
            "Also note that a leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event."
         )
         .examples(
            "on click:",
            "on rightclick holding a fishing rod:",
            "on leftclick on a stone or obsidian:",
            "on rightclick on a creeper:",
            "on click with a sword:",
            "on click on chest[facing=north]:",
            "on click on campfire[lit=true]:"
         )
         .since("1.0, 2.10 (blockdata)");
   }
}
