package com.viaversion.viaversion.api.protocol.packet.provider;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import java.util.Map;

public record SimplePacketTypesProvider<CU extends ClientboundPacketType, CM extends ClientboundPacketType, SM extends ServerboundPacketType, SU extends ServerboundPacketType>(
   Map<State, PacketTypeMap<CU>> unmappedClientboundPacketTypes,
   Map<State, PacketTypeMap<CM>> mappedClientboundPacketTypes,
   Map<State, PacketTypeMap<SM>> mappedServerboundPacketTypes,
   Map<State, PacketTypeMap<SU>> unmappedServerboundPacketTypes
) implements PacketTypesProvider<CU, CM, SM, SU> {
}
