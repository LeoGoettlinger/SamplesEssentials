package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.instanced.MatchDestroyEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.api.mind.EliteMindService;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.ai.MindBehavior;
import com.magmaguy.elitemobs.magmacore.ai.MindContext;
import com.magmaguy.elitemobs.magmacore.ai.MindControl;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.ai.route.CurvedRoute;
import com.magmaguy.elitemobs.magmacore.ai.route.RouteFlight;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.InternalMindActorSpawnRequest;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class TransportModule implements Listener, AutoCloseable {
   private static TransportModule instance;
   final TransportFiles files;
   private final Plugin plugin;
   private final Map<UUID, TransportModule.Journey> journeys = new HashMap<>();
   private final BukkitTask task;
   private final TransportEditor editor;
   private final TransportMenu menu;
   private volatile boolean closed;

   private TransportModule(Plugin plugin) throws Exception {
      this.plugin = plugin;
      this.files = new TransportFiles(plugin.getDataFolder().toPath(), plugin.getLogger());
      this.editor = new TransportEditor(this, plugin);
      this.menu = new TransportMenu(this, plugin);
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
   }

   public static void initialize() {
      shutdown();

      try {
         instance = new TransportModule(MetadataHandler.PLUGIN);
      } catch (Exception var1) {
         MetadataHandler.PLUGIN.getLogger().severe("Transport unavailable: " + var1);
      }
   }

   public static void shutdown() {
      if (instance != null) {
         instance.close();
      }

      instance = null;
   }

   public static TransportModule get() {
      return instance;
   }

   public static boolean isInTransit(Player player) {
      return instance != null && instance.riding(player);
   }

   public TransportEditor editor() {
      return this.editor;
   }

   public void openDestinations(Player player, NPCEntity npc) {
      this.menu.open(player, npc);
   }

   public static boolean startRoute(Player player, String routeId) {
      if (instance == null) {
         message(player, "Transport is unavailable.");
         return false;
      } else {
         return instance.start(player, instance.files.get(routeId));
      }
   }

   public boolean start(Player player, TransportRoute route) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Transport starts on the server thread");
      } else if (this.closed || !player.isOnline() || player.isDead() || this.journeys.containsKey(player.getUniqueId())) {
         message(player, "You cannot board right now.");
         return false;
      } else if (route == null) {
         message(player, "Unknown transport route.");
         return false;
      } else {
         try {
            if (Bukkit.getServicesManager().load(EliteMindService.class) == null) {
               throw new IllegalStateException("Native flying Minds are unavailable on this server version.");
            } else {
               requireTransportEntity(route);
               World world = resolveWorld(player, route);
               CurvedRoute curve = route.curve();
               MatchInstance match = PlayerData.getMatchInstance(player);
               TransportModule.Journey journey = new TransportModule.Journey(player, route, world, curve, match);
               this.journeys.put(player.getUniqueId(), journey);

               try {
                  this.prepareMount(journey);
               } catch (LinkageError | Exception var8) {
                  this.finish(journey, false, null);
                  throw var8;
               }

               message(player, "Stand still. Departing for " + route.name() + " in " + route.countdown() / 20.0 + " seconds.");
               return true;
            }
         } catch (LinkageError | Exception var9) {
            message(player, var9.getMessage());
            return false;
         }
      }
   }

   static World resolveWorld(Player player, TransportRoute route) {
      if (PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon
         && route.world().equals(dungeon.getContentPackagesConfigFields().getWorldName())
         && player.getWorld().equals(dungeon.getWorld())) {
         return dungeon.getWorld();
      } else if (!player.getWorld().getName().equals(route.world())) {
         throw new IllegalArgumentException("This route belongs to another world.");
      } else {
         return player.getWorld();
      }
   }

   private void tick() {
      for (TransportModule.Journey j : List.copyOf(this.journeys.values())) {
         try {
            if (j.player.isOnline()
               && !j.player.isDead()
               && j.player.getWorld() == j.world
               && PlayerData.getMatchInstance(j.player) == j.match
               && (j.match == null || !j.match.isDefunct())) {
               if (!j.actor.getLivingEntity().isValid()) {
                  this.finish(j, false, "Travel interrupted.");
               } else if (!j.boarded) {
                  if (++j.charge >= j.route.countdown()) {
                     this.board(j);
                  }
               } else if (j.player.getVehicle() != j.actor.getLivingEntity()) {
                  this.finish(j, false, "Travel interrupted.");
               } else if (j.status != RouteFlight.Status.FLYING) {
                  this.finish(
                     j,
                     j.status == RouteFlight.Status.ARRIVED,
                     j.status == RouteFlight.Status.ARRIVED ? "Arrived at " + j.route.name() + "." : "The mount does not support flight."
                  );
               }
            } else {
               this.finish(j, false, null);
            }
         } catch (LinkageError | Exception var4) {
            this.plugin.getLogger().log(Level.WARNING, "Transport " + j.route.id() + " failed", (Throwable)var4);
            this.finish(j, false, "Travel failed: " + var4.getMessage());
         }
      }
   }

   private void prepareMount(final TransportModule.Journey j) throws Exception {
      j.flight = new RouteFlight(j.curve, j.route.speed(), j.route.acceleration());
      MindProgram program = MindProgram.builder("elitemobs:transport/" + j.route.id(), 1L).behavior(new MindBehavior() {
         @Override
         public String identifier() {
            return "elitemobs:transport/flight";
         }

         @Override
         public Set<MindControl> controls() {
            return Set.of(MindControl.MOVE, MindControl.LOOK);
         }

         @Override
         public boolean canStart(MindContext context) {
            return TransportModule.this.journeys.get(j.player.getUniqueId()) == j && j.boarded;
         }

         @Override
         public void tick(MindContext context) {
            j.status = j.flight.tick(context);
         }
      }).build();
      CustomBossesConfigFields entityConfig = requireTransportEntity(j.route);
      EliteMindBodyProfile profile = new EliteMindBodyProfile(
         EliteMindBodyLocomotion.FLYING, entityConfig.getScale(), true, entityConfig.getEntityType().getKey()
      );
      j.actor = EliteMindServiceModule.spawnInternal(
            new InternalMindActorSpawnRequest(this.plugin, j.player.getUniqueId(), j.curve.at(0.0).toLocation(j.world), 1, profile, program, actor -> {
               LivingEntity body = actor.getLivingEntity();
               body.setPersistent(false);
               body.setRemoveWhenFarAway(false);
               body.setCanPickupItems(false);
               body.setInvulnerable(true);
               body.setGravity(false);
            }, entityConfig)
         )
         .orElseThrow(() -> new IllegalStateException("Native flying Minds are unavailable on this server version"));
   }

   private void board(TransportModule.Journey j) throws Exception {
      LivingEntity body = j.actor.getLivingEntity();
      j.player.leaveVehicle();
      if (body.addPassenger(j.player) && j.player.getVehicle() == body) {
         j.boarded = true;
         j.player.setFallDistance(0.0F);
         j.world.playSound(body.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0F, 1.0F);
      } else {
         throw new IllegalStateException("Could not seat the player on the mount");
      }
   }

   private void finish(TransportModule.Journey j, boolean arrived, String reason) {
      if (this.journeys.remove(j.player.getUniqueId(), j)) {
         try {
            if (j.actor != null) {
               try {
                  if (j.player.getVehicle() == j.actor.getLivingEntity()) {
                     j.player.leaveVehicle();
                  }
               } finally {
                  EliteMindServiceModule.clearInternal(j.actor);
               }
            }

            if (arrived && j.player.isOnline() && !j.player.isDead()) {
               boolean accepted = InstancePlayerMovement.teleportWithinWorld(j.player, j.destination, TeleportCause.PLUGIN);
               Location actual = j.player.getLocation();
               boolean placed = accepted && actual.getWorld().equals(j.destination.getWorld()) && actual.distanceSquared(j.destination) < 1.0E-6;
               if (placed) {
                  j.player.setFallDistance(0.0F);
               } else {
                  reason = "Travel interrupted. The final destination could not be reached.";
               }
            } else if (arrived) {
               reason = "Travel interrupted.";
            }

            if (reason != null && j.player.isOnline()) {
               message(j.player, reason);
            }
         } catch (Exception var10) {
            this.plugin.getLogger().warning("Transport cleanup: " + var10);
            if (j.player.isOnline()) {
               message(j.player, "Travel interrupted.");
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void moveDuringCountdown(PlayerMoveEvent event) {
      TransportModule.Journey journey = this.journeys.get(event.getPlayer().getUniqueId());
      if (journey != null && !journey.boarded && event.getTo() != null) {
         Location from = event.getFrom();
         Location to = event.getTo();
         if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            this.finish(journey, false, "Departure cancelled because you moved.");
         }
      }
   }

   @EventHandler
   public void quit(PlayerQuitEvent event) {
      TransportModule.Journey j = this.journeys.get(event.getPlayer().getUniqueId());
      if (j != null) {
         this.finish(j, false, null);
      }
   }

   @EventHandler
   public void leave(MatchLeaveEvent event) {
      TransportModule.Journey j = this.journeys.get(event.getPlayer().getUniqueId());
      if (j != null) {
         this.finish(j, false, null);
      }
   }

   @EventHandler
   public void destroy(MatchDestroyEvent event) {
      for (TransportModule.Journey j : List.copyOf(this.journeys.values())) {
         if (j.match == event.getInstance()) {
            this.finish(j, false, null);
         }
      }
   }

   private boolean riding(Player player) {
      TransportModule.Journey j = this.journeys.get(player.getUniqueId());
      return j != null && j.boarded;
   }

   public void cancel(Player player) {
      TransportModule.Journey j = this.journeys.get(player.getUniqueId());
      if (j != null) {
         this.finish(j, false, "Travel cancelled.");
      }
   }

   @Override
   public void close() {
      for (TransportModule.Journey j : List.copyOf(this.journeys.values())) {
         this.finish(j, false, null);
      }

      this.closed = true;
      this.task.cancel();
      this.editor.close();
      this.menu.close();
      HandlerList.unregisterAll(this);
   }

   static void message(Player player, String text) {
      player.sendMessage(ChatColorConverter.convert("&6[Travel] &f" + text));
   }

   private static CustomBossesConfigFields requireTransportEntity(TransportRoute route) {
      CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(route.transportEntity());
      if (config != null && config.isEnabled()) {
         return config;
      } else {
         throw new IllegalArgumentException("Transport entity is missing or disabled: " + route.transportEntity());
      }
   }

   private static final class Journey {
      final Player player;
      final TransportRoute route;
      final World world;
      final CurvedRoute curve;
      final MatchInstance match;
      final Location destination;
      EliteEntity actor;
      RouteFlight flight;
      RouteFlight.Status status = RouteFlight.Status.FLYING;
      boolean boarded;
      int charge;

      Journey(Player player, TransportRoute route, World world, CurvedRoute curve, MatchInstance match) {
         this.player = player;
         this.route = route;
         this.world = world;
         this.curve = curve;
         this.match = match;
         this.destination = curve.at(curve.length()).toLocation(world);
         this.destination.setYaw(route.arrivalYaw());
      }
   }
}
