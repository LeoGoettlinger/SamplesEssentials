package com.viaversion.viaversion.api.connection;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ConnectionManager {
   boolean hasServerConnection(UUID var1);

   @Deprecated
   default boolean isClientConnected(UUID uuid) {
      return this.hasServerConnection(uuid);
   }

   boolean hasClientConnection(UUID var1);

   @Nullable
   UserConnection getServerConnection(UUID var1);

   @Deprecated
   @Nullable
   default UserConnection getConnectedClient(UUID uuid) {
      return this.getServerConnection(uuid);
   }

   @Nullable
   UserConnection getClientConnection(UUID var1);

   Map<UUID, UserConnection> getServerConnections();

   @Deprecated
   default Map<UUID, UserConnection> getConnectedClients() {
      return this.getServerConnections();
   }

   Map<UUID, UserConnection> getClientConnections();

   Set<UserConnection> getConnections();

   void onLoginSuccess(UserConnection var1);

   void onDisconnect(UserConnection var1);
}
