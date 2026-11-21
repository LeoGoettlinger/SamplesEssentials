package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.HashSet;
import java.util.Set;

public class WrapperConfigServerUpdateEnabledFeatures extends PacketWrapper<WrapperConfigServerUpdateEnabledFeatures> {
   private Set<ResourceLocation> features;

   public WrapperConfigServerUpdateEnabledFeatures(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerUpdateEnabledFeatures(Set<ResourceLocation> features) {
      super(PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES);
      this.features = features;
   }

   @Override
   public void read() {
      this.features = this.readCollection(HashSet::new, PacketWrapper::readIdentifier);
   }

   @Override
   public void write() {
      this.writeCollection(this.features, PacketWrapper::writeIdentifier);
   }

   public void copy(WrapperConfigServerUpdateEnabledFeatures wrapper) {
      this.features = wrapper.features;
   }

   public Set<ResourceLocation> getFeatures() {
      return this.features;
   }

   public void setFeatures(Set<ResourceLocation> features) {
      this.features = features;
   }
}
