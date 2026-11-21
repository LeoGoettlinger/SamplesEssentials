package de.theredend2000.advancedhunt.menu.cron;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.CronUtils;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ValidationUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CronFieldMenu extends Menu {
   private final CronExpressionHolder holder;
   private final CronEditPolicy policy;
   private final Map<CronFieldMenu.CronField, String> fieldValues;
   private CronFieldMenu.CronField selectedField;

   public CronFieldMenu(Player playerMenuUtility, Main plugin, Collection collection, String initialExpression) {
      super(playerMenuUtility, plugin);
      this.holder = new CollectionProgressResetCronHolder(plugin, collection);
      this.policy = CronEditPolicy.progressReset();
      this.fieldValues = new LinkedHashMap<>();
      this.selectedField = CronFieldMenu.CronField.SECOND;
      this.parseExpression(initialExpression);
   }

   public CronFieldMenu(Player playerMenuUtility, Main plugin, Collection collection, ActRule actRule, String initialExpression) {
      super(playerMenuUtility, plugin);
      this.holder = new ActRuleCronHolder(plugin, collection, actRule);
      this.policy = CronEditPolicy.actSchedule();
      this.fieldValues = new LinkedHashMap<>();
      this.selectedField = CronFieldMenu.CronField.SECOND;
      this.parseExpression(initialExpression);
   }

   public CronFieldMenu(Player playerMenuUtility, Main plugin, CronExpressionHolder holder, CronEditPolicy policy, String initialExpression) {
      super(playerMenuUtility, plugin);
      this.holder = holder;
      this.policy = policy;
      this.fieldValues = new LinkedHashMap<>();
      this.selectedField = CronFieldMenu.CronField.SECOND;
      this.parseExpression(initialExpression);
   }

   @Override
   public String getMenuName() {
      return this.plugin
         .getMessageManager()
         .getMessage("gui.cron.builder.title", false, "%cron_type%", this.plugin.getMessageManager().getMessage(this.policy.cronTypeMessageKey(), false));
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
      List<String> previewLore = new ArrayList<>();
      previewLore.add(this.plugin.getMessageManager().getMessage("gui.cron.builder.preview.current_expression"));
      previewLore.add("§f" + this.buildExpression());
      previewLore.add("");
      List<String> nextRuns = CronUtils.getNextExecutions(this.buildExpression(), 2);
      if (!nextRuns.isEmpty()) {
         previewLore.add(this.plugin.getMessageManager().getMessage("gui.cron.builder.preview.next_runs"));

         for (int i = 0; i < nextRuns.size(); i++) {
            previewLore.add("§7" + (i + 1) + ". §f" + nextRuns.get(i));
         }
      } else {
         previewLore.add(this.plugin.getMessageManager().getMessage("gui.cron.common.invalid_expression"));
      }

      this.addStaticItem(
         4,
         new ItemBuilder(XMaterial.ENDER_EYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.cron.builder.preview.name"))
            .setLore(previewLore.toArray(new String[0]))
            .build()
      );
      int slot = 19;

      for (CronFieldMenu.CronField field : CronFieldMenu.CronField.values()) {
         boolean isSelected = this.selectedField == field;
         String value = this.fieldValues.getOrDefault(field, field.defaultValue);
         this.addButton(
            slot++,
            new ItemBuilder(isSelected ? XMaterial.LIME_DYE.get() : XMaterial.GRAY_DYE.get())
               .setDisplayName(
                  this.plugin
                     .getMessageManager()
                     .getMessage(
                        isSelected ? "gui.cron.builder.field.selected_name" : "gui.cron.builder.field.unselected_name",
                        "%name%",
                        field.getDisplayName(this.plugin)
                     )
               )
               .setLore(
                  this.plugin
                     .getMessageManager()
                     .getMessageList("gui.cron.builder.field.lore", "%value%", value, "%description%", field.getDescription(this.plugin))
                     .toArray(new String[0])
               )
               .build(),
            e -> {
               this.selectedField = field;
               this.refresh();
            }
         );
      }

      String currentValue = this.fieldValues.getOrDefault(this.selectedField, this.selectedField.defaultValue);
      List<String> presets = this.selectedField.getPresetValues();

      for (int i = 0; i < 7 && i < presets.size(); i++) {
         String preset = presets.get(i);
         boolean isCurrent = currentValue.equals(preset);
         this.addButton(
            28 + i,
            new ItemBuilder(isCurrent ? XMaterial.LIME_STAINED_GLASS_PANE.get() : XMaterial.WHITE_STAINED_GLASS_PANE.get())
               .setDisplayName(
                  this.plugin
                     .getMessageManager()
                     .getMessage(isCurrent ? "gui.cron.builder.preset.current_name" : "gui.cron.builder.preset.set_name", "%value%", preset)
               )
               .setLore(
                  this.plugin
                     .getMessageManager()
                     .getMessageList(
                        isCurrent ? "gui.cron.builder.preset.current_lore" : "gui.cron.builder.preset.set_lore",
                        "%description%",
                        this.selectedField.getValueDescription(this.plugin, preset)
                     )
                     .toArray(new String[0])
               )
               .build(),
            e -> {
               this.fieldValues.put(this.selectedField, preset);
               this.refresh();
            }
         );
      }

      this.addButton(
         31,
         new ItemBuilder(XMaterial.ANVIL)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.cron.builder.custom_input.name"))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.cron.builder.custom_input.lore", "%field%", this.selectedField.getDisplayName(this.plugin))
                  .toArray(new String[0])
            )
            .build(),
         e -> this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, input -> Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (input.equalsIgnoreCase("cancel")) {
               this.reopenMenu(this.buildExpression());
            } else {
               this.fieldValues.put(this.selectedField, input);
               this.reopenMenu(this.buildExpression());
            }
         }))
      );
      this.addButton(
         37,
         new ItemBuilder(XMaterial.RED_DYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.cron.builder.decrease.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.cron.builder.decrease.lore").toArray(new String[0]))
            .build(),
         e -> this.modifyNumericValue(-1)
      );
      this.addButton(
         43,
         new ItemBuilder(XMaterial.LIME_DYE)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.cron.builder.increase.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.cron.builder.increase.lore").toArray(new String[0]))
            .build(),
         e -> this.modifyNumericValue(1)
      );
      this.addButton(
         53,
         new ItemBuilder(XMaterial.EMERALD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.cron.builder.save.name"))
            .setLore(
               this.plugin.getMessageManager().getMessageList("gui.cron.builder.save.lore", "%expression%", this.buildExpression()).toArray(new String[0])
            )
            .build(),
         e -> {
            String expression = this.buildExpression();
            if (ValidationUtil.validateCron(expression)) {
               this.applyCron(expression);
            } else {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.cron.invalid"));
            }
         }
      );
   }

   private void parseExpression(String expression) {
      try {
         String[] parts = expression.split(" ");
         CronFieldMenu.CronField[] fields = CronFieldMenu.CronField.values();

         for (int i = 0; i < Math.min(parts.length, fields.length); i++) {
            this.fieldValues.put(fields[i], parts[i]);
         }

         for (CronFieldMenu.CronField field : fields) {
            this.fieldValues.putIfAbsent(field, field.defaultValue);
         }
      } catch (Exception var8) {
         for (CronFieldMenu.CronField field : CronFieldMenu.CronField.values()) {
            this.fieldValues.put(field, field.defaultValue);
         }
      }
   }

   private String buildExpression() {
      String[] parts = new String[CronFieldMenu.CronField.values().length];
      int i = 0;

      for (CronFieldMenu.CronField field : CronFieldMenu.CronField.values()) {
         parts[i++] = this.fieldValues.getOrDefault(field, field.defaultValue);
      }

      return String.join(" ", parts);
   }

   private void modifyNumericValue(int delta) {
      String current = this.fieldValues.getOrDefault(this.selectedField, this.selectedField.defaultValue);

      try {
         int value = Integer.parseInt(current);
         int newValue = Math.max(this.selectedField.minValue, Math.min(this.selectedField.maxValue, value + delta));
         this.fieldValues.put(this.selectedField, String.valueOf(newValue));
         this.refresh();
      } catch (NumberFormatException var5) {
      }
   }

   private void applyCron(String expression) {
      this.holder.setExpression(expression);
      this.holder.save().thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.cron.applied"));
         this.navigateBack();
      }));
   }

   private void reopenMenu(String expression) {
      new CronFieldMenu(this.playerMenuUtility, this.plugin, this.holder, this.policy, expression).setPreviousMenu(this.previousMenu).open();
   }

   private void navigateBack() {
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

   private static enum CronField {
      SECOND("second", "0", 0, 59, Arrays.asList("0", "*/5", "*/10", "*/15", "*/30", "*")),
      MINUTE("minute", "0", 0, 59, Arrays.asList("0", "*/5", "*/10", "*/15", "*/30", "*")),
      HOUR("hour", "0", 0, 23, Arrays.asList("0", "6", "12", "18", "*/6", "*/12", "*")),
      DAY("day", "*", 1, 31, Arrays.asList("1", "15", "L", "*", "?")),
      MONTH("month", "*", 1, 12, Arrays.asList("*", "1", "6", "12", "1,4,7,10")),
      DAY_OF_WEEK("day_of_week", "?", 1, 7, Arrays.asList("?", "*", "MON", "FRI", "SAT", "SUN", "MON-FRI")),
      YEAR("year", "*", 1970, 2099, Arrays.asList("*", "2025", "2026", "2027", "2030"));

      final String key;
      final String defaultValue;
      final int minValue;
      final int maxValue;
      final List<String> presetValues;

      private CronField(String key, String defaultValue, int minValue, int maxValue, List<String> presetValues) {
         this.key = key;
         this.defaultValue = defaultValue;
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.presetValues = presetValues;
      }

      String getDisplayName(Main plugin) {
         return plugin.getMessageManager().getMessage("gui.cron.builder.fields." + this.key + ".name");
      }

      String getDescription(Main plugin) {
         return plugin.getMessageManager().getMessage("gui.cron.builder.fields." + this.key + ".description");
      }

      List<String> getPresetValues() {
         return this.presetValues;
      }

      String getValueDescription(Main plugin, String value) {
         String fieldKey = "gui.cron.builder.fields." + this.key + ".values.";
         if (value.startsWith("*/")) {
            String interval = value.substring(2);
            return plugin.getMessageManager().getMessage(fieldKey + "every_interval", "%interval%", interval);
         } else {
            if (this == YEAR && !value.equals("*")) {
               try {
                  Integer.parseInt(value);
                  return plugin.getMessageManager().getMessage(fieldKey + "specific_year", "%year%", value);
               } catch (NumberFormatException var6) {
               }
            }

            String messageKey = fieldKey + this.sanitizeKey(value);
            String description = plugin.getMessageManager().getMessage(messageKey);
            return description.equals(messageKey) ? plugin.getMessageManager().getMessage("gui.cron.builder.fields.custom_value") : description;
         }
      }

      private String sanitizeKey(String value) {
         return value.replace("*", "wildcard")
            .replace("/", "_")
            .replace(",", "_")
            .replace("-", "_")
            .replace("?", "none")
            .replace("#", "nth")
            .replace("L", "last")
            .toLowerCase();
      }
   }
}
