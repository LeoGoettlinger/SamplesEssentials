package de.theredend2000.advancedhunt.menu.act;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.cron.CronEditorMenu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ActFormatParser;
import de.theredend2000.advancedhunt.util.CronUtils;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ActRuleEditorMenu extends Menu {
   private final Collection collection;
   private final ActRule rule;

   public ActRuleEditorMenu(Player player, Main plugin, Collection collection, ActRule rule) {
      super(player, plugin);
      this.collection = collection;
      this.rule = rule;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.act.editor.title", false, "%name%", this.rule.getName());
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
      String dateRangeReadable = ActFormatParser.getHumanReadableDateRange(this.rule.getDateRange());
      String durationReadable = ActFormatParser.getHumanReadableDuration(this.rule.getDuration());
      String lang = this.plugin.getConfig().getString("language", "en");
      String cronReadable = ActFormatParser.getHumanReadableCron(
         this.rule.getCronExpression(),
         CronUtils.toLocale(lang),
         this.plugin.getMessageManager().getMessage("gui.act.component.cron.description.none", false),
         this.plugin.getMessageManager().getMessage("gui.act.component.cron.description.invalid", false)
      );
      boolean isValid = ActFormatParser.isValidDateRange(this.rule.getDateRange())
         && ActFormatParser.isValidDuration(this.rule.getDuration())
         && ActFormatParser.isValidCron(this.rule.getCronExpression());
      List<String> previewLore = new ArrayList<>();
      previewLore.add(this.plugin.getMessageManager().getMessage("gui.act.editor.current.date_range", false, "%date_range%", this.rule.getDateRange()));
      previewLore.add(this.plugin.getMessageManager().getMessage("gui.act.editor.current.duration", false, "%duration%", this.rule.getDuration()));
      previewLore.add(this.plugin.getMessageManager().getMessage("gui.act.editor.current.cron", false, "%cron%", this.rule.getCronExpression()));
      previewLore.add("");
      if (!this.rule.getCronExpression().equalsIgnoreCase("NONE") && ActFormatParser.isValidCron(this.rule.getCronExpression())) {
         try {
            List<String> nextRuns = CronUtils.getNextExecutions(this.rule.getCronExpression(), 3);
            if (!nextRuns.isEmpty()) {
               previewLore.add(this.plugin.getMessageManager().getMessage("gui.act.editor.current.next_activation", false));

               for (int i = 0; i < nextRuns.size(); i++) {
                  previewLore.add("§7" + (i + 1) + ". §f" + nextRuns.get(i));
               }

               previewLore.add("");
            }
         } catch (Exception var9) {
         }
      }

      previewLore.add(
         this.plugin
            .getMessageManager()
            .getMessage(
               "gui.act.editor.current.validation",
               false,
               "%status%",
               isValid
                  ? this.plugin.getMessageManager().getMessage("gui.act.editor.current.valid", false)
                  : this.plugin.getMessageManager().getMessage("gui.act.editor.current.invalid", false)
            )
      );
      this.addButton(
         4,
         new ItemBuilder(XMaterial.PAPER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.editor.current.name", false))
            .setLore(previewLore.toArray(new String[0]))
            .build(),
         e -> {}
      );
      this.addButton(
         10,
         new ItemBuilder(XMaterial.NAME_TAG)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.rules.editor.name.name", false))
            .setLore(
               this.plugin.getMessageManager().getMessageList("gui.act.rules.editor.name.lore", false, "%name%", this.rule.getName()).toArray(new String[0])
            )
            .build(),
         e -> this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, input -> {
            this.rule.setName(input);
            this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, this::open));
         })
      );
      this.addButton(
         20,
         new ItemBuilder(XMaterial.CHEST)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.preset.apply.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.preset.apply.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            ActPresetMenu presetMenu = new ActPresetMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule);
            presetMenu.setPreviousMenu(this);
            presetMenu.open();
         }
      );
      this.addButton(
         22,
         new ItemBuilder(XMaterial.BOOK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.editor.guided_setup.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.editor.guided_setup.lore", false).toArray(new String[0]))
            .build(),
         e -> new ActSetupMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule).setPreviousMenu(this).open()
      );
      this.addButton(
         24,
         new ItemBuilder(XMaterial.WRITABLE_BOOK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.editor.manual_input.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.editor.manual_input.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            Player player = (Player)e.getWhoClicked();
            player.closeInventory();
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.components.act_format.prompt"));
            this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, input -> {
               ActFormatParser.ActComponents components = ActFormatParser.parseToComponents(input);
               if (components == null) {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.act.format.invalid_format"));
                  Bukkit.getScheduler().runTask(this.plugin, this::open);
               } else if (!ActFormatParser.isValidDateRange(components.getDateRange())) {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.validation.date_range"));
                  Bukkit.getScheduler().runTask(this.plugin, this::open);
               } else if (!ActFormatParser.isValidDuration(components.getDuration())) {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.validation.duration"));
                  Bukkit.getScheduler().runTask(this.plugin, this::open);
               } else if (!ActFormatParser.isValidCron(components.getCron())) {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.cron.invalid"));
                  Bukkit.getScheduler().runTask(this.plugin, this::open);
               } else {
                  this.rule.setDateRange(components.getDateRange());
                  this.rule.setDuration(components.getDuration());
                  this.rule.setCronExpression(components.getCron());
                  this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.applied"));
                     this.open();
                  }));
               }
            });
         }
      );
      this.addButton(
         30,
         new ItemBuilder(XMaterial.MAP)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.component.date_range.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.act.component.date_range.lore", false, "%value%", this.rule.getDateRange(), "%description%", dateRangeReadable)
                  .toArray(new String[0])
            )
            .build(),
         e -> new ActDateRangeMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule, this).setPreviousMenu(this).open()
      );
      this.addButton(
         31,
         new ItemBuilder(Material.CLOCK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.component.duration.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.act.component.duration.lore", false, "%value%", this.rule.getDuration(), "%description%", durationReadable)
                  .toArray(new String[0])
            )
            .build(),
         e -> new ActDurationMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule, this).setPreviousMenu(this).open()
      );
      this.addButton(
         32,
         new ItemBuilder(Material.REPEATER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.component.cron.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.act.component.cron.lore", false, "%value%", this.rule.getCronExpression(), "%description%", cronReadable)
                  .toArray(new String[0])
            )
            .build(),
         e -> new CronEditorMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule).setPreviousMenu(this).open()
      );
      String enabledStatus = this.rule.isEnabled()
         ? this.plugin.getMessageManager().getMessage("gui.common.enabled", false)
         : this.plugin.getMessageManager().getMessage("gui.common.disabled", false);
      Material statusMaterial = this.rule.isEnabled() ? XMaterial.LIME_DYE.get() : XMaterial.GRAY_DYE.get();
      this.addButton(
         38,
         new ItemBuilder(statusMaterial)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.rules.editor.enabled.name", false))
            .setLore(
               this.plugin.getMessageManager().getMessageList("gui.act.rules.editor.enabled.lore", false, "%status%", enabledStatus).toArray(new String[0])
            )
            .build(),
         e -> {
            this.rule.setEnabled(!this.rule.isEnabled());
            this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, this::refresh));
         }
      );
      this.addButton(
         42,
         new ItemBuilder(Material.RED_DYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.rules.editor.delete.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.rules.editor.delete.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            this.collection.removeActRule(this.rule.getId());
            this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.rules.rule_deleted", "%name%", this.rule.getName()));
               this.openPreviousMenu();
            }));
         }
      );
   }
}
