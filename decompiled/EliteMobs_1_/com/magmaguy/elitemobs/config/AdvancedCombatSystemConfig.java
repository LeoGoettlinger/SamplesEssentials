package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.IOException;
import java.util.List;
import lombok.Generated;

public final class AdvancedCombatSystemConfig extends ConfigurationFile {
   private static AdvancedCombatSystemConfig instance;
   private static boolean enabled;
   private static boolean allowClassAbilitiesOutsideEliteMobsWorlds;
   private static boolean enableCombatHud;
   private static boolean showJoinMessage;
   private static boolean showDeveloperMessage;

   public AdvancedCombatSystemConfig() {
      super("AdvancedCombatSystem.yml");
      instance = this;
   }

   public static boolean dismissDeveloperMessage() {
      if (instance == null) {
         return false;
      } else if (!showDeveloperMessage) {
         return true;
      } else {
         instance.fileConfiguration.set("showDeveloperMessage", false);

         try {
            instance.fileConfiguration.save(instance.file);
            showDeveloperMessage = false;
            return true;
         } catch (IOException var1) {
            instance.fileConfiguration.set("showDeveloperMessage", true);
            Logger.warn("Could not save the advanced combat developer message dismissal: " + var1.getMessage());
            return false;
         }
      }
   }

   @Override
   public void initializeValues() {
      enabled = ConfigurationEngine.setBoolean(
         List.of(
            "Enables the fixed [Alpha] Advanced Combat System baseline in EliteMobs dungeons and worlds.",
            "This feature is under active development. Please test the unchanged defaults and send feedback to the developer.",
            "Localization and customization settings are deliberately deferred during alpha.",
            "They will be added after the system has been tested, feedback has been gathered, and the system is out of alpha.",
            "Please keep the fixed defaults while we establish a solid baseline; no balance settings are exposed yet."
         ),
         this.fileConfiguration,
         "enabled",
         true
      );
      allowClassAbilitiesOutsideEliteMobsWorlds = ConfigurationEngine.setBoolean(
         List.of(
            "Allows players to enable class ability controls outside EliteMobs dungeons and protected worlds.",
            "Players opt in per session by double-tapping F while sneaking. EliteMobs combat content is always enabled."
         ),
         this.fileConfiguration,
         "allowClassAbilitiesOutsideEliteMobsWorlds",
         true
      );
      enableCombatHud = ConfigurationEngine.setBoolean(
         List.of(
            "Displays the graphical combat HUD when ResourcePackManager is enabled.",
            "Set to false to use the text action bar for health, class resources and ability feedback.",
            "The text display is also used automatically when ResourcePackManager is absent or disabled."
         ),
         this.fileConfiguration,
         "enableCombatHud",
         true
      );
      showJoinMessage = ConfigurationEngine.setBoolean(
         List.of(
            "Shows the class control toggle notice on login to players with at least one unlocked class.",
            "Set to false to hide this notice. This does not enable or disable combat."
         ),
         this.fileConfiguration,
         "showJoinMessage",
         true
      );
      showDeveloperMessage = ConfigurationEngine.setBoolean(
         List.of(
            "Shows administrators MagmaGuy's message about the new combat system when they log in.",
            "Clicking Dismiss permanently sets this to false for the entire server.",
            "Set this back to true to show the message again. This does not enable or disable combat."
         ),
         this.fileConfiguration,
         "showDeveloperMessage",
         true
      );
   }

   @Generated
   public static boolean isEnabled() {
      return enabled;
   }

   @Generated
   public static boolean isAllowClassAbilitiesOutsideEliteMobsWorlds() {
      return allowClassAbilitiesOutsideEliteMobsWorlds;
   }

   @Generated
   public static boolean isEnableCombatHud() {
      return enableCombatHud;
   }

   @Generated
   public static boolean isShowJoinMessage() {
      return showJoinMessage;
   }

   @Generated
   public static boolean isShowDeveloperMessage() {
      return showDeveloperMessage;
   }
}
