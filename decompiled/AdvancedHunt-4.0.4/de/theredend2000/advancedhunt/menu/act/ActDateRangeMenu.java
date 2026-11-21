package de.theredend2000.advancedhunt.menu.act;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ActFormatParser;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ActDateRangeMenu extends Menu {
   private final Collection collection;
   private final ActRule rule;
   private final Menu afterApplyMenu;

   public ActDateRangeMenu(Player player, Main plugin, Collection collection, ActRule rule, Menu previousMenu) {
      super(player, plugin);
      this.collection = collection;
      this.rule = rule;
      this.afterApplyMenu = previousMenu;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.act.components.date_range.editor.title", false);
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
      String currentValue = this.rule.getDateRange();
      String description = ActFormatParser.getHumanReadableDateRange(currentValue);
      boolean isValid = ActFormatParser.isValidDateRange(currentValue);
      this.addButton(
         13,
         new ItemBuilder(XMaterial.PAPER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.date_range.editor.current.name", false))
            .setLore(
               this.plugin.getMessageManager().getMessage("gui.act.components.date_range.editor.current.value", false, "%date_range%", currentValue),
               this.plugin.getMessageManager().getMessage("gui.act.components.date_range.editor.current.description", false, "%description%", description),
               "",
               isValid ? "§a✓ Valid" : "§c✗ Invalid"
            )
            .build(),
         e -> {}
      );
      this.addButton(
         19,
         new ItemBuilder(XMaterial.LIME_DYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.date_range.preset.always.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.date_range.preset.always.lore", false).toArray(new String[0]))
            .build(),
         e -> this.applyDateRange("*")
      );
      LocalDate now = LocalDate.now();
      int currentYear = now.getYear();
      String thisYearRange = currentYear + "-01-01:" + currentYear + "-12-31";
      this.addButton(
         20,
         new ItemBuilder(XMaterial.YELLOW_DYE)
            .setDisplayName(
               this.plugin.getMessageManager().getMessage("gui.act.components.date_range.preset.this_year.name", false, "%year%", String.valueOf(currentYear))
            )
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.act.components.date_range.preset.this_year.lore", false, "%year%", String.valueOf(currentYear))
                  .toArray(new String[0])
            )
            .build(),
         e -> this.applyDateRange(thisYearRange)
      );
      LocalDate monthStart = now.withDayOfMonth(1);
      LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
      String thisMonthRange = monthStart.format(formatter) + ":" + monthEnd.format(formatter);
      this.addButton(
         21,
         new ItemBuilder(XMaterial.ORANGE_DYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.date_range.preset.this_month.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.act.components.date_range.preset.this_month.lore",
                     false,
                     "%start%",
                     monthStart.format(formatter),
                     "%end%",
                     monthEnd.format(formatter)
                  )
                  .toArray(new String[0])
            )
            .build(),
         e -> this.applyDateRange(thisMonthRange)
      );
      LocalDate nextMonthStart = now.plusMonths(1L).withDayOfMonth(1);
      LocalDate nextMonthEnd = nextMonthStart.withDayOfMonth(nextMonthStart.lengthOfMonth());
      String nextMonthRange = nextMonthStart.format(formatter) + ":" + nextMonthEnd.format(formatter);
      this.addButton(
         22,
         new ItemBuilder(XMaterial.PINK_DYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.date_range.preset.next_month.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.act.components.date_range.preset.next_month.lore",
                     false,
                     "%start%",
                     nextMonthStart.format(formatter),
                     "%end%",
                     nextMonthEnd.format(formatter)
                  )
                  .toArray(new String[0])
            )
            .build(),
         e -> this.applyDateRange(nextMonthRange)
      );
      this.addButton(
         31,
         new ItemBuilder(XMaterial.ANVIL)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.components.date_range.manual.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.components.date_range.manual.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            Player player = (Player)e.getWhoClicked();
            player.closeInventory();
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.components.date_range.prompt"));
            this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, input -> {
               if (ActFormatParser.isValidDateRange(input)) {
                  this.applyDateRange(input);
               } else {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.validation.date_range"));
                  Bukkit.getScheduler().runTask(this.plugin, this::open);
               }
            });
         }
      );
   }

   private void applyDateRange(String dateRange) {
      this.rule.setDateRange(dateRange);
      this.plugin.getCollectionManager().saveCollection(this.collection).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.date_range.success"));
         if (this.afterApplyMenu != null) {
            this.afterApplyMenu.open();
         } else {
            new ActRuleEditorMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule).open();
         }
      }));
   }
}
