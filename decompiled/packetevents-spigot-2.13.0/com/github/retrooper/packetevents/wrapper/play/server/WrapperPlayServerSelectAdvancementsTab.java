package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerSelectAdvancementsTab extends PacketWrapper<WrapperPlayServerSelectAdvancementsTab> {
   @Nullable
   private ResourceLocation identifier;

   public WrapperPlayServerSelectAdvancementsTab(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerSelectAdvancementsTab(@Nullable ResourceLocation identifier) {
      super(PacketType.Play.Server.SELECT_ADVANCEMENTS_TAB);
      this.identifier = identifier;
   }

   @Override
   public void read() {
      this.identifier = this.readOptional(PacketWrapper::readIdentifier);
   }

   @Override
   public void write() {
      this.writeOptional(this.identifier, PacketWrapper::writeIdentifier);
   }

   public void copy(WrapperPlayServerSelectAdvancementsTab wrapper) {
      this.identifier = wrapper.identifier;
   }

   @Nullable
   public ResourceLocation getIdentifier() {
      return this.identifier;
   }

   public void setIdentifier(@Nullable ResourceLocation identifier) {
      this.identifier = identifier;
   }
}
