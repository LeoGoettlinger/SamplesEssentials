package ac.grim.grimac.internal.storage.backend.redis;

import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@ApiStatus.Internal
final class RedisCheckCatalogPersistence implements CheckCatalogPersistence {
   private static final String UPSERT_SCRIPT = "local existingId = redis.call('GET', KEYS[1]); if existingId and existingId ~= ARGV[1] then return {'stable', existingId}; end; local existingStable = redis.call('HGET', KEYS[2], 'stable_key'); if existingStable and existingStable ~= ARGV[2] then return {'id', existingStable}; end; redis.call('SET', KEYS[1], ARGV[1]); redis.call('DEL', KEYS[2]); redis.call('HSET', KEYS[2], 'check_id', ARGV[1], 'stable_key', ARGV[2], 'introduced_at', ARGV[9]); if ARGV[3] == '1' then redis.call('HSET', KEYS[2], 'display', ARGV[4]); end; if ARGV[5] == '1' then redis.call('HSET', KEYS[2], 'description', ARGV[6]); end; if ARGV[7] == '1' then redis.call('HSET', KEYS[2], 'introduced_version', ARGV[8]); end; redis.call('SADD', KEYS[3], ARGV[1]); local cur = redis.call('HGET', KEYS[4], 'check_id_seq'); if (not cur) or (tonumber(cur) < tonumber(ARGV[1])) then redis.call('HSET', KEYS[4], 'check_id_seq', ARGV[1]); end; return {'ok'};";
   private final JedisPool pool;
   private final String metaKey;
   private final String checksKey;

   RedisCheckCatalogPersistence(JedisPool pool, String metaKey, String checksKey) {
      this.pool = pool;
      this.metaKey = metaKey;
      this.checksKey = checksKey;
   }

   @Override
   public Iterable<CheckCatalogRow> loadAll() {
      List<CheckCatalogRow> out = new ArrayList<>();
      Jedis j = this.pool.getResource();

      try {
         for (String id : j.smembers(this.idsKey())) {
            Map<String, String> h = j.hgetAll(this.rowKey(id));
            CheckCatalogRow row = toRow(id, h);
            if (row != null) {
               out.add(row);
            }
         }
      } catch (Throwable var8) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (j != null) {
         j.close();
      }

      return out;
   }

