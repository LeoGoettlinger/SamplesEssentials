package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.debug.DebugSubscription;
import com.github.retrooper.packetevents.protocol.debug.DebugSubscriptions;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperPlayClientDebugSubscriptionRequest extends PacketWrapper<WrapperPlayClientDebugSubscriptionRequest> {
   private Set<DebugSubscription<?>> subscriptions;

   public WrapperPlayClientDebugSubscriptionRequest(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientDebugSubscriptionRequest(Set<DebugSubscription<?>> subscriptions) {
      super(PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST);
      this.subscriptions = subscriptions;
   }

   @Override
   public void read() {
      this.subscriptions = this.readSet(ew -> ew.readMappedEntity(DebugSubscriptions.getRegistry()));
   }

   @Override
   public void write() {
      this.writeSet(this.subscriptions, PacketWrapper::writeMappedEntity);
   }

   public void copy(WrapperPlayClientDebugSubscriptionRequest wrapper) {
      this.subscriptions = wrapper.subscriptions;
   }

   public Set<DebugSubscription<?>> getSubscriptions() {
      return this.subscriptions;
   }

   public void setSubscriptions(Set<DebugSubscription<?>> subscriptions) {
      this.subscriptions = subscriptions;
   }
}
