package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CustomMusic {
   private static final HashMap<UUID, CustomMusic> playerSongSingleton = new HashMap<>();
   private static final HashMap<UUID, CustomMusic> dungeonMusic = new HashMap<>();
   private static final Set<CustomMusic> activeMusic = new HashSet<>();
   private final PlayerTaskRegistry playerTasks = new PlayerTaskRegistry();
   private final CustomMusic.ContentType contentType;
   private CustomBossEntity customBossEntity = null;
   private ContentPackagesConfigFields contentPackagesConfigFields = null;
   private String name;
   private int durationTicks;
   private String name2 = null;
   private int durationTicks2 = -1;
   private BukkitTask bossScannerTask = null;
   private World world;
   private final boolean valid;

   public CustomMusic(String rawString, CustomBossEntity customBossEntity) {
      this.customBossEntity = customBossEntity;
      this.contentType = CustomMusic.ContentType.BOSS;
      this.valid = this.parseTracks(rawString);
   }

   public CustomMusic(String rawString, ContentPackagesConfigFields contentPackagesConfigFields, World world) {
      this.world = world;
      this.contentPackagesConfigFields = contentPackagesConfigFields;
      this.contentType = CustomMusic.ContentType.DUNGEON;
      this.valid = this.parseTracks(rawString);
      if (this.valid) {
         CustomMusic previousMusic = dungeonMusic.put(world.getUID(), this);
         if (previousMusic != null && previousMusic != this) {
            previousMusic.stop();
         }
      }
   }

   public static void shutdown() {
      Set<CustomMusic> musicToStop = new HashSet<>(activeMusic);
      musicToStop.addAll(dungeonMusic.values());
      musicToStop.addAll(playerSongSingleton.values());
      musicToStop.forEach(CustomMusic::stop);
      dungeonMusic.clear();
      playerSongSingleton.clear();
      activeMusic.clear();
   }

   public static void removeDungeonMusic(UUID worldUUID) {
      CustomMusic customMusic = dungeonMusic.remove(worldUUID);
      if (customMusic != null) {
         customMusic.stop();
         customMusic.world = null;
         playerSongSingleton.entrySet().removeIf(entry -> entry.getValue().equals(customMusic));
      }
   }

   private boolean parseTracks(String rawString) {
      try {
         String[] entries = rawString.split("->", -1);
         if (entries.length >= 1 && entries.length <= 2) {
            CustomMusic.Track first = parse(entries[0]);
            CustomMusic.Track second = entries.length == 2 ? parse(entries[1]) : null;
            this.name = first.name();
            this.durationTicks = first.ticks();
            if (second != null) {
               this.name2 = second.name();
               this.durationTicks2 = second.ticks();
            }

            return true;
         } else {
            throw new IllegalArgumentException("Expected one or two tracks");
         }
      } catch (IllegalArgumentException var5) {
         String owner = this.contentType == CustomMusic.ContentType.BOSS
            ? this.customBossEntity.getCustomBossesConfigFields().getFilename()
            : this.contentPackagesConfigFields.getFilename();
         Logger.warn("Disabled invalid music in " + owner + ": " + var5.getMessage());
         return false;
      }
   }

   private static CustomMusic.Track parse(String rawString) {
      String name = null;
      Long milliseconds = null;

      for (String field : rawString.trim().split("\\s+")) {
         String[] parsed = field.split("=", 2);
         if (parsed.length != 2) {
            throw new IllegalArgumentException("Expected name and length fields");
         }

         String var8 = parsed[0];
         switch (var8) {
            case "name":
               if (name != null || parsed[1].isBlank()) {
                  throw new IllegalArgumentException("Invalid track name");
               }

               name = parsed[1];
               break;
            case "length":
               if (milliseconds != null) {
                  throw new IllegalArgumentException("Duplicate track length");
               }

               milliseconds = Long.parseLong(parsed[1]);
               break;
            default:
               throw new IllegalArgumentException("Unknown music field: " + parsed[0]);
         }
      }

      if (name != null && milliseconds != null && milliseconds >= 50L && milliseconds / 50L <= 2147483647L) {
         return new CustomMusic.Track(name, (int)(milliseconds / 50L));
      } else {
         throw new IllegalArgumentException("Each track needs a name and a length of at least 50 milliseconds");
      }
   }

   public void start(final CustomBossEntity customBossEntity) {
      if (this.valid) {
         if (this.bossScannerTask != null) {
            this.bossScannerTask.cancel();
         }

         activeMusic.add(this);
         this.bossScannerTask = (new BukkitRunnable() {
            public void run() {
               if (!customBossEntity.exists()) {
                  CustomMusic.this.stop();
               } else {
                  CustomMusic.this.play(customBossEntity.getLocation(), (double)customBossEntity.getCustomBossesConfigFields().getFollowDistance().intValue());
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 10L);
      }
   }

   public void stop() {
      if (this.bossScannerTask != null) {
         this.bossScannerTask.cancel();
      }

      this.bossScannerTask = null;
      Set<UUID> playerIds = this.playerTasks.playerIds();
      this.playerTasks.cancelAll();

      for (UUID playerId : playerIds) {
         this.stopSounds(playerId);
      }

      playerSongSingleton.entrySet().removeIf(entry -> entry.getValue() == this);
      activeMusic.remove(this);
   }

   private void play(Location location, double range) {
      for (UUID playerId : this.playerTasks.playerIds(PlayerTaskRegistry.Role.PLAYBACK_LOOP)) {
         Player player = Bukkit.getPlayer(playerId);
         if (!this.isPlaybackEligible(player)) {
            this.stopPlayer(playerId, true);
         }
      }

      if (location != null && location.getWorld() != null) {
         for (Player player : location.getWorld().getPlayers()) {
            if (MusicPlaybackEligibility.isWithinRange(player.getLocation(), location, range)) {
               this.beginPlayback(player);
            }
         }
      }
   }

   private void play(Player player) {
      this.beginPlayback(player);
   }

   private void beginPlayback(Player player) {
      if (this.valid && this.isPlaybackEligible(player)) {
         UUID playerId = player.getUniqueId();
         CustomMusic currentMusic = playerSongSingleton.get(playerId);
         if (currentMusic != null && currentMusic != this) {
            if (currentMusic.isPlaybackEligible(player)
               && currentMusic.playerTasks.contains(playerId, PlayerTaskRegistry.Role.PLAYBACK_LOOP)
               && (this.contentType == currentMusic.contentType || this.contentType == CustomMusic.ContentType.DUNGEON)) {
               return;
            }

            currentMusic.stopPlayer(playerId, true);
         }

         if (!this.playerTasks.contains(playerId, PlayerTaskRegistry.Role.PLAYBACK_LOOP)) {
            this.playerTasks.cancel(playerId, PlayerTaskRegistry.Role.DELAYED_START);
            playerSongSingleton.put(playerId, this);
            player.playSound(player.getLocation(), this.name, SoundCategory.MUSIC, 1.0F, 1.0F);
            this.startLoopingTask(playerId);
         }
      }
   }

   private void startLoopingTask(final UUID playerId) {
      final String loopingTrack = this.name2 == null ? this.name : this.name2;
      long initialDelay = this.durationTicks;
      long period = this.name2 == null ? this.durationTicks : this.durationTicks2;
      BukkitTask task = (new BukkitRunnable() {
         public void run() {
            Player player = Bukkit.getPlayer(playerId);
            if (CustomMusic.playerSongSingleton.get(playerId) == CustomMusic.this && CustomMusic.this.isPlaybackEligible(player)) {
               player.playSound(player.getLocation(), loopingTrack, SoundCategory.MUSIC, 1.0F, 1.0F);
            } else {
               CustomMusic.this.stopPlayer(playerId, true);
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, initialDelay, period);
      this.playerTasks.replace(playerId, PlayerTaskRegistry.Role.PLAYBACK_LOOP, task::cancel);
      activeMusic.add(this);
   }

   private boolean isPlaybackEligible(Player player) {
      if (player == null || !player.isOnline()) {
         return false;
      } else if (this.contentType != CustomMusic.ContentType.DUNGEON) {
         return this.customBossEntity != null && this.customBossEntity.exists() && this.customBossEntity.getLivingEntity() != null
            ? MusicPlaybackEligibility.isWithinRange(
               player.getLocation(), this.customBossEntity.getLivingEntity().getLocation(), this.customBossEntity.getFollowDistance() * 1.5
            )
            : false;
      } else {
         return this.world != null && player.getWorld().equals(this.world);
      }
   }

   private void stopPlayer(UUID playerId, boolean stopSound) {
      this.playerTasks.cancelPlayer(playerId);
      playerSongSingleton.remove(playerId, this);
      if (stopSound) {
         this.stopSounds(playerId);
      }
   }

   private void stopSounds(UUID playerId) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null) {
         player.stopSound(this.name);
         if (this.name2 != null) {
            player.stopSound(this.name2);
         }
      }
   }

   private void schedulePlay(final UUID playerId) {
      this.playerTasks.cancel(playerId, PlayerTaskRegistry.Role.DELAYED_START);
      BukkitTask task = (new BukkitRunnable() {
         public void run() {
            CustomMusic.this.playerTasks.release(playerId, PlayerTaskRegistry.Role.DELAYED_START);
            CustomMusic.this.play(Bukkit.getPlayer(playerId));
         }
      }).runTaskLater(MetadataHandler.PLUGIN, 20L);
      this.playerTasks.replace(playerId, PlayerTaskRegistry.Role.DELAYED_START, task::cancel);
      activeMusic.add(this);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getDurationTicks() {
      return this.durationTicks;
   }

   @Generated
   public String getName2() {
      return this.name2;
   }

   @Generated
   public int getDurationTicks2() {
      return this.durationTicks2;
   }

   private static enum ContentType {
      BOSS,
      DUNGEON;
   }

   public static class CustomMusicEvents implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onTeleport(PlayerTeleportEvent event) {
         if (event.getDestination() != null && event.getDestination().getWorld() != null) {
            UUID playerId = event.getPlayer().getUniqueId();
            CustomMusic currentMusic = CustomMusic.playerSongSingleton.get(playerId);
            if (currentMusic != null
               && currentMusic.contentType == CustomMusic.ContentType.DUNGEON
               && !event.getDestination().getWorld().equals(currentMusic.world)) {
               currentMusic.stopPlayer(playerId, true);
            }

            EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getDestination().getWorld().getUID());
            if (eliteMobsWorld != null && eliteMobsWorld.getContentPackagesConfigFields().getSong() != null) {
               CustomMusic customMusic = CustomMusic.dungeonMusic.get(event.getDestination().getWorld().getUID());
               if (customMusic == null) {
                  Logger.warn("Failed to get custom music for " + event.getDestination().getWorld().getName());
               } else {
                  customMusic.schedulePlay(playerId);
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onLogin(PlayerJoinEvent event) {
         EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getPlayer().getWorld().getUID());
         if (eliteMobsWorld != null && eliteMobsWorld.getContentPackagesConfigFields().getSong() != null) {
            CustomMusic customMusic = CustomMusic.dungeonMusic.get(event.getPlayer().getWorld().getUID());
            if (customMusic == null) {
               Logger.warn("Failed to get custom music for " + event.getPlayer().getWorld().getName());
            } else {
               customMusic.schedulePlay(event.getPlayer().getUniqueId());
            }
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onQuit(PlayerQuitEvent event) {
         UUID playerId = event.getPlayer().getUniqueId();

         for (CustomMusic customMusic : new HashSet<>(CustomMusic.activeMusic)) {
            customMusic.stopPlayer(playerId, false);
         }
      }
   }

   private record Track(String name, int ticks) {
   }
}
