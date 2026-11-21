package com.viaversion.viaversion.protocols.v1_8to1_9.task;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MovementTransmitterProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;

public final class IdlePacketTask implements Runnable {
   @Override
   public void run() {
      for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
         if (connection.isActive()) {
            MovementTracker tracker = connection.get(MovementTracker.class);
            if (tracker != null) {
               long nextIdleUpdate = tracker.getNextIdlePacket();
               if (nextIdleUpdate <= System.currentTimeMillis()) {
                  Via.getManager().getProviders().get(MovementTransmitterProvider.class).sendPlayer(connection);
               }
            }
         }
      }
   }
}
