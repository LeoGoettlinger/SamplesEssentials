package com.viaversion.viaversion.protocol.shared_registration;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public record RegistrationContext<CU extends ClientboundPacketType, SU extends ServerboundPacketType>(
   AbstractProtocol<CU, ?, ?, SU> protocol, ProtocolVersion min, @Nullable ProtocolVersion max
) {
   public <P extends AbstractProtocol<CU, ?, ?, SU>> P castProtocol() {
      return (P)this.protocol;
   }

   public void clientbound(ClientboundPacketType type, Consumer<CU> action) {
      action.accept(this.clientboundPacketType(type));
   }

   public void clientbound(ClientboundPacketType type, Consumer<CU> action, PacketBound... markers) {
      if (!this.shouldSkip(markers)) {
         action.accept(this.clientboundPacketType(type));
      }
   }

   public void clientboundHandler(ClientboundPacketType type, PacketHandler handler) {
      this.protocol.registerClientbound(this.clientboundPacketType(type), handler);
   }

   public void clientboundHandler(ClientboundPacketType type, PacketHandler handler, PacketBound... markers) {
      if (!this.shouldSkip(markers)) {
         this.protocol.registerClientbound(this.clientboundPacketType(type), handler);
      }
   }

   public void serverbound(ServerboundPacketType type, Consumer<SU> action) {
      action.accept(this.serverboundPacketType(type));
   }

   public void serverbound(ServerboundPacketType type, Consumer<SU> action, PacketBound... markers) {
      if (!this.shouldSkip(markers)) {
         action.accept(this.serverboundPacketType(type));
      }
   }

   public void serverboundHandler(ServerboundPacketType type, PacketHandler handler) {
      this.protocol.registerServerbound(this.serverboundPacketType(type), handler);
   }

   public void serverboundHandler(ServerboundPacketType type, PacketHandler handler, PacketBound... markers) {
      if (!this.shouldSkip(markers)) {
         this.protocol.registerServerbound(this.serverboundPacketType(type), handler);
      }
   }

   private boolean shouldSkip(PacketBound... markers) {
      if (markers.length == 0) {
         return false;
      } else {
         ProtocolVersion serverVersion = this.protocol.getServerVersion();
         ProtocolVersion clientVersion = this.protocol.getClientVersion();
         ProtocolVersion lowerVersion = serverVersion.olderThan(clientVersion) ? serverVersion : clientVersion;
         ProtocolVersion higherVersion = serverVersion.newerThan(clientVersion) ? serverVersion : clientVersion;

         for (PacketBound marker : markers) {
            switch (marker) {
               case ADDED_AT_MIN:
                  if (lowerVersion.olderThan(this.min)) {
                     return true;
                  }
                  break;
               case REMOVED_AT_MAX:
                  Preconditions.checkArgument(this.max != null, "Cannot use REMOVED_AT_MAX in an open-ended range");
                  if (higherVersion.newerThanOrEqualTo(this.max)) {
                     return true;
                  }
            }
         }

         return false;
      }
   }

   public SU serverboundPacketType(ServerboundPacketType genericPacketType) {
      SU type = this.protocol.getPacketTypesProvider().unmappedServerboundType(genericPacketType.state(), genericPacketType.getName());
      if (type == null) {
         throw new IllegalArgumentException("Could not find serverbound packet type for " + genericPacketType.state() + " " + genericPacketType.getName());
      } else {
         return type;
      }
   }

   public CU clientboundPacketType(ClientboundPacketType genericPacketType) {
      CU type = this.protocol.getPacketTypesProvider().unmappedClientboundType(genericPacketType.state(), genericPacketType.getName());
      if (type == null) {
         throw new IllegalArgumentException(
            "Could not find clientbound packet type for "
               + genericPacketType.state()
               + " "
               + genericPacketType.getName()
               + " in "
               + this.protocol.getClass().getSimpleName()
         );
      } else {
         return type;
      }
   }
}
