package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationEngine;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class PatrolEditor implements Listener {
   private static final Map<UUID, PatrolEditor.Session> SESSIONS = new HashMap<>();
   private static final Map<Object, UUID> EDITORS = new IdentityHashMap<>();
   private static PatrolEditor listener;
   private static BukkitTask previewTask;
   private static int previewTicks;

   private PatrolEditor() {
   }

   public static void initialize() {
      shutdown();
      listener = new PatrolEditor();
      Bukkit.getPluginManager().registerEvents(listener, MetadataHandler.PLUGIN);
      previewTicks = 0;
      previewTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, PatrolEditor::preview, 1L, 1L);
   }

   public static void shutdown() {
      for (PatrolEditor.Session session : new ArrayList<>(SESSIONS.values())) {
         session.close(true);
      }

      SESSIONS.clear();
      EDITORS.clear();
      if (previewTask != null) {
         previewTask.cancel();
      }

      previewTask = null;
      if (listener != null) {
         HandlerList.unregisterAll(listener);
      }

      listener = null;
   }

   public static boolean isEditing(NPCEntity npc) {
      return EDITORS.containsKey(npc);
   }

   public static void edit(Player player) {
      cancel(player, false);
      Object owner = targetedOwner(player);
      if (owner == null) {
         message(player, "Look directly at a spawned custom boss or NPC within 12 blocks.");
      } else if (EDITORS.putIfAbsent(owner, player.getUniqueId()) != null) {
         message(player, "Another administrator is already editing that actor.");
      } else {
         PatrolEditor.Session session = null;

         try {
            session = PatrolEditor.Session.create(player, owner);
            PatrolService.pause(owner);
            SESSIONS.put(player.getUniqueId(), session);
         } catch (Error | RuntimeException var5) {
            EDITORS.remove(owner, player.getUniqueId());
            if (session != null) {
               session.close(true);
            }

            if (var5 instanceof Error error) {
               throw error;
            }

            message(player, rootMessage(var5));
            return;
         }

         message(
            player,
            "Editing "
               + session.filename()
               + " with "
               + session.nodes.size()
               + " node(s), mode "
               + session.mode
               + ", "
               + (session.relative ? "relative" : "absolute")
               + " coordinates. Use /em patrol add at each waypoint."
         );
      }
   }

   public static void add(Player player) {
      PatrolEditor.Session session = session(player);
      if (session != null) {
         Location waypoint = player.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
         if (waypoint.getWorld() != session.origin.getWorld()) {
            message(player, "Waypoints must be in the actor's authored world.");
         } else if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.doEliteMobsSpawnFlag(waypoint)) {
            message(player, "That waypoint is inside an elitemob-spawning deny region.");
         } else {
            Vector node = waypoint.toVector();
            if (session.relative) {
               node.subtract(session.origin.toVector());
            }

            Vector previous = session.nodes.getLast();
            double length = previous.distance(node);
            if (length <= 1.0E-4) {
               message(player, "That waypoint duplicates the previous node.");
            } else {
               session.snapshot();
               session.nodes.add(node);
               session.driver.moveTo(waypoint, session.speedModifier);
               message(player, "Added node " + (session.nodes.size() - 1) + " at " + vector(node) + "; the route solver is resolving the walk asynchronously.");
            }
         }
      }
   }

   public static void remove(Player player) {
      PatrolEditor.Session session = session(player);
      if (session != null) {
         if (session.nodes.size() <= 1) {
            message(player, "A route must retain its first node.");
         } else {
            session.snapshot();
            Vector removed = session.nodes.removeLast();
            message(player, "Removed node " + vector(removed) + ".");
         }
      }
   }

   public static void undo(Player player) {
      PatrolEditor.Session session = session(player);
      if (session != null) {
         PatrolEditor.Snapshot snapshot = session.undo.pollFirst();
         if (snapshot == null) {
            message(player, "Nothing to undo.");
         } else {
            session.nodes.clear();
            snapshot.nodes.forEach(node -> session.nodes.add(node.clone()));
            session.mode = snapshot.mode;
            message(player, "Restored the previous route state.");
         }
      }
   }

   public static void mode(Player player, String rawMode) {
      PatrolEditor.Session session = session(player);
      if (session != null) {
         PatrolMode mode;
         try {
            mode = PatrolMode.parse(rawMode);
         } catch (IllegalArgumentException var5) {
            message(player, "Mode must be LOOP or REVERSE.");
            return;
         }

         if (mode != session.mode) {
            session.snapshot();
            session.mode = mode;
            message(player, "Patrol mode set to " + mode + ".");
         }
      }
   }

   public static void save(Player player) {
      PatrolEditor.Session session = session(player);
      if (session != null) {
         PatrolRoute route;
         try {
            route = session.validate();
            session.persist(route);
         } catch (Exception var4) {
            message(player, "Could not save patrol: " + rootMessage(var4));
            return;
         }

         SESSIONS.remove(player.getUniqueId());
         session.close(false);
         PatrolService.refresh(session.owner);
         message(player, "Saved " + route.size() + " patrol nodes to " + session.filename() + " and refreshed the live actor.");
      }
   }

   public static void cancel(Player player) {
      cancel(player, true);
   }

   public static void status(Player player) {
      PatrolEditor.Session session = SESSIONS.get(player.getUniqueId());
      if (session != null) {
         message(
            player,
            session.filename()
               + ": editing "
               + session.nodes.size()
               + " nodes, mode "
               + session.mode
               + ", "
               + (session.relative ? "relative" : "absolute")
               + " coordinates, driver "
               + session.driver.status()
               + "."
         );
      } else {
         Object owner = targetedOwner(player);
         if (owner == null) {
            message(player, "No edit session; look at a patrolling actor to inspect it.");
         } else {
            Map<String, Object> diagnostics = PatrolService.diagnostics(owner);
            message(player, diagnostics.isEmpty() ? "That actor has no active patrol controller." : diagnostics.toString());
         }
      }
   }

   private static void cancel(Player player, boolean notify) {
      PatrolEditor.Session session = SESSIONS.remove(player.getUniqueId());
      if (session == null) {
         if (notify) {
            message(player, "No patrol edit session is active.");
         }
      } else {
         session.close(true);
         if (notify) {
            message(player, "Patrol edit cancelled; no route changes were written.");
         }
      }
   }

   private static PatrolEditor.Session session(Player player) {
      PatrolEditor.Session session = SESSIONS.get(player.getUniqueId());
      if (session == null) {
         message(player, "Start with /em patrol edit while looking at an actor.");
      }

      return session;
   }

   private static Object targetedOwner(Player player) {
      RayTraceResult trace = player.getWorld()
         .rayTraceEntities(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            12.0,
            0.5,
            entity -> entity != player && (EntityTracker.getNPCEntity(entity) != null || EntityTracker.getEliteMobEntity(entity) instanceof CustomBossEntity)
         );
      Entity target = trace == null ? null : trace.getHitEntity();
      if (target == null) {
         return null;
      } else {
         NPCEntity npc = EntityTracker.getNPCEntity(target);
         if (npc != null) {
            return npc;
         } else {
            return EntityTracker.getEliteMobEntity(target) instanceof CustomBossEntity customBoss ? customBoss : null;
         }
      }
   }

   private static void preview() {
      boolean renderRoute = ++previewTicks % 10 == 0;

      for (PatrolEditor.Session session : new ArrayList<>(SESSIONS.values())) {
         Player player = Bukkit.getPlayer(session.playerId);
         LivingEntity body = session.body();
         if (player != null && player.isOnline() && body != null && body.isValid() && body.getUniqueId().equals(session.bodyId)) {
            if (renderRoute) {
               session.render(player);
            }

            PathfindingStatus current = session.driver.status();
            if (current != session.lastReportedStatus
               && (current == PathfindingStatus.NO_PATH || current == PathfindingStatus.ENDED_SHORT || current == PathfindingStatus.STUCK)) {
               message(player, "The puppet could not complete the latest leg (" + current + ").");
            }

            session.lastReportedStatus = current;
         } else {
            SESSIONS.remove(session.playerId, session);
            session.close(true);
            if (player != null && player.isOnline()) {
               message(player, "Patrol edit cancelled because the puppet body changed.");
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void protectPuppet(EntityDamageEvent event) {
      for (PatrolEditor.Session session : SESSIONS.values()) {
         LivingEntity body = session.body();
         if (body != null && body.getUniqueId().equals(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
            return;
         }
      }
   }

   @EventHandler
   public void quit(PlayerQuitEvent event) {
      cancel(event.getPlayer(), false);
   }

   @EventHandler
   public void die(PlayerDeathEvent event) {
      cancel(event.getEntity(), false);
   }

   @EventHandler
   public void changeWorld(PlayerChangedWorldEvent event) {
      cancel(event.getPlayer(), false);
   }

   private static void message(Player player, String message) {
      Logger.sendMessage(player, "&8[&6Patrol&8] &f" + message);
   }

   private static String vector(Vector vector) {
      return format(vector.getX()) + "," + format(vector.getY()) + "," + format(vector.getZ());
   }

   private static String format(double value) {
      return Math.rint(value) == value ? Long.toString((long)value) : String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
   }

   private static String rootMessage(Throwable throwable) {
      Throwable current = throwable;

      while (current.getCause() != null) {
         current = current.getCause();
      }

      return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
   }

   private static final class Session {
      private final UUID playerId;
      private final Object owner;
      private final Location origin;
      private final boolean relative;
      private final double speedModifier;
      private final Double virtualSpeed;
      private final double startNodeWaitSeconds;
      private final boolean originalAi;
      private final boolean originalAware;
      private boolean closed;
      private final boolean hadPatrol;
      private final UUID bodyId;
      private final PathfindingHandle driver;
      private final Deque<PatrolEditor.Snapshot> undo = new ArrayDeque<>();
      private final List<Vector> nodes = new ArrayList<>();
      private final Map<Location, BlockData> bedrockPreview = new LinkedHashMap<>();
      private PatrolMode mode;
      private PathfindingStatus lastReportedStatus = PathfindingStatus.IDLE;

      private Session(
         Player player, Object owner, Location origin, PatrolRoute existing, UUID bodyId, PathfindingHandle driver, boolean originalAi, boolean originalAware
      ) {
         this.playerId = player.getUniqueId();
         this.owner = owner;
         this.origin = origin.clone();
         this.relative = existing == null || existing.relative();
         this.speedModifier = existing == null ? 1.0 : existing.speedModifier();
         this.virtualSpeed = existing == null ? null : existing.virtualSpeed();
         this.startNodeWaitSeconds = existing == null ? 0.0 : existing.startNodeWaitSeconds();
         this.originalAi = originalAi;
         this.originalAware = originalAware;
         this.hadPatrol = existing != null;
         this.bodyId = bodyId;
         this.driver = driver;
         this.mode = existing == null ? PatrolMode.LOOP : existing.mode();
         if (existing == null) {
            this.nodes.add(new Vector(0.0, 0.0, 0.0));
         } else {
            existing.nodes().forEach(node -> this.nodes.add(node.clone()));
         }
      }

      private static PatrolEditor.Session create(Player player, Object owner) {
         LivingEntity body;
         Location origin;
         PatrolRoute route;
         if (owner instanceof NPCEntity npc) {
            body = npc.getVillager();
            origin = npc.getSpawnLocation();
            route = npc.getNPCsConfigFields().getPatrolRoute();
            if (npc.getLocationString() == null) {
               throw new IllegalArgumentException("Runtime-only NPCs cannot be used as persistent route templates.");
            }

            if (body != null && body.isInsideVehicle()) {
               throw new IllegalArgumentException("A passenger cannot own a patrol; route its mount instead.");
            }
         } else {
            if (!(owner instanceof CustomBossEntity boss)) {
               throw new IllegalArgumentException("Unsupported patrol actor.");
            }

            body = boss.getLivingEntity();
            origin = boss.getSpawnLocation();
            route = boss.getCustomBossesConfigFields().getPatrolRoute();
            if (!boss.getCustomBossesConfigFields().isAi()) {
               throw new IllegalArgumentException("This boss has ai: false and cannot patrol.");
            }

            if (boss.getMovementSpeedAttribute() <= 0.0) {
               throw new IllegalArgumentException("This boss has zero movement speed and cannot patrol.");
            }

            if (body != null && body.isInsideVehicle()) {
               throw new IllegalArgumentException("A passenger cannot own a patrol; route its mount instead.");
            }
         }

         if (body == null || !body.isValid()) {
            throw new IllegalArgumentException("The targeted actor has no live body.");
         } else if (origin == null || origin.getWorld() == null) {
            throw new IllegalArgumentException("The actor has no authored origin.");
         } else if (NMSManager.getAdapter() == null) {
            throw new IllegalArgumentException("No MagmaCore NMS adapter is active on this server version.");
         } else {
            PathfindingHandle driver = NMSManager.getAdapter()
               .createPathfindingHandle(body)
               .orElseThrow(() -> new IllegalArgumentException("The targeted body has no native pathfinding handle."));
            boolean originalAi = body.hasAI();
            boolean originalAware = !(body instanceof Mob mob && !mob.isAware());

            try {
               if (owner instanceof NPCEntity && body instanceof Mob mobx) {
                  mobx.setAI(true);
                  mobx.setAware(true);
                  if (!NMSManager.getAdapter().removeFreeWill(body)) {
                     throw new IllegalArgumentException("This NPC body cannot be prepared for native pathfinding.");
                  }
               }

               return new PatrolEditor.Session(player, owner, origin, route, body.getUniqueId(), driver, originalAi, originalAware);
            } catch (Error | RuntimeException var11) {
               driver.close();
               body.setAI(originalAi);
               if (body instanceof Mob mobxx) {
                  mobxx.setAware(originalAware);
               }

               throw var11;
            }
         }
      }

      private LivingEntity body() {
         if (this.owner instanceof NPCEntity npc) {
            return npc.getVillager();
         } else {
            return this.owner instanceof CustomBossEntity boss ? boss.getLivingEntity() : null;
         }
      }

      private String filename() {
         return this.owner instanceof NPCEntity npc
            ? npc.getNPCsConfigFields().getFilename()
            : ((CustomBossEntity)this.owner).getCustomBossesConfigFields().getFilename();
      }

      private void snapshot() {
         List<Vector> copy = this.nodes.stream().<Vector>map(Vector::clone).toList();
         this.undo.addFirst(new PatrolEditor.Snapshot(copy, this.mode));

         while (this.undo.size() > 32) {
            this.undo.removeLast();
         }
      }

      private PatrolRoute validate() {
         YamlConfiguration validation = new YamlConfiguration();
         validation.set("patrol.enabled", true);
         validation.set("patrol.mode", this.mode.name());
         validation.set("patrol.relative", this.relative);
         validation.set("patrol.speed", this.speedModifier);
         validation.set("patrol.virtualSpeed", this.virtualSpeed);
         validation.set("patrol.startNodeWaitSeconds", this.startNodeWaitSeconds);
         validation.set("patrol.nodes", this.nodes.stream().map(node -> Double.toString(node.getX()) + "," + node.getY() + "," + node.getZ()).toList());
         return Objects.requireNonNull(PatrolRoute.parse(validation));
      }

      private void persist(PatrolRoute route) throws IOException {
         if (this.owner instanceof NPCEntity npc) {
            this.persistNpc(npc, route);
         } else {
            this.persistBoss((CustomBossEntity)this.owner, route);
         }
      }

      private void persistNpc(NPCEntity npc, PatrolRoute route) throws IOException {
         NPCsConfigFields fields = npc.getNPCsConfigFields();
         FileConfiguration raw = fields.getWritableFileConfiguration();
         YamlConfiguration prepared = detached(raw);
         List<String> locations = new ArrayList<>(raw.getStringList("spawnLocations"));
         if (locations.size() <= 1) {
            applyRoute(prepared, route);
            saveAtomic(prepared, fields.getFile());
            applyRoute(raw, route);
            fields.setPatrolRoute(route);
         } else {
            String selected = npc.getLocationString();
            if (!locations.remove(selected)) {
               throw new IOException("target NPC location is not present in spawnLocations");
            } else {
               File leafFile = nextLeaf(fields.getFile());
               YamlConfiguration leaf = new YamlConfiguration();
               leaf.set("extends", fields.getFilename());
               leaf.set("isEnabled", true);
               leaf.set("spawnLocations", List.of(selected));
               applyRoute(leaf, route);
               this.writeFork(fields.getFile(), prepared, locations, leafFile, leaf, () -> {
                  NPCsConfigFields leafFields = NPCsConfig.registerRuntimeFile(leafFile);
                  if (leafFields != null && leafFields.isEnabled() && leafFields.getPatrolRoute() != null) {
                     npc.rebindPatrolConfig(leafFields);
                  } else {
                     throw new IOException("runtime registration of " + leafFile.getName() + " failed");
                  }
               }, () -> NPCsConfig.unregisterRuntimeFile(leafFile));
               raw.set("spawnLocations", locations);
               raw.set("spawnLocation", null);
               fields.setLocations(locations);
            }
         }
      }

      private void persistBoss(CustomBossEntity boss, PatrolRoute route) throws IOException {
         CustomBossesConfigFields fields = boss.getCustomBossesConfigFields();
         synchronized (fields) {
            fields.discardQueuedRegionalSave();
            FileConfiguration raw = fields.getWritableFileConfiguration();
            YamlConfiguration prepared = detached(raw);
            List<RegionalBossEntity> actors = RegionalBossEntity.getRegionalBossEntities(fields);
            List<String> locations = actors.isEmpty()
               ? new ArrayList<>(raw.getStringList("spawnLocations"))
               : new ArrayList<>(actors.stream().filter(actor -> !actor.isRemoved()).map(RegionalBossEntity::getConfigurationLocationString).toList());
            prepared.set("spawnLocations", locations);
            if (locations.size() <= 1) {
               applyRoute(prepared, route);
               saveAtomic(prepared, fields.getFile());
               applyRoute(raw, route);
               raw.set("spawnLocations", locations);
               fields.setSpawnLocations(locations);
               fields.setPatrolRoute(route);
            } else if (boss instanceof RegionalBossEntity regionalBoss) {
               String current = regionalBoss.getConfigurationLocationString();
               String selected = locations.stream()
                  .filter(location -> sameConfiguredLocation(location, current))
                  .findFirst()
                  .orElseThrow(() -> new IOException("target boss location is not present in spawnLocations"));
               locations.remove(selected);
               File leafFile = nextLeaf(fields.getFile());
               YamlConfiguration leaf = new YamlConfiguration();
               leaf.set("extends", fields.getFilename());
               leaf.set("isEnabled", true);
               leaf.set("spawnLocations", List.of(current));
               copyExplicitRoots(raw, leaf, List.of("leashRadius", "powers", "eliteScript"));
               applyRoute(leaf, route);
               this.writeFork(fields.getFile(), prepared, locations, leafFile, leaf, () -> {
                  CustomBossesConfigFields leafFields = CustomBossesConfig.registerRuntimeFile(leafFile);
                  if (leafFields != null && leafFields.isEnabled() && leafFields.getPatrolRoute() != null) {
                     regionalBoss.rebindPatrolConfig(leafFields);
                  } else {
                     throw new IOException("runtime registration of " + leafFile.getName() + " failed");
                  }
               }, () -> CustomBossesConfig.unregisterRuntimeFile(leafFile));
               raw.set("spawnLocations", locations);
               raw.set("spawnLocation", null);
               fields.setSpawnLocations(locations);
            } else {
               throw new IOException("multi-spawn boss routes require a regional boss instance");
            }
         }
      }

      private void writeFork(
         File baseFile,
         FileConfiguration prepared,
         List<String> remainingLocations,
         File leafFile,
         YamlConfiguration leaf,
         PatrolEditor.Session.ForkBinding binding,
         Runnable unregister
      ) throws IOException {
         String originalBase = Files.readString(baseFile.toPath(), StandardCharsets.UTF_8);
         Files.createFile(leafFile.toPath());
         boolean basePublished = false;

         try {
            saveAtomic(leaf, leafFile);
            prepared.set("spawnLocations", remainingLocations);
            prepared.set("spawnLocation", null);
            saveAtomic(prepared, baseFile);
            basePublished = true;
            binding.bind();
         } catch (RuntimeException | IOException var14) {
            try {
               if (basePublished) {
                  writeAtomic(baseFile.toPath(), originalBase);
               }
            } catch (IOException var13) {
               var14.addSuppressed(var13);
               Logger.warn("Patrol rollback failed for " + baseFile + ". Retained " + leafFile + " for recovery: " + var13.getMessage());
               throw var14;
            }

            unregister.run();

            try {
               Files.deleteIfExists(leafFile.toPath());
            } catch (IOException var12) {
               var14.addSuppressed(var12);
               Logger.warn("Could not remove rolled-back patrol leaf " + leafFile + ": " + var12.getMessage());
            }

            throw var14;
         }
      }

      private static YamlConfiguration detached(FileConfiguration source) throws IOException {
         YamlConfiguration copy = new YamlConfiguration();

         try {
            copy.loadFromString(source.saveToString());
            return copy;
         } catch (InvalidConfigurationException var3) {
            throw new IOException("Could not snapshot patrol configuration", var3);
         }
      }

      private void render(Player player) {
         this.restoreBedrockPreview(player);
         if (player.getWorld() == this.origin.getWorld() && !this.nodes.isEmpty()) {
            PatrolEditor.Session.Preview preview = new PatrolEditor.Session.Preview(player);

            for (Vector node : this.nodes) {
               if (preview.full()) {
                  return;
               }

               preview.point(this.nodeLocation(node));
            }

            int segments = this.mode == PatrolMode.LOOP && this.nodes.size() > 2 ? this.nodes.size() : this.nodes.size() - 1;

            for (int start = 0; start < segments && !preview.full(); start++) {
               preview.segment(this.nodeLocation(this.nodes.get(start)), this.nodeLocation(this.nodes.get((start + 1) % this.nodes.size())));
            }

            if (!preview.full()) {
               for (Location resolvedPoint : this.driver.routePreview()) {
                  if (preview.full()) {
                     return;
                  }

                  preview.point(resolvedPoint);
               }
            }
         }
      }

      private Location nodeLocation(Vector node) {
         return this.relative ? this.origin.clone().add(node) : node.toLocation(this.origin.getWorld());
      }

      private void restoreBedrockPreview(Player player) {
         for (Entry<Location, BlockData> entry : this.bedrockPreview.entrySet()) {
            if (entry.getKey().getWorld() == player.getWorld()) {
               player.sendBlockChange(entry.getKey(), entry.getValue());
            }
         }

         this.bedrockPreview.clear();
      }

      private void close(boolean cancelled) {
         if (!this.closed) {
            this.closed = true;
            PatrolEditor.EDITORS.remove(this.owner, this.playerId);
            PatrolEditor.SESSIONS.remove(this.playerId, this);
            Player player = Bukkit.getPlayer(this.playerId);
            if (player != null) {
               this.restoreBedrockPreview(player);
            }

            this.driver.close();
            if (cancelled) {
               if (this.hadPatrol) {
                  PatrolService.refresh(this.owner);
               } else if (this.owner instanceof NPCEntity && this.body() instanceof Mob mob && mob.getUniqueId().equals(this.bodyId)) {
                  mob.setAI(this.originalAi);
                  mob.setAware(this.originalAware);
               }
            }
         }
      }

      private static void applyRoute(FileConfiguration configuration, PatrolRoute route) {
         configuration.set("patrol.enabled", true);
         configuration.set("patrol.mode", route.mode().name());
         configuration.set("patrol.relative", route.relative());
         configuration.set("patrol.speed", route.speedModifier());
         configuration.set("patrol.maxLegDistance", null);
         configuration.set("patrol.virtualSpeed", route.virtualSpeed());
         if (route.startNodeWaitSeconds() > 0.0 || configuration.contains("patrol.startNodeWaitSeconds", true)) {
            configuration.set("patrol.startNodeWaitSeconds", route.startNodeWaitSeconds());
         }

         configuration.set("patrol.nodes", route.serializeNodes());
      }

      private static void copyExplicitRoots(FileConfiguration source, FileConfiguration destination, List<String> roots) {
         for (Entry<String, Object> entry : source.getValues(true).entrySet()) {
            String path = entry.getKey();
            String root = path.contains(".") ? path.substring(0, path.indexOf(46)) : path;
            if (roots.contains(root) && !(entry.getValue() instanceof ConfigurationSection)) {
               destination.set(path, entry.getValue());
            }
         }
      }

      private static File nextLeaf(File baseFile) {
         String name = baseFile.getName();
         String stem = name.toLowerCase(Locale.ROOT).endsWith(".yml") ? name.substring(0, name.length() - 4) : name;
         int index = 1;

         while (true) {
            File candidate = new File(baseFile.getParentFile(), stem + "_patrol_" + index + ".yml");
            if (!candidate.exists()) {
               return candidate;
            }

            index++;
         }
      }

      private static boolean sameConfiguredLocation(String left, String right) {
         return left != null && right != null ? locationPart(left).equals(locationPart(right)) : false;
      }

      private static String locationPart(String value) {
         int separator = value.indexOf(58);
         return separator < 0 ? value : value.substring(0, separator);
      }

      private static void saveAtomic(FileConfiguration configuration, File file) throws IOException {
         writeAtomic(file.toPath(), configuration.saveToString());
      }

      private static void writeAtomic(Path target, String contents) throws IOException {
         try {
            ConfigurationEngine.fileSaverSerialized(contents, target.toFile());
         } catch (UncheckedIOException var3) {
            throw var3.getCause();
         }
      }

      @FunctionalInterface
      private interface ForkBinding {
         void bind() throws IOException;
      }

      private final class Preview {
         private static final double RADIUS = 48.0;
         private final Player player;
         private final Location viewer;
         private final boolean bedrock;
         private final BlockData marker;
         private int remaining = 256;

         private Preview(Player player) {
            this.player = player;
            this.viewer = player.getLocation();
            this.bedrock = BedrockChecker.isBedrock(player);
            this.marker = (this.bedrock ? Material.GLASS : Material.BARRIER).createBlockData();
         }

         private boolean full() {
            return this.remaining == 0;
         }

         private void segment(Location from, Location to) {
            Vector delta = to.toVector().subtract(from.toVector());
            double length = delta.length();
            if (!(length <= 1.0E-6)) {
               double[] offset = new double[]{from.getX() - this.viewer.getX(), from.getY() - this.viewer.getY(), from.getZ() - this.viewer.getZ()};
               double[] direction = new double[]{delta.getX(), delta.getY(), delta.getZ()};
               double first = 0.0;
               double last = 1.0;

               for (int axis = 0; axis < 3; axis++) {
                  if (direction[axis] == 0.0) {
                     if (Math.abs(offset[axis]) > 48.0) {
                        return;
                     }
                  } else {
                     double a = (-48.0 - offset[axis]) / direction[axis];
                     double b = (48.0 - offset[axis]) / direction[axis];
                     first = Math.max(first, Math.min(a, b));
                     last = Math.min(last, Math.max(a, b));
                     if (first > last) {
                        return;
                     }
                  }
               }

               double step = 0.75 / length;
               int samples = 0;

               for (double fraction = Math.max(step, Math.ceil(first / step) * step);
                  fraction < 1.0 && fraction <= last && !this.full() && samples++ < 256;
                  fraction += step
               ) {
                  this.point(from.clone().add(delta.clone().multiply(fraction)));
               }
            }
         }

         private void point(Location location) {
            if (!this.full()
               && location.getWorld() == this.viewer.getWorld()
               && !(Math.abs(location.getX() - this.viewer.getX()) > 48.0)
               && !(Math.abs(location.getY() - this.viewer.getY()) > 48.0)
               && !(Math.abs(location.getZ() - this.viewer.getZ()) > 48.0)) {
               this.remaining--;
               if (this.bedrock) {
                  if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                     return;
                  }

                  Location block = location.getBlock().getLocation();
                  if (Session.this.bedrockPreview.containsKey(block)) {
                     return;
                  }

                  Session.this.bedrockPreview.put(block, block.getBlock().getBlockData());
                  this.player.sendBlockChange(block, this.marker);
               } else {
                  this.player.spawnParticle(Particle.BLOCK_MARKER, location, 1, this.marker);
               }
            }
         }
      }
   }

   private record Snapshot(List<Vector> nodes, PatrolMode mode) {
   }
}
