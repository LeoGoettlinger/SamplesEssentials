package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.backend.BackendRegistry;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.config.DataStoreConfig;
import ac.grim.grimac.api.storage.config.DuplicatePersistentUuidAction;
import ac.grim.grimac.api.storage.config.HistoryConfig;
import ac.grim.grimac.api.storage.config.MigrationConfig;
import ac.grim.grimac.api.storage.config.OwnershipConfig;
import ac.grim.grimac.api.storage.config.RetentionRule;
import ac.grim.grimac.api.storage.config.SessionConfig;
import ac.grim.grimac.api.storage.config.WaitStrategyType;
import ac.grim.grimac.api.storage.config.WritePathConfig;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

public final class DataStoreConfigBuilder {
   private static final String NS = "database.";
   private final ConfigManager config;
   private final BackendRegistry registry;
   private final Path dataFolder;

   public DataStoreConfigBuilder(@NotNull BackendRegistry registry, @NotNull Path dataFolder, @NotNull ConfigManager config) {
      this.registry = Objects.requireNonNull(registry, "registry");
      this.dataFolder = Objects.requireNonNull(dataFolder, "dataFolder");
      this.config = Objects.requireNonNull(config, "config");
   }

   public boolean enabled() {
      return this.config.getBooleanElse("database.enabled", true);
   }

   @NotNull
   public DataStoreConfig build() {
      Map<Category<?>, String> routing = this.readRouting();
      Map<String, BackendConfig> backends = this.readBackends(routing);
      SessionConfig session = new SessionConfig(
         this.config.getLongElse("database.session.gap-ms", 600000L),
         this.config.getBooleanElse("database.session.scope-per-server", true),
         this.config.getLongElse("database.session.heartbeat-interval-ms", 30000L)
      );
      OwnershipConfig ownership = this.readOwnership();
      WritePathConfig writePath = this.readWritePath();
      Map<Category<?>, RetentionRule> retention = this.readRetention();
      MigrationConfig migration = new MigrationConfig(
         this.config.getBooleanElse("database.migration.skip", false), this.config.getLongElse("database.migration.max-duration-ms", 0L)
      );
      List<String> chain = this.config.getStringListElse("database.name-resolution.chain", List.of("local-cache", "offline-mode-uuid"));
      HistoryConfig history = new HistoryConfig(
         this.config.getIntElse("database.history.entries-per-page", 15), this.config.getLongElse("database.history.group-interval-ms", 30000L)
      );
      String serverName = this.config.getStringElse("database.server-name", "Unknown");
      return new DataStoreConfig(routing, backends, session, ownership, writePath, retention, migration, chain, history, serverName);
   }

