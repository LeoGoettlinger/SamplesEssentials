package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ValidWorldsConfig extends ConfigurationFile {
   private static final List<String> validWorlds = new ArrayList<>();
   private static ValidWorldsConfig instance;

   public ValidWorldsConfig() {
      super("ValidWorlds.yml");
      instance = this;
   }

   public static void addWorld(String worldName) {
      if (!instance.fileConfiguration.contains("validWorlds." + worldName)) {
         ConfigurationEngine.setBoolean(List.of("Whether elites will spawn in this world."), instance.fileConfiguration, "validWorlds." + worldName, true);
         ConfigurationEngine.fileSaverOnlyDefaults(instance.fileConfiguration, instance.file);
         validWorlds.add(worldName);
      }
   }

   @Override
   public void initializeValues() {
      for (World world : Bukkit.getWorlds()) {
         ConfigurationEngine.setBoolean(this.fileConfiguration, "validWorlds." + world.getName(), true);
      }

      this.fileConfiguration.options().copyDefaults(true);
      ConfigurationSection validWorldsSection = this.fileConfiguration.getConfigurationSection("validWorlds");
      validWorlds.clear();
      if (validWorldsSection != null) {
         for (String key : validWorldsSection.getKeys(false)) {
            if (validWorldsSection.getBoolean(key)) {
               validWorlds.add(key);
            }
         }
      }
   }

   @Generated
   public static List<String> getValidWorlds() {
      return validWorlds;
   }

   @Generated
   public static ValidWorldsConfig getInstance() {
      return instance;
   }
}
