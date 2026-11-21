package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;
import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

public class EvtPlayerArmorChange extends SkriptEvent {
   private static final boolean BODY_SLOT_EXISTS = Skript.fieldExists(EquipmentSlot.class, "BODY");
   private static Converter<PlayerArmorChangeEvent, EquipmentSlot> GET_SLOT;
   @Nullable
   private EquipmentSlot slot = null;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (args.length == 1) {
         Literal<EquipmentSlot> slotLiteral = (Literal<EquipmentSlot>)args[0];
         this.slot = slotLiteral.getSingle();
         if (this.slot == EquipmentSlot.HAND || this.slot == EquipmentSlot.OFF_HAND || BODY_SLOT_EXISTS && this.slot == EquipmentSlot.BODY) {
            Skript.error("You can't detect an armor change event for " + Utils.a(Classes.toString(this.slot)) + ".");
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      PlayerArmorChangeEvent changeEvent = (PlayerArmorChangeEvent)event;
      return this.slot == null ? true : this.slot == GET_SLOT.convert(changeEvent);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.slot == null) {
         builder.append("armor change");
      } else {
         builder.append(switch (this.slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "legging";
            case FEET -> "boots";
            default -> "";
         });
         builder.append("changed");
      }

      return builder.toString();
   }

   static {
      if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent")) {
         Skript.registerEvent(
               "Armor Change", EvtPlayerArmorChange.class, PlayerArmorChangeEvent.class, "[player] armo[u]r change[d]", "[player] %equipmentslot% change[d]"
            )
            .description("Called when armor pieces of a player are changed.")
            .keywords("armor", "armour")
            .examples("on armor change:", "\tbroadcast the old armor item", "on helmet change:")
            .since("2.5, 2.11 (equipment slots)");
         if (Skript.methodExists(PlayerArmorChangeEvent.class, "getSlot")) {
            GET_SLOT = PlayerArmorChangeEvent::getSlot;
         } else {
            Map<SlotType, EquipmentSlot> slotTypeMap = Map.of(
               Enum.valueOf(SlotType.class, "HEAD"),
               EquipmentSlot.HEAD,
               Enum.valueOf(SlotType.class, "CHEST"),
               EquipmentSlot.CHEST,
               Enum.valueOf(SlotType.class, "LEGS"),
               EquipmentSlot.LEGS,
               Enum.valueOf(SlotType.class, "FEET"),
               EquipmentSlot.FEET
            );
            GET_SLOT = event -> slotTypeMap.get(event.getSlotType());
         }

         EventValues.registerEventValue(PlayerArmorChangeEvent.class, EquipmentSlot.class, GET_SLOT);
         EventValues.registerEventValue(PlayerArmorChangeEvent.class, ItemStack.class, PlayerArmorChangeEvent::getOldItem, EventValues.TIME_PAST);
         EventValues.registerEventValue(PlayerArmorChangeEvent.class, ItemStack.class, PlayerArmorChangeEvent::getNewItem, EventValues.TIME_FUTURE);
         EventValues.registerEventValue(
            PlayerArmorChangeEvent.class, Slot.class, event -> new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer().getEquipment(), event.getSlot())
         );
      }
   }
}
