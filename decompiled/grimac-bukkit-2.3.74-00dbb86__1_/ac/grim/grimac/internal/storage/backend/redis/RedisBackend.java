package ac.grim.grimac.internal.storage.backend.redis;

import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.SettingEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.internal.storage.util.UuidV7;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.DefaultJedisClientConfig.Builder;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;

@ApiStatus.Internal
public final class RedisBackend implements Backend {
   public static final String ID = "redis";
   static final int CURRENT_SCHEMA_VERSION = 6;
   private final RedisBackendConfig config;
   private JedisPool pool;
   private Logger logger;

   public RedisBackend(RedisBackendConfig config) {
      this.config = config;
   }

   @NotNull
   @Override
   public String id() {
      return "redis";
   }

   @NotNull
   @Override
   public ApiVersion getApiVersion() {
      return ApiVersion.CURRENT;
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(Capability.INDEXED_KV, Capability.SETTINGS, Capability.PLAYER_IDENTITY, Capability.HISTORY, Capability.TIMESERIES_APPEND);
   }

   @NotNull
   @Override
   public Set<Category<?>> supportedCategories() {
      return Set.of(Categories.VIOLATION, Categories.SESSION, Categories.PLAYER_IDENTITY, Categories.SETTING);
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();

      try {
         Class.forName("redis.clients.jedis.Jedis");
      } catch (ClassNotFoundException var10) {
         throw new BackendException("jedis not on the classpath — shade it into the plugin jar or drop it into server/plugins", var10);
      }

      Builder b = DefaultJedisClientConfig.builder().database(this.config.database()).timeoutMillis(this.config.timeoutMs());
      if (this.config.user() != null && !this.config.user().isEmpty()) {
         b.user(this.config.user());
      }

      if (this.config.password() != null && !this.config.password().isEmpty()) {
         b.password(this.config.password());
      }

      try {
         JedisPoolConfig poolConfig = new JedisPoolConfig();
         poolConfig.setMaxTotal(16);
         this.pool = new JedisPool(poolConfig, new HostAndPort(this.config.host(), this.config.port()), b.build());
         Jedis j = this.pool.getResource();

         try {
            String existingVer = j.hget(this.metaKey(), "schema_version");
            if (existingVer == null) {
               j.hset(this.metaKey(), Map.of("schema_version", Integer.toString(6), "grim_core_version", "phase1"));
            } else {
               int existing;
               try {
                  existing = Integer.parseInt(existingVer);
               } catch (NumberFormatException var9) {
                  existing = 0;
               }

               if (existing < 6) {
                  this.migrateSchema(j, existing);
               }
            }
         } catch (Throwable var11) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (j != null) {
            j.close();
         }

         if (this.config.warnOnHistory()) {
            this.logger
               .warning(
                  "[grim-datastore] Redis backend '"
                     + this.config.keyPrefix()
                     + this.config.tableNames().violations()
                     + "' is serving violation/session history. Redis is primarily an in-memory store — ensure AOF persistence is enabled, or this history will disappear on restart. Set redis.warn-on-history=false to silence."
               );
         }
      } catch (RuntimeException var12) {
         throw new BackendException("failed to initialise Redis backend", var12);
      }
   }

   @NotNull
   @Override
   public CheckCatalogPersistence checkCatalog() {
      return new RedisCheckCatalogPersistence(this.pool, this.metaKey(), this.tableKey(this.config.tableNames().checks()));
   }

