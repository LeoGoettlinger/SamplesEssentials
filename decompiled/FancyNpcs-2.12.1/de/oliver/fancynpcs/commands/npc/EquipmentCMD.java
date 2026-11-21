package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.translations.message.SimpleMessage;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.api.utils.NpcEquipmentSlot;
import de.oliver.fancynpcs.commands.exceptions.ReplyingParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum EquipmentCMD {
   INSTANCE;

   private static final List<String> SLOT_SUGGESTIONS = Arrays.stream(NpcEquipmentSlot.values()).map(slot -> slot.name().toLowerCase()).toList();
   private static final List<String> MATERIAL_SUGGESTIONS = StreamSupport.<Material>stream(Registry.MATERIAL.spliterator(), false)
      .filter(Material::isItem)
      .map(material -> material.key().asString())
      .toList();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc equipment <npc> set <slot> <item>")
   @Permission({"fancynpcs.command.npc.equipment.set"})
   public void onEquipmentSet(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final NpcEquipmentSlot slot,
      @Argument(parserName = "EquipmentCMD/item") @NotNull final ItemStack item
   ) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.EQUIPMENT, new Object[]{slot, item}, sender).callEvent()) {
         npc.getData().addEquipment(slot, item);
         npc.updateForAll();
         this.translator
            .translate(item.getType() != Material.AIR ? "npc_equipment_set_item" : "npc_equipment_set_empty")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("slot", this.getTranslatedSlot(slot))
            .addTagResolver(
               Placeholder.component(
                  "item", (ComponentLike)(item.getType() != Material.AIR ? item.displayName().hoverEvent(item.asHoverEvent()) : Component.empty())
               )
            )
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }

   @Command("npc equipment <npc> clear")
   @Permission({"fancynpcs.command.npc.equipment.clear"})
   public void onEquipmentClear(@NotNull final CommandSender sender, @NotNull final Npc npc) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.EQUIPMENT, null, sender).callEvent()) {
         for (NpcEquipmentSlot slot : NpcEquipmentSlot.values()) {
            npc.getData().getEquipment().put(slot, new ItemStack(Material.AIR));
         }

         npc.updateForAll();
         this.translator.translate("npc_equipment_clear_success").withPrefix().replace("npc", npc.getData().getName()).send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }

   @Command("npc equipment <npc> list")
   @Permission({"fancynpcs.command.npc.equipment.list"})
   public void onEquipmentList(@NotNull final CommandSender sender, @NotNull final Npc npc) {
      if (!npc.getData().getEquipment().isEmpty()
         && !npc.getData().getEquipment().values().stream().allMatch(item -> item == null || item.getType() == Material.AIR)) {
         this.translator.translate("npc_equipment_list_header").send(sender);
         npc.getData()
            .getEquipment()
            .forEach(
               (slot, item) -> {
                  if (item != null && item.getType() != Material.AIR) {
                     this.translator
                        .translate("npc_equipment_list_entry")
                        .replace("slot", this.getTranslatedSlot(slot))
                        .addTagResolver(Placeholder.component("item", item.displayName().hoverEvent(item.asHoverEvent())))
                        .send(sender);
                  }
               }
            );
         this.translator.translate("npc_equipment_list_footer").send(sender);
      } else {
         this.translator.translate("npc_equipment_list_failure_empty").withPrefix().send(sender);
      }
   }

   @Parser(
      name = "",
      suggestions = "EquipmentCMD/slot"
   )
   public NpcEquipmentSlot parseSlot(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString().toLowerCase();
      NpcEquipmentSlot slot = NpcEquipmentSlot.parse(value);
      if (slot == null) {
         throw ReplyingParseException.replying(
            () -> this.translator.translate("command_invalid_equipment_slot").withPrefix().replaceStripped("input", value).send(context.sender())
         );
      } else {
         return slot;
      }
   }

   @Parser(
      name = "EquipmentCMD/item",
      suggestions = "EquipmentCMD/item"
   )
   public ItemStack parseItem(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString().toLowerCase();
      if (value.equals("@none")) {
         return new ItemStack(Material.AIR);
      } else if (value.equals("@hand") && context.sender() instanceof Player player) {
         return player.getInventory().getItemInMainHand().clone();
      } else {
         NamespacedKey key = NamespacedKey.fromString(value);
         if (key == null) {
            throw ReplyingParseException.replying(
               () -> this.translator.translate("command_invalid_material").withPrefix().replaceStripped("input", value).send(context.sender())
            );
         } else {
            Material material = (Material)Registry.MATERIAL.get(key);
            if (material != null && material.isItem()) {
               return new ItemStack(material);
            } else {
               throw ReplyingParseException.replying(
                  () -> this.translator.translate("command_invalid_material").withPrefix().replaceStripped("input", value).send(context.sender())
               );
            }
         }
      }
   }

   @Suggestions("EquipmentCMD/item")
   public List<String> suggestItem(final CommandContext<CommandSender> context, final CommandInput input) {
      return new ArrayList<String>(MATERIAL_SUGGESTIONS) {
         {
            Objects.requireNonNull(EquipmentCMD.this);
            this.add("@none");
            if (context.sender() instanceof Player) {
               this.add("@hand");
            }
         }
      };
   }

   @Suggestions("EquipmentCMD/slot")
   public List<String> suggestSlot(final CommandContext<CommandSender> context, final CommandInput input) {
      return SLOT_SUGGESTIONS;
   }

   @NotNull
   private String getTranslatedSlot(@NotNull final NpcEquipmentSlot slot) {
      return ((SimpleMessage)this.translator.translate(switch (slot) {
         case MAINHAND -> "main_hand";
         case OFFHAND -> "off_hand";
         case HEAD -> "head";
         case CHEST -> "chest";
         case LEGS -> "legs";
         case FEET -> "feet";
      })).getMessage();
   }
}
