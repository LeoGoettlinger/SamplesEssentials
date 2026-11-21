package ac.grim.grimac.internal.storage.backend.redis.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.instance.OwnershipClaimResult;
import ac.grim.grimac.api.storage.instance.OwnershipRenewResult;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipMetadata;
import ac.grim.grimac.api.storage.instance.ServerOwnershipSnapshot;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@ApiStatus.Internal
public final class RedisServerOwnershipAdapter implements ServerOwnershipAdapter {
   private static final String CLAIM_SCRIPT = "local closed = redis.call('HGET', KEYS[1], 'closed_at_ms')\nif not closed then\n  redis.call('HSET', KEYS[1],\n    'persistent_id', ARGV[1],\n    'owner_startup_id', ARGV[2],\n    'fence', ARGV[3],\n    'lease_expires_at_ms', ARGV[5],\n    'last_renewed_at_ms', ARGV[4],\n    'closed_at_ms', '0',\n    'server_name', ARGV[6],\n    'hostname', ARGV[7],\n    'grim_version', ARGV[8],\n    'server_version', ARGV[9])\n  redis.call('HDEL', KEYS[1], 'close_reason')\n  return 1\nend\nlocal expires = tonumber(redis.call('HGET', KEYS[1], 'lease_expires_at_ms') or '0')\nlocal owner = redis.call('HGET', KEYS[1], 'owner_startup_id')\nlocal fence = redis.call('HGET', KEYS[1], 'fence')\nif tonumber(closed) ~= 0 or expires <= tonumber(ARGV[4])\n   or (owner == ARGV[2] and fence == ARGV[3]) then\n  redis.call('HSET', KEYS[1],\n    'persistent_id', ARGV[1],\n    'owner_startup_id', ARGV[2],\n    'fence', ARGV[3],\n    'lease_expires_at_ms', ARGV[5],\n    'last_renewed_at_ms', ARGV[4],\n    'closed_at_ms', '0',\n    'server_name', ARGV[6],\n    'hostname', ARGV[7],\n    'grim_version', ARGV[8],\n    'server_version', ARGV[9])\n  redis.call('HDEL', KEYS[1], 'close_reason')\n  return 1\nend\nreturn 0\n";
   private static final String RENEW_SCRIPT = "local owner = redis.call('HGET', KEYS[1], 'owner_startup_id')\nlocal fence = redis.call('HGET', KEYS[1], 'fence')\nlocal closed = tonumber(redis.call('HGET', KEYS[1], 'closed_at_ms') or '0')\nlocal expires = tonumber(redis.call('HGET', KEYS[1], 'lease_expires_at_ms') or '0')\nif owner == ARGV[1] and fence == ARGV[2] and closed == 0 and expires > tonumber(ARGV[3]) then\n  redis.call('HSET', KEYS[1],\n    'lease_expires_at_ms', ARGV[4],\n    'last_renewed_at_ms', ARGV[3],\n    'closed_at_ms', '0')\n  redis.call('HDEL', KEYS[1], 'close_reason')\n  return 1\nend\nreturn 0\n";
   private static final String CLOSE_SCRIPT = "local owner = redis.call('HGET', KEYS[1], 'owner_startup_id')\nlocal fence = redis.call('HGET', KEYS[1], 'fence')\nlocal closed = tonumber(redis.call('HGET', KEYS[1], 'closed_at_ms') or '0')\nif owner == ARGV[1] and fence == ARGV[2] and closed == 0 then\n  redis.call('HSET', KEYS[1],\n    'closed_at_ms', ARGV[3],\n    'close_reason', ARGV[4])\n  return 1\nend\nreturn 0\n";
   @NotNull
   private final JedisPool pool;
   @NotNull
   private final String keyPrefix;
   @NotNull
   private final Logger logger;

   public RedisServerOwnershipAdapter(@NotNull JedisPool pool, @NotNull String keyPrefix, @NotNull Logger logger) {
      this.pool = pool;
      this.keyPrefix = keyPrefix;
      this.logger = logger;
   }

   @Override
   public void ensureStore(@NotNull StoreId id) {
   }

