package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.ParticleUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class HintManager {
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final CollectionManager collectionManager;
   private final PlayerManager playerManager;
   private final MessageManager messageManager;
   private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private final Map<UUID, BukkitTask> activeVisualHints = new ConcurrentHashMap<>();
   private BukkitTask cleanupTask;
   private boolean visualHintEnabled;
   private HintManager.VisualHintType visualHintType;
   private int visualHintDuration;
   private String visualHintParticle;
   private boolean applyFailCooldown;
   private boolean applyFailCooldownOnOP;
   private int cooldownSeconds;
   private int proximityRange;
   private HintManager.CoordinateRevealType revealType;
   private int visualHintMaxDistance;
   private int visualHintOffsetRange;
   private static final long VISUAL_UPDATE_INTERVAL = 20L;
   private static final double PARTICLE_SPACING = 1.25;
   private static final int BEACON_HEIGHT = 20;
   private static final double EYE_LEVEL_OFFSET = 1.0;
   private static final double TRAIL_START_DISTANCE = 1.5;
   private static final double TRAIL_Y_OFFSET = -0.35;
   private static final double TRAIL_OFFSET = 0.03;
   private static final double BEACON_OFFSET = 0.06;
   private static final int BEACON_Y_STEP = 2;
   private static final String[] DIRECTION_KEYS = new String[]{
      "command.direction.east",
      "command.direction.southeast",
      "command.direction.south",
      "command.direction.southwest",
      "command.direction.west",
      "command.direction.northwest",
      "command.direction.north",
      "command.direction.northeast"
   };

   public HintManager(
      Main plugin,
      TreasureManager treasureManager,
      CollectionManager collectionManager,
      PlayerManager playerManager,
      MessageManager messageManager,
      ParticleManager particleManager
   ) {
      this.plugin = plugin;
      this.treasureManager = treasureManager;
      this.collectionManager = collectionManager;
      this.playerManager = playerManager;
      this.messageManager = messageManager;
      this.reloadConfig();
      this.cleanupTask = (new BukkitRunnable() {
         public void run() {
            HintManager.this.cleanupExpiredCooldowns();
         }
      }).runTaskTimerAsynchronously(plugin, 6000L, 6000L);
   }

   public void stop() {
      this.cancelAllVisualHints();
      if (this.cleanupTask != null) {
         this.cleanupTask.cancel();
         this.cleanupTask = null;
      }
   }

   public void reloadConfig() {
      this.visualHintEnabled = this.plugin.getConfig().getBoolean("hint.visual-hint.enabled", false);
      String typeStr = this.plugin.getConfig().getString("hint.visual-hint.type", "NONE");

      try {
         this.visualHintType = HintManager.VisualHintType.valueOf(typeStr.toUpperCase());
      } catch (IllegalArgumentException var5) {
         this.visualHintType = HintManager.VisualHintType.NONE;
         this.plugin.getLogger().warning("Invalid visual hint type: " + typeStr + ", defaulting to NONE");
      }

      this.visualHintDuration = Math.max(1, this.plugin.getConfig().getInt("hint.visual-hint.duration-seconds", 15));
      this.visualHintParticle = this.plugin.getConfig().getString("hint.visual-hint.particle", "END_ROD");
      this.visualHintMaxDistance = Math.max(5, this.plugin.getConfig().getInt("hint.visual-hint.max-distance", 15));
      this.visualHintOffsetRange = Math.max(3, this.plugin.getConfig().getInt("hint.visual-hint.offset-range", 10));
      this.applyFailCooldown = this.plugin.getConfig().getBoolean("hint.apply-cooldown-on-fail", true);
      this.applyFailCooldownOnOP = this.plugin.getConfig().getBoolean("hint.apply-cooldown-to-operators", false);
      this.cooldownSeconds = Math.max(0, this.plugin.getConfig().getInt("hint.cooldown-seconds", 300));
      this.proximityRange = Math.max(1, this.plugin.getConfig().getInt("hint.proximity-range", 50));
      String revealStr = this.plugin.getConfig().getString("hint.reveal-coordinate", "RANDOM");

      try {
         this.revealType = HintManager.CoordinateRevealType.valueOf(revealStr.toUpperCase());
      } catch (IllegalArgumentException var4) {
         this.revealType = HintManager.CoordinateRevealType.RANDOM;
         this.plugin.getLogger().warning("Invalid reveal coordinate type: " + revealStr + ", defaulting to RANDOM");
      }
   }

   public boolean isOnCooldown(UUID playerId) {
      Long lastHint = this.cooldowns.get(playerId);
      if (lastHint == null) {
         return false;
      } else {
         long elapsed = (System.currentTimeMillis() - lastHint) / 1000L;
         return elapsed < this.cooldownSeconds;
      }
   }

   public long getRemainingCooldown(UUID playerId) {
      Long lastHint = this.cooldowns.get(playerId);
      if (lastHint == null) {
         return 0L;
      } else {
         long elapsed = (System.currentTimeMillis() - lastHint) / 1000L;
         return Math.max(0L, this.cooldownSeconds - elapsed);
      }
   }

   public void applyCooldown(UUID playerId) {
      this.cooldowns.put(playerId, System.currentTimeMillis());
   }

   public void applyFailureCooldown(Player player) {
      if (this.applyFailCooldown) {
         if (!player.isOp() || this.applyFailCooldownOnOP) {
            this.applyCooldown(player.getUniqueId());
         }
      }
   }

   private void cleanupExpiredCooldowns() {
      long now = System.currentTimeMillis();
      long expirationThreshold = this.cooldownSeconds * 1000L;
      this.cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > expirationThreshold);
   }

   public Optional<TreasureCore> findRandomUnfoundTreasure(Player player) {
      PlayerData playerData = this.playerManager.getPlayerData(player.getUniqueId());
      Location playerLoc = player.getLocation();
      List<Collection> allCollections = this.collectionManager.getAllCollections();
      Set<UUID> availableCollections = new HashSet<>();

      for (Collection collection : allCollections) {
         if (this.collectionManager.isCollectionAvailable(collection)) {
            availableCollections.add(collection.getId());
         }
      }

      if (availableCollections.isEmpty()) {
         return Optional.empty();
      } else {
         int chunkX = playerLoc.getBlockX() >> 4;
         int chunkZ = playerLoc.getBlockZ() >> 4;
         int chunkRadius = (int)Math.ceil(this.proximityRange / 16.0);
         int rangeSq = this.proximityRange * this.proximityRange;
         List<TreasureCore> candidateTreasures = null;

         for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
               List<TreasureCore> treasures = this.treasureManager.getTreasureCoresInChunk(x, z);
               if (treasures != null && !treasures.isEmpty()) {
                  for (TreasureCore treasure : treasures) {
                     Location treasureLoc = treasure.getLocation();
                     if (treasureLoc != null
                        && treasure.isInLoadedWorld(playerLoc.getWorld())
                        && !(treasureLoc.distanceSquared(playerLoc) > rangeSq)
                        && !playerData.hasFound(treasure.getId())
                        && availableCollections.contains(treasure.getCollectionId())) {
                        if (candidateTreasures == null) {
                           candidateTreasures = new ArrayList<>();
                        }

                        candidateTreasures.add(treasure);
                     }
                  }
               }
            }
         }

         if (candidateTreasures != null && !candidateTreasures.isEmpty()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(candidateTreasures.size());
            return Optional.of(candidateTreasures.get(randomIndex));
         } else {
            return Optional.empty();
         }
      }
   }

   public void deliverHint(Player player, TreasureCore treasure) {
      Location treasureLoc = treasure.getLocation();
      Location playerLoc = player.getLocation();
      double distance = playerLoc.distance(treasureLoc);
      String direction = this.getCardinalDirection(playerLoc, treasureLoc);
      HintManager.CoordinateRevealType actualReveal = this.selectCoordinateToReveal();
      String messageKey;
      String coordinateValue;
      switch (actualReveal) {
         case X:
            messageKey = "hint.coordinate_x";
            coordinateValue = String.valueOf(treasureLoc.getBlockX());
            break;
         case Z:
            messageKey = "hint.coordinate_z";
            coordinateValue = String.valueOf(treasureLoc.getBlockZ());
            break;
         default:
            return;
      }

      player.sendMessage(
         this.messageManager.getMessage(messageKey, "%value%", coordinateValue, "%distance%", String.format("%.1f", distance), "%direction%", direction)
      );
      if (this.visualHintEnabled && this.visualHintType != HintManager.VisualHintType.NONE) {
         this.applyVisualHint(player, treasure);
         player.sendMessage(this.messageManager.getMessage("hint.visual_active"));
      }

      this.applyFailureCooldown(player);
   }

   private HintManager.CoordinateRevealType selectCoordinateToReveal() {
      if (this.revealType != HintManager.CoordinateRevealType.RANDOM) {
         return this.revealType;
      } else {
         return ThreadLocalRandom.current().nextBoolean() ? HintManager.CoordinateRevealType.X : HintManager.CoordinateRevealType.Z;
      }
   }

   private String getCardinalDirection(Location from, Location to) {
      double dx = to.getX() - from.getX();
      double dz = to.getZ() - from.getZ();
      double angle = Math.toDegrees(Math.atan2(dz, dx));
      if (angle < 0.0) {
         angle += 360.0;
      }

      int directionIndex = (int)Math.round(angle / 45.0) % 8;
      return this.messageManager.getMessage(DIRECTION_KEYS[directionIndex], false);
   }

   private void applyVisualHint(Player player, TreasureCore treasure) {
      this.cancelVisualHint(player.getUniqueId());
      BukkitTask task;
      switch (this.visualHintType) {
         case TRAIL:
            task = this.startParticleTrail(player, treasure);
            break;
         case COMPASS:
            task = this.startCompassPointing(player, treasure);
            break;
         case BEACON:
            task = this.startBeaconEffect(player, treasure);
            break;
         default:
            return;
      }

      this.activeVisualHints.put(player.getUniqueId(), task);
   }

   private BukkitTask startParticleTrail(final Player player, TreasureCore treasure) {
      Location treasureLoc = treasure.getLocation();
      int offsetX = ThreadLocalRandom.current().nextInt(-this.visualHintOffsetRange, this.visualHintOffsetRange + 1);
      int offsetZ = ThreadLocalRandom.current().nextInt(-this.visualHintOffsetRange, this.visualHintOffsetRange + 1);
      final Location targetCenter = treasureLoc.clone().add(offsetX + 0.5, 0.5, offsetZ + 0.5);
      return (new BukkitRunnable() {
         int ticksRemaining = HintManager.this.visualHintDuration * 20;

         public void run() {
            if (player.isOnline() && this.ticksRemaining > 0) {
               Location playerEye = player.getEyeLocation();
               Vector direction = targetCenter.toVector().subtract(playerEye.toVector());
               double actualDistance = direction.length();
               direction.normalize();
               double hintDistance = Math.min(actualDistance, (double)HintManager.this.visualHintMaxDistance);
               double startDistance = Math.min(1.5, hintDistance);
               double steps = Math.ceil((hintDistance - startDistance) / 1.25);

               for (int i = 0; i < steps; i++) {
                  double offset = startDistance + i * 1.25;
                  Location particleLoc = playerEye.clone().add(direction.clone().multiply(offset)).add(0.0, -0.35, 0.0);
                  ParticleUtils.spawnParticleForPlayer(player, particleLoc, HintManager.this.visualHintParticle, 1, 0.03, 0.03, 0.03, 0.0);
               }

               this.ticksRemaining = (int)(this.ticksRemaining - 20L);
            } else {
               this.cancel();
               HintManager.this.activeVisualHints.remove(player.getUniqueId());
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   private BukkitTask startCompassPointing(final Player player, TreasureCore treasure) {
      Location treasureLoc = treasure.getLocation();
      int offsetX = ThreadLocalRandom.current().nextInt(-this.visualHintOffsetRange, this.visualHintOffsetRange + 1);
      int offsetZ = ThreadLocalRandom.current().nextInt(-this.visualHintOffsetRange, this.visualHintOffsetRange + 1);
      Location approximateTarget = treasureLoc.clone().add(offsetX, 0.0, offsetZ);
      player.setCompassTarget(approximateTarget);
      return (new BukkitRunnable() {
         public void run() {
            if (player.isOnline()) {
               player.setCompassTarget(player.getWorld().getSpawnLocation());
            }

            HintManager.this.activeVisualHints.remove(player.getUniqueId());
         }
      }).runTaskLater(this.plugin, this.visualHintDuration * 20L);
   }

   private BukkitTask startBeaconEffect(final Player player, TreasureCore treasure) {
      Location treasureLoc = treasure.getLocation();
      int offsetX = ThreadLocalRandom.current().nextInt(-this.visualHintOffsetRange, this.visualHintOffsetRange + 1);
      int offsetZ = ThreadLocalRandom.current().nextInt(-this.visualHintOffsetRange, this.visualHintOffsetRange + 1);
      final Location baseLoc = treasureLoc.clone().add(offsetX + 0.5, 0.0, offsetZ + 0.5);
      final int maxHeight = Math.min(20, baseLoc.getWorld().getMaxHeight() - baseLoc.getBlockY());
      return (new BukkitRunnable() {
         int ticksRemaining = HintManager.this.visualHintDuration * 20;

         public void run() {
            if (player.isOnline() && this.ticksRemaining > 0) {
               for (int y = 0; y < maxHeight; y += 2) {
                  Location particleLoc = baseLoc.clone().add(0.0, y, 0.0);
                  ParticleUtils.spawnParticleForPlayer(player, particleLoc, HintManager.this.visualHintParticle, 1, 0.06, 0.06, 0.06, 0.0);
               }

               this.ticksRemaining = (int)(this.ticksRemaining - 20L);
            } else {
               this.cancel();
               HintManager.this.activeVisualHints.remove(player.getUniqueId());
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void cancelVisualHint(UUID playerId) {
      BukkitTask task = this.activeVisualHints.remove(playerId);
      if (task != null) {
         task.cancel();
      }
   }

   public void cancelAllVisualHints() {
      this.activeVisualHints.values().forEach(BukkitTask::cancel);
      this.activeVisualHints.clear();
   }

   public static enum CoordinateRevealType {
      X,
      Y,
      Z,
      RANDOM;
   }

   public static enum VisualHintType {
      NONE,
      TRAIL,
      COMPASS,
      BEACON;
   }
}
