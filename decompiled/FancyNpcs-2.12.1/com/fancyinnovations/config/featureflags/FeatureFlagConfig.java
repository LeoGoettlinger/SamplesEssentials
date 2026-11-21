package com.fancyinnovations.config.featureflags;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

@Deprecated
public class FeatureFlagConfig {
   private final Plugin plugin;
   private final File configFile;
   private final List<FeatureFlag> featureFlags;

   public FeatureFlagConfig(Plugin plugin) {
      this.plugin = plugin;
      this.configFile = new File("plugins" + File.separator + plugin.getName() + File.separator + "featureFlags.yml");
      this.featureFlags = new ArrayList<>();
   }

   public void load() {
      if (!this.configFile.exists()) {
         try {
            new File(this.configFile.getParent()).mkdirs();
            this.configFile.createNewFile();
         } catch (IOException var7) {
            var7.printStackTrace();
         }
      }

      YamlConfiguration config = YamlConfiguration.loadConfiguration(this.configFile);

      for (FeatureFlag featureFlag : this.featureFlags) {
         if (!config.isSet("feature-flags." + featureFlag.getName())) {
            config.set("feature-flags." + featureFlag.getName(), false);
            config.setInlineComments("feature-flags." + featureFlag.getName(), List.of(featureFlag.getDescription()));
         }
      }

      for (String flagName : config.getConfigurationSection("feature-flags").getKeys(false)) {
         boolean enabled = config.getBoolean("feature-flags." + flagName, false);
         FeatureFlag flag = this.getFeatureFlag(flagName);
         if (flag == null) {
            flag = new FeatureFlag(flagName, "", false);
            this.featureFlags.add(flag);
         }

         flag.setEnabled(enabled);
      }

      try {
         config.save(this.configFile);
      } catch (IOException var6) {
         var6.printStackTrace();
      }
   }

   public FeatureFlag getFeatureFlag(String name) {
      for (FeatureFlag featureFlag : this.featureFlags) {
         if (featureFlag.getName().equalsIgnoreCase(name.toLowerCase())) {
            return featureFlag;
         }
      }

      return null;
   }

   public void addFeatureFlag(FeatureFlag featureFlag) {
      if (!this.featureFlags.contains(featureFlag)) {
         this.featureFlags.add(featureFlag);
      }
   }
}
