package org.lushplugins.lushrewards.module;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.libraries.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushrewards.libraries.lushlib.utils.converter.YamlConverter;

public abstract class RewardModule extends Module {
   protected final File moduleFile;
   private final boolean requiresTimeTracker;
   private boolean shouldNotify = false;
   private final ConcurrentHashMap<String, DisplayItemStack> itemTemplates = new ConcurrentHashMap<>();

   public RewardModule(String id, File moduleFile) {
      super(id);
      this.moduleFile = moduleFile;
      this.requiresTimeTracker = false;
   }

   public RewardModule(String id, File moduleFile, boolean requiresTimeTracker) {
      super(id);
      this.moduleFile = moduleFile;
      this.requiresTimeTracker = requiresTimeTracker;
   }

   public abstract boolean hasClaimableRewards(Player var1);

   public abstract boolean claimRewards(Player var1);

   public File getModuleFile() {
      return this.moduleFile;
   }

   public boolean requiresPlaytimeTracker() {
      return this.requiresTimeTracker;
   }

   public boolean shouldNotify() {
      return this.shouldNotify;
   }

   public void setShouldNotify(boolean shouldNotify) {
      this.shouldNotify = shouldNotify;
   }

   public DisplayItemStack getItemTemplate(String key) {
      DisplayItemStack itemTemplate = this.itemTemplates.get(key);
      return itemTemplate != null ? itemTemplate : DisplayItemStack.empty();
   }

   public void reloadItemTemplates(ConfigurationSection itemTemplatesSection) {
      this.itemTemplates.clear();
      if (itemTemplatesSection != null) {
         itemTemplatesSection.getValues(false).forEach((key, value) -> {
            if (value instanceof ConfigurationSection categorySection) {
               this.itemTemplates.put(categorySection.getName(), YamlConverter.getDisplayItem(categorySection));
               LushRewards.getInstance().getLogger().info("Loaded item-template: " + categorySection.getName());
            }
         });
      }
   }

   @FunctionalInterface
   public interface Constructor<T extends RewardModule> {
      T build(String var1, File var2);
   }

   public static class Type {
      public static final String DAILY_REWARDS = "daily-rewards";
      public static final String PLAYTIME_REWARDS = "playtime-rewards";
      public static final String PLAYTIME_TRACKER = "playtime-tracker";
   }
}
