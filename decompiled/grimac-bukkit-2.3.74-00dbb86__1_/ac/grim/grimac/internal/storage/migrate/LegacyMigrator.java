package ac.grim.grimac.internal.storage.migrate;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.checks.StableKeyMapping;
import ac.grim.grimac.internal.storage.util.UuidV7;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class LegacyMigrator {
   private static final SettingScope STATE_SCOPE = SettingScope.SERVER;
   private static final String STATE_SCOPE_KEY = "grim-core";
   private static final String STATE_KEY = "legacy_v0_migration_state";
   private final V0Reader v0;
   private final Backend v1;
   private final CheckRegistry checks;
   private final SessionReconstructor.ClientVersionResolver clientVersionResolver;
   private final long sessionGapMs;
   private final Logger logger;
   private final int chunkSize;

   public LegacyMigrator(V0Reader v0, Backend v1, CheckRegistry checks, long sessionGapMs, Logger logger) {
      this(v0, v1, checks, s -> -1, sessionGapMs, logger, 1000);
   }

   public LegacyMigrator(
      V0Reader v0, Backend v1, CheckRegistry checks, SessionReconstructor.ClientVersionResolver clientVersionResolver, long sessionGapMs, Logger logger
   ) {
      this(v0, v1, checks, clientVersionResolver, sessionGapMs, logger, 1000);
   }

   public LegacyMigrator(
      V0Reader v0,
      Backend v1,
      CheckRegistry checks,
      SessionReconstructor.ClientVersionResolver clientVersionResolver,
      long sessionGapMs,
      Logger logger,
      int chunkSize
   ) {
      this.v0 = v0;
      this.v1 = v1;
      this.checks = checks;
      this.clientVersionResolver = clientVersionResolver;
      this.sessionGapMs = sessionGapMs;
      this.logger = logger;
      this.chunkSize = chunkSize;
   }

   public LegacyMigrator.Result run(LongConsumer progress) throws BackendException {
      long start = System.currentTimeMillis();
      if (!this.v0.isLegacyStorePresent()) {
         this.logger.info("[grim-datastore] no legacy v0 store found — nothing to migrate");
         return new LegacyMigrator.Result(0, 0, 0L, false);
      } else {
         long totalLegacyViolations;
         try {
            totalLegacyViolations = this.v0.maxViolationId();
         } catch (SQLException var21) {
            throw new BackendException("legacy pre-scan failed", var21);
         }

         if (totalLegacyViolations == 0L) {
            this.logger.info("[grim-datastore] legacy v0 store is empty; nothing to migrate");
            return new LegacyMigrator.Result(0, 0, System.currentTimeMillis() - start, false);
         } else {
            LegacyMigrator.MigrationState state = this.readState();
            boolean resumed = state.lastMigratedViolationId > 0L;
            if ("COMPLETE".equals(state.state) && state.lastMigratedViolationId >= totalLegacyViolations) {
               this.logger.info("[grim-datastore] legacy migration already complete (v0 id " + state.lastMigratedViolationId + "); skipping");
               return new LegacyMigrator.Result(0, 0, System.currentTimeMillis() - start, true);
            } else {
               if (resumed) {
                  this.logger.info("[grim-datastore] resuming legacy migration from v0 id " + state.lastMigratedViolationId);
               } else {
                  this.logger.info("[grim-datastore] starting legacy migration (v0 max id " + totalLegacyViolations + ")");
                  this.writeState(0L, "IN_PROGRESS", System.currentTimeMillis(), 0L);
               }

               try {
                  SessionReconstructor.LookupLens lens = this.loadLookups();
                  SessionReconstructor.CheckIdResolver resolver = legacyName -> {
                     String stableKey = StableKeyMapping.stableKeyFor(legacyName).orElseGet(() -> StableKeyMapping.legacyFallback(legacyName));
                     return this.checks.intern(stableKey, legacyName);
                  };
                  AtomicLong sessionsEmitted = new AtomicLong();
                  AtomicLong violationsEmitted = new AtomicLong();
                  AtomicLong lastViolationLegacyId = new AtomicLong(state.lastMigratedViolationId);
                  Map<UUID, long[]> identityAcc = new LinkedHashMap<>();
                  SessionReconstructor.Emit emit = (session, violations) -> {
                     try {
                        this.v1.bulkImport(Categories.SESSION, List.of(session));
                        if (!violations.isEmpty()) {
                           List<ViolationRecord> rows = new ArrayList<>(violations.size());

                           for (SessionReconstructor.ReconstructedViolation v : violations) {
                              rows.add(
                                 new ViolationRecord(
                                    UuidV7.fromTimestampMs(v.occurredEpochMs(), v.legacyId()),
                                    v.sessionId(),
                                    v.playerUuid(),
                                    v.checkId(),
                                    v.vl(),
                                    v.occurredEpochMs(),
                                    v.verbose() == null ? null : v.verbose().getBytes(StandardCharsets.UTF_8)
                                 )
                              );
                              long legacyId = v.legacyId();
                              if (legacyId > lastViolationLegacyId.get()) {
                                 lastViolationLegacyId.set(legacyId);
                              }
                           }

                           this.v1.bulkImport(Categories.VIOLATION, rows);
                        }

                        sessionsEmitted.incrementAndGet();
                        violationsEmitted.addAndGet(violations.size());
                        this.writeState(lastViolationLegacyId.get(), "IN_PROGRESS", state.startedAt > 0L ? state.startedAt : System.currentTimeMillis(), 0L);
                        progress.accept(violationsEmitted.get());
                        long firstSeen = session.startedEpochMs();
                        long lastSeen = session.lastActivityEpochMs();
                        identityAcc.compute(
                           session.playerUuid(),
                           (k, curr) -> curr == null ? new long[]{firstSeen, lastSeen} : new long[]{Math.min(curr[0], firstSeen), Math.max(curr[1], lastSeen)}
                        );
                     } catch (BackendException var14x) {
                        throw new RuntimeException("failed to write reconstructed session", var14x);
                     }
                  };
                  SessionReconstructor reconstructor = new SessionReconstructor(this.sessionGapMs, lens, resolver, this.clientVersionResolver, emit);
                  long afterId = state.lastMigratedViolationId;

                  while (true) {
                     List<V0Reader.V0Violation> chunk = this.v0.readChunk(afterId, this.chunkSize);
                     if (chunk.isEmpty()) {
                        reconstructor.flush();
                        if (!identityAcc.isEmpty()) {
                           chunk = new ArrayList<>(identityAcc.size());

                           for (Entry<UUID, long[]> e : identityAcc.entrySet()) {
                              chunk.add(new PlayerIdentity(e.getKey(), null, e.getValue()[0], e.getValue()[1]));
                           }

                           this.v1.bulkImport(Categories.PLAYER_IDENTITY, chunk);
                        }

                        this.writeState(
                           lastViolationLegacyId.get(),
                           "COMPLETE",
                           state.startedAt > 0L ? state.startedAt : System.currentTimeMillis(),
                           System.currentTimeMillis()
                        );
                        long elapsed = System.currentTimeMillis() - start;
                        this.logger
                           .info(
                              "[grim-datastore] migration complete: "
                                 + sessionsEmitted.get()
                                 + " sessions, "
                                 + violationsEmitted.get()
                                 + " violations in "
                                 + elapsed
                                 + "ms"
                           );
                        return new LegacyMigrator.Result((int)sessionsEmitted.get(), (int)violationsEmitted.get(), elapsed, resumed);
                     }

                     for (V0Reader.V0Violation row : chunk) {
                        if (row.legacyId() > afterId) {
                           afterId = row.legacyId();
                        }

                        reconstructor.accept(row);
                     }
                  }
               } catch (SQLException var22) {
                  throw new BackendException("legacy migration read failed", var22);
               }
            }
         }
      }
   }

   private SessionReconstructor.LookupLens loadLookups() throws SQLException {
      final Map<Integer, String> servers = this.v0.loadLookup("grim_history_servers", "server_name");
      final Map<Integer, String> checkNames = this.v0.loadLookup("grim_history_check_names", "check_name_string");
      final Map<Integer, String> grimVersions = this.v0.loadLookup("grim_history_versions", "grim_version_string");
      final Map<Integer, String> clientBrands = this.v0.loadLookup("grim_history_client_brands", "client_brand_string");
      final Map<Integer, String> clientVersions = this.v0.loadLookup("grim_history_client_versions", "client_version_string");
      final Map<Integer, String> serverVersions = this.v0.loadLookup("grim_history_server_versions", "server_version_string");
      return new SessionReconstructor.LookupLens() {
         @Override
         public String server(int id) {
            return servers.getOrDefault(id, "Unknown");
         }

         @Override
         public String checkName(int id) {
            return checkNames.getOrDefault(id, "Unknown");
         }

         @Override
         public String grimVersion(int id) {
            return grimVersions.getOrDefault(id, "unknown");
         }

         @Override
         public String clientBrand(int id) {
            return clientBrands.getOrDefault(id, "unknown");
         }

         @Override
         public String clientVersion(int id) {
            return clientVersions.getOrDefault(id, "unknown");
         }

         @Override
         public String serverVersion(int id) {
            return serverVersions.getOrDefault(id, "unknown");
         }
      };
   }

   private LegacyMigrator.MigrationState readState() throws BackendException {
      Page<SettingRecord> page = this.v1.read(Categories.SETTING, Queries.getSetting(STATE_SCOPE, "grim-core", "legacy_v0_migration_state"));
      if (page.items().isEmpty()) {
         return new LegacyMigrator.MigrationState(0L, "PENDING", 0L, 0L);
      } else {
         byte[] value = page.items().get(0).value();
         if (value != null && value.length != 0) {
            String raw = new String(value, StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", -1);
            if (parts.length < 4) {
               return new LegacyMigrator.MigrationState(0L, "PENDING", 0L, 0L);
            } else {
               try {
                  return new LegacyMigrator.MigrationState(Long.parseLong(parts[0]), parts[1], Long.parseLong(parts[2]), Long.parseLong(parts[3]));
               } catch (NumberFormatException var6) {
                  return new LegacyMigrator.MigrationState(0L, "PENDING", 0L, 0L);
               }
            }
         } else {
            return new LegacyMigrator.MigrationState(0L, "PENDING", 0L, 0L);
         }
      }
   }

   private void writeState(long lastId, String state, long startedAt, long completedAt) {
      String packed = lastId + "|" + state + "|" + startedAt + "|" + completedAt;
      SettingRecord rec = new SettingRecord(
         STATE_SCOPE, "grim-core", "legacy_v0_migration_state", packed.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis()
      );

      try {
         this.v1.bulkImport(Categories.SETTING, List.of(rec));
      } catch (BackendException var11) {
         this.logger.log(Level.WARNING, "[grim-datastore] failed to update migration state", (Throwable)var11);
      }
   }

   public record MigrationState(long lastMigratedViolationId, String state, long startedAt, long completedAt) {
   }

   public record Result(int sessionsWritten, int violationsWritten, long elapsedMs, boolean resumed) {
   }
}
