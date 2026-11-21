package de.theredend2000.advancedhunt.menu.act;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.cron.CronEditorMenu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ActSetupMenu extends Menu {
   private final Collection collection;
   private final ActRule rule;

   public ActSetupMenu(Player player, Main plugin, Collection collection, ActRule rule) {
      super(player, plugin);
      this.collection = collection;
      this.rule = rule;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.act.setup.title", false);
   }

   @Override
   public int getSlots() {
      return 27;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(super.FILLER_GLASS);
      ActRulesMenu rulesMenu = this.previousMenu instanceof ActRulesMenu ? (ActRulesMenu)this.previousMenu : null;
      ActRuleEditorMenu editorMenu;
      if (this.previousMenu instanceof ActRuleEditorMenu) {
         editorMenu = (ActRuleEditorMenu)this.previousMenu;
      } else {
         editorMenu = new ActRuleEditorMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule);
         if (rulesMenu != null) {
            editorMenu.setPreviousMenu(rulesMenu);
         } else if (this.previousMenu != null) {
            editorMenu.setPreviousMenu(this.previousMenu);
         }
      }

      this.addButton(
         11,
         new ItemBuilder(XMaterial.MAP)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.setup.date_window.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.setup.date_window.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            this.rule.setDuration("*");
            this.rule.setCronExpression("NONE");
            this.plugin
               .getCollectionManager()
               .saveCollection(this.collection)
               .thenRun(
                  () -> Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> new ActDateRangeMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule, editorMenu).setPreviousMenu(this).open()
                     )
               );
         }
      );
      this.addButton(
         13,
         new ItemBuilder(XMaterial.CLOCK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.setup.repeating_schedule.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.setup.repeating_schedule.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            this.rule.setDateRange("*");
            CronEditorMenu cronMenu = new CronEditorMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule).setAfterApplyMenu(editorMenu);
            ActDurationMenu durationMenu = new ActDurationMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule, this, cronMenu);
            cronMenu.setPreviousMenu(durationMenu);
            durationMenu.open();
         }
      );
      this.addButton(
         15,
         new ItemBuilder(XMaterial.COMPARATOR)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.act.setup.advanced.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.act.setup.advanced.lore", false).toArray(new String[0]))
            .build(),
         e -> editorMenu.open()
      );
   }
}
