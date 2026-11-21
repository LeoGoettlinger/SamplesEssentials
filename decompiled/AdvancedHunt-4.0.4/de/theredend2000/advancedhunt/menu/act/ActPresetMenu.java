package de.theredend2000.advancedhunt.menu.act;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ActFormatParser;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ActPresetMenu extends PagedMenu {
   private final Collection collection;
   private final ActRule rule;
   private static final List<ActPresetMenu.ActPreset> PRESETS = Collections.unmodifiableList(
      Arrays.asList(
         new ActPresetMenu.ActPreset("always_available", "[*] [*] [NONE]", XMaterial.NETHER_STAR.get()),
         new ActPresetMenu.ActPreset("always_available", "[*] [*] [NONE]", XMaterial.NETHER_STAR.get()),
         new ActPresetMenu.ActPreset("daily_2h", "[*] [2h] [0 0 9 * * ?]", XMaterial.CLOCK.get()),
         new ActPresetMenu.ActPreset("daily_4h", "[*] [4h] [0 0 14 * * ?]", XMaterial.GOLDEN_CARROT.get()),
         new ActPresetMenu.ActPreset("weekend_8h", "[*] [8h] [0 0 10 ? * SAT,SUN]", XMaterial.CAKE.get()),
         new ActPresetMenu.ActPreset("weekly_24h", "[*] [24h] [0 0 0 ? * MON]", XMaterial.PAPER.get()),
         new ActPresetMenu.ActPreset("holiday_special", "[" + getChristmasDateRange() + "] [*] [0 0 0 * * ?]", XMaterial.PLAYER_HEAD.get()),
         new ActPresetMenu.ActPreset("seasonal_summer", "[" + getSummerDateRange() + "] [*] [NONE]", XMaterial.SUNFLOWER.get()),
         new ActPresetMenu.ActPreset("hourly_30m", "[*] [30m] [0 0 * * * ?]", XMaterial.IRON_NUGGET.get()),
         new ActPresetMenu.ActPreset("twice_daily_3h", "[*] [3h] [0 0 9,18 * * ?]", XMaterial.GOLDEN_APPLE.get())
      )
   );

   public ActPresetMenu(Player player, Main plugin, Collection collection, ActRule rule) {
      super(player, plugin);
      this.collection = collection;
      this.rule = rule;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.act.presets.title", false);
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
         ActPresetMenu.ActPreset preset = PRESETS.get(i);
         int relativeIndex = i - startIndex;
         this.addPagedItem(
            relativeIndex,
            new ItemBuilder(preset.material).setDisplayName(preset.getName(this.plugin)).setLore(preset.getDescription(this.plugin)).build(),
            e -> this.applyPreset(preset)
         );
      }
   }

   private void applyPreset(ActPresetMenu.ActPreset preset) {
      Optional<ActFormatParser.ActSchedule> parsed = ActFormatParser.parse(preset.actFormat);
      if (parsed.isPresent()) {
         ActFormatParser.ActSchedule schedule = parsed.get();
         this.rule.setDateRange(schedule.getDateRange());
         this.rule.setDuration(schedule.getDuration());
         this.rule.setCronExpression(schedule.getCron());
         this.plugin
            .getCollectionManager()
            .saveCollection(this.collection)
            .thenRun(
               () -> Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        this.playerMenuUtility
                           .sendMessage(this.plugin.getMessageManager().getMessage("feedback.act.presets.applied", "%preset%", preset.getName(this.plugin)));
                        if (this.previousMenu != null) {
                           this.previousMenu.open();
                        } else {
                           new ActRuleEditorMenu(this.playerMenuUtility, this.plugin, this.collection, this.rule).open();
                        }
                     }
                  )
            );
      } else {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.act.format.invalid_format"));
         if (this.previousMenu != null) {
            this.previousMenu.open();
         }
      }
   }

   private static String getChristmasDateRange() {
      int year = LocalDate.now().getYear();
      return year + "-12-20:" + year + "-12-26";
   }

   private static String getSummerDateRange() {
      int year = LocalDate.now().getYear();
      return year + "-06-01:" + year + "-08-31";
   }

   private static final class ActPreset {
      final String key;
      final String actFormat;
      final Material material;

      ActPreset(String key, String actFormat, Material material) {
         this.key = key;
         this.actFormat = actFormat;
         this.material = material;
      }

      String getName(Main plugin) {
         return plugin.getMessageManager().getMessage("gui.act.presets.list." + this.key + ".name", false);
      }

      String[] getDescription(Main plugin) {
         return plugin.getMessageManager().getMessageList("gui.act.presets.list." + this.key + ".lore", false).toArray(new String[0]);
      }
   }
}