   @NotNull
   private OwnershipConfig readOwnership() {
      String actionRaw = this.config.getStringElse("database.ownership.duplicate-persistent-uuid-action", "disable-storage");

      DuplicatePersistentUuidAction action;
      try {
         action = DuplicatePersistentUuidAction.valueOf(actionRaw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
      } catch (IllegalArgumentException var4) {
         throw new IllegalArgumentException(
            "database.ownership.duplicate-persistent-uuid-action must be one of disable-storage, fail-startup, allow-unsafe (got '" + actionRaw + "')"
         );
      }

      return new OwnershipConfig(
         this.config.getBooleanElse("database.ownership.enforce-persistent-uuid-ownership", true),
         action,
         this.config.getLongElse("database.ownership.lease-ttl-ms", 20000L),
         this.config.getLongElse("database.ownership.renew-interval-ms", 10000L),
         this.config.getLongElse("database.ownership.startup-wait-ms", 20000L),
         this.config.getLongElse("database.ownership.safety-margin-ms", 5000L),
         this.config.getLongElse("database.ownership.stale-startup-ttl-ms", 30000L),
         this.config.getLongElse("database.ownership.recovery-sweep-interval-ms", 10000L),
         this.config.getBooleanElse("database.ownership.cleanup-other-servers", true)
      );
   }

   private Map<Category<?>, String> readRouting() {
      Map<String, Object> raw = this.config.getMapElse("database.routing", Map.of());
      Map<Category<?>, String> out = new LinkedHashMap<>();

      for (Entry<String, Object> e : raw.entrySet()) {
         Category<?> cat = categoryFor(e.getKey());
         if (cat != null) {
            out.put(cat, Objects.toString(e.getValue(), "none"));
         }
      }

      return out;
   }

   private Map<String, BackendConfig> readBackends(Map<Category<?>, String> routing) {
      Map<String, BackendConfig> out = new LinkedHashMap<>();

      for (String backendId : routing.values()) {
         if (!backendId.equals("none") && !out.containsKey(backendId)) {
            BackendProvider provider = this.registry.lookup(backendId);
            if (provider == null) {
               throw new IllegalArgumentException(
                  "no backend provider registered for id '" + backendId + "' referenced in routing (registered: " + this.registry.registeredIds() + ")"
               );
            }

            out.put(backendId, provider.readConfig(new DataStoreConfigBuilder.PrefixedSource(this.config, backendId)));
         }
      }

      return out;
   }

   private WritePathConfig readWritePath() {
      int capacity = this.config.getIntElse("database.write-path.queue-capacity", 16384);
      if (capacity > 0 && Integer.bitCount(capacity) == 1) {
         String waitRaw = this.config.getStringElse("database.write-path.wait-strategy", "BLOCKING");

         WaitStrategyType wait;
         try {
            wait = WaitStrategyType.valueOf(waitRaw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
         } catch (IllegalArgumentException var5) {
            throw new IllegalArgumentException(
               "database.write-path.wait-strategy must be one of BLOCKING, TIMEOUT_BLOCKING, SLEEPING, YIELDING, BUSY_SPIN (got '" + waitRaw + "')"
            );
         }

         return new WritePathConfig(
            capacity,
            this.config.getIntElse("database.write-path.batch-size", 256),
            this.config.getLongElse("database.write-path.flush-interval-ms", 1000L),
            this.config.getLongElse("database.write-path.warn-rate-ms", 10000L),
            this.config.getLongElse("database.write-path.shutdown-drain-timeout-ms", 5000L),
            wait
         );
      } else {
         throw new IllegalArgumentException(
            "database.write-path.queue-capacity must be a positive power of two (got " + capacity + "). Example values: 4096, 8192, 16384, 32768."
         );
      }
   }

   private Map<Category<?>, RetentionRule> readRetention() {
      Map<String, Object> raw = this.config.getMapElse("database.retention", Map.of());
      Map<Category<?>, RetentionRule> out = new LinkedHashMap<>();

      for (Entry<String, Object> e : raw.entrySet()) {
         Category<?> cat = categoryFor(e.getKey());
         if (cat != null) {
            String base = "database.retention." + e.getKey() + ".";
            boolean enabled = this.config.getBooleanElse(base + "enabled", false);
            long days = this.config.getLongElse(base + "max-age-days", 0L);
            out.put(cat, new RetentionRule(enabled, days));
         }
      }

      return out;
   }

   private static Category<?> categoryFor(String id) {
      return switch (id) {
         case "violation" -> Categories.VIOLATION;
         case "session" -> Categories.SESSION;
         case "player-identity" -> Categories.PLAYER_IDENTITY;
         case "setting" -> Categories.SETTING;
         case "blob" -> Categories.BLOB;
         default -> null;
      };
   }

   private static final class PrefixedSource implements BackendConfigSource {
      private final ConfigManager delegate;
      private final String prefix;

      PrefixedSource(ConfigManager delegate, String backendId) {
         this.delegate = delegate;
         this.prefix = backendId + ".";
      }

      @NotNull
      @Override
      public String getString(@NotNull String key, @NotNull String defaultValue) {
         return this.delegate.getStringElse(this.prefix + key, defaultValue);
      }

      @Override
      public int getInt(@NotNull String key, int defaultValue) {
         return this.delegate.getIntElse(this.prefix + key, defaultValue);
      }

      @Override
      public long getLong(@NotNull String key, long defaultValue) {
         return this.delegate.getLongElse(this.prefix + key, defaultValue);
      }

      @Override
      public boolean getBoolean(@NotNull String key, boolean defaultValue) {
         return this.delegate.getBooleanElse(this.prefix + key, defaultValue);
      }

      @NotNull
      @Override
      public List<String> getStringList(@NotNull String key, @NotNull List<String> defaultValue) {
         return this.delegate.getStringListElse(this.prefix + key, defaultValue);
      }
   }
}
