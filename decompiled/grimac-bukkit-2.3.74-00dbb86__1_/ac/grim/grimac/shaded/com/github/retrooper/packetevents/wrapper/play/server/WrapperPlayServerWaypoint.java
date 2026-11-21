package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.waypoint.TrackedWaypoint;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerWaypoint extends PacketWrapper<WrapperPlayServerWaypoint> {
   private WrapperPlayServerWaypoint.Operation operation;
   private TrackedWaypoint waypoint;

   public WrapperPlayServerWaypoint(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerWaypoint(WrapperPlayServerWaypoint.Operation operation, TrackedWaypoint waypoint) {
      super(PacketType.Play.Server.WAYPOINT);
      this.operation = operation;
      this.waypoint = waypoint;
   }

   @Override
   public void read() {
      this.operation = this.readEnum(WrapperPlayServerWaypoint.Operation.class);
      this.waypoint = TrackedWaypoint.read(this);
   }

   @Override
   public void write() {
      this.writeEnum(this.operation);
      TrackedWaypoint.write(this, this.waypoint);
   }

   public void copy(WrapperPlayServerWaypoint wrapper) {
      this.operation = wrapper.operation;
      this.waypoint = wrapper.waypoint;
   }

   public WrapperPlayServerWaypoint.Operation getOperation() {
      return this.operation;
   }

   public void setOperation(WrapperPlayServerWaypoint.Operation operation) {
      this.operation = operation;
   }

   public TrackedWaypoint getWaypoint() {
      return this.waypoint;
   }

   public void setWaypoint(TrackedWaypoint waypoint) {
      this.waypoint = waypoint;
   }

   public static enum Operation {
      TRACK,
      UNTRACK,
      UPDATE;
   }
}
