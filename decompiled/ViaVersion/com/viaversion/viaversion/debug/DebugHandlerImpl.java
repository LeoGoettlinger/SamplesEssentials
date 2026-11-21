package com.viaversion.viaversion.debug;

import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import java.util.HashSet;
import java.util.Set;

public final class DebugHandlerImpl implements DebugHandler {
   private final Set<String> packetTypesToLog = new HashSet<>();
   private final Set<PacketType> clientboundPacketsToLog = new HashSet<>();
   private final Set<PacketType> serverboundPacketsToLog = new HashSet<>();
   private boolean logPrePacketTransform = true;
   private boolean logPostPacketTransform;
   private boolean enabled;

   @Override
   public boolean enabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Override
   public void addPacketTypeNameToLog(String packetTypeName) {
      this.packetTypesToLog.add(packetTypeName);
   }

   @Override
   public void addPacketTypeToLog(PacketType packetType) {
      (packetType.direction() == Direction.SERVERBOUND ? this.serverboundPacketsToLog : this.clientboundPacketsToLog).add(packetType);
   }

   @Override
   public boolean removePacketTypeNameToLog(String packetTypeName) {
      return this.packetTypesToLog.remove(packetTypeName);
   }

   @Override
   public boolean removePacketTypeToLog(PacketType packetType) {
      return (packetType.direction() == Direction.SERVERBOUND ? this.serverboundPacketsToLog : this.clientboundPacketsToLog).remove(packetType);
   }

   @Override
   public void clearPacketTypesToLog() {
      this.packetTypesToLog.clear();
      this.serverboundPacketsToLog.clear();
      this.clientboundPacketsToLog.clear();
   }

   @Override
   public boolean logPrePacketTransform() {
      return this.logPrePacketTransform;
   }

   @Override
   public void setLogPrePacketTransform(boolean logPrePacketTransform) {
      this.logPrePacketTransform = logPrePacketTransform;
   }

   @Override
   public boolean logPostPacketTransform() {
      return this.logPostPacketTransform;
   }

   @Override
   public void setLogPostPacketTransform(boolean logPostPacketTransform) {
      this.logPostPacketTransform = logPostPacketTransform;
   }

   @Override
   public boolean shouldLog(PacketWrapper wrapper, Direction direction) {
      if (this.packetTypesToLog.isEmpty() && this.serverboundPacketsToLog.isEmpty() && this.clientboundPacketsToLog.isEmpty()) {
         return true;
      } else {
         Set<PacketType> packetTypes = direction == Direction.SERVERBOUND ? this.serverboundPacketsToLog : this.clientboundPacketsToLog;
         if (wrapper.getPacketType() == null) {
            State state = wrapper.user().getProtocolInfo().getState(direction);

            for (PacketType packetType : packetTypes) {
               if (packetType.getId() == wrapper.getId() && packetType.state() == state) {
                  return true;
               }
            }

            return false;
         } else {
            return this.packetTypesToLog.contains(wrapper.getPacketType().getName()) || packetTypes.contains(wrapper.getPacketType());
         }
      }
   }
}
