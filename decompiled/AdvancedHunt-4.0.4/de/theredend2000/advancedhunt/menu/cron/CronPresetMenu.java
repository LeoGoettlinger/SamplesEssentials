package de.theredend2000.advancedhunt.menu.cron;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CronPresetMenu extends PagedMenu {
   private final CronExpressionHolder holder;
   private final CronEditPolicy policy;
   private static final List<CronPresetMenu.CronPreset> PRESETS = Collections.unmodifiableList(
      Arrays.asList(
         new CronPresetMenu.CronPreset("hourly", "0 0 * * * ? *", XMaterial.CLOCK.get()),
         new CronPresetMenu.CronPreset("every_6_hours", "0 0 */6 * * ? *", XMaterial.CLOCK.get()),
         new CronPresetMenu.CronPreset("every_12_hours", "0 0 */12 * * ? *", XMaterial.CLOCK.get()),
         new CronPresetMenu.CronPreset("daily_midnight", "0 0 0 * * ? *", XMaterial.SUNFLOWER.get()),
         new CronPresetMenu.CronPreset("daily_noon", "0 0 12 * * ? *", XMaterial.GOLDEN_CARROT.get()),
         new CronPresetMenu.CronPreset("daily_6am", "0 0 6 * * ? *", XMaterial.EMERALD.get()),
         new CronPresetMenu.CronPreset("weekdays_9am", "0 0 9 ? * MON-FRI *", XMaterial.DIAMOND.get()),
         new CronPresetMenu.CronPreset("monday_midnight", "0 0 0 ? * MON *", XMaterial.PAPER.get()),
         new CronPresetMenu.CronPreset("friday_6pm", "0 0 18 ? * FRI *", XMaterial.CAKE.get()),
         new CronPresetMenu.CronPreset("sunday_midnight", "0 0 0 ? * SUN *", XMaterial.BELL.get()),
         new CronPresetMenu.CronPreset("first_day_month", "0 0 0 1 * ? *", XMaterial.WRITABLE_BOOK.get()),
         new CronPresetMenu.CronPreset("last_day_month", "0 0 0 L * ? *", XMaterial.BOOK.get()),
         new CronPresetMenu.CronPreset("first_monday_month", "0 0 0 ? * MON#1 *", XMaterial.ENCHANTED_BOOK.get()),
         new CronPresetMenu.CronPreset("new_year", "0 0 0 1 1 ? *", XMaterial.EXPERIENCE_BOTTLE.get()),
         new CronPresetMenu.CronPreset("christmas", "0 0 0 25 12 ? *", XMaterial.PLAYER_HEAD.get()),
         new CronPresetMenu.CronPreset("quarterly", "0 0 0 1 1,4,7,10 ? *", XMaterial.COMPASS.get())
      )
   );

   public CronPresetMenu(Player playerMenuUtility, Main plugin, Collection collection) {
      super(playerMenuUtility, plugin);
      this.holder = new CollectionProgressResetCronHolder(plugin, collection);
      this.policy = CronEditPolicy.progressReset();
   }

   public CronPresetMenu(Player playerMenuUtility, Main plugin, Collection collection, ActRule actRule) {
      super(playerMenuUtility, plugin);
      this.holder = new ActRuleCronHolder(plugin, collection, actRule);
      this.policy = CronEditPolicy.actSchedule();
   }

   public CronPresetMenu(Player playerMenuUtility, Main plugin, CronExpressionHolder holder, CronEditPolicy policy) {
      super(playerMenuUtility, plugin);
      this.holder = holder;
      this.policy = policy;
   }

   @Override
   public String getMenuName() {
      return this.plugin
         .getMessageManager()
         .getMessage("gui.cron.presets.title", false, "%cron_type%", this.plugin.getMessageManager().getMessage(this.policy.cronTypeMessageKey(), false));
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
      this.addMenuBorder();
      this.addPagedButtons(PRESETS.size());
      int startIndex = this.page * this.maxItemsPerPage;
      int endIndex = Math.min(startIndex + this.maxItemsPerPage, PRESETS.size());

      for (int i = startIndex; i < endIndex; i++) {
         CronPresetMenu.CronPreset preset = PRESETS.get(i);
         int relativeIndex = i - startIndex;
         this.addPagedItem(
            relativeIndex,
            new ItemBuilder(preset.material)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.cron.presets.item.name", "%name%", preset.getName(this.plugin)))
               .setLore(preset.getDescription(this.plugin))
               .build(),
            e -> {
               this.holder.setExpression(preset.expression);
               this.holder
                  .save()
                  .thenRun(
                     () -> Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              this.playerMenuUtility
                                 .sendMessage(
                                    this.plugin.getMessageManager().getMessage("feedback.cron.preset_applied", "%preset%", preset.getName(this.plugin))
                                 );
                              if (this.previousMenu instanceof CronEditorMenu) {
                                 CronEditorMenu cronEditorMenu = (CronEditorMenu)this.previousMenu;
                                 if (cronEditorMenu.getAfterApplyMenu() != null) {
                                    cronEditorMenu.getAfterApplyMenu().open();
                                    return;
                                 }
                              }

                              if (this.previousMenu != null) {
                                 this.previousMenu.open();
                              } else {
                                 new CronEditorMenu(this.playerMenuUtility, this.plugin, this.holder, this.policy).open();
                              }
                           }
                        )
                  );
            }
         );
      }
   }

   private static final class CronPreset {
      final String key;
      final String expression;
      final Material material;

      CronPreset(String key, String expression, Material material) {
         this.key = key;
         this.expression = expression;
         this.material = material;
      }

      String getName(Main plugin) {
         return plugin.getMessageManager().getMessage("gui.cron.presets.list." + this.key + ".name");
      }

      String[] getDescription(Main plugin) {
         return plugin.getMessageManager().getMessageList("gui.cron.presets.list." + this.key + ".lore").toArray(new String[0]);
      }
   }
}
