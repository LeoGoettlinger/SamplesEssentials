package com.viaversion.viabackwards.api;

import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.protocol.registration.BackwardsRegistrations;
import com.viaversion.viabackwards.utils.BackwardsProtocolLogger;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.util.ProtocolLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class BackwardsProtocol<CU extends ClientboundPacketType, CM extends ClientboundPacketType, SM extends ServerboundPacketType, SU extends ServerboundPacketType>
   extends AbstractProtocol<CU, CM, SM, SU> {
   @Deprecated
   protected BackwardsProtocol() {
   }

   protected BackwardsProtocol(
      @Nullable Class<CU> oldClientboundPacketEnum,
      @Nullable Class<CM> clientboundPacketEnum,
      @Nullable Class<SM> oldServerboundPacketEnum,
      @Nullable Class<SU> serverboundPacketEnum
   ) {
      super(oldClientboundPacketEnum, clientboundPacketEnum, oldServerboundPacketEnum, serverboundPacketEnum);
   }

   protected void applySharedRegistrations() {
      super.applySharedRegistrations();
      BackwardsRegistrations.registrations().applyMatching(this);
   }

   protected ProtocolLogger createLogger() {
      return new BackwardsProtocolLogger((Class<? extends Protocol>)this.getClass());
   }

   @Nullable
   public Class<? extends Protocol<?, ?, ?, ?>> dependsOn() {
      return this.getMappingData() != null ? this.getMappingData().getViaVersionProtocolClass() : null;
   }

   @Nullable
   public BackwardsMappingData getMappingData() {
      return null;
   }

   @Nullable
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return null;
   }
}
