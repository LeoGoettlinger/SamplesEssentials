package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.TrackedPacketEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
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

public final class FakeCustomEntityImpl implements FakeCustomEntity, TrackedPacketEntity {
   private final CustomEntityDefinition definition;
   private final PacketEntityInterface packetEntity;
   private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
   private final CustomEntityPropertyState properties = new CustomEntityPropertyState();
   private volatile Float scale;
   private volatile Integer color;
   private volatile Integer variant;
   private volatile Float hitboxWidth;
   private volatile Float hitboxHeight;
   private volatile boolean removed;

   public FakeCustomEntityImpl(NMSAdapter adapter, Location location, CustomEntityDefinition definition) {
      this.definition = definition;
      this.scale = definition.scale();
      this.color = definition.color();
      this.variant = definition.variant();
      this.hitboxWidth = definition.width();
      this.hitboxHeight = definition.height();
      this.packetEntity = adapter.createPacketEntity(definition.carrierEntityType(), location);
      BedrockCustomEntityBridgeRegistry.bridge().registerDefinition(definition);
      if (definition.tracked()) {
         PacketEntityTracker.getInstance().register(this);
      }
   }

   @Override
   public CustomEntityDefinition definition() {
      return this.definition;
   }

   @Override
   public PacketEntityInterface packetEntity() {
      return this.packetEntity;
   }

   @Override
   public int getEntityId() {
      return this.packetEntity.getEntityId();
   }

   @Override
   public Location getLocation() {
      return this.packetEntity.getLocation();
   }

   @Override
   public UUID getUniqueId() {
      return this.packetEntity.getUniqueId();
   }

   @Override
   public Set<UUID> getViewers() {
      return Collections.unmodifiableSet(new LinkedHashSet<>(this.viewers));
   }

   @Override
   public void displayTo(Player player) {
      if (player != null && !this.removed && !this.viewers.contains(player.getUniqueId())) {
         boolean bedrock = BedrockChecker.isBedrock(player);
         if (bedrock) {
            this.displayToBedrock(player);
         } else {
            this.packetEntity.displayTo(player.getUniqueId());
            this.viewers.add(player.getUniqueId());
            this.runHookSafely(() -> this.definition.showHook().handle(this, player));
         }
      }
   }

   private void displayToBedrock(Player player) {
      UUID uuid = player.getUniqueId();
      this.viewers.add(uuid);
      this.prepareBedrockSpawn(player);
      this.runHookSafely(() -> this.definition.showHook().handle(this, player));
      Plugin plugin = NMSManager.pluginProvider;
      if (plugin != null && plugin.isEnabled()) {
         Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player currentPlayer = Bukkit.getPlayer(uuid);
            if (currentPlayer != null && currentPlayer.isOnline() && !this.removed && this.viewers.contains(uuid)) {
               this.prepareBedrockSpawn(currentPlayer);
               this.packetEntity.displayTo(uuid);
               this.schedulePostSpawnSync(currentPlayer);
            }
         }, 1L);
      } else {
         this.packetEntity.displayTo(uuid);
         this.schedulePostSpawnSync(player);
      }
   }

   private void prepareBedrockSpawn(Player player) {
      this.runBridgeSafely(() -> {
         BedrockCustomEntityBridge bridge = BedrockCustomEntityBridgeRegistry.bridge();
         bridge.registerDefinition(this.definition);
         bridge.prepareEntitySpawn(player, this.packetEntity.getEntityId(), this.definition);
      });
   }

   @Override
   public void displayTo(UUID uuid) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
         this.displayTo(player);
      }
   }

   @Override
   public void hideFrom(Player player) {
      if (player != null) {
         this.hideFrom(player.getUniqueId());
      }
   }

   @Override
   public void hideFrom(UUID uuid) {
      boolean wasViewing = this.viewers.remove(uuid);
      this.packetEntity.hideFrom(uuid);
      if (wasViewing) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            this.runHookSafely(() -> this.definition.hideHook().handle(this, player));
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
         this.packetEntity.remove();
         this.viewers.clear();
      }
   }

   @Override
   public void teleport(Location location) {
      this.packetEntity.teleport(location);
   }

   @Override
   public void setVisible(boolean visible) {
      this.packetEntity.setVisible(visible);
   }

   @Override
   public void mountTo(int vehicleEntityId) {
      this.packetEntity.mountTo(vehicleEntityId);
   }

   @Override
   public void dismount() {
      this.packetEntity.dismount();
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

   @Override
   public Location getTrackingLocation() {
      return this.getLocation();
   }

   @Override
   public World getWorld() {
      Location location = this.getLocation();
      return location == null ? null : location.getWorld();
   }

   @Override
   public void showToPlayer(Player player) {
      this.displayTo(player);
   }

   @Override
   public void hideFromPlayer(Player player) {
      this.hideFrom(player);
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
   public boolean isValid() {
      return !this.removed && this.getWorld() != null;
   }

   @Override
   public Entity getVehicle() {
      return null;
   }

   @Override
   public void remount() {
   }

   private void schedulePostSpawnSync(Player player) {
      Plugin plugin = NMSManager.pluginProvider;
      if (plugin != null && plugin.isEnabled()) {
         this.schedulePostSpawnSync(player.getUniqueId(), 4L);
         this.schedulePostSpawnSync(player.getUniqueId(), 10L);
      } else {
         this.synchronizeBedrockPresentation(player);
      }
   }

   private void schedulePostSpawnSync(UUID uuid, long delayTicks) {
      Plugin plugin = NMSManager.pluginProvider;
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline() && this.viewers.contains(uuid)) {
            this.synchronizeBedrockPresentation(player);
         }
      }, delayTicks);
   }

   private void synchronizeBedrockPresentation(Player player) {
      this.prepareBedrockSpawn(player);
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
            .sendEntityData(player, this.packetEntity.getEntityId(), this.hitboxHeight, this.hitboxWidth, this.scale, this.color, this.variant)
      );
   }

   private void sendProperties(Player player) {
      Map<String, Object> snapshot = this.properties.snapshot();
      if (!snapshot.isEmpty()) {
         this.runBridgeSafely(() -> BedrockCustomEntityBridgeRegistry.bridge().sendProperties(player, this.packetEntity.getEntityId(), snapshot));
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
