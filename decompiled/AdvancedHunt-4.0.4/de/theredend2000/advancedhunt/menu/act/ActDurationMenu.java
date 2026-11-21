package de.theredend2000.advancedhunt.menu.act;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ActFormatParser;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ActDurationMenu extends Menu {
   private final Collection collection;
   private final ActRule rule;
   private final Menu afterApplyMenu;
   private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhd])");

   public ActDurationMenu(Player player, Main plugin, Collection collection, ActRule rule, Menu previousMenu) {
      super(player, plugin);
      this.collection = collection;
      this.rule = rule;
      this.afterApplyMenu = null;
      this.setPreviousMenu(previousMenu);
   }

   public ActDurationMenu(Player player, Main plugin, Collection collection, ActRule rule, Menu previousMenu, Menu afterApplyMenu) {
      super(player, plugin);
      this.collection = collection;
      this.rule = rule;
      this.afterApplyMenu = afterApplyMenu;
      this.setPreviousMenu(previousMenu);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.act.components.duration.editor.title", false);
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(super.FILLER_GLASS);
      String currentValue = this.rule.getDuration();
      String description = ActFormatParser.getHumanReadableDuration(currentValue);
      boolean isValid = ActFormatParser.isValidDuration(currentValue);
      this.addButton(
         13,
         new ItemBuilder(XMaterial.CLOCK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.editor.current.name", false))
            .setLore(
               this.plugin.getMessageManager().getMessage("gui.act.components.duration.editor.current.value", false, "%duration%", currentValue),
               this.plugin.getMessageManager().getMessage("gui.act.components.duration.editor.current.description", false, "%description%", description),
               "",
               isValid ? "§a✓ Valid" : "§c✗ Invalid"
            )
            .build(),
         e -> {}
      );
      this.addButton(
         19,
         new ItemBuilder(XMaterial.NETHER_STAR)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.permanent.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.permanent.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("*")
      );
      this.addButton(
         20,
         new ItemBuilder(XMaterial.IRON_NUGGET)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.30_minutes.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.30_minutes.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("30m")
      );
      this.addButton(
         21,
         new ItemBuilder(XMaterial.GOLD_NUGGET)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.1_hour.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.1_hour.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("1h")
      );
      this.addButton(
         22,
         new ItemBuilder(XMaterial.GOLD_INGOT)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.2_hours.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.2_hours.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("2h")
      );
      this.addButton(
         23,
         new ItemBuilder(XMaterial.GOLDEN_CARROT)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.4_hours.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.4_hours.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("4h")
      );
      this.addButton(
         24,
         new ItemBuilder(XMaterial.GOLDEN_APPLE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.8_hours.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.8_hours.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("8h")
      );
      this.addButton(
         25,
         new ItemBuilder(XMaterial.ENCHANTED_GOLDEN_APPLE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.24_hours.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.24_hours.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("24h")
      );
      this.addButton(
         28,
         new ItemBuilder(XMaterial.TOTEM_OF_UNDYING)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.preset.7_days.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.preset.7_days.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDuration("7d")
      );
      this.addButton(
         37,
         new ItemBuilder(XMaterial.RED_CONCRETE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.decrease.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.decrease.lore", false).toArray(new String[0]))
            .build(),
         e -> this.modifyNumericDuration(-1)
      );
      this.addButton(
         43,
         new ItemBuilder(XMaterial.LIME_CONCRETE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.increase.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.increase.lore", false).toArray(new String[0]))
            .build(),
         e -> this.modifyNumericDuration(1)
      );
      this.addButton(
         40,
         new ItemBuilder(XMaterial.ANVIL)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.duration.custom.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.duration.custom.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            Player player = (Player)e.getWhoClicked();
            player.closeInventory();
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.components.duration.prompt"));
            this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, input -> {
               if (ActFormatParser.isValidDuration(input)) {
                  this.applyDuration(input);
               } else {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.validation.duration"));
                  Bukkit.getScheduler().runTask(this.plugin, this::open);
               }
            });
         }
      );
   }

   private void modifyNumericDuration(int delta) {
      String current = this.rule.getDuration();
      Matcher matcher = DURATION_PATTERN.matcher(current);
      if (matcher.matches()) {
         try {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            int newValue = Math.max(1, value + delta);
            String newDuration = newValue + unit;
            this.applyDuration(newDuration);
         } catch (NumberFormatException var8) {
         }
      }
   }

   private void applyDuration(String duration) {
      this.rule.setDuration(duration);
      this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.duration.success"));
         if (this.afterApplyMenu != null) {
            this.afterApplyMenu.open();
         } else {
            this.refresh();
         }
      }));
   }
}
