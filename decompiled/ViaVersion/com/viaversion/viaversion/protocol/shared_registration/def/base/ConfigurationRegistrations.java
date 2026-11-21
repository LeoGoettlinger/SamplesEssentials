package com.viaversion.viaversion.protocol.shared_registration.def.base;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;

public final class ConfigurationRegistrations {
   public static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerConfigurationStateSwitching(RegistrationContext<CU, SU> ctx) {
      ctx.serverbound(
         ServerboundPackets1_20_2.CONFIGURATION_ACKNOWLEDGED,
         packetType -> ctx.protocol().registerServerbound(packetType, wrapper -> wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION)),
         PacketBound.ADDED_AT_MIN
      );
      ctx.clientbound(ClientboundPackets1_20_2.START_CONFIGURATION, packetType -> ctx.protocol().registerClientbound(packetType, wrapper -> {
         wrapper.user().getProtocolInfo().setServerState(State.CONFIGURATION);
         EntityTracker tracker = wrapper.user().getEntityTracker((Class<? extends Protocol>)ctx.protocol().getClass());
         if (tracker != null) {
            tracker.clear();
         }
      }), PacketBound.ADDED_AT_MIN);
      ctx.serverbound(
         ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION,
         packetType -> ctx.protocol().registerServerbound(packetType, wrapper -> wrapper.user().getProtocolInfo().setClientState(State.PLAY)),
         PacketBound.ADDED_AT_MIN
      );
      ctx.clientbound(ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION, packetType -> ctx.protocol().registerClientbound(packetType, wrapper -> {
         RegistryDataRewriter registryDataRewriter = ctx.protocol().getRegistryDataRewriter();
         if (registryDataRewriter != null) {
            registryDataRewriter.sendMissingRegistries(wrapper.user());
         }

         wrapper.user().getProtocolInfo().setServerState(State.PLAY);
      }), PacketBound.ADDED_AT_MIN);
   }
}
