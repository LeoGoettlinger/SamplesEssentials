package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import com.google.common.collect.Sets;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketPassengerRegistry;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketSendObserver;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.CraftBukkitBridge;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class AbstractPacketEntity<T extends Entity> implements PacketEntityInterface {
   protected final T entity;
   private final Set<UUID> viewers = Sets.newConcurrentHashSet();
   private final List<Runnable> removeCallbacks = new LinkedList<>();
   protected boolean visible = true;
   private final int EntityID;
   private int currentVehicleId = -1;

   protected AbstractPacketEntity(Location location) {
      this.entity = this.createEntity(location);
      this.EntityID = this.entity.getId();
   }

   protected AbstractPacketEntity(T entity) {
      if (entity == null) {
         throw new IllegalArgumentException("entity cannot be null");
      } else {
         this.entity = entity;
         this.EntityID = entity.getId();
      }
   }

   public List<Player> getViewersAsPlayers() {
      List<Player> players = new ArrayList<>(this.viewers.size());

      for (UUID viewer : this.viewers) {
         Player player = Bukkit.getPlayer(viewer);
         if (player != null) {
            players.add(player);
         }
      }

      return players;
   }

   @Override
   public boolean hasViewers() {
      return !this.viewers.isEmpty();
   }

   protected abstract T createEntity(Location var1);

   protected boolean hasViewer(UUID viewer) {
      return this.viewers.contains(viewer);
   }

   @Override
   public void addViewer(UUID player) {
      this.viewers.add(player);
   }

   @Override
   public void removeViewer(UUID player) {
      this.detachPassengerFor(player);
      this.viewers.remove(player);
   }

   @Override
   public void addRemoveCallback(Runnable callback) {
      this.removeCallbacks.add(callback);
   }

   protected Level getNMSLevel(Location location) {
      return CraftBukkitBridge.getServerLevel(location);
   }

   public T getNMSEntity() {
      return this.entity;
   }

   @Override
   public <B extends org.bukkit.entity.Entity> B getBukkitEntity() {
      return (B)CraftBukkitBridge.getBukkitEntity(this.entity);
   }

   @Override
   public void syncMetadata() {
      this.sendPacketToAll(this.createEntityDataPacket());
   }

   @Override
   public Location getLocation() {
      return new Location(
         CraftBukkitBridge.getBukkitWorld(this.entity.level()),
         this.entity.getX(),
         this.entity.getY(),
         this.entity.getZ(),
         this.entity.getYRot(),
         this.entity.getXRot()
      );
   }

   @Override
   public UUID getUniqueId() {
      return this.entity.getUUID();
   }

   protected Packet<?> createEntityDataPacket() {
      List<DataValue<?>> dataValues = this.entity.getEntityData().getNonDefaultValues();
      return dataValues == null ? null : new ClientboundSetEntityDataPacket(this.EntityID, dataValues);
   }

   protected Packet<?> createDirtyEntityDataPacket() {
      List<DataValue<?>> dataValues = this.entity.getEntityData().packDirty();
      return dataValues != null && !dataValues.isEmpty() ? new ClientboundSetEntityDataPacket(this.EntityID, dataValues) : null;
   }

   public Packet<?> generateRemovePacket() {
      return new ClientboundRemoveEntitiesPacket(new int[]{this.EntityID});
   }

   @Override
   public void remove() {
      this.dismount();
      this.sendPacketToAll(this.generateRemovePacket());
      this.viewers.clear();
      this.removeCallbacks.forEach(Runnable::run);
   }

   public Packet<?> generateSetVisiblePacket(boolean visible) {
      this.visible = visible;
      return (Packet<?>)(visible ? this.createEntityDataPacket() : new ClientboundRemoveEntitiesPacket(new int[]{this.EntityID}));
   }

   @Override
   public void setVisible(boolean visible) {
      if (!visible) {
         for (UUID viewer : this.viewers) {
            this.detachPassengerFor(viewer);
         }
      }

      this.sendPacketToAll(this.generateSetVisiblePacket(visible));
      if (visible) {
         for (UUID viewer : this.viewers) {
            this.refreshPassengerFor(viewer);
         }
      }
   }

   @Override
   public void hideFrom(UUID uuid) {
      Player player = Bukkit.getPlayer(uuid);
      if (player == null) {
         this.removeViewer(uuid);
      } else {
         this.sendPacketToPlayer(player, new ClientboundRemoveEntitiesPacket(new int[]{this.EntityID}));
         this.removeViewer(uuid);
      }
   }

   public void displayTo(Player player) {
      if (!this.viewers.contains(player.getUniqueId())) {
         this.viewers.add(player.getUniqueId());
         this.sendPacketToPlayer(
            player,
            new ClientboundAddEntityPacket(
               this.EntityID,
               this.entity.getUUID(),
               this.entity.getX(),
               this.entity.getY(),
               this.entity.getZ(),
               this.entity.getXRot(),
               this.entity.getYRot(),
               this.entity.getType(),
               0,
               new Vec3(0.0, 0.0, 0.0),
               this.headYaw()
            ),
            this.generateHeadRotationPacket(),
            this.createEntityDataPacket()
         );
         this.refreshPassengerFor(player.getUniqueId());
      }
   }

   public void displayTo(Player player, AbstractPacketBundle packetBundle) {
      if (packetBundle == null) {
         this.displayTo(player);
      } else if (player != null && player.isOnline() && !this.viewers.contains(player.getUniqueId())) {
         this.viewers.add(player.getUniqueId());
         List<Player> playerOnly = List.of(player);
         packetBundle.addPacket(
            new ClientboundAddEntityPacket(
               this.EntityID,
               this.entity.getUUID(),
               this.entity.getX(),
               this.entity.getY(),
               this.entity.getZ(),
               this.entity.getXRot(),
               this.entity.getYRot(),
               this.entity.getType(),
               0,
               new Vec3(0.0, 0.0, 0.0),
               this.headYaw()
            ),
            playerOnly
         );
         packetBundle.addPacket(this.generateHeadRotationPacket(), playerOnly);
         packetBundle.addPacket(this.createEntityDataPacket(), playerOnly);
         if (this.currentVehicleId != -1 && this.visible) {
            PacketPassengerRegistry.attach(player.getUniqueId(), this.currentVehicleId, this.EntityID);
            packetBundle.addPacket(
               PassengerPackets.create(
                  this.currentVehicleId, PacketPassengerRegistry.compose(player.getUniqueId(), this.currentVehicleId, this.nativePassengerIds())
               ),
               playerOnly
            );
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
   public void teleport(Location location) {
      this.entity.teleportTo(location.getX(), location.getY(), location.getZ());
      this.applyRotation(location);
      this.sendTeleportPacket();
   }

   protected void applyRotation(Location location) {
      this.entity.setYRot(location.getYaw());
      this.entity.setXRot(location.getPitch());
      if (this.entity instanceof LivingEntity livingEntity) {
         livingEntity.setYHeadRot(location.getYaw());
         livingEntity.setYBodyRot(location.getYaw());
      }
   }

   protected float headYaw() {
      return this.entity instanceof LivingEntity livingEntity ? livingEntity.yHeadRot : this.entity.getYRot();
   }

   protected void sendTeleportPacket() {
      this.sendPacketToAll(this.generateTeleportPacket());
      this.sendPacketToAll(this.generateHeadRotationPacket());
   }

   protected Packet<?> generateTeleportPacket() {
      return EntityPositionPackets.create(this.EntityID, this.entity.position(), this.entity.getYRot(), this.entity.getXRot());
   }

   protected Packet<?> generateHeadRotationPacket() {
      return !(this.entity instanceof LivingEntity) ? null : new ClientboundRotateHeadPacket(this.entity, (byte)(this.headYaw() * 256.0F / 360.0F));
   }

   @Override
   public void teleport(Location location, AbstractPacketBundle bundle) {
      if (bundle == null) {
         this.teleport(location);
      } else {
         Packet<?> move = this.generateMovePacket(location);
         if (move != null) {
            bundle.addPacket(move, this.getViewersAsPlayers());
         }

         Packet<?> head = this.generateHeadRotationPacket();
         if (head != null) {
            bundle.addPacket(head, this.getViewersAsPlayers());
         }
      }
   }

   public Packet<?> generateMovePacket(Location location) {
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      if (!(this.entity instanceof Display) || this.entity.getX() != x || this.entity.getY() != y || this.entity.getZ() != z) {
         this.entity.setPos(x, y, z);
      }

      this.applyRotation(location);
      return this.viewers.isEmpty() ? null : this.generateTeleportPacket();
   }

   public void move(Location location) {
      this.sendPacketToAll(this.generateMovePacket(location));
      this.sendPacketToAll(this.generateHeadRotationPacket());
   }

   @Override
   public int getEntityId() {
      return this.EntityID;
   }

   @Override
   public void mountTo(int vehicleEntityId) {
      if (this.currentVehicleId != vehicleEntityId) {
         this.dismount();
      }

      this.currentVehicleId = vehicleEntityId;

      for (UUID viewer : this.viewers) {
         this.refreshPassengerFor(viewer);
      }
   }

   @Override
   public void dismount() {
      if (this.currentVehicleId != -1) {
         for (UUID viewer : this.viewers) {
            this.detachPassengerFor(viewer);
         }

         this.currentVehicleId = -1;
      }
   }

   private int[] nativePassengerIds() {
      Entity vehicle = this.entity.level().getEntity(this.currentVehicleId);
      return vehicle == null ? new int[0] : vehicle.getPassengers().stream().mapToInt(Entity::getId).toArray();
   }

   private void refreshPassengerFor(UUID viewer) {
      if (this.currentVehicleId != -1 && this.visible) {
         Player player = Bukkit.getPlayer(viewer);
         if (player == null) {
            PacketPassengerRegistry.clearViewer(viewer);
         } else {
            PacketPassengerRegistry.attach(viewer, this.currentVehicleId, this.EntityID);
            this.sendPacketToPlayer(
               player,
               PassengerPackets.create(this.currentVehicleId, PacketPassengerRegistry.compose(viewer, this.currentVehicleId, this.nativePassengerIds()))
            );
         }
      }
   }

   private void detachPassengerFor(UUID viewer) {
      if (this.currentVehicleId != -1) {
         PacketPassengerRegistry.detach(viewer, this.currentVehicleId, this.EntityID);
         Player player = Bukkit.getPlayer(viewer);
         if (player != null) {
            this.sendPacketToPlayer(
               player,
               PassengerPackets.create(this.currentVehicleId, PacketPassengerRegistry.compose(viewer, this.currentVehicleId, this.nativePassengerIds()))
            );
         }
      }
   }

   protected void sendPacketToPlayer(Player player, Packet<?>... nmsPackets) {
      ServerPlayer nmsPlayer = CraftBukkitBridge.getServerPlayer(player);

      for (Packet<?> nmsPacket : nmsPackets) {
         if (nmsPacket != null) {
            nmsPlayer.connection.send(nmsPacket);
         }
      }
   }

   protected void sendPacketToAll(Packet<?> nmsPacket) {
      if (nmsPacket != null) {
         int delivered = 0;

         for (UUID viewer : this.viewers) {
            Player player = Bukkit.getPlayer(viewer);
            if (player == null) {
               this.viewers.remove(viewer);
            } else {
               this.sendPacketToPlayer(player, nmsPacket);
               delivered++;
            }
         }

         if (delivered > 0) {
            PacketSendObserver.observe(nmsPacket, delivered);
         }
      }
   }

   @Deprecated
   protected void sendPacket(Player player, Packet<?>... nmsPackets) {
      this.sendPacketToPlayer(player, nmsPackets);
   }

   @Deprecated
   protected void sendPacket(Packet<?> nmsPacket) {
      this.sendPacketToAll(nmsPacket);
   }

   public PacketBundle createPacketBundle() {
      return new PacketBundle();
   }

   protected ServerPlayer getNMSPlayer(Player bukkitPlayer) {
      return CraftBukkitBridge.getServerPlayer(bukkitPlayer);
   }
}