   @NotNull
   @Override
   public CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> legacyToCatalogCheckIds, String introducedVersionReplacement) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         CheckCatalogRepairResult var8;
         try {
            long violationsUpdated = this.repairViolationCheckIds(j, legacyToCatalogCheckIds);
            long versionsUpdated = this.repairStubCatalogVersions(j, introducedVersionReplacement);
            var8 = new CheckCatalogRepairResult(legacyToCatalogCheckIds.size(), violationsUpdated, versionsUpdated);
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

         return var8;
      } catch (RuntimeException var11) {
         throw new BackendException("check catalog repair failed", var11);
      }
   }

   private void migrateSchema(Jedis j, int existing) {
      if (existing < 6) {
         this.migrateV5ToV6(j);
      }

      j.hset(this.metaKey(), "schema_version", Integer.toString(6));
      j.hdel(this.metaKey(), new String[]{"violation_id_seq"});
   }

   private void migrateV5ToV6(Jedis j) {
      this.logger.info("[grim-datastore] migrating Redis violations id long→UUIDv7 …");
      String prefix = this.tableKey(this.config.tableNames().violations()) + ":";
      String pattern = prefix + "*";
      ScanParams sp = new ScanParams().match(pattern).count(500);
      String cursor = ScanParams.SCAN_POINTER_START;
      long migrated = 0L;

      do {
         ScanResult<String> res = j.scan(cursor, sp);
         cursor = res.getCursor();

         for (String oldKey : res.getResult()) {
            if (!oldKey.contains(":by-session:") && !oldKey.contains(":by-player:")) {
               Map<String, String> h = j.hgetAll(oldKey);
               if (!h.isEmpty()) {
                  String oldId = h.get("id");
                  if (oldId != null && (oldId.length() != 32 || !oldId.matches("[0-9a-fA-F]+"))) {
                     String occurredStr = h.get("occurred_at");
                     long occurred = occurredStr == null ? 0L : Long.parseLong(occurredStr);

                     long legacyId;
                     try {
                        legacyId = Long.parseLong(oldId);
                     } catch (NumberFormatException var25) {
                        legacyId = 0L;
                     }

                     UUID newId = UuidV7.fromTimestampMs(occurred, legacyId);
                     String newIdHex = hex(newId);
                     String newKey = prefix + newIdHex;
                     String sessionHex = h.get("session_id");
                     String playerHex = h.get("player_uuid");
                     h.put("id", newIdHex);
                     Pipeline p = j.pipelined();
                     p.hset(newKey, h);
                     p.del(oldKey);
                     if (sessionHex != null) {
                        String zSession = this.tableKey(this.config.tableNames().violations()) + ":by-session:" + sessionHex;
                        p.zrem(zSession, new String[]{oldId});
                        p.zadd(zSession, occurred, newIdHex);
                     }

                     if (playerHex != null) {
                        String zPlayer = this.tableKey(this.config.tableNames().violations()) + ":by-player:" + playerHex;
                        p.zrem(zPlayer, new String[]{oldId});
                        p.zadd(zPlayer, occurred, newIdHex);
                     }

                     p.sync();
                     migrated++;
                  }
               }
            }
         }
      } while (!ScanParams.SCAN_POINTER_START.equals(cursor));

      this.logger.info("[grim-datastore] migrated " + migrated + " violation rows to UUIDv7 id");
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      if (this.pool != null) {
         this.pool.close();
         this.pool = null;
      }
   }

   @NotNull
   @Override
   public <E> StorageEventHandler<E> eventHandlerFor(@NotNull Category<E> cat) throws BackendException {
      StorageEventHandler<?> h;
      if (cat == Categories.VIOLATION) {
         h = this::writeViolation;
      } else if (cat == Categories.SESSION) {
         h = this::writeSession;
      } else if (cat == Categories.PLAYER_IDENTITY) {
         h = this::writeIdentity;
      } else {
         if (cat != Categories.SETTING) {
            throw new IllegalArgumentException("unsupported category: " + cat.id());
         }

         h = this::writeSetting;
      }

      return (StorageEventHandler<E>)h;
   }

   private void writeViolation(ViolationEvent v, long sequence, boolean endOfBatch) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         try {
            UUID id = v.id();
            String idHex = hex(id);
            String sessionHex = hex(v.sessionId());
            String playerHex = hex(v.playerUuid());
            String recordKey = this.tableKey(this.config.tableNames().violations()) + ":" + idHex;
            Map<String, String> h = new HashMap<>();
            h.put("id", idHex);
            h.put("session_id", sessionHex);
            h.put("player_uuid", playerHex);
            h.put("check_id", Integer.toString(v.checkId()));
            h.put("vl", Double.toString(v.vl()));
            h.put("occurred_at", Long.toString(v.occurredEpochMs()));
            String verbose = encodeVerbose(v.verboseData(), v.verboseFormat());
            if (verbose != null) {
               h.put("verbose", verbose);
            }

            h.put("verbose_format", Integer.toString(v.verboseFormat().code()));
            Pipeline p = j.pipelined();
            p.hset(recordKey, h);
            p.zadd(this.tableKey(this.config.tableNames().violations()) + ":by-session:" + sessionHex, v.occurredEpochMs(), idHex);
            p.zadd(this.tableKey(this.config.tableNames().violations()) + ":by-player:" + playerHex, v.occurredEpochMs(), idHex);
            p.sync();
         } catch (Throwable var15) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }
            }

            throw var15;
         }

         if (j != null) {
            j.close();
         }
      } catch (RuntimeException var16) {
         throw new BackendException("violation write failed", var16);
      }
   }

   private void writeSession(SessionEvent s, long sequence, boolean endOfBatch) throws BackendException {
      if (!s.sessionBlobs().isEmpty()) {
         throw new BackendException("replay-clip serialisation isn't implemented by this backend");
      } else {
         try {
            Jedis j = this.pool.getResource();

            try {
               String sessionHex = hex(s.sessionId());
               String playerHex = hex(s.playerUuid());
               String key = this.tableKey(this.config.tableNames().sessions()) + ":" + sessionHex;
               Map<String, String> h = new HashMap<>();
               h.put("session_id", sessionHex);
               h.put("player_uuid", playerHex);
               if (s.serverName() != null) {
                  h.put("server_name", s.serverName());
               }

               h.put("started_at", Long.toString(s.startedEpochMs()));
               h.put("last_activity", Long.toString(s.lastActivityEpochMs()));
               if (s.isClosed()) {
                  h.put("closed_at", Long.toString(s.closedAtEpochMs()));
               }

               if (s.grimVersion() != null) {
                  h.put("grim_version", s.grimVersion());
               }

               if (s.clientBrand() != null) {
                  h.put("client_brand", s.clientBrand());
               }

               h.put("client_version_pvn", Integer.toString(s.clientVersion()));
               if (s.serverVersionString() != null) {
                  h.put("server_version", s.serverVersionString());
               }

               if (s.instanceId() != null) {
                  h.put("instance_id", hex(s.instanceId()));
               }

               Pipeline p = j.pipelined();
               p.hset(key, h);
               p.zadd(this.tableKey(this.config.tableNames().sessions()) + ":by-player:" + playerHex, s.startedEpochMs(), sessionHex);
               p.sync();
            } catch (Throwable var12) {
               if (j != null) {
                  try {
                     j.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }
               }

               throw var12;
            }

            if (j != null) {
               j.close();
            }
         } catch (RuntimeException var13) {
            throw new BackendException("session write failed", var13);
         }
      }
   }

   private void writeIdentity(PlayerIdentityEvent e, long sequence, boolean endOfBatch) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         try {
            String hex = hex(e.uuid());
            String key = this.tableKey(this.config.tableNames().players()) + ":" + hex;
            Map<String, String> existing = j.hgetAll(key);
            long firstSeen = existing.get("first_seen") == null
               ? e.firstSeenEpochMs()
               : Math.min(e.firstSeenEpochMs(), Long.parseLong(existing.get("first_seen")));
            long lastSeen = existing.get("last_seen") == null ? e.lastSeenEpochMs() : Math.max(e.lastSeenEpochMs(), Long.parseLong(existing.get("last_seen")));
            Map<String, String> h = new HashMap<>();
            h.put("uuid", hex);
            if (e.currentName() != null) {
               h.put("current_name", e.currentName());
            }

            h.put("first_seen", Long.toString(firstSeen));
            h.put("last_seen", Long.toString(lastSeen));
            Pipeline p = j.pipelined();
            p.hset(key, h);
            if (e.currentName() != null) {
               p.set(this.tableKey(this.config.tableNames().players()) + ":by-name-lower:" + e.currentName().toLowerCase(Locale.ROOT), hex);
            }

            p.sync();
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
      } catch (RuntimeException var17) {
         throw new BackendException("identity write failed", var17);
      }
   }

   private void writeSetting(SettingEvent s, long sequence, boolean endOfBatch) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         try {
            String key = this.tableKey(this.config.tableNames().settings()) + ":" + s.scope().name() + ":" + s.scopeKey() + ":" + s.key();
            Map<String, String> h = new HashMap<>();
            h.put("scope", s.scope().name());
            h.put("scope_key", s.scopeKey());
            h.put("key", s.key());
            h.put("value_b64", Base64.getEncoder().encodeToString(s.value()));
            h.put("updated_at", Long.toString(s.updatedEpochMs()));
            j.hset(key, h);
         } catch (Throwable var9) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (j != null) {
            j.close();
         }
      } catch (RuntimeException var10) {
         throw new BackendException("setting write failed", var10);
      }
   }

   @Override
   public <R> void bulkImport(@NotNull Category<?> cat, @NotNull List<R> records) throws BackendException {
      if (!records.isEmpty()) {
         try {
            if (cat == Categories.VIOLATION) {
               for (ViolationRecord v : records) {
                  this.bulkViolation(v);
               }
            } else if (cat == Categories.SESSION) {
               for (SessionRecord s : records) {
                  this.bulkSession(s);
               }
            } else if (cat == Categories.PLAYER_IDENTITY) {
               for (PlayerIdentity p : records) {
                  this.bulkIdentity(p);
               }
            } else {
               if (cat != Categories.SETTING) {
                  throw new BackendException("unsupported category: " + cat.id());
               }

               for (SettingRecord s : records) {
                  this.bulkSetting(s);
               }
            }
         } catch (RuntimeException var5) {
            throw new BackendException("bulkImport failed for " + cat.id(), var5);
         }
      }
   }

   private void bulkViolation(ViolationRecord v) {
      Jedis j = this.pool.getResource();

      try {
         String idHex = hex(v.id());
         String sessionHex = hex(v.sessionId());
         String playerHex = hex(v.playerUuid());
         String key = this.tableKey(this.config.tableNames().violations()) + ":" + idHex;
         Map<String, String> h = new HashMap<>();
         h.put("id", idHex);
         h.put("session_id", sessionHex);
         h.put("player_uuid", playerHex);
         h.put("check_id", Integer.toString(v.checkId()));
         h.put("vl", Double.toString(v.vl()));
         h.put("occurred_at", Long.toString(v.occurredEpochMs()));
         String verbose = encodeVerbose(v.verboseData(), v.verboseFormat());
         if (verbose != null) {
            h.put("verbose", verbose);
         }

         h.put("verbose_format", Integer.toString(v.verboseFormat().code()));
         Pipeline p = j.pipelined();
         p.hset(key, h);
         p.zadd(this.tableKey(this.config.tableNames().violations()) + ":by-session:" + sessionHex, v.occurredEpochMs(), idHex);
         p.zadd(this.tableKey(this.config.tableNames().violations()) + ":by-player:" + playerHex, v.occurredEpochMs(), idHex);
         p.sync();
      } catch (Throwable var11) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (j != null) {
         j.close();
      }
   }

   private void bulkSession(SessionRecord s) {
      Jedis j = this.pool.getResource();

      try {
         String sessionHex = hex(s.sessionId());
         String playerHex = hex(s.playerUuid());
         String key = this.tableKey(this.config.tableNames().sessions()) + ":" + sessionHex;
         Map<String, String> h = new HashMap<>();
         h.put("session_id", sessionHex);
         h.put("player_uuid", playerHex);
         if (s.serverName() != null) {
            h.put("server_name", s.serverName());
         }

         h.put("started_at", Long.toString(s.startedEpochMs()));
         h.put("last_activity", Long.toString(s.lastActivityEpochMs()));
         if (s.isClosed()) {
            h.put("closed_at", Long.toString(s.closedAtEpochMs()));
         }

         if (s.grimVersion() != null) {
            h.put("grim_version", s.grimVersion());
         }

         if (s.clientBrand() != null) {
            h.put("client_brand", s.clientBrand());
         }

         h.put("client_version_pvn", Integer.toString(s.clientVersion()));
         if (s.serverVersionString() != null) {
            h.put("server_version", s.serverVersionString());
         }

         if (s.instanceId() != null) {
            h.put("instance_id", hex(s.instanceId()));
         }

         Pipeline p = j.pipelined();
         p.hset(key, h);
         p.zadd(this.tableKey(this.config.tableNames().sessions()) + ":by-player:" + playerHex, s.startedEpochMs(), sessionHex);
         p.sync();
      } catch (Throwable var9) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (j != null) {
         j.close();
      }
   }

   private void bulkIdentity(PlayerIdentity id) {
      Jedis j = this.pool.getResource();

      try {
         String hex = hex(id.uuid());
         String key = this.tableKey(this.config.tableNames().players()) + ":" + hex;
         Map<String, String> h = new HashMap<>();
         h.put("uuid", hex);
         if (id.currentName() != null) {
            h.put("current_name", id.currentName());
         }

         h.put("first_seen", Long.toString(id.firstSeenEpochMs()));
         h.put("last_seen", Long.toString(id.lastSeenEpochMs()));
         Pipeline p = j.pipelined();
         p.hset(key, h);
         if (id.currentName() != null) {
            p.set(this.tableKey(this.config.tableNames().players()) + ":by-name-lower:" + id.currentName().toLowerCase(Locale.ROOT), hex);
         }

         p.sync();
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

   private void bulkSetting(SettingRecord s) {
      Jedis j = this.pool.getResource();

      try {
         String key = this.tableKey(this.config.tableNames().settings()) + ":" + s.scope().name() + ":" + s.scopeKey() + ":" + s.key();
         Map<String, String> h = new HashMap<>();
         h.put("scope", s.scope().name());
         h.put("scope_key", s.scopeKey());
         h.put("key", s.key());
         h.put("value_b64", Base64.getEncoder().encodeToString(s.value()));
         h.put("updated_at", Long.toString(s.updatedEpochMs()));
         j.hset(key, h);
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

   @NotNull
   @Override
   public <R> Page<R> read(@NotNull Category<?> cat, @NotNull Query<R> query) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         Page var20;
         label118: {
            label119: {
               label120: {
                  label121: {
                     label122: {
                        label123: {
                           try {
                              if (query instanceof Queries.ListSessionsByPlayer q) {
                                 var20 = this.listSessionsByPlayer(j, q);
                                 break label118;
                              }

                              if (query instanceof Queries.GetSessionById q) {
                                 var20 = this.getSessionById(j, q);
                                 break label119;
                              }

                              if (query instanceof Queries.ListViolationsInSession q) {
                                 var20 = this.listViolationsInSession(j, q);
                                 break label120;
                              }

                              if (query instanceof Queries.GetPlayerIdentity q) {
                                 var20 = this.getPlayerIdentity(j, q);
                                 break label121;
                              }

                              if (query instanceof Queries.GetPlayerIdentityByName q) {
                                 var20 = this.getPlayerIdentityByName(j, q);
                                 break label122;
                              }

                              if (query instanceof Queries.ListPlayersByNamePrefix q) {
                                 var20 = this.listPlayersByNamePrefix(j, q);
                                 break label123;
                              }

                              if (!(query instanceof Queries.GetSetting q)) {
                                 throw new BackendException("unsupported query: " + query.getClass().getSimpleName());
                              }

                              var20 = this.getSetting(j, q);
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

                           return var20;
                        }

                        if (j != null) {
                           j.close();
                        }

                        return var20;
                     }

                     if (j != null) {
                        j.close();
                     }

                     return var20;
                  }

                  if (j != null) {
                     j.close();
                  }

                  return var20;
               }

               if (j != null) {
                  j.close();
               }

               return var20;
            }

            if (j != null) {
               j.close();
            }

            return var20;
         }

         if (j != null) {
            j.close();
         }

         return var20;
      } catch (RuntimeException var8) {
         throw new BackendException("read failed", var8);
      }
   }

   private Page<SessionRecord> listSessionsByPlayer(Jedis j, Queries.ListSessionsByPlayer q) {
      long cursorStarted = decodeStartedCursor(q.cursor(), Long.MAX_VALUE);
      String zsetKey = this.tableKey(this.config.tableNames().sessions()) + ":by-player:" + hex(q.player());
      String max = q.cursor() == null ? "+inf" : "(" + cursorStarted;
      List<Tuple> members = j.zrevrangeByScoreWithScores(zsetKey, max, "-inf", 0, q.pageSize() + 1);
      List<SessionRecord> out = new ArrayList<>();
      boolean hasMore = false;

      for (Tuple t : members) {
         if (out.size() >= q.pageSize()) {
            hasMore = true;
            break;
         }

         Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().sessions()) + ":" + t.getElement());
         if (!h.isEmpty()) {
            out.add(mapSession(h));
         }
      }

      Cursor next = null;
      if (hasMore && !out.isEmpty()) {
         SessionRecord last = out.get(out.size() - 1);
         next = encodeStartedCursor(last.startedEpochMs(), last.sessionId());
      }

      return new Page<>(out, next);
   }

   private Page<SessionRecord> getSessionById(Jedis j, Queries.GetSessionById q) {
      Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().sessions()) + ":" + hex(q.sessionId()));
      return h.isEmpty() ? Page.empty() : new Page<>(List.of(mapSession(h)), null);
   }

   private Page<ViolationRecord> listViolationsInSession(Jedis j, Queries.ListViolationsInSession q) {
      long cursorScore = decodeViolationOccurredCursor(q.cursor(), Long.MIN_VALUE);
      UUID cursorId = decodeViolationIdCursor(q.cursor());
      String zsetKey = this.tableKey(this.config.tableNames().violations()) + ":by-session:" + hex(q.sessionId());
      String min = cursorId == null ? "-inf" : Long.toString(cursorScore);
      String cursorMemberHex = cursorId == null ? null : hex(cursorId);
      List<ViolationRecord> out = new ArrayList<>();
      boolean hasMore = false;
      boolean skipping = cursorId != null;
      long offset = 0L;
      int wanted = q.pageSize() + 1;

      while (out.size() < wanted) {
         List<Tuple> members = j.zrangeByScoreWithScores(zsetKey, min, "+inf", (int)offset, wanted - out.size());
         if (members.isEmpty()) {
            break;
         }

         offset += members.size();

         for (Tuple t : members) {
            if (skipping) {
               if ((long)t.getScore() == cursorScore && t.getElement().compareTo(cursorMemberHex) <= 0) {
                  continue;
               }

               skipping = false;
            }

            if (out.size() >= q.pageSize()) {
               hasMore = true;
               break;
            }

            Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().violations()) + ":" + t.getElement());
            if (!h.isEmpty()) {
               out.add(mapViolation(h));
            }
         }

         if (hasMore || members.size() < wanted - out.size()) {
            break;
         }
      }

      Cursor next = null;
      if (hasMore && !out.isEmpty()) {
         ViolationRecord last = out.get(out.size() - 1);
         next = encodeViolationCursor(last.occurredEpochMs(), last.id());
      }

      return new Page<>(out, next);
   }

   private Page<PlayerIdentity> getPlayerIdentity(Jedis j, Queries.GetPlayerIdentity q) {
      Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().players()) + ":" + hex(q.uuid()));
      return h.isEmpty() ? Page.empty() : new Page<>(List.of(mapIdentity(h)), null);
   }

   private Page<PlayerIdentity> getPlayerIdentityByName(Jedis j, Queries.GetPlayerIdentityByName q) {
      String hexId = j.get(this.tableKey(this.config.tableNames().players()) + ":by-name-lower:" + q.name().toLowerCase(Locale.ROOT));
      if (hexId == null) {
         return Page.empty();
      } else {
         Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().players()) + ":" + hexId);
         return h.isEmpty() ? Page.empty() : new Page<>(List.of(mapIdentity(h)), null);
      }
   }

   private Page<PlayerIdentity> listPlayersByNamePrefix(Jedis j, Queries.ListPlayersByNamePrefix q) {
      String prefix = q.lowerPrefix();
      if (prefix != null && !prefix.isEmpty() && q.limit() > 0) {
         String pattern = this.tableKey(this.config.tableNames().players()) + ":by-name-lower:" + globEscape(prefix) + "*";
         ScanParams sp = new ScanParams().match(pattern).count(Math.min(Math.max(q.limit() * 4, 50), 500));
         List<PlayerIdentity> out = new ArrayList<>();
         int maxCandidates = Math.max(q.limit() * 8, 200);
         String cursor = ScanParams.SCAN_POINTER_START;

         do {
            ScanResult<String> r = j.scan(cursor, sp);
            cursor = r.getCursor();

            for (String key : r.getResult()) {
               String hexId = j.get(key);
               if (hexId != null) {
                  Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().players()) + ":" + hexId);
                  if (!h.isEmpty()) {
                     out.add(mapIdentity(h));
                     if (out.size() >= maxCandidates) {
                        break;
                     }
                  }
               }
            }
         } while (out.size() < maxCandidates && !cursor.equals(ScanParams.SCAN_POINTER_START));

         out.sort(Comparator.comparingLong(PlayerIdentity::lastSeenEpochMs).reversed());
         if (out.size() > q.limit()) {
            out = out.subList(0, q.limit());
         }

         return new Page<>(out, null);
      } else {
         return Page.empty();
      }
   }

   private static String globEscape(String s) {
      StringBuilder out = new StringBuilder(s.length());

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '*' || c == '?' || c == '[' || c == ']' || c == '\\') {
            out.append('\\');
         }

         out.append(c);
      }

      return out.toString();
   }

   private Page<SettingRecord> getSetting(Jedis j, Queries.GetSetting q) {
      Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().settings()) + ":" + q.scope().name() + ":" + q.scopeKey() + ":" + q.key());
      return h.isEmpty() ? Page.empty() : new Page<>(List.of(mapSetting(h)), null);
   }

   private static SessionRecord mapSession(Map<String, String> h) {
      String closedAt = h.get("closed_at");
      String instanceHex = h.get("instance_id");
      UUID instanceId = instanceHex != null && !instanceHex.isEmpty() ? fromHex(instanceHex) : null;
      return new SessionRecord(
         fromHex(h.get("session_id")),
         fromHex(h.get("player_uuid")),
         h.get("server_name"),
         Long.parseLong(h.get("started_at")),
         Long.parseLong(h.get("last_activity")),
         closedAt != null && !closedAt.isEmpty() ? Long.parseLong(closedAt) : 0L,
         h.get("grim_version"),
         h.get("client_brand"),
         Integer.parseInt(h.getOrDefault("client_version_pvn", "-1")),
         h.get("server_version"),
         instanceId,
         List.of()
      );
   }

   private static ViolationRecord mapViolation(Map<String, String> h) {
      VerboseFormat verboseFormat = VerboseFormat.fromCode(Integer.parseInt(h.getOrDefault("verbose_format", "0")));
      return new ViolationRecord(
         fromHex(h.get("id")),
         fromHex(h.get("session_id")),
         fromHex(h.get("player_uuid")),
         Integer.parseInt(h.get("check_id")),
         Double.parseDouble(h.get("vl")),
         Long.parseLong(h.get("occurred_at")),
         decodeVerbose(h.get("verbose"), verboseFormat),
         verboseFormat
      );
   }

   private static PlayerIdentity mapIdentity(Map<String, String> h) {
      return new PlayerIdentity(fromHex(h.get("uuid")), h.get("current_name"), Long.parseLong(h.get("first_seen")), Long.parseLong(h.get("last_seen")));
   }

   private static SettingRecord mapSetting(Map<String, String> h) {
      return new SettingRecord(
         SettingScope.valueOf(h.get("scope")),
         h.get("scope_key"),
         h.get("key"),
         Base64.getDecoder().decode(h.get("value_b64")),
         Long.parseLong(h.get("updated_at"))
      );
   }

   private static String encodeVerbose(byte[] verboseData, VerboseFormat verboseFormat) {
      if (verboseData == null) {
         return null;
      } else {
         return verboseFormat == VerboseFormat.STRUCTURED_V1
            ? Base64.getEncoder().encodeToString(verboseData)
            : new String(verboseData, StandardCharsets.UTF_8);
      }
   }

   private static byte[] decodeVerbose(String verbose, VerboseFormat verboseFormat) {
      if (verbose == null) {
         return null;
      } else {
         return verboseFormat == VerboseFormat.STRUCTURED_V1 ? Base64.getDecoder().decode(verbose) : verbose.getBytes(StandardCharsets.UTF_8);
      }
   }

   @Override
   public <E> void delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         try {
            if (criteria instanceof Deletes.ByPlayer d) {
               String phex = hex(d.uuid());
               if (cat == Categories.VIOLATION) {
                  this.deletePlayerViolations(j, phex);
               } else if (cat == Categories.SESSION) {
                  this.deletePlayerViolations(j, phex);
                  this.deletePlayerSessions(j, phex);
               } else if (cat == Categories.PLAYER_IDENTITY) {
                  this.deletePlayerIdentity(j, d.uuid(), phex);
               } else {
                  if (cat != Categories.SETTING) {
                     throw new BackendException("unsupported category for delete: " + cat.id());
                  }

                  this.scanDel(j, this.tableKey(this.config.tableNames().settings()) + ":PLAYER:" + d.uuid() + ":*");
               }
            } else {
               if (!(criteria instanceof Deletes.OlderThan dx)) {
                  throw new BackendException("unknown DeleteCriteria: " + criteria.getClass().getSimpleName());
               }

               long cutoff = System.currentTimeMillis() - dx.maxAgeMs();
               if (cat == Categories.SESSION) {
                  this.deleteSessionsOlderThan(j, cutoff);
               } else {
                  if (cat != Categories.VIOLATION) {
                     throw new BackendException("unsupported category for retention: " + cat.id());
                  }

                  this.deleteViolationsOlderThan(j, cutoff);
               }
            }
         } catch (Throwable var9) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (j != null) {
            j.close();
         }
      } catch (RuntimeException var10) {
         throw new BackendException("delete failed", var10);
      }
   }

   private void deletePlayerViolations(Jedis j, String phex) {
      String zsetKey = this.tableKey(this.config.tableNames().violations()) + ":by-player:" + phex;

      for (String id : j.zrange(zsetKey, 0L, -1L)) {
         String recordKey = this.tableKey(this.config.tableNames().violations()) + ":" + id;
         Map<String, String> h = j.hgetAll(recordKey);
         if (!h.isEmpty()) {
            j.zrem(this.tableKey(this.config.tableNames().violations()) + ":by-session:" + h.get("session_id"), new String[]{id});
         }

         j.del(recordKey);
      }

      j.del(zsetKey);
   }

   private void deletePlayerSessions(Jedis j, String phex) {
      String zsetKey = this.tableKey(this.config.tableNames().sessions()) + ":by-player:" + phex;

      for (String sessionHex : j.zrange(zsetKey, 0L, -1L)) {
         j.del(this.tableKey(this.config.tableNames().sessions()) + ":" + sessionHex);
      }

      j.del(zsetKey);
   }

   private void deletePlayerIdentity(Jedis j, UUID uuid, String phex) {
      Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().players()) + ":" + phex);
      if (!h.isEmpty() && h.get("current_name") != null) {
         j.del(this.tableKey(this.config.tableNames().players()) + ":by-name-lower:" + h.get("current_name").toLowerCase(Locale.ROOT));
      }

      j.del(this.tableKey(this.config.tableNames().players()) + ":" + phex);
   }

   private void deleteSessionsOlderThan(Jedis j, long cutoff) {
      String pattern = this.tableKey(this.config.tableNames().sessions()) + ":by-player:*";
      ScanParams sp = new ScanParams().match(pattern).count(100);
      String cursor = ScanParams.SCAN_POINTER_START;

      do {
         ScanResult<String> res = j.scan(cursor, sp);
         cursor = res.getCursor();

         for (String zsetKey : res.getResult()) {
            for (Tuple t : j.zrangeByScoreWithScores(zsetKey, "-inf", "(" + cutoff)) {
               String sessionHex = t.getElement();
               String recordKey = this.tableKey(this.config.tableNames().sessions()) + ":" + sessionHex;
               j.del(recordKey);
               j.zrem(zsetKey, new String[]{sessionHex});
               String vZset = this.tableKey(this.config.tableNames().violations()) + ":by-session:" + sessionHex;

               for (String vid : j.zrange(vZset, 0L, -1L)) {
                  String vkey = this.tableKey(this.config.tableNames().violations()) + ":" + vid;
                  Map<String, String> h = j.hgetAll(vkey);
                  if (!h.isEmpty()) {
                     j.zrem(this.tableKey(this.config.tableNames().violations()) + ":by-player:" + h.get("player_uuid"), new String[]{vid});
                  }

                  j.del(vkey);
               }

               j.del(vZset);
            }
         }
      } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
   }

   private void deleteViolationsOlderThan(Jedis j, long cutoff) {
      String pattern = this.tableKey(this.config.tableNames().violations()) + ":by-session:*";
      ScanParams sp = new ScanParams().match(pattern).count(100);
      String cursor = ScanParams.SCAN_POINTER_START;

      do {
         ScanResult<String> res = j.scan(cursor, sp);
         cursor = res.getCursor();

         for (String zsetKey : res.getResult()) {
            for (Tuple t : j.zrangeByScoreWithScores(zsetKey, "-inf", "(" + cutoff)) {
               String id = t.getElement();
               String vkey = this.tableKey(this.config.tableNames().violations()) + ":" + id;
               Map<String, String> h = j.hgetAll(vkey);
               if (!h.isEmpty()) {
                  j.zrem(this.tableKey(this.config.tableNames().violations()) + ":by-player:" + h.get("player_uuid"), new String[]{id});
               }

               j.del(vkey);
               j.zrem(zsetKey, new String[]{id});
            }
         }
      } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
   }

   private void scanDel(Jedis j, String pattern) {
      ScanParams sp = new ScanParams().match(pattern).count(100);
      String cursor = ScanParams.SCAN_POINTER_START;

      do {
         ScanResult<String> res = j.scan(cursor, sp);
         cursor = res.getCursor();

         for (String k : res.getResult()) {
            j.del(k);
         }
      } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
   }

   private long repairViolationCheckIds(Jedis j, Map<Integer, Integer> legacyToCatalogCheckIds) {
      if (legacyToCatalogCheckIds.isEmpty()) {
         return 0L;
      } else {
         String violationsBase = this.tableKey(this.config.tableNames().violations());
         String bySessionPrefix = violationsBase + ":by-session:";
         String byPlayerPrefix = violationsBase + ":by-player:";
         long updated = 0L;
         ScanParams sp = new ScanParams().match(violationsBase + ":*").count(500);
         String cursor = ScanParams.SCAN_POINTER_START;

         do {
            ScanResult<String> res = j.scan(cursor, sp);
            cursor = res.getCursor();

            for (String key : res.getResult()) {
               if (!key.startsWith(bySessionPrefix) && !key.startsWith(byPlayerPrefix)) {
                  String raw = j.hget(key, "check_id");
                  if (raw != null) {
                     int legacyId;
                     try {
                        legacyId = Integer.parseInt(raw);
                     } catch (NumberFormatException var16) {
                        continue;
                     }

                     Integer catalogId = legacyToCatalogCheckIds.get(legacyId);
                     if (catalogId != null && catalogId != legacyId) {
                        j.hset(key, "check_id", Integer.toString(catalogId));
                        updated++;
                     }
                  }
               }
            }
         } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

         return updated;
      }
   }

   private long repairStubCatalogVersions(Jedis j, String introducedVersionReplacement) {
      if (introducedVersionReplacement != null && !introducedVersionReplacement.isBlank()) {
         String checksBase = this.tableKey(this.config.tableNames().checks());
         long updated = 0L;

         for (String id : j.smembers(checksBase + ":ids")) {
            String rowKey = checksBase + ":" + id;
            if ("0.0.0-stub".equals(j.hget(rowKey, "introduced_version"))) {
               j.hset(rowKey, "introduced_version", introducedVersionReplacement);
               updated++;
            }
         }

         return updated;
      } else {
         return 0L;
      }
   }

   @Override
   public long countViolationsInSession(@NotNull UUID sessionId) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         long var3;
         try {
            var3 = j.zcard(this.tableKey(this.config.tableNames().violations()) + ":by-session:" + hex(sessionId));
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

         return var3;
      } catch (RuntimeException var7) {
         throw new BackendException("countViolationsInSession failed", var7);
      }
   }

   @Override
   public long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         long var11;
         try {
            String zsetKey = this.tableKey(this.config.tableNames().violations()) + ":by-session:" + hex(sessionId);
            Set<String> seenChecks = new HashSet<>();

            for (String id : j.zrange(zsetKey, 0L, -1L)) {
               Map<String, String> h = j.hgetAll(this.tableKey(this.config.tableNames().violations()) + ":" + id);
               if (!h.isEmpty()) {
                  seenChecks.add(h.get("check_id"));
               }
            }

            var11 = seenChecks.size();
         } catch (Throwable var9) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (j != null) {
            j.close();
         }

         return var11;
      } catch (RuntimeException var10) {
         throw new BackendException("countUniqueChecksInSession failed", var10);
      }
   }

   @Override
   public long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
      try {
         Jedis j = this.pool.getResource();

         long var3;
         try {
            var3 = j.zcard(this.tableKey(this.config.tableNames().sessions()) + ":by-player:" + hex(player));
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

         return var3;
      } catch (RuntimeException var7) {
         throw new BackendException("countSessionsByPlayer failed", var7);
      }
   }

   @Override
   public long markCrashedSessions() throws BackendException {
      long affected = 0L;

      try {
         Jedis j = this.pool.getResource();

         long var16;
         try {
            String prefix = this.tableKey(this.config.tableNames().sessions()) + ":";
            String matchPattern = prefix + "*";
            ScanParams sp = new ScanParams().match(matchPattern).count(500);
            String cursor = ScanParams.SCAN_POINTER_START;

            do {
               ScanResult<String> res = j.scan(cursor, sp);

               for (String key : res.getResult()) {
                  if (!key.contains(":by-player:") && !key.contains(":seq:")) {
                     Map<String, String> h = j.hgetAll(key);
                     if (!h.isEmpty() && h.get("closed_at") == null) {
                        String lastActivity = h.get("last_activity");
                        if (lastActivity != null) {
                           j.hset(key, "closed_at", lastActivity);
                           affected++;
                        }
                     }
                  }
               }

               cursor = res.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));

            var16 = affected;
         } catch (Throwable var14) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }
            }

            throw var14;
         }

         if (j != null) {
            j.close();
         }

         return var16;
      } catch (RuntimeException var15) {
         throw new BackendException("markCrashedSessions failed", var15);
      }
   }

   private String metaKey() {
      return this.tableKey(this.config.tableNames().meta());
   }

   private String tableKey(String logicalName) {
      return this.config.keyPrefix() + logicalName;
   }

   private static String hex(UUID u) {
      return u.toString().replace("-", "");
   }

   private static UUID fromHex(String hex) {
      if (hex != null && hex.length() == 32) {
         byte[] bytes = new byte[16];

         for (int i = 0; i < 16; i++) {
            bytes[i] = (byte)Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
         }

         return UuidCodec.fromBytes(bytes);
      } else {
         throw new IllegalArgumentException("uuid hex must be 32 chars, got " + (hex == null ? "null" : hex.length()));
      }
   }

   private static Cursor encodeStartedCursor(long started, UUID sessionId) {
      return new Cursor(started + ":" + hex(sessionId));
   }

   private static long decodeStartedCursor(Cursor c, long defaultVal) {
      if (c == null) {
         return defaultVal;
      } else {
         String t = c.token();
         int colon = t.indexOf(58);
         if (colon <= 0) {
            return defaultVal;
         } else {
            try {
               return Long.parseLong(t.substring(0, colon));
            } catch (NumberFormatException var6) {
               return defaultVal;
            }
         }
      }
   }

   private static Cursor encodeViolationCursor(long occurredAt, UUID id) {
      return new Cursor("v:" + occurredAt + ":" + id);
   }

   private static long decodeViolationOccurredCursor(Cursor c, long defaultVal) {
      if (c == null) {
         return defaultVal;
      } else {
         String[] parts = c.token().split(":", 3);
         if (parts.length >= 3 && "v".equals(parts[0])) {
            try {
               return Long.parseLong(parts[1]);
            } catch (NumberFormatException var5) {
               return defaultVal;
            }
         } else {
            return defaultVal;
         }
      }
   }

   private static UUID decodeViolationIdCursor(Cursor c) {
      if (c == null) {
         return null;
      } else {
         String[] parts = c.token().split(":", 3);
         if (parts.length >= 3 && "v".equals(parts[0])) {
            try {
               return UUID.fromString(parts[2]);
            } catch (IllegalArgumentException var3) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   @ApiStatus.Internal
   public TableNames tableNames() {
      return this.config.tableNames();
   }
}
