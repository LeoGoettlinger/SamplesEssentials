package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerUpdateSimulationDistance extends PacketWrapper<WrapperPlayServerUpdateSimulationDistance> {
   private int simulationDistance;

   public WrapperPlayServerUpdateSimulationDistance(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerUpdateSimulationDistance(int simulationDistance) {
      super(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE);
      this.simulationDistance = simulationDistance;
   }

   @Override
   public void read() {
      this.simulationDistance = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.simulationDistance);
   }

   public void copy(WrapperPlayServerUpdateSimulationDistance wrapper) {
      this.simulationDistance = wrapper.simulationDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public void setSimulationDistance(int simulationDistance) {
      this.simulationDistance = simulationDistance;
   }
}
