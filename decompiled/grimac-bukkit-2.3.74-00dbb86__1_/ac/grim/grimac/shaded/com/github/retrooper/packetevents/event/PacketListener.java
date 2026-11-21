package ac.grim.grimac.shaded.com.github.retrooper.packetevents.event;

public interface PacketListener {
   default PacketListenerAbstract asAbstract(PacketListenerPriority priority) {
      return new PacketListenerAbstract(priority) {
         @Override
         public void onUserConnect(UserConnectEvent event) {
            PacketListener.this.onUserConnect(event);
         }

         @Override
         public void onUserLogin(UserLoginEvent event) {
            PacketListener.this.onUserLogin(event);
         }

         @Override
         public void onUserDisconnect(UserDisconnectEvent event) {
            PacketListener.this.onUserDisconnect(event);
         }

         @Override
         public void onPacketReceive(PacketReceiveEvent event) {
            PacketListener.this.onPacketReceive(event);
         }

         @Override
         public void onPacketSend(PacketSendEvent event) {
            PacketListener.this.onPacketSend(event);
         }

         @Override
         public void onPacketEventExternal(PacketEvent event) {
            PacketListener.this.onPacketEventExternal(event);
         }
      };
   }

   default void onUserConnect(UserConnectEvent event) {
   }

   default void onUserLogin(UserLoginEvent event) {
   }

   default void onUserDisconnect(UserDisconnectEvent event) {
   }

   default void onPacketReceive(PacketReceiveEvent event) {
   }

   default void onPacketSend(PacketSendEvent event) {
   }

   default void onPacketEventExternal(PacketEvent event) {
   }
}
