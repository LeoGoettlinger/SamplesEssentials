package ac.grim.grimac.shaded.com.github.retrooper.packetevents.event;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class UserConnectEvent extends PacketEvent implements CancellableEvent, UserEvent {
   private final User user;
   private boolean cancelled;

   public UserConnectEvent(User user) {
      this.user = user;
   }

   @Override
   public User getUser() {
      return this.user;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Override
   public void call(PacketListenerCommon listener) {
      listener.onUserConnect(this);
   }
}
