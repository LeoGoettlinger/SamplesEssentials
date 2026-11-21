package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugSample;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Obsolete
public class WrapperPlayClientDebugSampleSubscription extends PacketWrapper<WrapperPlayClientDebugSampleSubscription> {
   private WrapperPlayServerDebugSample.SampleType sampleType;

   public WrapperPlayClientDebugSampleSubscription(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientDebugSampleSubscription(WrapperPlayServerDebugSample.SampleType sampleType) {
      super(PacketType.Play.Client.DEBUG_SAMPLE_SUBSCRIPTION);
      this.sampleType = sampleType;
   }

   @Override
   public void read() {
      this.sampleType = WrapperPlayServerDebugSample.SampleType.values()[this.readVarInt()];
   }

   @Override
   public void write() {
      this.writeVarInt(this.sampleType.ordinal());
   }

   public void copy(WrapperPlayClientDebugSampleSubscription wrapper) {
      this.sampleType = wrapper.sampleType;
   }

   public WrapperPlayServerDebugSample.SampleType getSampleType() {
      return this.sampleType;
   }

   public void setSampleType(WrapperPlayServerDebugSample.SampleType sampleType) {
      this.sampleType = sampleType;
   }
}
