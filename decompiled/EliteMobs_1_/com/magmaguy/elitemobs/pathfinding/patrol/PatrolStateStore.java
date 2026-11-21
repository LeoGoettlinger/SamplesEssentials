package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

final class PatrolStateStore {
   private static final int FORMAT_VERSION = 1;
   private final Path file;
   private final Map<String, PatrolStateStore.StoredState> states = new HashMap<>();
   private boolean dirty;

   PatrolStateStore(File pluginDataFolder) {
      this.file = pluginDataFolder.toPath().resolve("patrol-state.yml");
      this.load();
   }

   Optional<PatrolStateStore.StoredState> get(String canonicalIdentity, PatrolRoute route) {
      return Optional.ofNullable(this.states.get(PatrolIdentity.storageKey(canonicalIdentity))).filter(state -> state.routeKey().equals(route.geometryKey()));
   }

   void put(String canonicalIdentity, PatrolStateStore.StoredState state) {
      String key = PatrolIdentity.storageKey(canonicalIdentity);
      PatrolStateStore.StoredState normalized = state.withIdentity(canonicalIdentity);
      PatrolStateStore.StoredState previous = this.states.put(key, normalized);
      if (!normalized.equals(previous)) {
         this.dirty = true;
      }
   }

   void remove(String canonicalIdentity) {
      if (this.states.remove(PatrolIdentity.storageKey(canonicalIdentity)) != null) {
         this.dirty = true;
      }
   }

   void saveIfDirty() {
      if (this.dirty) {
         this.saveNow();
      }
   }

   void saveNow() {
      try {
         Files.createDirectories(this.file.getParent());
         String serialized = this.serialize();
         Path temporary = this.file.resolveSibling(this.file.getFileName() + ".tmp");
         Files.writeString(temporary, serialized, StandardCharsets.UTF_8);

         try {
            Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var4) {
            Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING);
         }

         this.dirty = false;
      } catch (IOException var5) {
         Logger.warn("Failed to save patrol state: " + var5.getMessage());
      }
   }

   private void load() {
      if (Files.isRegularFile(this.file)) {
         try {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.file.toFile());
            ConfigurationSection actors = configuration.getConfigurationSection("actors");
            if (actors == null) {
               return;
            }

            for (String key : actors.getKeys(false)) {
               ConfigurationSection section = actors.getConfigurationSection(key);
               if (section != null) {
                  PatrolStateStore.StoredState state = PatrolStateStore.StoredState.read(section);
                  if (state != null) {
                     this.states.put(key, state);
                  }
               }
            }
         } catch (RuntimeException var7) {
            Logger.warn("Failed to load patrol-state.yml; configured patrols will start at their nearest node: " + var7.getMessage());
         }
      }
   }

   private String serialize() {
      YamlConfiguration configuration = new YamlConfiguration();
      configuration.set("formatVersion", 1);

      for (Entry<String, PatrolStateStore.StoredState> entry : this.states.entrySet()) {
         String root = "actors." + entry.getKey();
         PatrolStateStore.StoredState state = entry.getValue();
         configuration.set(root + ".identity", state.identity());
         configuration.set(root + ".currentNode", state.currentNode());
         configuration.set(root + ".targetNode", state.targetNode());
         configuration.set(root + ".direction", state.direction());
         configuration.set(root + ".fraction", state.fraction());
         configuration.set(root + ".virtualSpeed", state.virtualSpeed());
         configuration.set(root + ".routeKey", state.routeKey());
         if (state.safeLocation() != null) {
            configuration.set(root + ".safe.world", state.safeLocation().worldName());
            configuration.set(root + ".safe.x", state.safeLocation().x());
            configuration.set(root + ".safe.y", state.safeLocation().y());
            configuration.set(root + ".safe.z", state.safeLocation().z());
            configuration.set(root + ".safe.yaw", state.safeLocation().yaw());
            configuration.set(root + ".safe.pitch", state.safeLocation().pitch());
         }
      }

      return configuration.saveToString();
   }

   record StoredLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
      static PatrolStateStore.StoredLocation from(Location location, String fallbackWorldName) {
         if (location == null) {
            return null;
         } else {
            String worldName = location.getWorld() == null ? fallbackWorldName : location.getWorld().getName();
            return worldName != null && !worldName.isBlank()
               ? new PatrolStateStore.StoredLocation(worldName, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch())
               : null;
         }
      }

      static PatrolStateStore.StoredLocation read(ConfigurationSection section) {
         String worldName = section.getString("world");
         if (worldName != null && !worldName.isBlank()) {
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
               ? new PatrolStateStore.StoredLocation(worldName, x, y, z, (float)section.getDouble("yaw"), (float)section.getDouble("pitch"))
               : null;
         } else {
            return null;
         }
      }

      Location resolve() {
         World world = Bukkit.getWorld(this.worldName);
         return world == null ? null : new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
      }
   }

   record StoredState(
      String identity,
      int currentNode,
      int targetNode,
      int direction,
      double fraction,
      double virtualSpeed,
      PatrolStateStore.StoredLocation safeLocation,
      String routeKey
   ) {
      PatrolStateStore.StoredState withIdentity(String canonicalIdentity) {
         return new PatrolStateStore.StoredState(
            canonicalIdentity, this.currentNode, this.targetNode, this.direction, this.fraction, this.virtualSpeed, this.safeLocation, this.routeKey
         );
      }

      static PatrolStateStore.StoredState read(ConfigurationSection section) {
         String identity = section.getString("identity");
         if (identity != null && !identity.isBlank()) {
            double fraction = section.getDouble("fraction", 0.0);
            if (!Double.isFinite(fraction)) {
               fraction = 0.0;
            }

            double virtualSpeed = section.getDouble("virtualSpeed", 0.0);
            if (!Double.isFinite(virtualSpeed) || virtualSpeed < 0.0) {
               virtualSpeed = 0.0;
            }

            ConfigurationSection safe = section.getConfigurationSection("safe");
            PatrolStateStore.StoredLocation safeLocation = safe == null ? null : PatrolStateStore.StoredLocation.read(safe);
            return new PatrolStateStore.StoredState(
               identity,
               section.getInt("currentNode", 0),
               section.getInt("targetNode", 0),
               section.getInt("direction", 1) < 0 ? -1 : 1,
               Math.max(0.0, Math.min(1.0, fraction)),
               virtualSpeed,
               safeLocation,
               section.getString("routeKey", "")
            );
         } else {
            return null;
         }
      }
   }
}
