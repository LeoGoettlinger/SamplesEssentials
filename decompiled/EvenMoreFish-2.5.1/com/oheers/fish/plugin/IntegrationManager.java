package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.InternalAddonLoader;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.addons.AddonManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.processors.LavaFishingProcessor;
import com.oheers.fish.fishing.processors.VoidFishingProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;

public class IntegrationManager {
   private final EvenMoreFish plugin;
   private AddonManager addonManager;

   public IntegrationManager(EvenMoreFish plugin) {
      this.plugin = plugin;
   }

   public void loadAddons() {
      this.addonManager = new AddonManager(this.plugin);
      this.saveDefaultAddons();
      this.addonManager.load();
      new InternalAddonLoader(this.plugin).load();
   }

   public void unloadAddons() {
      this.addonManager.unload();
   }

   public void setupDimensionFishing() {
      Bukkit.getPluginManager().registerEvents(new LavaFishingProcessor(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new VoidFishingProcessor(), this.plugin);
      DimensionFishing.getInstance().reload(null);
   }

   private void saveDefaultAddons() {
      if (MainConfig.getInstance().useAdditionalAddons()) {
         Set<String> addons = this.getAddonFileNames();
         if (addons.isEmpty()) {
            this.plugin.getLogger().warning("Could not find any addons.");
         } else {
            Path addonsDir = this.plugin.getDataFolder().toPath().resolve("addons");

            for (String fileName : addons) {
               try {
                  String resourcePath = "addons/" + fileName;
                  this.plugin.saveResource(resourcePath, true);
                  Path addonFile = addonsDir.resolve(fileName);
                  Path jarFile = addonsDir.resolve(fileName.replace(".addon", ".jar"));
                  Files.move(addonFile, jarFile, StandardCopyOption.REPLACE_EXISTING);
                  this.plugin.debug(Level.INFO, "Converted addon file: " + fileName);
               } catch (IllegalArgumentException var8) {
                  this.plugin.debug(Level.WARNING, "Default addon not found: " + fileName);
               } catch (IOException var9) {
                  this.plugin.debug(Level.WARNING, "Failed to rename addon file: " + fileName, var9);
               }
            }
         }
      }
   }

   private Set<String> getAddonFileNames() {
      try {
         return FileUtil.getAddonFilenames(this.getClass(), "addons");
      } catch (IOException var2) {
         return Collections.emptySet();
      }
   }

   public AddonManager getAddonManager() {
      return this.addonManager;
   }
}
