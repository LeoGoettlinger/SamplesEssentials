package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.TrackedPacketEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BukkitCustomEntityImpl implements BukkitCustomEntity, TrackedPacketEntity {
   private final Entity entity;
   private final CustomEntityDefinition definition;
   private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
   private final CustomEntityPropertyState properties = new CustomEntityPropertyState();
   private volatile Float scale;
   private volatile Integer color;
   private volatile Integer variant;
   private volatile Float hitboxWidth;
   private volatile Float hitboxHeight;
   private volatile boolean removed;

   public BukkitCustomEntityImpl(Entity entity, CustomEntityDefinition definition) {
      if (entity == null) {
         throw new IllegalArgumentException("entity cannot be null");
      } else if (definition == null) {
         throw new IllegalArgumentException("definition cannot be null");
      } else {
         this.entity = entity;
         this.definition = definition;
         this.scale = definition.scale();
         this.color = definition.color();
         this.variant = definition.variant();
         this.hitboxWidth = definition.width();
         this.hitboxHeight = definition.height();
         this.runBridgeSafely(() -> BedrockCustomEntityBridgeRegistry.bridge().registerDefinition(definition));
         if (definition.tracked()) {
            PacketEntityTracker.getInstance().register(this);
         }
      }
   }

   @Override
   public CustomEntityDefinition definition() {
      return this.definition;
   }

   @Override
   public Entity entity() {
      return this.entity;
   }

   @Override
   public int getEntityId() {
      return this.entity.getEntityId();
   }

   @Override
   public Location getLocation() {
      return this.entity.getLocation();
   }

   @Override
   public UUID getUniqueId() {
      return this.entity.getUniqueId();
   }

   @Override
   public Set<UUID> getViewers() {
      return Collections.unmodifiableSet(new LinkedHashSet<>(this.viewers));
   }

   @Override
   public void prepareSpawnFor(Player player) {
      if (player != null && !this.removed && this.isValid()) {
         boolean bedrock = BedrockChecker.isBedrock(player);
         if (bedrock) {
            this.runBridgeSafely(() -> {
               BedrockCustomEntityBridge bridge = BedrockCustomEntityBridgeRegistry.bridge();
               bridge.registerDefinition(this.definition);
               bridge.prepareEntitySpawn(player, this.entity.getEntityId(), this.definition);
            });
         }

         boolean newViewer = this.viewers.add(player.getUniqueId());
         if (newViewer) {
            this.runHookSafely(() -> this.definition.showHook().handle(this, player));
         }

         if (bedrock) {
            this.schedulePostSpawnSync(player);
         }
      }
   }

   @Override
   public void prepareSpawnFor(Collection<? extends Player> players) {
      if (players != null) {
         players.forEach(this::prepareSpawnFor);
      }
   }

   @Override
   public void syncTo(Player player) {
      if (player != null && !this.removed && this.isValid()) {
         boolean newViewer = this.viewers.add(player.getUniqueId());
         if (newViewer) {
            this.runHookSafely(() -> this.definition.showHook().handle(this, player));
         }

         if (BedrockChecker.isBedrock(player)) {
            this.sendBedrockState(player);
         }
      }
   }

   @Override
   public void syncToViewers() {
      for (UUID uuid : this.viewers) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            this.syncTo(player);
         }
      }
   }

   @Override
   public void forgetViewer(Player player) {
      if (player != null) {
         this.forgetViewer(player.getUniqueId());
      }
   }

   @Override
   public void forgetViewer(UUID uuid) {
      if (uuid != null) {
         boolean wasViewing = this.viewers.remove(uuid);
         if (wasViewing) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
               this.runHookSafely(() -> this.definition.hideHook().handle(this, player));
            }
         }
      }
   }

   @Override
   public void remove() {
      if (!this.removed) {
         this.removed = true;
         PacketEntityTracker.getInstance().unregister(this);

         for (UUID uuid : new LinkedHashSet<>(this.viewers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
               this.runHookSafely(() -> this.definition.hideHook().handle(this, player));
            }
         }

         this.runHookSafely(() -> this.definition.removeHook().handle(this));
         this.viewers.clear();
      }
   }

   @Override
   public boolean isValid() {
      return !this.removed && this.entity.isValid();
   }

   @Override
   public Location getTrackingLocation() {
      Entity vehicle = this.getVehicle();
      return vehicle == null ? this.getLocation() : vehicle.getLocation();
   }

   @Override
   public World getWorld() {
      Location location = this.getLocation();
      return location == null ? null : location.getWorld();
   }

   @Override
   public void showToPlayer(Player player) {
      this.prepareSpawnFor(player);
   }

   @Override
   public void hideFromPlayer(Player player) {
      this.forgetViewer(player);
   }

   @Override
   public boolean isVisibleTo(Player player) {
      return player != null && this.viewers.contains(player.getUniqueId());
   }

   @Override
   public Set<UUID> getCurrentViewers() {
      return this.getViewers();
   }

   @Override
   public Entity getVehicle() {
      return this.entity.getVehicle();
   }

   @Override
   public void remount() {
   }

   @Override
   public void setScale(float scale) {
      this.scale = scale;
      this.sendEntityDataToBedrockViewers();
   }

   @Override
   public void setColor(Integer color) {
      this.color = color;
      this.sendEntityDataToBedrockViewers();
   }

   @Override
   public void setVariant(Integer variant) {
      this.variant = variant;
      this.sendEntityDataToBedrockViewers();
   }

   @Override
   public void setHitbox(float width, float height) {
      this.hitboxWidth = width;
      this.hitboxHeight = height;
      this.sendEntityDataToBedrockViewers();
   }

   @Override
   public void setProperties(Map<String, ?> properties) {
      this.properties.setAll(properties);
      this.sendPropertiesToBedrockViewers();
   }

   @Override
   public void setProperty(String identifier, Object value) {
      this.properties.set(identifier, value);
      this.sendPropertiesToBedrockViewers();
   }

   private void schedulePostSpawnSync(Player player) {
      Plugin plugin = NMSManager.pluginProvider;
      if (plugin != null && plugin.isEnabled()) {
         Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && this.viewers.contains(player.getUniqueId()) && this.isValid()) {
               this.sendBedrockState(player);
            }
         }, 1L);
      } else {
         this.sendBedrockState(player);
      }
   }

   private void sendBedrockState(Player player) {
      this.sendEntityData(player);
      this.sendProperties(player);
   }

   private void sendEntityDataToBedrockViewers() {
      for (UUID uuid : this.viewers) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && BedrockChecker.isBedrock(player)) {
            this.sendEntityData(player);
         }
      }
   }

   private void sendPropertiesToBedrockViewers() {
      for (UUID uuid : this.viewers) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && BedrockChecker.isBedrock(player)) {
            this.sendProperties(player);
         }
      }
   }

   private void sendEntityData(Player player) {
      this.runBridgeSafely(
         () -> BedrockCustomEntityBridgeRegistry.bridge()
            .sendEntityData(player, this.entity.getEntityId(), this.hitboxHeight, this.hitboxWidth, this.scale, this.color, this.variant)
      );
   }

   private void sendProperties(Player player) {
      Map<String, Object> snapshot = this.properties.snapshot();
      if (!snapshot.isEmpty()) {
         this.runBridgeSafely(() -> BedrockCustomEntityBridgeRegistry.bridge().sendProperties(player, this.entity.getEntityId(), snapshot));
      }
   }

   private void runBridgeSafely(Runnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var3) {
         if (NMSManager.pluginProvider != null) {
            NMSManager.pluginProvider
               .getLogger()
               .warning("Bedrock custom entity bridge call failed for " + this.definition.identifier() + ": " + var3.getMessage());
         }
      }
   }

   private void runHookSafely(Runnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var3) {
         if (NMSManager.pluginProvider != null) {
            NMSManager.pluginProvider.getLogger().warning("Custom entity lifecycle hook failed for " + this.definition.identifier() + ": " + var3.getMessage());
         }
      }
   }
}
