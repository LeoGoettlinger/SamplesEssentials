package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.BackendRegistry;
import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.config.DataStoreConfig;
import ac.grim.grimac.api.storage.config.DuplicatePersistentUuidAction;
import ac.grim.grimac.api.storage.history.HistoryService;
import ac.grim.grimac.api.storage.identity.NameResolver;
import ac.grim.grimac.api.storage.identity.NameResolverLink;
import ac.grim.grimac.api.storage.instance.OwnershipClaimResult;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipGate;
import ac.grim.grimac.api.storage.instance.ServerOwnershipMetadata;
import ac.grim.grimac.api.storage.instance.ServerOwnershipSnapshot;
import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.api.storage.submit.ViolationSink;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.internal.storage.backend.mongo.MongoBackendConfig;
import ac.grim.grimac.internal.storage.backend.mongo.v2.MongoBackendV2;
import ac.grim.grimac.internal.storage.backend.mongo.v2.MongoMigrationContext;
import ac.grim.grimac.internal.storage.backend.mysql.MysqlBackendConfig;
import ac.grim.grimac.internal.storage.backend.mysql.v2.MysqlBackendV2;
import ac.grim.grimac.internal.storage.backend.postgres.PostgresBackendConfig;
import ac.grim.grimac.internal.storage.backend.postgres.v2.PostgresBackendV2;
import ac.grim.grimac.internal.storage.backend.redis.RedisBackendConfig;
import ac.grim.grimac.internal.storage.backend.redis.v2.RedisBackendV2;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackendConfig;
import ac.grim.grimac.internal.storage.backend.sqlite.v2.SqliteBackendV2;
import ac.grim.grimac.internal.storage.category.V2BuiltinKinds;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.checks.DataStoreCheckCatalogPersistence;
import ac.grim.grimac.internal.storage.checks.InMemoryCheckCatalogPersistence;
import ac.grim.grimac.internal.storage.checks.JdbcCheckCatalogPersistence;
import ac.grim.grimac.internal.storage.core.CategoryRouter;
import ac.grim.grimac.internal.storage.core.DataStoreImpl;
import ac.grim.grimac.internal.storage.core.V2BackendBootstrap;
import ac.grim.grimac.internal.storage.core.V2Routes;
import ac.grim.grimac.internal.storage.history.HistoryServiceImpl;
import ac.grim.grimac.internal.storage.identity.LocalCacheLink;
import ac.grim.grimac.internal.storage.identity.NameResolverChain;
import ac.grim.grimac.internal.storage.identity.OfflineModeUuidLink;
import ac.grim.grimac.internal.storage.identity.PlayerIdentityService;
import ac.grim.grimac.internal.storage.instance.HeartbeatScheduler;
import ac.grim.grimac.internal.storage.migrate.LegacyMigrator;
import ac.grim.grimac.internal.storage.migrate.V0Reader;
import ac.grim.grimac.internal.storage.retention.RetentionSweeper;
import ac.grim.grimac.internal.storage.submit.ViolationSinkImpl;
import ac.grim.grimac.internal.storage.verbose.VerboseManifest;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistryImpl;
import ac.grim.grimac.manager.init.start.StartableInitable;
import ac.grim.grimac.manager.init.stop.StoppableInitable;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.types.Binary;

public final class DataStoreLifecycle implements StartableInitable, StoppableInitable {
   private static final String CHECKS_STORE = "grim_checks";
   private static final StoreId OWNERSHIP_STORE = StoreId.grim("server_ownership");
   private final GrimPlugin plugin;
   private final Logger logger;
   private final BackendRegistry backendRegistry;
   private DataStoreConfig config;
   private DataStoreImpl dataStore;
   private CheckRegistry checkRegistry;
   private VerboseRegistry verboseRegistry;
   private HistoryServiceImpl historyService;
   private PlayerIdentityService playerIdentityService;
   private NameResolver nameResolver;
   private ViolationSinkImpl violationSink;
   private RetentionSweeper retentionSweeper;
   private SessionTracker sessionTracker = SessionTracker.NOOP;
   private LiveWriteHooks liveWriteHooks = LiveWriteHooks.NOOP;
   private PlayerToggleStore playerToggleStore = PlayerToggleStore.NOOP;
   private V2InstanceRegistry instanceRegistry;
   private HeartbeatScheduler heartbeatScheduler;
   private OwnershipHeartbeatScheduler ownershipHeartbeatScheduler;
   private ServerOwnershipAdapter ownershipAdapter;
   private ServerOwnershipGate ownershipGate = ServerOwnershipGate.disabled();
   private UUID instanceId;
   private UUID startupId;
   private UUID ownershipFence;
   private long startupStartedEpochMs;
   private ScheduledExecutorService duplicateWarningExecutor;
   private ScheduledExecutorService recoverySweepExecutor;
   private boolean enabled = true;
   private boolean loaded;
   private final List<BackendV2> v2Backends = new ArrayList<>();
   private static final MigrationContext NO_OP_MIGRATION_CONTEXT = new MigrationContext() {};

