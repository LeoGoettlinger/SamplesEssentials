package me.usainsrht.ujobs.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.usainsrht.ujobs.utils.MathUtil;
import me.usainsrht.ujobs.yaml.YamlMessage;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorFlag;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

public class Job {
   private final String id;
   private Component name;
   private TextColor firstColor;
   private TextColor lastColor;
   private Material icon;
   private String levelEquation;
   private YamlMessage levelUpMessage;
   private List<String> levelUpCommands;
   private Job.BossBarConfig bossBarConfig;
   private final Map<Action, Map<String, Job.ActionReward>> actions;
   private final List<JobInfoLine> infoLines;

   public Job(
      String id, Component name, Material icon, String levelEquation, YamlMessage levelUpMessage, List<String> levelUpCommands, Job.BossBarConfig bossBarConfig
   ) {
      this.id = id;
      this.name = name;
      this.icon = icon;
      this.levelEquation = levelEquation;
      this.levelUpMessage = levelUpMessage;
      this.levelUpCommands = levelUpCommands;
      this.bossBarConfig = bossBarConfig;
      this.actions = new HashMap<>();
      this.infoLines = new ArrayList<>();
   }

   public void update(Job otherJobInstance) {
      if (otherJobInstance != null && this.id.equalsIgnoreCase(otherJobInstance.id)) {
         this.name = otherJobInstance.name;
         this.icon = otherJobInstance.icon;
         this.levelEquation = otherJobInstance.levelEquation;
         this.levelUpCommands = otherJobInstance.levelUpCommands;
         this.levelUpMessage = otherJobInstance.levelUpMessage;
         this.bossBarConfig = otherJobInstance.bossBarConfig;
         this.actions.clear();
         this.actions.putAll(otherJobInstance.actions);
         this.infoLines.clear();
         this.infoLines.addAll(otherJobInstance.infoLines);
      }
   }

   public void setColors() {
      TextColor firstColor = null;
      TextColor lastColor = null;

      for (Component part : this.name.iterable(ComponentIteratorType.DEPTH_FIRST, new ComponentIteratorFlag[0])) {
         if (part instanceof TextComponent text && !text.content().isEmpty()) {
            TextColor currentColor = text.color();
            if (firstColor == null) {
               firstColor = currentColor;
            }

            lastColor = currentColor;
         }
      }

      this.firstColor = firstColor;
      this.lastColor = lastColor;
   }

   public TextColor getFirstColor() {
      if (this.firstColor == null) {
         this.setColors();
      }

      return this.firstColor != null ? this.firstColor : TextColor.color(16777215);
   }

   public TextColor getLastColor() {
      if (this.lastColor == null) {
         this.setColors();
      }

      return this.lastColor != null ? this.lastColor : TextColor.color(16777215);
   }

   public void addAction(Action action, String value, Job.ActionReward reward) {
      this.actions.computeIfAbsent(action, k -> new HashMap<>()).put(value, reward);
   }

   public Job.ActionReward getActionReward(Action action, String value) {
      Map<String, Job.ActionReward> actionMap = this.actions.get(action);
      return actionMap != null ? actionMap.get(value) : null;
   }

   public long calculateExpForLevel(int level) {
      String equation = this.levelEquation.replace("<level>", String.valueOf(level)).replace("<next_level>", String.valueOf(level + 1));

      try {
         return (long)this.evaluateExpression(equation);
      } catch (Exception var4) {
         return (long)Math.pow(level + 1, 2.0) * 5L;
      }
   }

   private double evaluateExpression(String expression) {
      try {
         return MathUtil.eval(expression);
      } catch (Exception var3) {
         throw new RuntimeException("Invalid expression: " + expression, var3);
      }
   }

   @Generated
   public String getId() {
      return this.id;
   }

   @Generated
   public Component getName() {
      return this.name;
   }

   @Generated
   public Material getIcon() {
      return this.icon;
   }

   @Generated
   public String getLevelEquation() {
      return this.levelEquation;
   }

   @Generated
   public YamlMessage getLevelUpMessage() {
      return this.levelUpMessage;
   }

   @Generated
   public List<String> getLevelUpCommands() {
      return this.levelUpCommands;
   }

   @Generated
   public Job.BossBarConfig getBossBarConfig() {
      return this.bossBarConfig;
   }

   @Generated
   public Map<Action, Map<String, Job.ActionReward>> getActions() {
      return this.actions;
   }

   @Generated
   public List<JobInfoLine> getInfoLines() {
      return this.infoLines;
   }

   public static class ActionReward {
      private final double exp;
      private final double money;

      public ActionReward(double exp, double money) {
         this.exp = exp;
         this.money = money;
      }

      @Generated
      public double getExp() {
         return this.exp;
      }

      @Generated
      public double getMoney() {
         return this.money;
      }
   }

   public static class BossBarConfig {
      private final String titleTemplate;
      private final String levelUpTemplate;
      private final Color color;
      private final Overlay overlay;

      public BossBarConfig(String titleTemplate, String levelUpTemplate, Color color, Overlay overlay) {
         this.titleTemplate = titleTemplate;
         this.levelUpTemplate = levelUpTemplate;
         this.color = color;
         this.overlay = overlay;
      }

      @Generated
      public String getTitleTemplate() {
         return this.titleTemplate;
      }

      @Generated
      public String getLevelUpTemplate() {
         return this.levelUpTemplate;
      }

      @Generated
      public Color getColor() {
         return this.color;
      }

      @Generated
      public Overlay getOverlay() {
         return this.overlay;
      }
   }
}
