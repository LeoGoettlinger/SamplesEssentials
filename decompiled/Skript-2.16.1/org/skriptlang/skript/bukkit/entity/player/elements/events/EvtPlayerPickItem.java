package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventConverter;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerPickItem extends SkriptEvent {
   private static final Patterns<EvtPlayerPickItem.PickType> PATTERNS = new Patterns<>(
      new Object[][]{
         {"[player] pick[ing] [of] [an|any] item", EvtPlayerPickItem.PickType.ANY},
         {"[player] pick[ing] [of] [a|any] block", EvtPlayerPickItem.PickType.BLOCK},
         {"[player] pick[ing] [of] [an|any] entity", EvtPlayerPickItem.PickType.ENTITY},
         {"[player] pick[ing] [of] %entitydata/itemtype/blockdata%", null}
      }
   );
   @Nullable
   private EvtPlayerPickItem.PickType pickType;
   @Nullable
   private Literal<?> type;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtPlayerPickItem.class, "Player Pick Item")
                  .supplier(EvtPlayerPickItem::new))
               .addEvents(CollectionUtils.array(PlayerPickBlockEvent.class, PlayerPickEntityEvent.class))
               .addPatterns(PATTERNS.getPatterns()))
            .addDescription(
               "Called when a player picks an item, block or an entity using the pick block key (default middle mouse button).",
               "The past event-slot represents the slot containing the item that will be put into the players hotbar, or nothing, if the item is not in the inventory.",
               "The event-slot represents the slot in the hotbar where the picked item will be placed.",
               "Both event-slots may be set to new slots."
            )
            .addExample("on player picking a diamond block:\n\tcancel event\n\tsend \"You cannot pick diamond blocks!\" to the player\n")
            .addSince("2.15")
            .addRequiredPlugin("1.21.5+")
            .build()
      );
      EventValues.registerEventValue(PlayerPickItemEvent.class, Slot.class, new EventConverter<PlayerPickItemEvent, Slot>() {
         public void set(PlayerPickItemEvent event, @Nullable Slot slot) {
            if (slot instanceof InventorySlot inventorySlot && inventorySlot.getInventory() == event.getPlayer().getInventory()) {
               event.setSourceSlot(inventorySlot.getIndex());
            }
         }

         @Nullable
         public Slot convert(PlayerPickItemEvent event) {
            int source = event.getSourceSlot();
            return source == -1 ? null : new InventorySlot(event.getPlayer().getInventory(), source);
         }
      }, EventValues.TIME_PAST);
      EventValues.registerEventValue(PlayerPickItemEvent.class, Slot.class, new EventConverter<PlayerPickItemEvent, Slot>() {
         public void set(PlayerPickItemEvent event, @Nullable Slot slot) {
            if (slot instanceof InventorySlot inventorySlot && inventorySlot.getInventory() == event.getPlayer().getInventory()) {
               event.setTargetSlot(inventorySlot.getIndex());
            }
         }

         public Slot convert(PlayerPickItemEvent event) {
            return new InventorySlot(event.getPlayer().getInventory(), event.getTargetSlot());
         }
      });
   }

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.pickType = PATTERNS.getInfo(matchedPattern);
      if (this.pickType == null) {
         this.type = args[0];
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      if (this.pickType != null) {
         return switch (this.pickType) {
            case ANY -> true;
            case BLOCK -> event instanceof PlayerPickBlockEvent;
            case ENTITY -> event instanceof PlayerPickEntityEvent;
         };
      } else {
         Block pickedBlock;
         Entity pickedEntity;
         if (event instanceof PlayerPickBlockEvent pickBlockEvent) {
            pickedBlock = pickBlockEvent.getBlock();
            pickedEntity = null;
         } else {
            if (!(event instanceof PlayerPickEntityEvent pickEntityEvent)) {
               assert false;

               return false;
            }

            pickedEntity = pickEntityEvent.getEntity();
            pickedBlock = null;
         }

         assert this.type != null;

         return this.type.check(event, object -> {
            Objects.requireNonNull(object);

            return switch (object) {
               case EntityData<?> entityData when pickedEntity != null -> entityData.isInstance(pickedEntity);
               case ItemType itemType when pickedEntity != null -> {
                  Relation comparison = DefaultComparators.entityItemComparator.compare(EntityData.fromEntity(pickedEntity), itemType);
                  yield Relation.EQUAL.isImpliedBy(comparison);
               }
               case ItemType itemTypex -> itemTypex.isOfType(pickedBlock);
               case BlockData blockData when pickedBlock != null -> pickedBlock.getBlockData().matches(blockData);
               default -> false;
            };
         });
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("player picking");
      if (this.pickType != null) {
         switch (this.pickType) {
            case ANY:
               builder.append("an item");
               break;
            case BLOCK:
               builder.append("a block");
               break;
            case ENTITY:
               builder.append("an entity");
         }
      } else if (this.type != null) {
         builder.append(this.type);
      }

      return builder.toString();
   }

   private static enum PickType {
      ANY,
      BLOCK,
      ENTITY;
   }
}