   public DataStoreLifecycle(@NotNull GrimPlugin plugin, @NotNull BackendRegistry backendRegistry) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.backendRegistry = Objects.requireNonNull(backendRegistry, "backendRegistry");
      this.logger = Logger.getLogger("grim-datastore");
   }

   @Override
   public void start() {
      Path dataFolder = this.plugin.getDataFolder().toPath();
      DataStoreConfigBuilder builder = new DataStoreConfigBuilder(this.backendRegistry, dataFolder, GrimAPI.INSTANCE.getConfigManager().getConfig());
      if (!builder.enabled()) {
         this.logger.info("[grim-datastore] disabled in database.yml - skipping storage init");
         this.enabled = false;
         this.installLocalVerboseRegistry();
      } else {
         this.enabled = true;

         try {
            this.config = builder.build();
         } catch (RuntimeException var9) {
            this.logger.log(Level.SEVERE, "[grim-datastore] database.yml rejected - storage disabled", (Throwable)var9);
            this.enabled = false;
            this.installLocalVerboseRegistry();
            return;
         }

         try {
            this.loaded = this.buildAndStart(dataFolder);
         } catch (DataStoreLifecycle.FatalStorageStartupException var7) {
            this.logger.log(Level.SEVERE, "[grim-datastore] fatal storage startup failure - shutting down server", (Throwable)var7);

            try {
               this.close();
            } catch (Exception var6) {
            }

            this.enabled = false;
            this.shutdownServerAfterFatalStorageStartup();
         } catch (LinkageError | Exception var8) {
            this.logger.log(Level.SEVERE, "[grim-datastore] failed to initialise storage - falling back to disabled", (Throwable)var8);

            try {
               this.close();
            } catch (Exception var5) {
            }

            this.enabled = false;
            this.installLocalVerboseRegistry();
         }
      }
   }

   private boolean buildAndStart(Path dataFolder) throws Exception {
      V2Routes.Builder routesBuilder = V2Routes.builder();
      int allFailures = 0;
      Map<String, BackendV2> v2ById = new LinkedHashMap<>();

      for (Entry<String, BackendConfig> entry : this.config.backends().entrySet()) {
         String backendId = entry.getKey();
         BackendConfig backendConfig = entry.getValue();
         BackendV2 v2 = this.constructV2Direct(backendId, backendConfig);
         if (v2 == null) {
            this.logger.warning("[grim-datastore] no v2 backend for id '" + backendId + "' - categories routed here will be unavailable");
         } else {
            try {
               v2.init(new DataStoreLifecycle.SimpleContext(backendConfig, this.logger, dataFolder));
            } catch (Exception var15) {
               this.logger.log(Level.SEVERE, "[grim-datastore] v2 backend init failed for '" + backendId + "'", (Throwable)var15);

               try {
                  v2.close();
               } catch (Exception var14) {
                  this.logger.log(Level.WARNING, "[grim-datastore] v2 backend close after failed init failed for '" + backendId + "'", (Throwable)var14);
               }
               continue;
            }

            this.v2Backends.add(v2);
            v2ById.put(backendId, v2);
         }
      }

      for (Entry<Category<?>, String> r : this.config.routing().entrySet()) {
         Category<?> cat = r.getKey();
         String backendId = r.getValue();
         if (!"none".equals(backendId)) {
            BackendV2 v2 = v2ById.get(backendId);
            if (v2 != null) {
               Map<Category<?>, V2BackendBootstrap.Binding<?>> bindings = this.bindingsForCategory(cat);
               if (!bindings.isEmpty()) {
                  MigrationContext mctx = this.buildMigrationContext(v2);
                  if (mctx == null) {
                     mctx = NO_OP_MIGRATION_CONTEXT;
                  }

                  V2BackendBootstrap.Result result = V2BackendBootstrap.install(bindings, v2, mctx, routesBuilder, this.logger);
                  allFailures += result.failures().size();
                  if (!result.ok()) {
                     this.logger
                        .warning(
                           "[grim-datastore] v2 bootstrap for '"
                              + backendId
                              + "' had "
                              + result.failures().size()
                              + " failure(s):\n  - "
                              + String.join("\n  - ", result.failures())
                        );
                  }
               }
            }
         }
      }

      boolean startupRouteInstalled = false;
      String sessionBackendId = this.config.routing().get(Categories.SESSION);
      if (sessionBackendId != null && !"none".equals(sessionBackendId)) {
         BackendV2 sessionBackend = v2ById.get(sessionBackendId);
         if (sessionBackend != null) {
            this.ownershipAdapter = sessionBackend.ownershipAdapter().orElse(null);
            if (this.ownershipAdapter != null) {
               try {
                  this.ownershipAdapter.ensureStore(OWNERSHIP_STORE);
               } catch (Exception var13) {
                  allFailures++;
                  this.logger.log(Level.WARNING, "[grim-datastore] failed to ensure server ownership store on '" + sessionBackendId + "'", (Throwable)var13);
               }
            }

            MigrationContext mctxx = this.buildMigrationContext(sessionBackend);
            if (mctxx == null) {
               mctxx = NO_OP_MIGRATION_CONTEXT;
            }

            Map<Category<?>, V2BackendBootstrap.Binding<?>> bindings = Map.of(
               V2InstanceRegistry.STARTUPS, new V2BackendBootstrap.Binding<>(StoreId.grim("server_startups"), V2BuiltinKinds.serverStartups())
            );
            V2BackendBootstrap.Result result = V2BackendBootstrap.install(bindings, sessionBackend, mctxx, routesBuilder, this.logger);
            allFailures += result.failures().size();
            if (result.ok()) {
               startupRouteInstalled = true;
            } else {
               this.logger
                  .warning(
                     "[grim-datastore] v2 bootstrap for server startup registry on '"
                        + sessionBackendId
                        + "' had "
                        + result.failures().size()
                        + " failure(s):\n  - "
                        + String.join("\n  - ", result.failures())
                  );
            }
         }
      }

      if (allFailures > 0) {
         this.logger.warning("[grim-datastore] v2 cutover skipped " + allFailures + " failed route(s); continuing with successfully installed routes");
      }

      V2Routes routes = routesBuilder.build();
      if (routes.isEmpty()) {
         throw new RuntimeException("no v2 routes installed");
      } else {
         CategoryRouter router = startupRouteInstalled
            ? new CategoryRouter(Map.of(V2InstanceRegistry.STARTUPS, V2InstanceRegistry.ROUTER_SENTINEL_BACKEND))
            : new CategoryRouter(Map.of());
         boolean enforceOwnership = this.config.ownership().enforcePersistentUuidOwnership()
            && this.config.ownership().duplicatePersistentUuidAction() != DuplicatePersistentUuidAction.ALLOW_UNSAFE;
         this.ownershipGate = new ServerOwnershipGate(enforceOwnership);
         this.dataStore = new DataStoreImpl(router, this.config.writePath(), this.logger);
         this.dataStore.withV2Routes(routes);
         this.dataStore.withOwnershipGate(this.ownershipGate);
         this.dataStore.start();
         this.checkRegistry = this.buildCheckRegistry(v2ById);
         this.verboseRegistry = this.buildVerboseRegistry();
         this.logger.info("[grim-datastore] v2 cutover complete: " + v2ById.size() + " v2 backend(s), " + routes + " routes installed, 0 legacy backends");
         if (!this.buildServices(routes)) {
            this.disableStorageAfterDuplicate();
            return false;
         } else {
            return true;
         }
      }
   }

   @NotNull
   private CheckRegistry buildCheckRegistry(@NotNull Map<String, BackendV2> v2ById) {
      String backendId = this.config.routing().get(Categories.VIOLATION);
      BackendConfig backendConfig = backendId == null ? null : this.config.backends().get(backendId);
      BackendV2 backend = backendId == null ? null : v2ById.get(backendId);
      if (backend != null && this.dataStore.v2Routes().contains(Categories.CHECK_CATALOG)) {
         List<CheckCatalogRow> initialRows;
         try {
            initialRows = this.loadExistingCheckCatalogRows(backend, backendConfig);
         } catch (RuntimeException var8) {
            this.logger
               .log(
                  Level.WARNING,
                  "[grim-datastore] failed to load persisted check catalog for backend '" + backendId + "' - falling back to process-local check names",
                  (Throwable)var8
               );
            CheckRegistry fallback = new CheckRegistry(new InMemoryCheckCatalogPersistence());
            fallback.reload();
            return fallback;
         }

         CheckCatalogPersistence persistence = new DataStoreCheckCatalogPersistence(initialRows, this.dataStore, this.logger);
         CheckRegistry registry = new CheckRegistry(persistence);
         registry.reload();
         return registry;
      } else {
         this.logger.warning("[grim-datastore] no routed check catalog available for v2 backend '" + backendId + "' - check names will be process-local only");
         CheckRegistry fallback = new CheckRegistry(new InMemoryCheckCatalogPersistence());
         fallback.reload();
         return fallback;
      }
   }

   @NotNull
   private List<CheckCatalogRow> loadExistingCheckCatalogRows(@NotNull BackendV2 backend, @Nullable BackendConfig backendConfig) {
      DataSource dataSource = backend.unwrap(DataSource.class).orElse(null);
      if (dataSource != null) {
         return rowsFrom(new JdbcCheckCatalogPersistence(dataSource::getConnection, "grim_checks").loadAll());
      } else {
         MongoDatabase mongo = backend.unwrap(MongoDatabase.class).orElse(null);
         if (mongo != null) {
            List<CheckCatalogRow> out = new ArrayList<>();
            MongoCursor var10 = mongo.getCollection("grim_checks").find().iterator();

            while (var10.hasNext()) {
               Document d = (Document)var10.next();
               CheckCatalogRow row = checkCatalogRowFromDocument(d);
               if (row != null) {
                  out.add(row);
               }
            }

            return out;
         } else {
            if (backendConfig instanceof RedisBackendConfig redisConfig) {
               List<CheckCatalogRow> rows = loadRedisCheckCatalogRows(backend, redisConfig.keyPrefix());
               if (rows != null) {
                  return rows;
               }
            }

            this.logger
               .warning(
                  "[grim-datastore] no persisted check catalog loader available for v2 backend '"
                     + backend.id()
                     + "' - starting with an empty routed catalog view"
               );
            return List.of();
         }
      }
   }

   @NotNull
   private static List<CheckCatalogRow> rowsFrom(@NotNull Iterable<CheckCatalogRow> rows) {
      List<CheckCatalogRow> out = new ArrayList<>();
      rows.forEach(out::add);
      return out;
   }

   @Nullable
   private static CheckCatalogRow checkCatalogRowFromDocument(@NotNull Document d) {
      Number id = (Number)d.get("check_id", Number.class);
      String stableKey = d.getString("stable_key");
      if (stableKey == null && d.get("_id") instanceof String s) {
         stableKey = s;
      }

      if (id != null && stableKey != null) {
         Number introducedAt = (Number)d.get("introduced_at", Number.class);
         return new CheckCatalogRow(
            id.intValue(),
            stableKey,
            d.getString("display"),
            d.getString("description"),
            d.getString("introduced_version"),
            introducedAt == null ? 0L : introducedAt.longValue()
         );
      } else {
         return null;
      }
   }

   @Nullable
   private static List<CheckCatalogRow> loadRedisCheckCatalogRows(@NotNull BackendV2 backend, @NotNull String keyPrefix) {
      try {
         Class<?> poolClass = Class.forName("redis.clients.jedis.JedisPool");
         Object pool = backend.unwrap(poolClass).orElse(null);
         if (pool == null) {
            return null;
         } else {
            Class<?> scanParamsClass = Class.forName("redis.clients.jedis.params.ScanParams");
            Object params = scanParamsClass.getConstructor().newInstance();
            String rowPrefix = keyPrefix + "grim_checks:";
            scanParamsClass.getMethod("match", String.class).invoke(params, rowPrefix + "*");
            scanParamsClass.getMethod("count", int.class).invoke(params, 1000);
            List<CheckCatalogRow> out = new ArrayList<>();
            Object jedis = poolClass.getMethod("getResource").invoke(pool);

            try {
               String cursor = "0";

               do {
                  Object result = jedis.getClass().getMethod("scan", String.class, scanParamsClass).invoke(jedis, cursor, params);

                  for (String key : (List)result.getClass().getMethod("getResult").invoke(result)) {
                     if (!key.startsWith(rowPrefix + "__idx:")) {
                        Map<String, String> hash = (Map<String, String>)jedis.getClass().getMethod("hgetAll", String.class).invoke(jedis, key);
                        CheckCatalogRow row = checkCatalogRowFromRedis(hash);
                        if (row != null) {
                           out.add(row);
                        }
                     }
                  }

                  cursor = (String)result.getClass().getMethod("getCursor").invoke(result);
               } while (!"0".equals(cursor));
            } finally {
               if (jedis instanceof AutoCloseable closeable) {
                  closeable.close();
               }
            }

            return out;
         }
      } catch (ClassNotFoundException var23) {
         return null;
      } catch (ReflectiveOperationException var24) {
         throw new IllegalStateException("failed to scan Redis check catalog", var24);
      } catch (Exception var25) {
         throw new IllegalStateException("failed to close Redis resource while scanning check catalog", var25);
      }
   }

   @Nullable
   private static CheckCatalogRow checkCatalogRowFromRedis(@NotNull Map<String, String> h) {
      String id = h.get("check_id");
      String stableKey = h.get("stable_key");
      return id != null && stableKey != null
         ? new CheckCatalogRow(
            Integer.parseInt(id), stableKey, h.get("display"), h.get("description"), h.get("introduced_version"), parseLong(h.get("introduced_at"))
         )
         : null;
   }

   private static long parseLong(@Nullable String raw) {
      if (raw == null) {
         return 0L;
      } else {
         try {
            return Long.parseLong(raw);
         } catch (NumberFormatException var2) {
            return 0L;
         }
      }
   }

   @Nullable
   private BackendV2 constructV2Direct(@NotNull String backendId, @NotNull BackendConfig config) {
      return (BackendV2)(switch (backendId) {
         case "mongo" -> config instanceof MongoBackendConfig c ? new MongoBackendV2(c) : null;
         case "postgres" -> config instanceof PostgresBackendConfig c ? new PostgresBackendV2(c) : null;
         case "mysql" -> config instanceof MysqlBackendConfig c ? new MysqlBackendV2(c) : null;
         case "sqlite" -> config instanceof SqliteBackendConfig c ? new SqliteBackendV2(c) : null;
         case "redis" -> config instanceof RedisBackendConfig c ? new RedisBackendV2(c) : null;
         default -> null;
      });
   }

   @NotNull
   private Map<Category<?>, V2BackendBootstrap.Binding<?>> bindingsForCategory(@NotNull Category<?> cat) {
      Map<Category<?>, V2BackendBootstrap.Binding<?>> out = new LinkedHashMap<>();
      if (cat == Categories.VIOLATION) {
         out.put(cat, new V2BackendBootstrap.Binding<>(StoreId.grim("grim_violations"), V2BuiltinKinds.violations()));
         out.put(Categories.CHECK_CATALOG, new V2BackendBootstrap.Binding<>(StoreId.grim("grim_checks"), V2BuiltinKinds.checks()));
      } else if (cat == Categories.SESSION) {
         out.put(cat, new V2BackendBootstrap.Binding<>(StoreId.grim("grim_sessions"), V2BuiltinKinds.sessions()));
      } else if (cat == Categories.PLAYER_IDENTITY) {
         out.put(cat, new V2BackendBootstrap.Binding<>(StoreId.grim("grim_players"), V2BuiltinKinds.players()));
      } else if (cat == Categories.SETTING) {
         out.put(cat, new V2BackendBootstrap.Binding<>(StoreId.grim("grim_settings"), V2BuiltinKinds.settings()));
      }

      return out;
   }

   private boolean buildServices(@NotNull V2Routes routes) {
      V2InstanceRegistry.StartupClaim claim = this.startInstanceRegistry();
      if (claim != null && claim.duplicate()) {
         this.startDuplicateWarning(claim.warningMessage());
         return false;
      } else {
         boolean sessionRouted = routes.contains(Categories.SESSION);
         boolean violationRouted = routes.contains(Categories.VIOLATION);
         boolean playerIdentityRouted = routes.contains(Categories.PLAYER_IDENTITY);
         boolean settingRouted = routes.contains(Categories.SETTING);
         if (sessionRouted && violationRouted) {
            this.historyService = new HistoryServiceImpl(
                  this.dataStore, this.checkRegistry, this.config.history().entriesPerPage(), this.config.history().groupIntervalMs()
               )
               .withV2Startups(Categories.SERVER_STARTUP)
               .withVerboseRegistry(this.verboseRegistry);
         } else {
            this.logger.warning("[grim-datastore] history disabled; missing " + missingRoutes(sessionRouted, "session", violationRouted, "violation"));
         }

         this.playerIdentityService = new PlayerIdentityService(this.dataStore);
         this.nameResolver = this.buildNameResolver(this.dataStore, this.config.nameResolutionChain(), playerIdentityRouted);
         this.violationSink = violationRouted ? new ViolationSinkImpl(this.dataStore) : null;
         this.retentionSweeper = new RetentionSweeper(this.dataStore, this.config.retention(), this.logger);
         if (sessionRouted) {
            this.sessionTracker = new SessionTrackerImpl(this.dataStore, this.config.serverName(), this.config.session().heartbeatIntervalMs(), this.startupId);
         } else {
            this.sessionTracker = SessionTracker.NOOP;
            this.logger.warning("[grim-datastore] session tracking disabled; missing session route");
         }

         if (sessionRouted && violationRouted) {
            this.liveWriteHooks = new LiveWriteHooksImpl(this.dataStore, this.playerIdentityService, this.checkRegistry, this.sessionTracker);
         } else if (playerIdentityRouted) {
            this.liveWriteHooks = new IdentityLiveWriteHooks(this.playerIdentityService);
         } else {
            this.liveWriteHooks = LiveWriteHooks.NOOP;
         }

         if (settingRouted) {
            this.playerToggleStore = new PlayerToggleStoreImpl(this.dataStore, this.logger);
         } else {
            this.playerToggleStore = PlayerToggleStore.NOOP;
            this.logger.warning("[grim-datastore] player toggle persistence disabled; missing setting route");
         }

         return true;
      }
   }

   @NotNull
   private static String missingRoutes(boolean firstPresent, @NotNull String first, boolean secondPresent, @NotNull String second) {
      if (!firstPresent && !secondPresent) {
         return first + " and " + second + " routes";
      } else {
         return !firstPresent ? first + " route" : second + " route";
      }
   }

   @Nullable
   private V2InstanceRegistry.StartupClaim startInstanceRegistry() {
      long heartbeatMs = this.ownershipGate.enforced() ? this.config.ownership().renewIntervalMs() : this.instanceHeartbeatIntervalMs();
      long leaseTtlMs = this.config.ownership().leaseTtlMs();
      this.instanceId = this.loadPersistentInstanceId(this.plugin.getDataFolder().toPath());
      this.startupId = UUID.randomUUID();
      this.ownershipFence = UUID.randomUUID();
      this.startupStartedEpochMs = System.currentTimeMillis();
      this.instanceRegistry = V2InstanceRegistry.create(this.dataStore, this.dataStore.v2Routes(), this.logger);
      if (this.instanceRegistry == null) {
         String message = "[grim-datastore] v2 server startup registry route missing; startup history and crash recovery are disabled";
         this.logger.warning(message);
         return this.ownershipGate.enforced() ? this.ownershipDenied(message, null, -1L) : null;
      } else {
         VerboseRegistry manifestRegistry = this.verboseRegistry;
         Supplier<byte[]> verboseManifest = () -> manifestRegistry == null
            ? VerboseManifest.textOnly(1)
            : VerboseManifest.encode(1, manifestRegistry.checkIdVersions(this.checkRegistry));
         ServerOwnershipMetadata metadata = new ServerOwnershipMetadata(
            this.config.serverName(),
            this.hostname(),
            GrimAPI.INSTANCE.getExternalAPI().getGrimVersion(),
            GrimAPI.INSTANCE.getPlatformServer().getPlatformImplementationString()
         );
         boolean enforceOwnership = this.ownershipGate.enforced();
         OwnershipClaimResult ownershipClaim = null;
         if (enforceOwnership) {
            if (this.ownershipAdapter == null) {
               String message = "[grim-datastore] STORAGE DISABLED: backend for sessions does not expose a server ownership adapter, but persistent UUID ownership is enforced.";
               return this.ownershipDenied(message, null, -1L);
            }

            ownershipClaim = this.claimOwnershipWithOptionalWait(metadata);
            if (!ownershipClaim.claimed()) {
               ServerOwnershipSnapshot owner = ownershipClaim.currentOwner();
               long remaining = owner == null ? -1L : Math.max(0L, owner.leaseExpiresAtEpochMs() - ownershipClaim.dbNowEpochMs());
               String message = "[grim-datastore] STORAGE DISABLED: live duplicate persistent storage UUID "
                  + this.instanceId
                  + " detected. Existing startupId="
                  + (owner == null ? "unknown" : owner.ownerStartupId())
                  + " serverName='"
                  + (owner == null ? "unknown" : owner.serverName())
                  + "' leaseRemainingMs="
                  + remaining
                  + "; this startupId="
                  + this.startupId
                  + ". A server data folder or storage-instance.uuid appears to be copied.";
               return this.ownershipDenied(message, owner == null ? null : owner.ownerStartupId(), remaining);
            }

            this.ownershipGate.open(this.startupId, this.ownershipFence, leaseTtlMs, this.config.ownership().safetyMarginMs());
         }

         long dbNow = ownershipClaim != null ? ownershipClaim.dbNowEpochMs() : this.dbNowBestEffort();
         V2InstanceRegistry.StartupClaim claim = this.instanceRegistry
            .openStartup(
               this.config.serverName(),
               this.instanceId,
               this.startupId,
               this.ownershipFence,
               this.startupStartedEpochMs,
               dbNow,
               metadata.hostname(),
               metadata.grimVersion(),
               metadata.serverVersionString(),
               verboseManifest.get()
            );
         long recovered = this.recoverAfterStartupClaim(ownershipClaim, dbNow);
         if (recovered > 0L) {
            this.logger.warning("[grim-datastore] recovered " + recovered + " open session(s) after claiming storage startup");
         }

         if (enforceOwnership) {
            this.ownershipHeartbeatScheduler = new OwnershipHeartbeatScheduler(
               this.ownershipAdapter,
               OWNERSHIP_STORE,
               this.startupId,
               this.instanceId,
               this.ownershipFence,
               this.config.serverName(),
               this.startupStartedEpochMs,
               metadata.hostname(),
               metadata.grimVersion(),
               metadata.serverVersionString(),
               verboseManifest,
               leaseTtlMs,
               this.config.ownership().safetyMarginMs(),
               Duration.ofMillis(heartbeatMs),
               this.ownershipGate,
               this.instanceRegistry::publish,
               this::disablePersistenceAfterLostOwnership,
               this.logger
            );
            this.ownershipHeartbeatScheduler.start();
         } else {
            this.heartbeatScheduler = new HeartbeatScheduler(
               this.startupId,
               this.instanceId,
               this.config.serverName(),
               this.startupStartedEpochMs,
               metadata.hostname(),
               metadata.grimVersion(),
               metadata.serverVersionString(),
               verboseManifest,
               Duration.ofMillis(heartbeatMs),
               this.instanceRegistry::publish,
               this.logger
            );
            this.heartbeatScheduler.start();
         }

         if (manifestRegistry != null) {
            if (this.ownershipHeartbeatScheduler != null) {
               manifestRegistry.onChange(this.ownershipHeartbeatScheduler::publishNowAndWait);
            } else if (this.heartbeatScheduler != null) {
               manifestRegistry.onChange(this.heartbeatScheduler::publishNowAndWait);
            }
         }

         this.startRecoverySweep();
         return claim;
      }
   }

   @NotNull
   private OwnershipClaimResult claimOwnershipWithOptionalWait(@NotNull ServerOwnershipMetadata metadata) {
      long ttlMs = this.config.ownership().leaseTtlMs();
      OwnershipClaimResult first = this.claimOwnership(metadata, ttlMs);
      if (first.claimed()) {
         return first;
      } else {
         ServerOwnershipSnapshot owner = first.currentOwner();
         long remaining = owner == null ? 0L : Math.max(0L, owner.leaseExpiresAtEpochMs() - first.dbNowEpochMs());
         long waitMs = Math.min(this.config.ownership().startupWaitMs(), remaining + 50L);
         if (waitMs > 0L && owner != null) {
            this.logger
               .warning(
                  "[grim-datastore] persistent storage UUID "
                     + this.instanceId
                     + " is owned by startupId="
                     + owner.ownerStartupId()
                     + "; waiting "
                     + waitMs
                     + "ms for the ownership lease to expire before retrying"
               );

            try {
               Thread.sleep(waitMs);
            } catch (InterruptedException var11) {
               Thread.currentThread().interrupt();
               return first;
            }

            return this.claimOwnership(metadata, ttlMs);
         } else {
            return first;
         }
      }
   }

   @NotNull
   private OwnershipClaimResult claimOwnership(@NotNull ServerOwnershipMetadata metadata, long ttlMs) {
      try {
         return this.ownershipAdapter.claimOwnership(OWNERSHIP_STORE, this.instanceId, this.startupId, this.ownershipFence, ttlMs, metadata);
      } catch (Exception var5) {
         throw new RuntimeException("failed to claim server ownership", var5);
      }
   }

   @Nullable
   private V2InstanceRegistry.StartupClaim ownershipDenied(@NotNull String message, @Nullable UUID conflictingStartupId, long leaseRemainingMs) {
      if (this.config.ownership().duplicatePersistentUuidAction() == DuplicatePersistentUuidAction.FAIL_STARTUP) {
         throw new DataStoreLifecycle.FatalStorageStartupException(message);
      } else {
         this.logger.warning(message);
         this.ownershipGate.close("duplicate-persistent-uuid");
         return V2InstanceRegistry.StartupClaim.duplicate(
            this.startupId, this.instanceId, conflictingStartupId == null ? this.startupId : conflictingStartupId, leaseRemainingMs, message
         );
      }
   }

   private void shutdownServerAfterFatalStorageStartup() {
      Runnable stop = () -> GrimAPI.INSTANCE.getPlatformServer().dispatchCommand(GrimAPI.INSTANCE.getPlatformServer().getConsoleSender(), "stop");

      try {
         GrimAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().run(this.plugin, stop);
      } catch (RuntimeException var5) {
         this.logger.log(Level.SEVERE, "[grim-datastore] failed to schedule server shutdown after fatal storage startup", (Throwable)var5);

         try {
            stop.run();
         } catch (RuntimeException var4) {
            this.logger.log(Level.SEVERE, "[grim-datastore] failed to dispatch stop command after fatal storage startup", (Throwable)var4);
         }
      }
   }

   private long recoverAfterStartupClaim(@Nullable OwnershipClaimResult ownershipClaim, long dbNow) {
      long closed = 0L;
      if (ownershipClaim != null && ownershipClaim.previousOwner() != null) {
         ServerOwnershipSnapshot previous = ownershipClaim.previousOwner();
         if (!this.startupId.equals(previous.ownerStartupId()) && (previous.closedAtEpochMs() != 0L || previous.leaseExpiresAtEpochMs() <= dbNow)) {
            closed += this.instanceRegistry.recoverStartup(previous.ownerStartupId(), "expired-ownership");
         }
      }

      if (this.config.ownership().cleanupOtherServers()) {
         closed += this.instanceRegistry.recoverStaleStartups(this.startupId, dbNow, this.config.ownership().staleStartupTtlMs());
      }

      return closed;
   }

   private long dbNowBestEffort() {
      if (this.ownershipAdapter != null) {
         try {
            return this.ownershipAdapter.dbNowEpochMs();
         } catch (Exception var2) {
            this.logger.log(Level.FINE, "[grim-datastore] failed to read DB time; using local time", (Throwable)var2);
         }
      }

      return System.currentTimeMillis();
   }

   private void startRecoverySweep() {
      if (this.config.ownership().cleanupOtherServers() && this.instanceRegistry != null && this.startupId != null) {
         this.stopRecoverySweep();
         this.recoverySweepExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "grim-storage-recovery-sweep");
            t.setDaemon(true);
            return t;
         });
         long intervalMs = this.config.ownership().recoverySweepIntervalMs();
         this.recoverySweepExecutor.scheduleAtFixedRate(this::runRecoverySweep, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
      }
   }

   private void runRecoverySweep() {
      V2InstanceRegistry registry = this.instanceRegistry;
      UUID currentStartup = this.startupId;
      if (registry != null && currentStartup != null) {
         if (!this.ownershipGate.enforced() || this.ownershipGate.allowWrites()) {
            try {
               long closed = registry.recoverStaleStartups(currentStartup, this.dbNowBestEffort(), this.config.ownership().staleStartupTtlMs());
               if (closed > 0L) {
                  this.logger.warning("[grim-datastore] recovery sweep closed " + closed + " open session(s) from stale startup rows");
               }
            } catch (RuntimeException var5) {
               this.logger.log(Level.WARNING, "[grim-datastore] recovery sweep failed", (Throwable)var5);
            }
         }
      }
   }

   private void stopRecoverySweep() {
      if (this.recoverySweepExecutor != null) {
         this.recoverySweepExecutor.shutdownNow();
         this.recoverySweepExecutor = null;
      }
   }

   private long instanceHeartbeatIntervalMs() {
      long configured = this.config.session().heartbeatIntervalMs();
      return configured > 0L ? configured : 30000L;
   }

   @NotNull
   private VerboseRegistry buildVerboseRegistry() {
      VerboseCodecs.ensureRegistered();
      return new VerboseRegistryImpl(this.dataStore, this.checkRegistry, 1);
   }

   private void installLocalVerboseRegistry() {
      try {
         CheckRegistry localChecks = new CheckRegistry(new InMemoryCheckCatalogPersistence());
         localChecks.reload();
         this.checkRegistry = localChecks;
         this.verboseRegistry = this.buildVerboseRegistry();
      } catch (RuntimeException var2) {
         this.logger.log(Level.WARNING, "[grim-datastore] failed to initialise local verbose registry", (Throwable)var2);
      }
   }

   @NotNull
   private UUID loadPersistentInstanceId(@NotNull Path dataFolder) {
      Path file = dataFolder.resolve("data").resolve("storage-instance.uuid");

      try {
         Files.createDirectories(file.getParent());
         if (Files.exists(file)) {
            String raw = Files.readString(file, StandardCharsets.UTF_8).trim();

            try {
               return UUID.fromString(raw);
            } catch (IllegalArgumentException var6) {
               Path backup = file.resolveSibling(file.getFileName() + ".invalid-" + System.currentTimeMillis());
               Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
               this.logger
                  .warning("[grim-datastore] invalid storage instance UUID in " + file + "; moved it to " + backup + " and generated a new persistent id");
            }
         }

         UUID generated = UUID.randomUUID();
         Files.writeString(file, generated + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
         return generated;
      } catch (IOException var7) {
         throw new IllegalStateException("failed to load persistent storage instance id from " + file, var7);
      }
   }

   private void disableStorageAfterDuplicate() {
      this.logger
         .warning(
            "[grim-datastore] storage disabled for this boot because another live Grim startup is using this storage instance id. Runtime checks remain active."
         );
      if (this.heartbeatScheduler != null) {
         this.heartbeatScheduler.stop();
         this.heartbeatScheduler = null;
      }

      if (this.ownershipHeartbeatScheduler != null) {
         this.ownershipHeartbeatScheduler.stop();
         this.ownershipHeartbeatScheduler = null;
      }

      this.ownershipGate.close("duplicate-persistent-uuid");
      if (this.dataStore != null) {
         this.dataStore.flushAndClose(this.config.writePath().shutdownDrainTimeoutMs());
      }

      this.closeV2Backends();
      this.dataStore = null;
      this.historyService = null;
      this.playerIdentityService = null;
      this.nameResolver = null;
      this.violationSink = null;
      this.retentionSweeper = null;
      this.sessionTracker = SessionTracker.NOOP;
      this.liveWriteHooks = LiveWriteHooks.NOOP;
      this.playerToggleStore = PlayerToggleStore.NOOP;
      this.instanceRegistry = null;
      this.verboseRegistry = null;
      this.ownershipAdapter = null;
      this.ownershipFence = null;
      this.ownershipGate = ServerOwnershipGate.disabled();
      this.loaded = false;
      this.enabled = false;
   }

   private void startDuplicateWarning(@NotNull String message) {
      this.stopDuplicateWarning();
      this.logger.warning(message);
      this.duplicateWarningExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "grim-storage-duplicate-warning");
         t.setDaemon(true);
         return t;
      });
      this.duplicateWarningExecutor.scheduleAtFixedRate(() -> this.logger.warning(message), 60L, 60L, TimeUnit.SECONDS);
   }

   private void stopDuplicateWarning() {
      if (this.duplicateWarningExecutor != null) {
         this.duplicateWarningExecutor.shutdownNow();
         this.duplicateWarningExecutor = null;
      }
   }

   @Nullable
   private String hostname() {
      try {
         return InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException var2) {
         return null;
      }
   }

   @Nullable
   private MigrationContext buildMigrationContext(@NotNull BackendV2 backend) {
      if (backend instanceof MongoBackendV2 mongo) {
         MongoDatabase db = mongo.unwrap(MongoDatabase.class).orElse(null);
         if (db == null) {
            return null;
         } else {
            this.maybeWarnUnexpectedIdShape(db);
            return new MongoMigrationContext(db, this.logger);
         }
      } else {
         return !(backend instanceof PostgresBackendV2)
               && !(backend instanceof MysqlBackendV2)
               && !(backend instanceof SqliteBackendV2)
               && !(backend instanceof RedisBackendV2)
            ? null
            : NO_OP_MIGRATION_CONTEXT;
      }
   }

   private void maybeWarnUnexpectedIdShape(@NotNull MongoDatabase db) {
      for (String coll : new String[]{"grim_sessions", "grim_players"}) {
         try {
            Document first = (Document)db.getCollection(coll).find().limit(1).first();
            if (first != null) {
               Object id = first.get("_id");
               if (!(id instanceof UUID)
                  && !(
                     id instanceof Binary b
                        && (b.getType() == BsonBinarySubType.BINARY.getValue() || b.getType() == BsonBinarySubType.UUID_STANDARD.getValue())
                        && b.getData().length == 16
                  )) {
                  String idClass = id == null ? "null" : id.getClass().getSimpleName();
                  this.logger
                     .warning(
                        () -> "[grim-datastore] "
                           + coll
                           + " first-doc _id is "
                           + idClass
                           + ", expected UUID-shaped binary - entity migration will not handle this row correctly. Halt and inspect before proceeding if this is unexpected."
                     );
               }
            }
         } catch (RuntimeException var9) {
            this.logger.fine(() -> "[grim-datastore] _id sanity probe failed for " + coll + ": " + var9.getMessage());
         }
      }
   }

   private void closeV2Backends() {
      for (BackendV2 v2 : this.v2Backends) {
         try {
            v2.flush();
         } catch (Exception var5) {
            this.logger.log(Level.WARNING, "[grim-datastore] v2 flush failed for " + v2.id(), (Throwable)var5);
         }

         try {
            v2.close();
         } catch (Exception var4) {
            this.logger.log(Level.WARNING, "[grim-datastore] v2 close failed for " + v2.id(), (Throwable)var4);
         }
      }

      this.v2Backends.clear();
   }

   private NameResolver buildNameResolver(DataStore store, List<String> chain, boolean playerIdentityRouted) {
      List<NameResolverLink> links = new ArrayList<>();

      for (String id : chain) {
         switch (id) {
            case "local-cache":
               if (playerIdentityRouted) {
                  links.add(new LocalCacheLink(store));
               } else {
                  this.logger.warning("[grim-datastore] local-cache name resolver disabled; missing player-identity route");
               }
               break;
            case "offline-mode-uuid":
               links.add(new OfflineModeUuidLink());
               break;
            default:
               this.logger.warning("[grim-datastore] unknown name-resolver link: " + id);
         }
      }

      return new NameResolverChain(links);
   }

   private void maybeMigrateLegacy(Path dataFolder, SqliteBackend sqliteBackend) {
      if (sqliteBackend != null) {
         if (this.config.migration().skip()) {
            this.logger.info("[grim-datastore] migration.skip=true; leaving legacy v0 un-migrated");
         } else {
            V0Sources.V0Source source = V0Sources.detect(dataFolder, GrimAPI.INSTANCE.getConfigManager().getConfig());
            if (source == null) {
               this.logger.info("[grim-datastore] no legacy v0 store detected; nothing to migrate");
            } else {
               this.logger.info("[grim-datastore] legacy v0 source: " + source.summary());
               V0Reader reader = new V0Reader(source.jdbcUrl(), source.username(), source.password());
               LegacyMigrator migrator = new LegacyMigrator(
                  reader, sqliteBackend, this.checkRegistry, ClientVersionResolver::legacyStringToPvn, this.config.session().gapMs(), this.logger
               );
               long startMs = System.currentTimeMillis();

               try {
                  LegacyMigrator.Result result = migrator.run(count -> {
                     if (count % 5000L == 0L) {
                        this.logger.info("[grim-datastore] migrated " + count + " violations so far");
                     }
                  });
                  long elapsed = System.currentTimeMillis() - startMs;
                  this.logger
                     .info(
                        "[grim-datastore] legacy migration: "
                           + result.sessionsWritten()
                           + " sessions, "
                           + result.violationsWritten()
                           + " violations, "
                           + elapsed
                           + "ms"
                           + (result.resumed() ? " (resumed)" : "")
                     );
               } catch (BackendException var11) {
                  this.logger.log(Level.SEVERE, "[grim-datastore] legacy migration failed", (Throwable)var11);
               }
            }
         }
      }
   }

   @Override
   public void stop() {
      this.close();
   }

   public synchronized void reload() {
      this.logger.info("[grim-datastore] /grim reload: tearing down datastore...");
      this.close();
      this.start();
   }

   private synchronized void close() {
      this.stopDuplicateWarning();
      this.stopRecoverySweep();
      if (this.enabled) {
         this.stopHeartbeatSchedulersForShutdown();
         this.playerToggleStore.shutdown();
         if (this.violationSink != null) {
            this.violationSink.shutDown();
         }

         this.shutdownInstanceRegistry();
         if (this.dataStore != null && this.config != null) {
            this.dataStore.flushAndClose(this.config.writePath().shutdownDrainTimeoutMs());
         }

         this.closeOwnership("graceful");
         this.ownershipGate.close("shutdown");
         this.closeV2Backends();
         this.dataStore = null;
         this.historyService = null;
         this.playerIdentityService = null;
         this.nameResolver = null;
         this.violationSink = null;
         this.retentionSweeper = null;
         this.sessionTracker = SessionTracker.NOOP;
         this.liveWriteHooks = LiveWriteHooks.NOOP;
         this.playerToggleStore = PlayerToggleStore.NOOP;
         this.instanceRegistry = null;
         this.ownershipAdapter = null;
         this.ownershipFence = null;
         this.ownershipGate = ServerOwnershipGate.disabled();
         this.instanceId = null;
         this.startupId = null;
         this.startupStartedEpochMs = 0L;
         this.checkRegistry = null;
         this.config = null;
         this.loaded = false;
      }
   }

   private void stopHeartbeatSchedulersForShutdown() {
      if (this.ownershipHeartbeatScheduler != null) {
         this.ownershipHeartbeatScheduler.publishNowAndWait();
         this.ownershipHeartbeatScheduler.stop();
         this.ownershipHeartbeatScheduler = null;
      }

      if (this.heartbeatScheduler != null) {
         this.heartbeatScheduler.publishNowAndWait();
         this.heartbeatScheduler.stop();
         this.heartbeatScheduler = null;
      }
   }

   private void shutdownInstanceRegistry() {
      if (this.instanceRegistry != null && this.startupId != null && this.config != null) {
         if (this.ownershipGate.enforced() && !this.ownershipGate.allowWrites()) {
            this.logger.warning("[grim-datastore] skipping startup/session shutdown writes because DB ownership is no longer held");
         } else {
            long now = this.dbNowBestEffort();

            try {
               long closed = this.instanceRegistry.closeCurrentStartup(this.startupId, now);
               if (closed > 0L) {
                  this.logger.info("[grim-datastore] closed " + closed + " still-open session(s) for this server startup");
               }
            } catch (RuntimeException var5) {
               this.logger.log(Level.WARNING, "[grim-datastore] failed to close sessions for this server startup", (Throwable)var5);
            }
         }
      }
   }

   private void closeOwnership(@NotNull String reason) {
      if (this.ownershipAdapter != null && this.instanceId != null && this.startupId != null && this.ownershipFence != null) {
         try {
            this.ownershipAdapter.closeOwnership(OWNERSHIP_STORE, this.instanceId, this.startupId, this.ownershipFence, reason);
         } catch (Exception var3) {
            this.logger.log(Level.WARNING, "[grim-datastore] failed to close server ownership row", (Throwable)var3);
         }
      }
   }

   private synchronized void disablePersistenceAfterLostOwnership() {
      this.stopRecoverySweep();
      this.sessionTracker = SessionTracker.NOOP;
      this.liveWriteHooks = LiveWriteHooks.NOOP;
      this.playerToggleStore = PlayerToggleStore.NOOP;
      if (this.violationSink != null) {
         this.violationSink.shutDown();
         this.violationSink = null;
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   @Nullable
   public DataStore dataStore() {
      return this.loaded ? this.dataStore : null;
   }

   @Nullable
   public HistoryService historyService() {
      return this.historyService;
   }

   @Nullable
   public NameResolver nameResolver() {
      return this.nameResolver;
   }

   @Nullable
   public ViolationSink violationSink() {
      return this.violationSink;
   }

   @Nullable
   public DataStoreConfig config() {
      return this.config;
   }

   @Nullable
   public VerboseRegistry verboseRegistry() {
      return this.verboseRegistry;
   }

   @NotNull
   public LiveWriteHooks liveWriteHooks() {
      return this.liveWriteHooks;
   }

   @NotNull
   public SessionTracker sessionTracker() {
      return this.sessionTracker;
   }

   @NotNull
   public PlayerToggleStore playerToggleStore() {
      return this.playerToggleStore;
   }

   @ApiStatus.Internal
   @Nullable
   public SqliteBackend sqliteBackendForCommands() {
      return null;
   }

   @ApiStatus.Internal
   @Nullable
   public CheckRegistry checkRegistryForCommands() {
      return this.checkRegistry;
   }

   @ApiStatus.Internal
   @NotNull
   public Map<String, Backend> allBackendsForCommands() {
      return Map.of();
   }

   private static final class FatalStorageStartupException extends RuntimeException {
      FatalStorageStartupException(@NotNull String message) {
         super(message);
      }
   }

   private record SimpleContext(BackendConfig config, Logger logger, Path dataDirectory) implements BackendContext {
   }
}
