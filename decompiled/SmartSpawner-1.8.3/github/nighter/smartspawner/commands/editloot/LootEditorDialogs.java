package github.nighter.smartspawner.commands.editloot;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry.Builder;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback.Options;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LootEditorDialogs {
   private static final int CONTENT_WIDTH = 320;
   private static final int HALF_WIDTH = 160;
   private static final int FIELD_MAX_LENGTH = 16;
   private static final int MAX_AMOUNT = 64;
   private static final int MAX_DURABILITY = 100000;
   private final SmartSpawner plugin;
   private final LootEditorService service;
   private final LootEditorUI ui;

   public LootEditorDialogs(SmartSpawner plugin, LootEditorService service, LootEditorUI ui) {
      this.plugin = plugin;
      this.service = service;
      this.ui = ui;
   }

   public void openAddForm(Player player, LootEditorTarget target, String entryKey, ItemStack pending) {
      boolean durability = this.isDamageable(pending);
      this.show(player, () -> this.render(player, new LootEditorDialogs.Form(target, entryKey, pending, null, durability, "1", "100", "")));
   }

   public void openEditForm(Player player, LootEditorTarget target, String entryKey, String lootKey) {
      LootEditorService.LootView loot = this.service.readLoot(target, entryKey, lootKey);
      if (loot == null) {
         this.show(player, () -> this.ui.openLootList(player, target, entryKey));
      } else {
         boolean durability = loot.minDurability() != null || this.isDamageable(loot.preview());
         String durabilityInit = loot.durabilityLabel() == null ? "" : loot.durabilityLabel();
         this.show(
            player,
            () -> this.render(
               player, new LootEditorDialogs.Form(target, entryKey, null, lootKey, durability, loot.amountLabel(), trim(loot.chance()), durabilityInit)
            )
         );
      }
   }

   private void render(Player player, LootEditorDialogs.Form form) {
      this.render(player, form, List.of());
   }

   private void render(Player player, LootEditorDialogs.Form form, List<Component> errors) {
      List<DialogInput> inputs = new ArrayList<>(3);
      inputs.add(DialogInput.text("amount", this.label("editloot.field_amount")).width(320).maxLength(16).initial(form.amount()).build());
      inputs.add(DialogInput.text("chance", this.label("editloot.field_chance")).width(320).maxLength(16).initial(form.chance()).build());
      if (form.showDurability()) {
         inputs.add(DialogInput.text("durability", this.label("editloot.field_durability")).width(320).maxLength(16).initial(form.durability()).build());
      }

      List<DialogBody> body = new ArrayList<>();
      body.add(DialogBody.plainMessage(this.label("editloot.dialog_body"), 320));

      for (Component error : errors) {
         body.add(DialogBody.plainMessage(error, 320));
      }

      ActionButton save = ActionButton.create(this.label("editloot.dialog_save"), null, 160, click((view, audience) -> this.onSave(player, form, view)));
      ActionButton cancel = ActionButton.create(this.label("editloot.dialog_cancel"), null, 160, click((view, audience) -> this.onCancel(player, form)));
      List<ActionButton> buttons = new ArrayList<>(3);
      buttons.add(save);
      buttons.add(cancel);
      if (!form.isAdd()) {
         buttons.add(ActionButton.create(this.label("editloot.dialog_delete"), null, 160, click((view, audience) -> this.onDelete(player, form))));
      }

      player.showDialog(
         Dialog.create(
            builder -> ((Builder)builder.empty())
               .base(DialogBase.builder(this.label("editloot.dialog_title")).body(body).inputs(inputs).build())
               .type(DialogType.multiAction(buttons, null, 2))
         )
      );
   }

   private void onSave(Player player, LootEditorDialogs.Form form, DialogResponseView view) {
      String amountText = text(view, "amount");
      String chanceText = text(view, "chance");
      String durabilityText = form.showDurability() ? text(view, "durability") : "";
      List<Component> errors = new ArrayList<>();
      int[] amount = this.parseAmount(amountText, errors);
      Double chance = this.parseChance(chanceText, errors);
      int[] durability = form.showDurability() ? this.parseDurability(durabilityText, errors) : null;
      LootEditorDialogs.Form filled = form.withValues(amountText, chanceText, durabilityText);
      this.show(player, () -> {
         if (!errors.isEmpty()) {
            this.render(player, filled, errors);
         } else {
            Integer durabilityMin = durability == null ? null : durability[0];
            Integer durabilityMax = durability == null ? null : durability[1];
            if (form.isAdd()) {
               String label = this.service.addLoot(form.target(), form.entryKey(), form.pending(), amount[0], amount[1], chance, durabilityMin, durabilityMax);
               this.returnItem(player, form.pending());
               this.plugin.getMessageService().sendMessage(player, "editloot.loot_added", Map.of("label", label));
            } else {
               if (this.service.readLoot(form.target(), form.entryKey(), form.lootKey()) == null) {
                  this.plugin.getMessageService().sendMessage(player, "editloot.entry_missing");
                  this.ui.openLootList(player, form.target(), form.entryKey());
                  return;
               }

               this.service.setLootAmount(form.target(), form.entryKey(), form.lootKey(), amount[0], amount[1]);
               this.service.setLootChance(form.target(), form.entryKey(), form.lootKey(), chance);
               this.service.setLootDurability(form.target(), form.entryKey(), form.lootKey(), durabilityMin, durabilityMax);
               this.plugin.getMessageService().sendMessage(player, "editloot.loot_saved", Map.of("label", form.lootKey()));
            }

            this.ui.openLootList(player, form.target(), form.entryKey());
         }
      });
   }

   private void onCancel(Player player, LootEditorDialogs.Form form) {
      this.show(player, () -> {
         if (form.isAdd()) {
            this.returnItem(player, form.pending());
         }

         this.ui.openLootList(player, form.target(), form.entryKey());
      });
   }

   private void onDelete(Player player, LootEditorDialogs.Form form) {
      this.show(player, () -> {
         if (this.service.readLoot(form.target(), form.entryKey(), form.lootKey()) == null) {
            this.plugin.getMessageService().sendMessage(player, "editloot.entry_missing");
            this.ui.openLootList(player, form.target(), form.entryKey());
         } else {
            this.service.removeLoot(form.target(), form.entryKey(), form.lootKey());
            this.plugin.getMessageService().sendMessage(player, "editloot.loot_deleted", Map.of("label", form.lootKey()));
            this.ui.openLootList(player, form.target(), form.entryKey());
         }
      });
   }

   private int[] parseAmount(String raw, List<Component> errors) {
      int[] range = readRange(raw);
      if (range != null && range[0] >= 0 && range[1] >= range[0] && range[1] <= 64) {
         return range;
      } else {
         errors.add(this.label("editloot.error_amount", Map.of("max", String.valueOf(64))));
         return null;
      }
   }

   private Double parseChance(String raw, List<Component> errors) {
      try {
         double value = Double.parseDouble(raw.trim());
         if (!(value < 0.0) && !(value > 100.0)) {
            return value;
         } else {
            errors.add(this.label("editloot.error_chance"));
            return null;
         }
      } catch (NumberFormatException var5) {
         errors.add(this.label("editloot.error_chance"));
         return null;
      }
   }

   private int[] parseDurability(String raw, List<Component> errors) {
      if (raw != null && !raw.isBlank()) {
         int[] range = readRange(raw);
         if (range != null && range[0] >= 0 && range[1] >= range[0] && range[1] <= 100000) {
            return range;
         } else {
            errors.add(this.label("editloot.error_durability"));
            return null;
         }
      } else {
         return null;
      }
   }

   private static int[] readRange(String raw) {
      String value = raw == null ? "" : raw.trim();
      if (value.isEmpty()) {
         return null;
      } else {
         try {
            int separator = value.indexOf(45, 1);
            if (separator < 0) {
               int single = Integer.parseInt(value);
               return new int[]{single, single};
            } else {
               return new int[]{Integer.parseInt(value.substring(0, separator).trim()), Integer.parseInt(value.substring(separator + 1).trim())};
            }
         } catch (NumberFormatException var4) {
            return null;
         }
      }
   }

   private boolean isDamageable(ItemStack item) {
      return item != null && item.getType().getMaxDurability() > 0;
   }

   private void returnItem(Player player, ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         for (ItemStack leftover : player.getInventory().addItem(new ItemStack[]{item}).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
         }
      }
   }

   private static String text(DialogResponseView view, String key) {
      String value = view.getText(key);
      return value == null ? "" : value;
   }

   private static String trim(double value) {
      return value == Math.floor(value) ? String.valueOf((long)value) : String.valueOf(value);
   }

   private static DialogAction click(BiConsumer<DialogResponseView, Audience> body) {
      return DialogAction.customClick(body::accept, (Options)Options.builder().build());
   }

   private void show(Player player, Runnable action) {
      Scheduler.runEntityTask(player, () -> {
         if (player.isOnline()) {
            action.run();
         }
      });
   }

   private Component label(String key) {
      return this.label(key, Map.of());
   }

   private Component label(String key, Map<String, String> placeholders) {
      return LegacyComponentSerializer.legacySection().deserialize(this.plugin.getLanguageManager().commandGui().name(key + ".name", placeholders));
   }

   private record Form(
      LootEditorTarget target, String entryKey, ItemStack pending, String lootKey, boolean showDurability, String amount, String chance, String durability
   ) {
      LootEditorDialogs.Form withValues(String amount, String chance, String durability) {
         return new LootEditorDialogs.Form(this.target, this.entryKey, this.pending, this.lootKey, this.showDurability, amount, chance, durability);
      }

      boolean isAdd() {
         return this.pending != null;
      }
   }
}
