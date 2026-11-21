package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.lib.xseries.XSound;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundManager {
   private final JavaPlugin plugin;
   private ConfigurationSection soundConfig;
   private static ConfigurationSection staticSoundConfig;

   public SoundManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      this.soundConfig = this.plugin.getConfig().getConfigurationSection("sounds");
      staticSoundConfig = this.plugin.getConfig().getConfigurationSection("sounds");
   }

   public boolean isEnabled() {
      return this.soundConfig != null && this.soundConfig.getBoolean("enabled", true);
   }

   public static boolean isEnabledStatic() {
      return staticSoundConfig != null && staticSoundConfig.getBoolean("enabled", true);
   }

   public boolean isSoundEnabled(String key) {
      ConfigurationSection section = this.soundConfig.getConfigurationSection(key);
      return section != null && section.getBoolean("enabled", true);
   }

   public void playTreasureFound(Player player) {
      this.playSound(player, "treasure-found");
   }

   public void playCollectionCompleted(Player player) {
      this.playSound(player, "collection-completed");
   }

   public void playTreasurePlaced(Player player) {
      this.playSound(player, "treasure-placed");
   }

   public void playTreasureBroken(Player player) {
      this.playSound(player, "treasure-broken");
   }

   public void playTreasureAlreadyFound(Player player) {
      this.playSound(player, "treasure-already-found");
   }

   public void playTreasureClaimedByOther(Player player) {
      this.playSound(player, "treasure-claimed-by-other");
   }

   public void playBlockProtected(Player player) {
      this.playSound(player, "block-protected");
   }

   public void playPlaceModeBreakDeny(Player player) {
      this.playSound(player, "place-mode-break-denied");
   }

   public void playPlaceModeCollectDeny(Player player) {
      this.playSound(player, "place-mode-collect-denied");
   }

   public void playCollectionUnavailable(Player player) {
      this.playSound(player, "collection_unavailable");
   }

   private void playSound(Player player, String key) {
      if (this.isEnabled()) {
         if (this.isSoundEnabled(key)) {
            ConfigurationSection section = this.soundConfig.getConfigurationSection(key);
            if (section != null) {
               String soundName = section.getString("sound");
               if (soundName != null && !soundName.isEmpty()) {
                  float volume = (float)section.getDouble("volume", 1.0);
                  float pitch = (float)section.getDouble("pitch", 1.0);
                  Optional<XSound> xSound = XSound.matchXSound(soundName);
                  if (xSound.isPresent()) {
                     xSound.get().play(player, volume, pitch);
                  } else {
                     this.plugin.getLogger().warning("Invalid sound name in config: " + soundName + " (Section: " + key + ")");
                  }
               }
            }
         }
      }
   }
}
