package ac.grim.grimac.events.packets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerInitializeWorldBorder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWorldBorder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWorldBorderCenter;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWorldBorderSize;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayWorldBorderLerpSize;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.worldborder.BorderExtent;
import ac.grim.grimac.utils.worldborder.RealTimeMovingBorderExtent;
import ac.grim.grimac.utils.worldborder.StaticBorderExtent;
import ac.grim.grimac.utils.worldborder.TickBasedMovingBorderExtent;
import lombok.Generated;

public class PacketWorldBorder extends Check implements PacketCheck {
   private static final boolean SERVER_TICK_BASED = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_11);
   private double centerX;
   private double centerZ;
   private double absoluteMaxSize;
   private BorderExtent extent = new StaticBorderExtent(5.999997E7);

   public PacketWorldBorder(GrimPlayer playerData) {
      super(playerData);
   }

   public double getCurrentDiameter() {
      return this.extent.size();
   }

   public double getMinX() {
      return this.extent.getMinX(this.centerX, this.absoluteMaxSize);
   }

   public double getMaxX() {
      return this.extent.getMaxX(this.centerX, this.absoluteMaxSize);
   }

   public double getMinZ() {
      return this.extent.getMinZ(this.centerZ, this.absoluteMaxSize);
   }

   public double getMaxZ() {
      return this.extent.getMaxZ(this.centerZ, this.absoluteMaxSize);
   }

   public void tickBorder() {
      this.extent = this.extent.tick();
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.WORLD_BORDER) {
         WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder(event);
         this.player.sendTransaction();
         if (packet.getAction() == WrapperPlayServerWorldBorder.WorldBorderAction.SET_SIZE) {
            double size = packet.getRadius();
            this.player.addRealTimeTaskNow(() -> this.setSize(size));
         } else if (packet.getAction() == WrapperPlayServerWorldBorder.WorldBorderAction.LERP_SIZE) {
            double oldDiameter = packet.getOldRadius();
            double newDiameter = packet.getNewRadius();
            long speed = packet.getSpeed();
            this.player.addRealTimeTaskNow(() -> this.setLerp(oldDiameter, newDiameter, speed));
         } else if (packet.getAction() == WrapperPlayServerWorldBorder.WorldBorderAction.SET_CENTER) {
            double centerX = packet.getCenterX();
            double centerZ = packet.getCenterZ();
            this.player.addRealTimeTaskNow(() -> this.setCenter(centerX, centerZ));
         } else if (packet.getAction() == WrapperPlayServerWorldBorder.WorldBorderAction.INITIALIZE) {
            double centerX = packet.getCenterX();
            double centerZ = packet.getCenterZ();
            double oldDiameter = packet.getOldRadius();
            double newDiameter = packet.getNewRadius();
            long speed = packet.getSpeed();
            int portalTeleportBoundary = packet.getPortalTeleportBoundary();
            this.player.addRealTimeTaskNow(() -> {
               this.setCenter(centerX, centerZ);
               this.setLerp(oldDiameter, newDiameter, speed);
               this.absoluteMaxSize = portalTeleportBoundary;
            });
         }
      }

      if (event.getPacketType() == PacketType.Play.Server.INITIALIZE_WORLD_BORDER) {
         this.player.sendTransaction();
         WrapperPlayServerInitializeWorldBorder packet = new WrapperPlayServerInitializeWorldBorder(event);
         double centerX = packet.getX();
         double centerZ = packet.getZ();
         double oldDiameter = packet.getOldDiameter();
         double newDiameter = packet.getNewDiameter();
         long speed = packet.getSpeed();
         int portalTeleportBoundary = packet.getPortalTeleportBoundary();
         this.player.addRealTimeTaskNow(() -> {
            this.setCenter(centerX, centerZ);
            this.setLerp(oldDiameter, newDiameter, speed);
            this.absoluteMaxSize = portalTeleportBoundary;
         });
      }

      if (event.getPacketType() == PacketType.Play.Server.WORLD_BORDER_CENTER) {
         this.player.sendTransaction();
         WrapperPlayServerWorldBorderCenter packet = new WrapperPlayServerWorldBorderCenter(event);
         double centerX = packet.getX();
         double centerZ = packet.getZ();
         this.player.addRealTimeTaskNow(() -> this.setCenter(centerX, centerZ));
      }

      if (event.getPacketType() == PacketType.Play.Server.WORLD_BORDER_SIZE) {
         this.player.sendTransaction();
         double size = new WrapperPlayServerWorldBorderSize(event).getDiameter();
         this.player.addRealTimeTaskNow(() -> this.setSize(size));
      }

      if (event.getPacketType() == PacketType.Play.Server.WORLD_BORDER_LERP_SIZE) {
         this.player.sendTransaction();
         WrapperPlayWorldBorderLerpSize packet = new WrapperPlayWorldBorderLerpSize(event);
         double oldDiameter = packet.getOldDiameter();
         double newDiameter = packet.getNewDiameter();
         long speed = packet.getSpeed();
         this.player.addRealTimeTaskNow(() -> this.setLerp(oldDiameter, newDiameter, speed));
      }
   }

   @Contract(
      mutates = "this"
   )
   private void setCenter(double x, double z) {
      this.centerX = x;
      this.centerZ = z;
   }

   @Contract(
      mutates = "this"
   )
   private void setSize(double size) {
      this.extent = new StaticBorderExtent(size);
   }

   @Contract(
      mutates = "this"
   )
   private void setLerp(double oldDiameter, double newDiameter, long speed) {
      if (speed > 0L && oldDiameter != newDiameter) {
         this.extent = this.createMovingExtent(oldDiameter, newDiameter, speed);
      } else {
         this.extent = new StaticBorderExtent(newDiameter);
      }
   }

   @Contract("_, _, _ -> new")
   @NotNull
   private BorderExtent createMovingExtent(double from, double to, long speed) {
      if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_11)) {
         long durationTicks = SERVER_TICK_BASED ? speed : speed / 50L;
         return new TickBasedMovingBorderExtent(from, to, durationTicks);
      } else {
         long durationMs = SERVER_TICK_BASED ? speed * 50L : speed;
         return new RealTimeMovingBorderExtent(from, to, durationMs);
      }
   }

   @Generated
   public double getCenterX() {
      return this.centerX;
   }

   @Generated
   public double getCenterZ() {
      return this.centerZ;
   }

   @Generated
   public double getAbsoluteMaxSize() {
      return this.absoluteMaxSize;
   }

   @Generated
   public BorderExtent getExtent() {
      return this.extent;
   }
}
