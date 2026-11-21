package com.viaversion.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ViaListener {
   protected final Class<? extends Protocol> requiredPipeline;
   private boolean registered;

   protected ViaListener(Class<? extends Protocol> requiredPipeline) {
      this.requiredPipeline = requiredPipeline;
   }

   @Nullable
   protected UserConnection getUserConnection(UUID uuid) {
      return Via.getManager().getConnectionManager().getServerConnection(uuid);
   }

   protected boolean isOnPipe(UUID uuid) {
      return this.isOnPipe(this.getUserConnection(uuid));
   }

   protected boolean isOnPipe(UserConnection userConnection) {
      return userConnection != null && (this.requiredPipeline == null || userConnection.getProtocolInfo().getPipeline().contains(this.requiredPipeline));
   }

   public abstract void register();

   protected boolean isRegistered() {
      return this.registered;
   }

   protected void setRegistered(boolean registered) {
      this.registered = registered;
   }
}
