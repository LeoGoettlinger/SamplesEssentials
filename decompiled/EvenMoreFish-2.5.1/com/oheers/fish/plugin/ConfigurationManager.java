package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.DimensionFishingConfig;
import com.oheers.fish.config.GuiFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.config.gui.GuiConversions;
import com.oheers.fish.config.gui.impl.ApplyBaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.BaitsMenuGuiConfig;
import com.oheers.fish.config.gui.impl.JournalFishGuiConfig;
import com.oheers.fish.config.gui.impl.JournalRaritiesGuiConfig;
import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import java.util.logging.Level;

public class ConfigurationManager {
   private final EvenMoreFish plugin;

   public ConfigurationManager(EvenMoreFish plugin) {
      this.plugin = plugin;
   }

   public void loadConfigurations() {
      try {
         new MainConfig();
         new MessageConfig();
         new GuiFillerConfig();
         ApplyBaitsMenuGuiConfig.getInstance();
         BaitsMenuGuiConfig.getInstance();
         JournalFishGuiConfig.getInstance();
         JournalRaritiesGuiConfig.getInstance();
         MainMenuGuiConfig.getInstance();
         SellMenuConfirmGuiConfig.getInstance();
         SellMenuNormalGuiConfig.getInstance();
         new GuiConversions().performCheck();
         if (EvenMoreFish.getInstance().getDimensionFishing() != null) {
            DimensionFishingConfig.getInstance().reload();
         }

         this.plugin.getLogger().info("Successfully loaded all configurations");
      } catch (Exception var2) {
         this.plugin.getLogger().log(Level.SEVERE, "Failed to load configurations", (Throwable)var2);
         this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
      }
   }

   public void reloadConfigurations() {
      try {
         this.plugin.reloadConfig();
         this.plugin.saveDefaultConfig();
         MainConfig.getInstance().reload();
         MessageConfig.getInstance().reload();
         ApplyBaitsMenuGuiConfig.getInstance().reload();
         BaitsMenuGuiConfig.getInstance().reload();
         JournalFishGuiConfig.getInstance().reload();
         JournalRaritiesGuiConfig.getInstance().reload();
         MainMenuGuiConfig.getInstance().reload();
         SellMenuConfirmGuiConfig.getInstance().reload();
         SellMenuNormalGuiConfig.getInstance().reload();
         GuiFillerConfig.getInstance().reload();
         this.plugin.getLogger().info("Successfully reloaded all configurations");
      } catch (Exception var2) {
         this.plugin.getLogger().log(Level.SEVERE, "Failed to reload configurations", (Throwable)var2);
      }
   }
}
