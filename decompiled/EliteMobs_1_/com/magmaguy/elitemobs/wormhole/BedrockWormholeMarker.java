package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.BedrockCustomEntityBridgeRegistry;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.CustomEntityPropertySchema;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.FakeCustomEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

final class BedrockWormholeMarker {
   private static final String IDENTIFIER = "elitemobs:wormhole_marker";
   private FakeCustomEntity marker;
   private Location lastLocation;
   private float lastScale = Float.NaN;
   private int lastColor = Integer.MIN_VALUE;
   private boolean failedInitialization;

   void tick(Wormhole wormhole, Location location, Collection<Player> nearbyPlayers) {
      if (this.failedInitialization || !available() || wormhole == null || location == null || location.getWorld() == null) {
         this.remove();
      } else {
         if (this.marker == null) {
            try {
               this.marker = NMSManager.getAdapter()
                  .fakeCustomEntityBuilder()
                  .identifier("elitemobs:wormhole_marker")
                  .carrierEntityType(EntityType.PIG)
                  .dimensions(0.9F, 0.9F)
                  .scale(scale(wormhole))
                  .color(color(wormhole))
                  .tracked(false)
                  .propertySchema(CustomEntityPropertySchema.empty())
                  .build(location);
            } catch (RuntimeException var5) {
               this.failedInitialization = true;
               Logger.warn("Failed to initialize Bedrock wormhole marker; disabling marker for this wormhole: " + var5.getMessage());
               this.remove();
               return;
            }
         }

         this.syncLocation(location);
         this.syncScale(wormhole);
         this.syncColor(wormhole);
         this.syncViewers(nearbyPlayers);
      }
   }

   void remove() {
      if (this.marker != null) {
         this.marker.remove();
         this.marker = null;
         this.lastLocation = null;
         this.lastScale = Float.NaN;
         this.lastColor = Integer.MIN_VALUE;
      }
   }

   private static boolean available() {
      return NMSManager.getAdapter() != null && BedrockCustomEntityBridgeRegistry.isAvailable();
   }

   private void syncLocation(Location location) {
      if (this.lastLocation == null || this.lastLocation.getWorld() != location.getWorld() || this.lastLocation.distanceSquared(location) > 1.0E-4) {
         this.marker.teleport(location);
         this.lastLocation = location.clone();
      }
   }

   private void syncScale(Wormhole wormhole) {
      float scale = scale(wormhole);
      if (Float.compare(scale, this.lastScale) != 0) {
         this.marker.setScale(scale);
         this.lastScale = scale;
      }
   }

   private void syncColor(Wormhole wormhole) {
      int color = color(wormhole);
      if (color != this.lastColor) {
         this.marker.setColor(color);
         this.lastColor = color;
      }
   }

   private void syncViewers(Collection<Player> nearbyPlayers) {
      Set<UUID> shouldSee = new HashSet<>();

      for (Player player : nearbyPlayers) {
         if (player != null && player.isOnline() && BedrockChecker.isBedrock(player)) {
            shouldSee.add(player.getUniqueId());
            if (!this.marker.getViewers().contains(player.getUniqueId())) {
               this.marker.displayTo(player);
            }
         }
      }

      for (UUID viewer : this.marker.getViewers()) {
         if (!shouldSee.contains(viewer)) {
            this.marker.hideFrom(viewer);
         }
      }
   }

   private static float scale(Wormhole wormhole) {
      return (float)Math.max(0.5, wormhole.getWormholeConfigFields().getSizeMultiplier());
   }

   private static int color(Wormhole wormhole) {
      Color color = wormhole.getParticleColor();
      return color == null ? 16777215 : color.asRGB();
   }
}
