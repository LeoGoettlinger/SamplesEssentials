package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItemSettings;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.TrackedPacketEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FakeItemImpl implements FakeItem, TrackedPacketEntity {
   private final FakeItemSettings settings;
   private Location location;
   private ItemStack currentItemStack;
   private boolean visible = true;
   private final UUID uniqueId = UUID.randomUUID();
   private final Map<UUID, FakeItemImpl.ItemDisplayPacketEntity> playerEntities = new ConcurrentHashMap<>();
   private final List<Runnable> removeCallbacks = new ArrayList<>();
   private int currentVehicleId = -1;
   private Entity vehicleEntity = null;
   private boolean autoTracked = false;
   private boolean valid = true;

   public FakeItemImpl(Location location, FakeItemSettings settings) {
      this.location = location.clone();
      this.settings = new FakeItemSettings(settings);
      this.currentItemStack = settings.getItemStack() != null ? settings.getItemStack().clone() : null;
   }

   @Override
   public void setItemStack(ItemStack itemStack) {
      this.currentItemStack = itemStack != null ? itemStack.clone() : null;

      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         this.applyItemStack(entity);
         entity.syncMetadata();
      }
   }

   @Override
   public ItemStack getItemStack() {
      return this.currentItemStack != null ? this.currentItemStack.clone() : null;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;

      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         entity.setVisible(visible);
      }
   }

   @Override
   public void displayTo(Player player) {
      if (player != null) {
         UUID uuid = player.getUniqueId();
         if (!this.playerEntities.containsKey(uuid)) {
            FakeItemImpl.ItemDisplayPacketEntity entity = this.createItemDisplayEntity(this.location);
            entity.teleport(this.location);
            this.playerEntities.put(uuid, entity);
            entity.displayTo(uuid);
            if (this.currentVehicleId != -1) {
               entity.mountTo(this.currentVehicleId);
            }
         }
      }
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
      FakeItemImpl.ItemDisplayPacketEntity entity = this.playerEntities.remove(uuid);
      if (entity != null) {
         entity.remove();
      }
   }

   @Override
   public void teleport(Location location) {
      this.location = location.clone();

      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         entity.teleport(location);
      }
   }

   @Override
   public Location getLocation() {
      return this.location.clone();
   }

   @Override
   public void remove() {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         entity.remove();
      }

      this.playerEntities.clear();
      this.removeCallbacks.forEach(Runnable::run);
      this.valid = false;
   }

   @Override
   public boolean hasViewers() {
      return !this.playerEntities.isEmpty();
   }

   @Override
   public void setScale(float scale) {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         ItemDisplay itemDisplay = entity.getBukkitEntity();
         Transformation transform = itemDisplay.getTransformation();
         itemDisplay.setTransformation(
            new Transformation(transform.getTranslation(), transform.getLeftRotation(), new Vector3f(scale, scale, scale), transform.getRightRotation())
         );
         entity.syncMetadata();
      }
   }

   @Override
   public void setBillboard(Billboard billboard) {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         ((ItemDisplay)entity.getBukkitEntity()).setBillboard(billboard);
         entity.syncMetadata();
      }
   }

   @Override
   public void setGlowing(boolean glowing) {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         entity.getNMSEntity().j(glowing);
         entity.syncMetadata();
      }
   }

   @Override
   public void setCustomName(String name) {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         entity.getNMSEntity().b(name != null && !name.isEmpty() ? CraftChatMessage.fromStringOrNull(name) : null);
         entity.syncMetadata();
      }
   }

   @Override
   public void setCustomNameVisible(boolean visible) {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         entity.getNMSEntity().p(visible);
         entity.syncMetadata();
      }
   }

   @Override
   public void setYawRotation(float yawDegrees) {
      for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
         ItemDisplay itemDisplay = entity.getBukkitEntity();
         Transformation transform = itemDisplay.getTransformation();
         float radians = (float)Math.toRadians(yawDegrees);
         Quaternionf yawRotation = new Quaternionf().rotateY(radians);
         itemDisplay.setTransformation(new Transformation(transform.getTranslation(), transform.getLeftRotation(), transform.getScale(), yawRotation));
         entity.syncMetadata();
      }
   }

   @Override
   public Location getTrackingLocation() {
      return this.vehicleEntity != null && this.vehicleEntity.isValid() ? this.vehicleEntity.getLocation() : this.location;
   }

   @Override
   public World getWorld() {
      return this.vehicleEntity != null && this.vehicleEntity.isValid()
         ? this.vehicleEntity.getWorld()
         : (this.location != null ? this.location.getWorld() : null);
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
      return player != null && this.playerEntities.containsKey(player.getUniqueId());
   }

   @Override
   public Set<UUID> getCurrentViewers() {
      return new HashSet<>(this.playerEntities.keySet());
   }

   @Override
   public boolean isValid() {
      return this.valid && (this.vehicleEntity == null || this.vehicleEntity.isValid());
   }

   @Override
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Override
   public Entity getVehicle() {
      return this.vehicleEntity;
   }

   @Override
   public void remount() {
      if (this.vehicleEntity != null && this.vehicleEntity.isValid()) {
         this.currentVehicleId = this.vehicleEntity.getEntityId();

         for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
            entity.mountTo(this.currentVehicleId);
         }
      }
   }

   public void attachTo(Entity vehicle) {
      if (vehicle != null) {
         this.vehicleEntity = vehicle;
         this.currentVehicleId = vehicle.getEntityId();
         this.autoTracked = true;
         this.valid = true;
         PacketEntityTracker.getInstance().register(this);
      }
   }

   public void detach() {
      if (this.autoTracked) {
         PacketEntityTracker.getInstance().unregister(this);

         for (UUID viewerUUID : new HashSet<>(this.playerEntities.keySet())) {
            Player player = Bukkit.getPlayer(viewerUUID);
            if (player != null) {
               this.hideFrom(player);
            }
         }

         this.dismount();
         this.autoTracked = false;
         this.valid = false;
      }
   }

   public void mountTo(Entity vehicle) {
      if (vehicle != null) {
         this.currentVehicleId = vehicle.getEntityId();

         for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
            entity.mountTo(this.currentVehicleId);
         }
      }
   }

   public void dismount() {
      if (this.currentVehicleId != -1) {
         for (FakeItemImpl.ItemDisplayPacketEntity entity : this.playerEntities.values()) {
            entity.dismount();
         }

         this.currentVehicleId = -1;
         this.vehicleEntity = null;
      }
   }

   public void addRemoveCallback(Runnable callback) {
      this.removeCallbacks.add(callback);
   }

   private FakeItemImpl.ItemDisplayPacketEntity createItemDisplayEntity(Location location) {
      FakeItemImpl.ItemDisplayPacketEntity entity = new FakeItemImpl.ItemDisplayPacketEntity(location);
      ItemDisplay itemDisplay = entity.getBukkitEntity();
      this.applyItemStack(entity);
      itemDisplay.setBillboard(this.settings.getBillboard());
      itemDisplay.setViewRange(this.settings.getViewRange());
      if (this.settings.getScale() != 1.0F) {
         Transformation transform = itemDisplay.getTransformation();
         float scale = this.settings.getScale();
         itemDisplay.setTransformation(
            new Transformation(transform.getTranslation(), transform.getLeftRotation(), new Vector3f(scale, scale, scale), transform.getRightRotation())
         );
      }

      if (this.settings.isGlowing()) {
         entity.getNMSEntity().j(true);
      }

      if (this.settings.hasCustomName()) {
         entity.getNMSEntity().b(CraftChatMessage.fromStringOrNull(this.settings.getCustomName()));
         entity.getNMSEntity().p(this.settings.isCustomNameVisible());
      }

      entity.syncMetadata();
      return entity;
   }

   private void applyItemStack(FakeItemImpl.ItemDisplayPacketEntity entity) {
      if (this.currentItemStack != null) {
         entity.getNMSEntity().a(CraftItemStack.asNMSCopy(this.currentItemStack));
      }
   }

   private static class ItemDisplayPacketEntity extends AbstractPacketEntity<net.minecraft.world.entity.Display.ItemDisplay> {
      public ItemDisplayPacketEntity(Location location) {
         super(location);
      }

      protected net.minecraft.world.entity.Display.ItemDisplay createEntity(Location location) {
         return new net.minecraft.world.entity.Display.ItemDisplay(EntityTypes.ar, this.getNMSLevel(location));
      }
   }
}