   @Override
   public long dbNowEpochMs() throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         long var7;
         try {
            List<String> time = j.time();
            long seconds = Long.parseLong(time.get(0));
            long micros = Long.parseLong(time.get(1));
            var7 = seconds * 1000L + micros / 1000L;
         } catch (Throwable var10) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (j != null) {
            j.close();
         }

         return var7;
      } catch (RuntimeException var11) {
         throw new BackendException("failed to read Redis time", var11);
      }
   }

   @NotNull
   @Override
   public OwnershipClaimResult claimOwnership(
      @NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long ttlMs, @NotNull ServerOwnershipMetadata metadata
   ) throws BackendException {
      try {
         ServerOwnershipSnapshot previous = this.readOwnership(id, persistentId).orElse(null);
         long now = this.dbNowEpochMs();
         long expires = now + ttlMs;
         Jedis j = this.pool.getResource();

         Object result;
         try {
            result = j.eval(
               "local closed = redis.call('HGET', KEYS[1], 'closed_at_ms')\nif not closed then\n  redis.call('HSET', KEYS[1],\n    'persistent_id', ARGV[1],\n    'owner_startup_id', ARGV[2],\n    'fence', ARGV[3],\n    'lease_expires_at_ms', ARGV[5],\n    'last_renewed_at_ms', ARGV[4],\n    'closed_at_ms', '0',\n    'server_name', ARGV[6],\n    'hostname', ARGV[7],\n    'grim_version', ARGV[8],\n    'server_version', ARGV[9])\n  redis.call('HDEL', KEYS[1], 'close_reason')\n  return 1\nend\nlocal expires = tonumber(redis.call('HGET', KEYS[1], 'lease_expires_at_ms') or '0')\nlocal owner = redis.call('HGET', KEYS[1], 'owner_startup_id')\nlocal fence = redis.call('HGET', KEYS[1], 'fence')\nif tonumber(closed) ~= 0 or expires <= tonumber(ARGV[4])\n   or (owner == ARGV[2] and fence == ARGV[3]) then\n  redis.call('HSET', KEYS[1],\n    'persistent_id', ARGV[1],\n    'owner_startup_id', ARGV[2],\n    'fence', ARGV[3],\n    'lease_expires_at_ms', ARGV[5],\n    'last_renewed_at_ms', ARGV[4],\n    'closed_at_ms', '0',\n    'server_name', ARGV[6],\n    'hostname', ARGV[7],\n    'grim_version', ARGV[8],\n    'server_version', ARGV[9])\n  redis.call('HDEL', KEYS[1], 'close_reason')\n  return 1\nend\nreturn 0\n",
               List.of(this.key(id, persistentId)),
               List.of(
                  persistentId.toString(),
                  startupId.toString(),
                  fence.toString(),
                  Long.toString(now),
                  Long.toString(expires),
                  nullToEmpty(metadata.serverName()),
                  nullToEmpty(metadata.hostname()),
                  nullToEmpty(metadata.grimVersion()),
                  nullToEmpty(metadata.serverVersionString())
               )
            );
         } catch (Throwable var18) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var17) {
                  var18.addSuppressed(var17);
               }
            }

            throw var18;
         }

         if (j != null) {
            j.close();
         }

         if (number(result) == 1L) {
            return OwnershipClaimResult.claimed(persistentId, startupId, fence, now, expires, previous);
         } else {
            ServerOwnershipSnapshot current = this.readOwnership(id, persistentId).orElse(null);
            return current == null
               ? OwnershipClaimResult.claimed(persistentId, startupId, fence, now, expires, previous)
               : OwnershipClaimResult.denied(persistentId, startupId, fence, now, current);
         }
      } catch (RuntimeException var19) {
         throw new BackendException("failed to claim Redis server ownership", var19);
      }
   }

   @NotNull
   @Override
   public OwnershipRenewResult renewOwnership(@NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long ttlMs) throws BackendException {
      try {
         long now = this.dbNowEpochMs();
         long expires = now + ttlMs;
         Jedis j = this.pool.getResource();

         Object result;
         try {
            result = j.eval(
               "local owner = redis.call('HGET', KEYS[1], 'owner_startup_id')\nlocal fence = redis.call('HGET', KEYS[1], 'fence')\nlocal closed = tonumber(redis.call('HGET', KEYS[1], 'closed_at_ms') or '0')\nlocal expires = tonumber(redis.call('HGET', KEYS[1], 'lease_expires_at_ms') or '0')\nif owner == ARGV[1] and fence == ARGV[2] and closed == 0 and expires > tonumber(ARGV[3]) then\n  redis.call('HSET', KEYS[1],\n    'lease_expires_at_ms', ARGV[4],\n    'last_renewed_at_ms', ARGV[3],\n    'closed_at_ms', '0')\n  redis.call('HDEL', KEYS[1], 'close_reason')\n  return 1\nend\nreturn 0\n",
               List.of(this.key(id, persistentId)),
               List.of(startupId.toString(), fence.toString(), Long.toString(now), Long.toString(expires))
            );
         } catch (Throwable var16) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }
            }

            throw var16;
         }

         if (j != null) {
            j.close();
         }

         return number(result) == 1L
            ? OwnershipRenewResult.renewed(persistentId, startupId, fence, now, expires)
            : OwnershipRenewResult.lost(persistentId, startupId, fence, now);
      } catch (RuntimeException var17) {
         throw new BackendException("failed to renew Redis server ownership", var17);
      }
   }

   @Override
   public boolean closeOwnership(@NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, @NotNull String reason) throws BackendException {
      try {
         long now = this.dbNowEpochMs();
         Jedis j = this.pool.getResource();

         Object result;
         try {
            result = j.eval(
               "local owner = redis.call('HGET', KEYS[1], 'owner_startup_id')\nlocal fence = redis.call('HGET', KEYS[1], 'fence')\nlocal closed = tonumber(redis.call('HGET', KEYS[1], 'closed_at_ms') or '0')\nif owner == ARGV[1] and fence == ARGV[2] and closed == 0 then\n  redis.call('HSET', KEYS[1],\n    'closed_at_ms', ARGV[3],\n    'close_reason', ARGV[4])\n  return 1\nend\nreturn 0\n",
               List.of(this.key(id, persistentId)),
               List.of(startupId.toString(), fence.toString(), Long.toString(now), reason)
            );
         } catch (Throwable var13) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (j != null) {
            j.close();
         }

         return number(result) == 1L;
      } catch (RuntimeException var14) {
         throw new BackendException("failed to close Redis server ownership", var14);
      }
   }

   @NotNull
   @Override
   public Optional<ServerOwnershipSnapshot> readOwnership(@NotNull StoreId id, @NotNull UUID persistentId) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         Optional var9;
         label50: {
            try {
               Map<String, String> h = j.hgetAll(this.key(id, persistentId));
               if (h != null && !h.isEmpty()) {
                  var9 = Optional.of(snapshot(h));
                  break label50;
               }

               var9 = Optional.empty();
            } catch (Throwable var7) {
               if (j != null) {
                  try {
                     j.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (j != null) {
               j.close();
            }

            return var9;
         }

         if (j != null) {
            j.close();
         }

         return var9;
      } catch (RuntimeException var8) {
         throw new BackendException("failed to read Redis server ownership", var8);
      }
   }

   @NotNull
   private String key(@NotNull StoreId id, @NotNull UUID persistentId) {
      return this.keyPrefix + id.name() + ":" + persistentId;
   }

   @NotNull
   private static ServerOwnershipSnapshot snapshot(@NotNull Map<String, String> h) {
      return new ServerOwnershipSnapshot(
         UUID.fromString(h.get("persistent_id")),
         UUID.fromString(h.get("owner_startup_id")),
         UUID.fromString(h.get("fence")),
         parseLong(h.get("lease_expires_at_ms")),
         parseLong(h.get("last_renewed_at_ms")),
         parseLong(h.get("closed_at_ms")),
         emptyToNull(h.get("close_reason")),
         emptyToNull(h.get("server_name")),
         emptyToNull(h.get("hostname")),
         emptyToNull(h.get("grim_version")),
         emptyToNull(h.get("server_version"))
      );
   }

   private static long parseLong(String raw) {
      return raw != null && !raw.isEmpty() ? Long.parseLong(raw) : 0L;
   }

   private static long number(Object value) {
      return value instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(value));
   }

   @NotNull
   private static String nullToEmpty(String value) {
      return value == null ? "" : value;
   }

   private static String emptyToNull(String value) {
      return value != null && !value.isEmpty() ? value : null;
   }
}
