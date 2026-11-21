package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionTrackerImpl implements SessionTracker {
   private final DataStore store;
   private final long heartbeatIntervalMs;
   @Nullable
   private final UUID startupId;
   private final Map<UUID, SessionTrackerImpl.State> states = new ConcurrentHashMap<>();

   public SessionTrackerImpl(@NotNull DataStore store, @NotNull String serverName, long heartbeatIntervalMs) {
      this(store, serverName, heartbeatIntervalMs, null);
   }

   public SessionTrackerImpl(@NotNull DataStore store, @NotNull String serverName, long heartbeatIntervalMs, @Nullable UUID startupId) {
      this.store = store;
      this.heartbeatIntervalMs = heartbeatIntervalMs;
      this.startupId = startupId;
   }

   @NotNull
   @Override
   public UUID observeActivity(@NotNull UUID playerUuid, long now, @NotNull SessionTracker.ClientMeta meta) {
      UUID candidateSessionId = null;

      while (true) {
         SessionTrackerImpl.State current = this.states.get(playerUuid);
         if (current == null) {
            if (candidateSessionId == null) {
               candidateSessionId = UUID.randomUUID();
            }

            SessionTrackerImpl.State fresh = new SessionTrackerImpl.State(candidateSessionId, now, now, now, meta);
            if (this.states.putIfAbsent(playerUuid, fresh) == null) {
               this.emit(fresh, playerUuid, now, null);
               return fresh.sessionId;
            }
         } else {
            SessionTrackerImpl.State next = new SessionTrackerImpl.State(
               current.sessionId, current.startedEpochMs, now, now, mergeMeta(current.cachedMeta, meta)
            );
            if (this.states.replace(playerUuid, current, next)) {
               this.emit(next, playerUuid, now, null);
               return next.sessionId;
            }
         }
      }
   }

   @Override
   public void pollHeartbeat(@NotNull UUID playerUuid, long now) {
      if (this.heartbeatIntervalMs > 0L) {
         SessionTrackerImpl.State current = this.states.get(playerUuid);
         if (current != null) {
            if (now - current.lastEmittedEpochMs >= this.heartbeatIntervalMs) {
               SessionTrackerImpl.State next = new SessionTrackerImpl.State(current.sessionId, current.startedEpochMs, now, now, current.cachedMeta);
               if (this.states.replace(playerUuid, current, next)) {
                  this.emit(next, playerUuid, now, null);
               }
            }
         }
      }
   }

   @Override
   public void close(@NotNull UUID playerUuid, long now, @NotNull SessionTracker.ClientMeta meta) {
      SessionTrackerImpl.State prev = this.states.remove(playerUuid);
      if (prev != null) {
         SessionTrackerImpl.State closed = new SessionTrackerImpl.State(
            prev.sessionId, prev.startedEpochMs, prev.lastActivityEpochMs, now, mergeMeta(prev.cachedMeta, meta)
         );
         this.emit(closed, playerUuid, prev.lastActivityEpochMs, now);
      }
   }

   @Nullable
   @Override
   public UUID currentSessionId(@NotNull UUID playerUuid) {
      SessionTrackerImpl.State s = this.states.get(playerUuid);
      return s == null ? null : s.sessionId;
   }

   private void emit(SessionTrackerImpl.State s, UUID playerUuid, long now, @Nullable Long closedAt) {
      UUID sessionId = s.sessionId;
      long started = s.startedEpochMs;
      SessionTracker.ClientMeta meta = s.cachedMeta;
      this.store
         .submit(
            Categories.SESSION,
            e -> e.sessionId(sessionId)
               .playerUuid(playerUuid)
               .startedEpochMs(started)
               .lastActivityEpochMs(now)
               .closedAtEpochMs(closedAt == null ? 0L : closedAt)
               .clientBrand(meta.clientBrand())
               .clientVersion(meta.clientVersion())
               .startupId(this.startupId)
         );
   }

   private static SessionTracker.ClientMeta mergeMeta(SessionTracker.ClientMeta current, SessionTracker.ClientMeta incoming) {
      return new SessionTracker.ClientMeta(
         latest(incoming.grimVersion(), current.grimVersion()),
         current.clientBrand() != null ? current.clientBrand() : incoming.clientBrand(),
         incoming.clientVersion() >= 0 ? incoming.clientVersion() : current.clientVersion(),
         latest(incoming.serverVersion(), current.serverVersion())
      );
   }

   private static <T> T latest(T incoming, T current) {
      return incoming != null ? incoming : current;
   }

   private record State(UUID sessionId, long startedEpochMs, long lastActivityEpochMs, long lastEmittedEpochMs, SessionTracker.ClientMeta cachedMeta) {
   }
}
