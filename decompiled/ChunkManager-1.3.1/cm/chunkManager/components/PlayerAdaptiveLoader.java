package cm.chunkManager.components;

import cm.chunkManager.hooks.GeyserBridge;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerAdaptiveLoader {
   private final JavaPlugin plugin;
   private boolean enabled;
   private final int pingLow;
   private final int pingMed;
   private final int pingHigh;
   private final int geyserReduction;
   private final Map<UUID, PlayerAdaptiveLoader.PlayerLoadProfile> profiles;

   public PlayerAdaptiveLoader(JavaPlugin plugin, int pingLow, int pingMed, int pingHigh, int geyserReduction) {
      this.plugin = plugin;
      this.enabled = true;
      this.pingLow = pingLow;
      this.pingMed = pingMed;
      this.pingHigh = pingHigh;
      this.geyserReduction = geyserReduction;
      this.profiles = new ConcurrentHashMap<>();
   }

   public int getRadiusModifier(Player player) {
      if (!this.enabled) {
         return 0;
      } else {
         PlayerAdaptiveLoader.PlayerLoadProfile profile = this.profiles.get(player.getUniqueId());
         if (profile == null) {
            return 0;
         } else {
            int modifier = 0;
            int ping = profile.lastPing;
            if (ping < this.pingLow) {
               modifier = 0;
            } else if (ping < this.pingMed) {
               modifier = -1;
            } else if (ping < this.pingHigh) {
               modifier = -2;
            } else {
               modifier = -3;
            }

            if (profile.isBedrockPlayer) {
               modifier -= this.geyserReduction;
            }

            return modifier;
         }
      }
   }

   public void updatePlayer(Player player) {
      PlayerAdaptiveLoader.PlayerLoadProfile profile = this.profiles.computeIfAbsent(player.getUniqueId(), PlayerAdaptiveLoader.PlayerLoadProfile::new);
      profile.lastPing = player.getPing();
      profile.isBedrockPlayer = this.isBedrockPlayer(player);
   }

   public boolean isBedrockPlayer(Player player) {
      if (GeyserBridge.isAvailable()) {
         return GeyserBridge.isBedrockPlayer(player.getUniqueId());
      } else {
         PlayerAdaptiveLoader.PlayerLoadProfile profile = this.profiles.get(player.getUniqueId());
         if (profile != null && profile.clientBrand != null) {
            String brand = profile.clientBrand.toLowerCase();
            return brand.contains("geyser") || brand.contains("bedrock");
         } else {
            return false;
         }
      }
   }

   public void onPlayerQuit(UUID uuid) {
      this.profiles.remove(uuid);
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public Map<UUID, PlayerAdaptiveLoader.PlayerLoadProfile> getProfiles() {
      return this.profiles;
   }

   public static class PlayerLoadProfile {
      public final UUID uuid;
      public int lastPing;
      public int radiusModifier;
      public boolean isBedrockPlayer;
      public String clientBrand;

      public PlayerLoadProfile(UUID uuid) {
         this.uuid = uuid;
         this.lastPing = 0;
         this.radiusModifier = 0;
         this.isBedrockPlayer = false;
         this.clientBrand = "vanilla";
      }
   }
}
