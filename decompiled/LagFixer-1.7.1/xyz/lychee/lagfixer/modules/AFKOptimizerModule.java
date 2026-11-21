package xyz.lychee.lagfixer.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.hooks.PacketEventsHook;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.objects.ISupportNms;

public class AFKOptimizerModule extends AbstractModule implements Listener, Runnable {
   private final Map<UUID, AFKOptimizerModule.AfkPlayer> afk_players = new ConcurrentHashMap<>();
   private BukkitTask task;
   private int afk_check_interval;
   private int afk_check_mode;
   private boolean kick_afk_players_enabled;
   private int kick_afk_time;
   private boolean hide_entities;
   private int hide_afk_time;
   private int hide_distance_search;
   private boolean hide_players;
   private boolean teleport_afk_players;
   private int teleport_afk_time;
   private boolean teleport_back;
   private Location teleport_location;
   private boolean optimize_packets;
   private Map<String, Integer> cancelled_packets;

   public AFKOptimizerModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.MEDIUM,
         "AFKOptimizer",
         new String[]{
            "Identifies and manages AFK (Away From Keyboard) players to reduce server load.",
            "Useful for servers with many inactive players.",
            "Helps maintain optimal server performance by minimizing resource usage from AFK players."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5ZThhOWU1MTY5NTYxNTdmZTQ0MjIzOTU4NWE2NzY1OGI0MzM3ZjRjYzE5ZDkxN2ZhZjk1NDQxOWY4MTcwZiJ9fX0="
      );
   }

   @EventHandler
   public void onQuit(@NotNull PlayerQuitEvent event) {
      this.afk_players.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onJoin(@NotNull PlayerJoinEvent event) {
      this.afk_players.put(event.getPlayer().getUniqueId(), new AFKOptimizerModule.AfkPlayer(event.getPlayer()));
   }

   @Override
   public void run() {
      for (AFKOptimizerModule.AfkPlayer afkPlayer : this.afk_players.values()) {
         Player player = afkPlayer.getPlayer();
         if (player.isOnline() && this.canContinue(player.getWorld())) {
            Location lastLocation = afkPlayer.getLocation();
            Location currentLocation = player.getLocation();
            afkPlayer.setRotationAfk(currentLocation.getPitch() == lastLocation.getPitch() && currentLocation.getYaw() == lastLocation.getYaw());
            afkPlayer.setPositionAfk(
               lastLocation.getBlockX() == currentLocation.getBlockX()
                  && lastLocation.getBlockY() == currentLocation.getBlockY()
                  && lastLocation.getBlockZ() == currentLocation.getBlockZ()
            );

            boolean isAfk = switch (this.afk_check_mode) {
               case 0 -> afkPlayer.isPositionAfk() || afkPlayer.isRotationAfk();
               case 1 -> afkPlayer.isPositionAfk() && afkPlayer.isRotationAfk();
               case 2 -> afkPlayer.isPositionAfk();
               case 3 -> afkPlayer.isRotationAfk();
               default -> false;
            };
            if (isAfk) {
               afkPlayer.getAfkTime().add(this.afk_check_interval);
               long afkTime = afkPlayer.getAfkTime().longValue();
               if (this.kick_afk_players_enabled && afkTime > this.kick_afk_time) {
                  String kickMessage = this.getLanguage().getString("afk_kick", true);
                  SupportManager.getInstance().getFork().runNow(false, currentLocation, () -> {
                     if (player.isOnline()) {
                        player.kickPlayer(kickMessage);
                     }
                  });
               } else if (this.teleport_afk_players && afkTime > this.teleport_afk_time && this.teleport_location != null) {
                  if (!Objects.equals(currentLocation.getWorld(), this.teleport_location.getWorld())) {
                     SupportManager.getInstance().getFork().runNow(false, currentLocation, () -> {
                        if (player.isOnline()) {
                           if (this.teleport_back) {
                              afkPlayer.setBackLocation(currentLocation);
                           }

                           player.teleport(this.teleport_location);
                        }
                     });
                  }
               } else if (this.hide_entities && afkTime > this.hide_afk_time && afkPlayer.getHiddenEntities().isEmpty()) {
                  SupportManager.getInstance().getFork().runNow(false, currentLocation, () -> {
                     if (player.isOnline()) {
                        if (this.hide_players) {
                           for (Player o : Bukkit.getOnlinePlayers()) {
                              if (!o.equals(player) && afkPlayer.getHiddenEntities().add(o)) {
                                 player.hidePlayer(this.getPlugin(), o);
                              }
                           }
                        }

                        List<Entity> entitiesToHide = player.getNearbyEntities(this.hide_distance_search, this.hide_distance_search, this.hide_distance_search);
                        if (!entitiesToHide.isEmpty()) {
                           ISupportNms nms = SupportManager.getInstance().getNms();
                           entitiesToHide.forEach(entity -> {
                              if (!(entity instanceof Player) && afkPlayer.getHiddenEntities().add(entity)) {
                                 nms.hideEntity(this.getPlugin(), player, entity);
                              }
                           });
                        }
                     }
                  });
               }
            } else {
               afkPlayer.getAfkTime().reset();
               if (this.teleport_back && afkPlayer.getBackLocation() != null) {
                  SupportManager.getInstance().getFork().runNow(false, currentLocation, () -> {
                     if (player.isOnline()) {
                        player.teleport(afkPlayer.getBackLocation());
                        afkPlayer.setBackLocation(null);
                     }
                  });
               } else if (this.hide_entities && !afkPlayer.getHiddenEntities().isEmpty()) {
                  SupportManager.getInstance().getFork().runNow(false, currentLocation, () -> {
                     if (player.isOnline()) {
                        ISupportNms nms = SupportManager.getInstance().getNms();
                        afkPlayer.getHiddenEntities().forEach(entity -> {
                           if (entity instanceof Player targetPlayer) {
                              player.showPlayer(this.getPlugin(), targetPlayer);
                           } else if (entity.isValid() && !entity.isDead()) {
                              nms.showEntity(this.getPlugin(), player, entity);
                           }
                        });
                        afkPlayer.getHiddenEntities().clear();
                     }
                  });
               }
            }

            afkPlayer.setLocation(currentLocation);
         }
      }
   }

   @Override
   public void load() {
      Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

      for (Player player : Bukkit.getOnlinePlayers()) {
         this.afk_players.putIfAbsent(player.getUniqueId(), new AFKOptimizerModule.AfkPlayer(player));
      }

      this.task = SupportManager.getInstance().getFork().runTimer(true, this, this.afk_check_interval, this.afk_check_interval, TimeUnit.MILLISECONDS);
   }

   @Override
   public boolean loadConfig() {
      this.afk_check_interval = Math.max(500, this.getSection().getInt("afk_check.interval"));
      this.afk_check_mode = this.getSection().getInt("afk_check.mode");
      this.kick_afk_players_enabled = this.getSection().getBoolean("kick_afk_players.enabled");
      if (this.kick_afk_players_enabled) {
         this.kick_afk_time = this.getSection().getInt("kick_afk_players.afk_time") * 1000;
      }

      this.hide_entities = this.getSection().getBoolean("hide_entities.enabled");
      if (this.hide_entities) {
         this.hide_afk_time = this.getSection().getInt("hide_entities.afk_time") * 1000;
         this.hide_distance_search = this.getSection().getInt("hide_entities.distance_search");
         this.hide_players = this.getSection().getBoolean("hide_entities.include_players");
      }

      this.teleport_afk_players = this.getSection().getBoolean("teleport_afk_players.enabled");
      if (this.teleport_afk_players) {
         this.teleport_afk_time = this.getSection().getInt("teleport_afk_players.afk_time") * 1000;
         this.teleport_back = this.getSection().getBoolean("teleport_afk_players.teleport_back");
         String worldName = this.getSection().getString("teleport_afk_players.location.world");
         if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
               this.teleport_location = new Location(
                  world,
                  this.getSection().getDouble("teleport_afk_players.location.x"),
                  this.getSection().getDouble("teleport_afk_players.location.y"),
                  this.getSection().getDouble("teleport_afk_players.location.z")
               );
            }
         }
      }

      PacketEventsHook hook = HookManager.getInstance().getHookIfLoaded(PacketEventsHook.class);
      this.cancelled_packets = new HashMap<>();
      if (hook != null) {
         this.optimize_packets = this.getSection().getBoolean("optimize_packets.enabled");
         if (this.optimize_packets) {
            for (String str : this.getSection().getStringList("optimize_packets.cancelled_packets")) {
               int equalSignIndex = str.indexOf(61);
               if (equalSignIndex == -1) {
                  this.getPlugin().getLogger().warning("Skipping malformed packet (no \"=\" found): " + str);
               } else {
                  String packet = str.substring(0, equalSignIndex).toUpperCase();
                  String afkTime = str.substring(equalSignIndex + 1);

                  try {
                     int parsedAfkTime = Integer.parseInt(afkTime);
                     if (parsedAfkTime > 0) {
                        this.cancelled_packets.put(packet, parsedAfkTime * 1000);
                     }
                  } catch (NumberFormatException var8) {
                     this.getPlugin().getLogger().warning("Invalid index format in packet: " + afkTime + " for packet \"" + packet + "\"");
                  }
               }
            }

            hook.register(this);
         } else {
            hook.unregisterAfkOptimizer();
         }
      }

      return true;
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.task != null && !this.task.isCancelled()) {
         this.task.cancel();
      }
   }

   @Generated
   public Map<UUID, AFKOptimizerModule.AfkPlayer> getAfk_players() {
      return this.afk_players;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }

   @Generated
   public int getAfk_check_interval() {
      return this.afk_check_interval;
   }

   @Generated
   public int getAfk_check_mode() {
      return this.afk_check_mode;
   }

   @Generated
   public boolean isKick_afk_players_enabled() {
      return this.kick_afk_players_enabled;
   }

   @Generated
   public int getKick_afk_time() {
      return this.kick_afk_time;
   }

   @Generated
   public boolean isHide_entities() {
      return this.hide_entities;
   }

   @Generated
   public int getHide_afk_time() {
      return this.hide_afk_time;
   }

   @Generated
   public int getHide_distance_search() {
      return this.hide_distance_search;
   }

   @Generated
   public boolean isHide_players() {
      return this.hide_players;
   }

   @Generated
   public boolean isTeleport_afk_players() {
      return this.teleport_afk_players;
   }

   @Generated
   public int getTeleport_afk_time() {
      return this.teleport_afk_time;
   }

   @Generated
   public boolean isTeleport_back() {
      return this.teleport_back;
   }

   @Generated
   public Location getTeleport_location() {
      return this.teleport_location;
   }

   @Generated
   public boolean isOptimize_packets() {
      return this.optimize_packets;
   }

   @Generated
   public Map<String, Integer> getCancelled_packets() {
      return this.cancelled_packets;
   }

   public static class AfkPlayer {
      private final Player player;
      private final LongAdder afkTime = new LongAdder();
      private final Set<Entity> hiddenEntities = new HashSet<>();
      private volatile Location backLocation;
      private volatile Location location;
      private volatile boolean positionAfk;
      private volatile boolean rotationAfk;

      public AfkPlayer(Player player) {
         this.player = player;
         this.location = player.getLocation();
      }

      @Override
      public int hashCode() {
         return this.player.getUniqueId().hashCode();
      }

      @Generated
      public Player getPlayer() {
         return this.player;
      }

      @Generated
      public LongAdder getAfkTime() {
         return this.afkTime;
      }

      @Generated
      public Set<Entity> getHiddenEntities() {
         return this.hiddenEntities;
      }

      @Generated
      public Location getBackLocation() {
         return this.backLocation;
      }

      @Generated
      public Location getLocation() {
         return this.location;
      }

      @Generated
      public boolean isPositionAfk() {
         return this.positionAfk;
      }

      @Generated
      public boolean isRotationAfk() {
         return this.rotationAfk;
      }

      @Generated
      public void setBackLocation(Location backLocation) {
         this.backLocation = backLocation;
      }

      @Generated
      public void setLocation(Location location) {
         this.location = location;
      }

      @Generated
      public void setPositionAfk(boolean positionAfk) {
         this.positionAfk = positionAfk;
      }

      @Generated
      public void setRotationAfk(boolean rotationAfk) {
         this.rotationAfk = rotationAfk;
      }
   }
}
