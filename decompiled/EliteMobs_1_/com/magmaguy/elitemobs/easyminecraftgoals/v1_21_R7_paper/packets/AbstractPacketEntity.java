package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.packets;

import com.google.common.collect.Sets;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketPassengerRegistry;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.CraftBukkitBridge;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher.c;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
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
      this.EntityID = this.entity.aA();
   }

   protected AbstractPacketEntity(T entity) {
      if (entity == null) {
         throw new IllegalArgumentException("entity cannot be null");
      } else {
         this.entity = entity;
         this.EntityID = entity.aA();
      }
   }

   public List<Player> getViewersAsPlayers() {
      List<Player> players = new ArrayList<>();

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

   protected World getNMSLevel(Location location) {
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
         CraftBukkitBridge.getBukkitWorld(this.entity.ao()), this.entity.dP(), this.entity.dR(), this.entity.dV(), this.entity.ec(), this.entity.ee()
      );
   }

   @Override
   public UUID getUniqueId() {
      return this.entity.cY();
   }

   protected Packet<?> createEntityDataPacket() {
      List<c<?>> dataValues = this.entity.aD().c();
      return dataValues == null ? null : new PacketPlayOutEntityMetadata(this.EntityID, dataValues);
   }

   public Packet<?> generateRemovePacket() {
      return new PacketPlayOutEntityDestroy(new int[]{this.EntityID});
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
      return (Packet<?>)(visible ? this.createEntityDataPacket() : new PacketPlayOutEntityDestroy(new int[]{this.EntityID}));
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
         this.sendPacketToPlayer(player, new PacketPlayOutEntityDestroy(new int[]{this.EntityID}));
         this.removeViewer(uuid);
      }
   }

   public void displayTo(Player player) {
      if (!this.viewers.contains(player.getUniqueId())) {
         this.viewers.add(player.getUniqueId());
         this.sendPacketToPlayer(
            player,
            new PacketPlayOutSpawnEntity(
               this.EntityID,
               this.entity.cY(),
               this.entity.dP(),
               this.entity.dR(),
               this.entity.dV(),
               this.entity.ee(),
               this.entity.ec(),
               this.entity.ay(),
               0,
               new Vec3D(0.0, 0.0, 0.0),
               this.headYaw()
            ),
            this.generateHeadRotationPacket(),
            this.createEntityDataPacket()
         );
         this.refreshPassengerFor(player.getUniqueId());
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
      this.entity.b(location.getX(), location.getY(), location.getZ());
      this.applyRotation(location);
      this.sendTeleportPacket();
   }

   protected void applyRotation(Location location) {
      this.entity.v(location.getYaw());
      this.entity.w(location.getPitch());
      if (this.entity instanceof EntityLiving livingEntity) {
         livingEntity.r(location.getYaw());
         livingEntity.s(location.getYaw());
      }
   }

   protected float headYaw() {
      return this.entity instanceof EntityLiving livingEntity ? livingEntity.bE : this.entity.ec();
   }

   protected void sendTeleportPacket() {
      this.sendPacketToAll(this.generateTeleportPacket());
      this.sendPacketToAll(this.generateHeadRotationPacket());
   }

   protected Packet<?> generateTeleportPacket() {
      return new ClientboundEntityPositionSyncPacket(
         this.EntityID, new PositionMoveRotation(this.entity.dI(), new Vec3D(0.0, 0.0, 0.0), this.entity.ec(), this.entity.ee()), true
      );
   }

   protected Packet<?> generateHeadRotationPacket() {
      return new PacketPlayOutEntityHeadRotation(this.entity, (byte)(this.headYaw() * 256.0F / 360.0F));
   }

   public Packet<?> generateMovePacket(Location location) {
      this.entity.a_(location.getX(), location.getY(), location.getZ());
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
      Entity vehicle = this.entity.ao().a(this.currentVehicleId);
      return vehicle == null ? new int[0] : vehicle.dn().stream().mapToInt(Entity::aA).toArray();
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
      EntityPlayer nmsPlayer = CraftBukkitBridge.getServerPlayer(player);

      for (Packet<?> nmsPacket : nmsPackets) {
         if (nmsPacket != null) {
            nmsPlayer.g.b(nmsPacket);
         }
      }
   }

   protected void sendPacketToAll(Packet<?> nmsPacket) {
      if (nmsPacket != null) {
         for (UUID viewer : this.viewers) {
            Player player = Bukkit.getPlayer(viewer);
            if (player == null) {
               this.viewers.remove(viewer);
            } else {
               this.sendPacketToPlayer(player, nmsPacket);
            }
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

   protected EntityPlayer getNMSPlayer(Player bukkitPlayer) {
      return CraftBukkitBridge.getServerPlayer(bukkitPlayer);
   }
}
