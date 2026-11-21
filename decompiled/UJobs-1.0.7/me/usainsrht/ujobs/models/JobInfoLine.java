package me.usainsrht.ujobs.models;

import java.util.Locale;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class JobInfoLine {
   final Action action;
   final String value;
   final Job.ActionReward reward;
   final Component actionValue;

   public JobInfoLine(Action action, String value, Job.ActionReward reward) {
      this.action = action;
      this.value = value;
      this.reward = reward;
      String line = UJobsPlugin.instance.getConfig().getString("actions." + action.getName().toLowerCase(Locale.ROOT));
      if (line == null) {
         UJobsPlugin.instance.getLogger().severe("Please add " + action.getName().toLowerCase(Locale.ROOT) + " to the config.yml under 'actions:'");
         line = "<gray>" + action.getName() + "</gray> <value>";
      }

      Component parsed;
      if (action instanceof EntityAction) {
         EntityType entityType = EntityType.valueOf(value.toUpperCase(Locale.ROOT));
         parsed = Component.translatable(entityType);
      } else if (action instanceof MaterialAction) {
         String numbersRemoved = value.replaceAll("\\d+", "");
         Material material = Material.matchMaterial(numbersRemoved);
         if (material != null) {
            parsed = Component.translatable(material);
         } else {
            parsed = Component.text(value);
         }
      } else if (action instanceof SpecialAction) {
         String configValue = UJobsPlugin.instance.getConfig().getString("special_values." + value);
         if (configValue == null) {
            parsed = Component.text(value);
         } else {
            parsed = UJobsPlugin.instance.getMiniMessage().deserialize(configValue);
         }
      } else {
         parsed = Component.text(value);
      }

      this.actionValue = UJobsPlugin.instance.getMiniMessage().deserialize(line, Placeholder.component("value", parsed));
   }

   @Generated
   public Action getAction() {
      return this.action;
   }

   @Generated
   public String getValue() {
      return this.value;
   }

   @Generated
   public Job.ActionReward getReward() {
      return this.reward;
   }

   @Generated
   public Component getActionValue() {
      return this.actionValue;
   }
}
