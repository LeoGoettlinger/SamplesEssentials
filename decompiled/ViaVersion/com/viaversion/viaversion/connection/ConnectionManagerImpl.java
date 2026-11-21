package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConnectionManagerImpl implements ConnectionManager {
   protected final Map<UUID, UserConnection> serverConnections = new ConcurrentHashMap<>();
   protected final Map<UUID, UserConnection> clientConnections = new ConcurrentHashMap<>();
   protected final Set<UserConnection> connections = ConcurrentHashMap.newKeySet();
   private final Set<UserConnection> connectionsView = Collections.unmodifiableSet(this.connections);

   @Override
   public void onLoginSuccess(UserConnection connection) {
      Objects.requireNonNull(connection, "connection is null!");
      Channel channel = connection.getChannel();
      if (channel == null || channel.isOpen()) {
         boolean newlyAdded = this.connections.add(connection);
         UUID id = connection.getProtocolInfo().getUuid();
         if (connection.isServerSide()) {
            UserConnection previous = this.serverConnections.put(id, connection);
            if (previous != null && previous != connection) {
               Via.getPlatform().getLogger().warning("Duplicate UUID on frontend connection! (" + id + ")");
            }
         } else {
            this.clientConnections.put(id, connection);
         }

         if (channel != null) {
            if (!channel.isOpen()) {
               this.onDisconnect(connection);
            } else if (newlyAdded) {
               channel.closeFuture().addListener((ChannelFutureListener)future -> this.onDisconnect(connection));
            }
         }
      }
   }

   @Override
   public void onDisconnect(UserConnection connection) {
      Objects.requireNonNull(connection, "connection is null!");
      connection.setActive(false);
      this.connections.remove(connection);
      UUID id = connection.getProtocolInfo().getUuid();
      if (connection.isServerSide()) {
         this.serverConnections.remove(id, connection);
      } else {
         this.clientConnections.remove(id, connection);
      }

      connection.getChannel().eventLoop().execute(connection::clearStoredObjects);
   }

   @Override
   public boolean hasServerConnection(UUID playerId) {
      return this.serverConnections.containsKey(playerId);
   }

   @Nullable
   @Override
   public UserConnection getServerConnection(UUID uuid) {
      return this.serverConnections.get(uuid);
   }

   @Override
   public boolean hasClientConnection(UUID uuid) {
      return this.clientConnections.containsKey(uuid);
   }

   @Nullable
   @Override
   public UserConnection getClientConnection(UUID uuid) {
      return this.clientConnections.get(uuid);
   }

   @Override
   public Map<UUID, UserConnection> getServerConnections() {
      return Collections.unmodifiableMap(this.serverConnections);
   }

   @Override
   public Map<UUID, UserConnection> getClientConnections() {
      return Collections.unmodifiableMap(this.clientConnections);
   }

   @Override
   public Set<UserConnection> getConnections() {
      return this.connectionsView;
   }
}