   @Override
   public int insert(String stableKey, @Nullable String display, @Nullable String description, @Nullable String introducedVersion, long introducedAt) {
      Jedis j = this.pool.getResource();

      int var17;
      label74: {
         int var19;
         label75: {
            try {
               String stableKeyIndex = this.stableKeyIndex(stableKey);
               String existing = j.get(stableKeyIndex);
               if (existing != null) {
                  var17 = Integer.parseInt(existing);
                  break label74;
               }

               long id = j.hincrBy(this.metaKey, "check_id_seq", 1L);
               String idString = Long.toString(id);
               if (j.setnx(stableKeyIndex, idString) == 0L) {
                  String raced = j.get(stableKeyIndex);
                  if (raced == null) {
                     throw new IllegalStateException("Redis check stable-key index vanished during insert");
                  }

                  var19 = Integer.parseInt(raced);
                  break label75;
               }

               Map<String, String> h = new HashMap<>();
               h.put("check_id", idString);
               h.put("stable_key", stableKey);
               if (display != null) {
                  h.put("display", display);
               }

               if (description != null) {
                  h.put("description", description);
               }

               if (introducedVersion != null) {
                  h.put("introduced_version", introducedVersion);
               }

               h.put("introduced_at", Long.toString(introducedAt));
               j.hset(this.rowKey(idString), h);
               j.sadd(this.idsKey(), new String[]{idString});
               var19 = (int)id;
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

            return var19;
         }

         if (j != null) {
            j.close();
         }

         return var19;
      }

      if (j != null) {
         j.close();
      }

      return var17;
   }

   @Override
   public void upsert(CheckCatalogRow row) {
      Jedis j = this.pool.getResource();

      try {
         String id = Integer.toString(row.checkId());
         Object result = j.eval(
            "local existingId = redis.call('GET', KEYS[1]); if existingId and existingId ~= ARGV[1] then return {'stable', existingId}; end; local existingStable = redis.call('HGET', KEYS[2], 'stable_key'); if existingStable and existingStable ~= ARGV[2] then return {'id', existingStable}; end; redis.call('SET', KEYS[1], ARGV[1]); redis.call('DEL', KEYS[2]); redis.call('HSET', KEYS[2], 'check_id', ARGV[1], 'stable_key', ARGV[2], 'introduced_at', ARGV[9]); if ARGV[3] == '1' then redis.call('HSET', KEYS[2], 'display', ARGV[4]); end; if ARGV[5] == '1' then redis.call('HSET', KEYS[2], 'description', ARGV[6]); end; if ARGV[7] == '1' then redis.call('HSET', KEYS[2], 'introduced_version', ARGV[8]); end; redis.call('SADD', KEYS[3], ARGV[1]); local cur = redis.call('HGET', KEYS[4], 'check_id_seq'); if (not cur) or (tonumber(cur) < tonumber(ARGV[1])) then redis.call('HSET', KEYS[4], 'check_id_seq', ARGV[1]); end; return {'ok'};",
            List.of(this.stableKeyIndex(row.stableKey()), this.rowKey(id), this.idsKey(), this.metaKey),
            List.of(
               id,
               row.stableKey(),
               row.display() == null ? "0" : "1",
               row.display() == null ? "" : row.display(),
               row.description() == null ? "0" : "1",
               row.description() == null ? "" : row.description(),
               row.introducedVersion() == null ? "0" : "1",
               row.introducedVersion() == null ? "" : row.introducedVersion(),
               Long.toString(row.introducedAt())
            )
         );
         handleUpsertResult(row, id, result);
      } catch (Throwable var6) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (j != null) {
         j.close();
      }
   }

   @Override
   public void updateDisplayAndDescription(int checkId, @Nullable String display, @Nullable String description) {
      Jedis j = this.pool.getResource();

      try {
         String key = this.rowKey(Integer.toString(checkId));
         if (display == null) {
            j.hdel(key, new String[]{"display"});
         } else {
            j.hset(key, "display", display);
         }

         if (description == null) {
            j.hdel(key, new String[]{"description"});
         } else {
            j.hset(key, "description", description);
         }
      } catch (Throwable var8) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (j != null) {
         j.close();
      }
   }

   private String idsKey() {
      return this.checksKey + ":ids";
   }

   private String rowKey(String id) {
      return this.checksKey + ":" + id;
   }

   private String stableKeyIndex(String stableKey) {
      String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(stableKey.getBytes(StandardCharsets.UTF_8));
      return this.checksKey + ":by-stable:" + encoded;
   }

   @Nullable
   private static CheckCatalogRow toRow(String id, Map<String, String> h) {
      if (h.isEmpty()) {
         return null;
      } else {
         String stableKey = h.get("stable_key");
         return stableKey == null
            ? null
            : new CheckCatalogRow(
               Integer.parseInt(h.getOrDefault("check_id", id)),
               stableKey,
               h.get("display"),
               h.get("description"),
               h.get("introduced_version"),
               parseLong(h.get("introduced_at"))
            );
      }
   }

   private static void handleUpsertResult(CheckCatalogRow row, String id, Object result) {
      if (result instanceof List<?> parts && !parts.isEmpty()) {
         String status = redisString(parts.get(0));
         if (!"ok".equals(status)) {
            String value = parts.size() > 1 ? redisString(parts.get(1)) : "unknown";
            if ("stable".equals(status)) {
               throw new IllegalStateException("stable key " + row.stableKey() + " already maps to check_id " + value + ", cannot import as " + id);
            } else if ("id".equals(status)) {
               throw new IllegalStateException("check_id " + id + " already maps to stable key " + value + ", cannot import " + row.stableKey());
            } else {
               throw new IllegalStateException("Redis check catalog upsert returned status " + status);
            }
         }
      } else {
         throw new IllegalStateException("Redis check catalog upsert returned " + result);
      }
   }

   private static String redisString(Object value) {
      return value instanceof byte[] bytes ? new String(bytes, StandardCharsets.UTF_8) : String.valueOf(value);
   }

   private static long parseLong(@Nullable String s) {
      if (s == null) {
         return 0L;
      } else {
         try {
            return Long.parseLong(s);
         } catch (NumberFormatException var2) {
            return 0L;
         }
      }
   }
}
