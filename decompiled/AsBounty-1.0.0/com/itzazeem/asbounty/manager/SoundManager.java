package com.itzazeem.asbounty.manager;

import com.itzazeem.asbounty.AsBounty;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {
   private final AsBounty plugin;

   public SoundManager(AsBounty plugin) {
      this.plugin = plugin;
   }

   public void playSound(Player player, String soundKey) {
      if (player != null) {
         boolean enabled = (Boolean)this.plugin.getConfigManager().getSoundConfig().getOrDefault(soundKey + ".enabled", true);
         if (enabled) {
            String soundName = (String)this.plugin.getConfigManager().getSoundConfig().getOrDefault(soundKey + ".sound", "UI_BUTTON_CLICK");

            try {
               Sound sound = Sound.valueOf(soundName.toUpperCase());
               player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            } catch (Throwable var8) {
               try {
                  player.playSound(player.getLocation(), soundName.toLowerCase().replace("_", "."), 1.0F, 1.0F);
               } catch (Throwable var7) {
               }
            }
         }
      }
   }
}
