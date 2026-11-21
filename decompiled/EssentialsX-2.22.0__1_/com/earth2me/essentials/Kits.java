package com.earth2me.essentials;

import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.utils.NumberUtil;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.ess3.api.TranslatableException;

public class Kits implements IConf {
   private final IEssentials ess;
   private final EssentialsConfiguration rootConfig;
   private final Map<String, EssentialsConfiguration> kitToConfigMap = new HashMap<>();
   private final Map<String, Map<String, Object>> kitMap = new HashMap<>();

   public Kits(final IEssentials essentials) {
      this.ess = essentials;
      this.rootConfig = new EssentialsConfiguration(new File(essentials.getDataFolder(), "kits.yml"), "/kits.yml");
      this.reloadConfig();
   }

   @Override
   public void reloadConfig() {
      this.rootConfig.load();
      this.parseKits();
   }

   public File getFile() {
      return this.rootConfig.getFile();
   }

   private void parseKit(final String kitName, final CommentedConfigurationNode kitSection, final EssentialsConfiguration parentConfig) {
      if (kitSection.isMap()) {
         String effectiveKitName = kitName.toLowerCase(Locale.ENGLISH);
         this.kitToConfigMap.put(effectiveKitName, parentConfig);
         this.kitMap.put(effectiveKitName, ConfigurateUtil.getRawMap(kitSection));
      }
   }

   private void parseKits() {
      this.kitToConfigMap.clear();
      this.kitMap.clear();
      CommentedConfigurationNode fileKits = this.rootConfig.getSection("kits");
      if (fileKits != null) {
         for (Entry<String, CommentedConfigurationNode> kitEntry : ConfigurateUtil.getMap(fileKits).entrySet()) {
            this.parseKit(kitEntry.getKey(), kitEntry.getValue(), this.rootConfig);
         }
      }

      File kitsFolder = new File(this.ess.getDataFolder(), "kits");
      if (kitsFolder.exists() && kitsFolder.isDirectory()) {
         File[] kitsFiles = kitsFolder.listFiles();

         for (File kitFile : kitsFiles) {
            if (kitFile.getName().endsWith(".yml")) {
               EssentialsConfiguration kitConfig = new EssentialsConfiguration(kitFile);
               kitConfig.load();
               CommentedConfigurationNode kits = kitConfig.getSection("kits");
               if (kits != null) {
                  for (Entry<String, CommentedConfigurationNode> kitEntry : ConfigurateUtil.getMap(kits).entrySet()) {
                     this.parseKit(kitEntry.getKey(), kitEntry.getValue(), kitConfig);
                  }
               }
            }
         }
      }
   }

   public EssentialsConfiguration getRootConfig() {
      return this.rootConfig;
   }

   public Set<String> getKitKeys() {
      return this.kitMap.keySet();
   }

   public Map<String, Object> getKit(final String name) {
      return name != null ? this.kitMap.get(name.replace('.', '_').replace('/', '_')) : null;
   }

   public String matchKit(final String name) {
      if (name != null) {
         for (String kitName : this.kitMap.keySet()) {
            if (kitName.equalsIgnoreCase(name)) {
               return kitName;
            }
         }
      }

      return null;
   }

   public void addKit(String name, final List<String> lines, final long delay) {
      name = name.replace('.', '_').replace('/', '_').toLowerCase(Locale.ENGLISH);
      this.rootConfig.setProperty("kits." + name + ".delay", delay);
      this.rootConfig.setProperty("kits." + name + ".items", lines);
      this.parseKits();
      this.rootConfig.save();
   }

   public void removeKit(String name) {
      name = name.replace('.', '_').replace('/', '_').toLowerCase(Locale.ENGLISH);
      if (this.kitToConfigMap.containsKey(name) && this.kitMap.containsKey(name)) {
         EssentialsConfiguration config = this.kitToConfigMap.get(name);
         config.removeProperty("kits." + name);
         config.blockingSave();
         this.parseKits();
      }
   }

   public String listKits(final net.ess3.api.IEssentials ess, final User user) throws Exception {
      try {
         StringBuilder list = new StringBuilder();

         for (String kitItem : this.kitMap.keySet()) {
            if (user == null) {
               list.append(" ").append(I18n.capitalCase(kitItem));
            } else if (user.isAuthorized("essentials.kits." + kitItem.toLowerCase(Locale.ENGLISH))) {
               String cost = "";
               String name = I18n.capitalCase(kitItem);
               BigDecimal costPrice = new Trade("kit-" + kitItem.toLowerCase(Locale.ENGLISH), ess).getCommandCost(user);
               if (costPrice.signum() > 0) {
                  cost = user.playerTl("kitCost", NumberUtil.displayCurrency(costPrice, ess));
               }

               Kit kit = new Kit(kitItem, ess);
               double nextUse = kit.getNextUse(user);
               if (nextUse != -1.0 || !ess.getSettings().isSkippingUsedOneTimeKitsFromKitList()) {
                  if (nextUse != 0.0) {
                     name = user.playerTl("kitDelay", name);
                  }

                  list.append(" ").append(name).append(cost);
               }
            }
         }

         return list.toString().trim();
      } catch (Exception var12) {
         throw new TranslatableException(var12, "kitError");
      }
   }
}
