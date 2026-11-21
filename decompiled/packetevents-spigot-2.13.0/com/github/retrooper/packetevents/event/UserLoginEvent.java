package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.protocol.player.User;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class UserLoginEvent extends PacketEvent implements CallableEvent, UserEvent, PlayerEvent {
   private final User user;
   private final Object player;

   public UserLoginEvent(User user, Object player) {
      this.user = user;
      this.player = player;
   }

   @Override
   public User getUser() {
      return this.user;
   }

   @Override
   public <T> T getPlayer() {
      return (T)this.player;
   }

   @Override
   public void call(PacketListenerCommon listener) {
      listener.onUserLogin(this);
   }
}
