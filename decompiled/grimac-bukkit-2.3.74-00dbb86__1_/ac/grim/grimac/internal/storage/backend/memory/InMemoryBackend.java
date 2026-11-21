package ac.grim.grimac.internal.storage.backend.memory;

import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.SettingEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.internal.storage.checks.InMemoryCheckCatalogPersistence;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

@ApiStatus.Internal
public final class InMemoryBackend implements Backend {
   public static final String ID = "memory";
   private final Object writeMutex = new Object();
   private final HashMap<UUID, List<ViolationRecord>> violationsBySession = new HashMap<>();
   private final HashMap<UUID, List<ViolationRecord>> violationsByPlayer = new HashMap<>();
   private final HashMap<UUID, SessionRecord> sessions = new HashMap<>();
   private final HashMap<UUID, List<SessionRecord>> sessionsByPlayer = new HashMap<>();
   private final HashMap<UUID, PlayerIdentity> identities = new HashMap<>();
   private final HashMap<String, PlayerIdentity> identityByName = new HashMap<>();
   private final HashMap<String, SettingRecord> settings = new HashMap<>();
   private final CheckCatalogPersistence checkCatalog = new InMemoryCheckCatalogPersistence();

   @NotNull
   @Override
   public String id() {
      return "memory";
   }

   @NotNull
   @Override
   public ApiVersion getApiVersion() {
      return ApiVersion.CURRENT;
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(
         Capability.INDEXED_KV,
         Capability.TIMESERIES_APPEND,
         Capability.TTL,
         Capability.TRANSACTIONS,
         Capability.HISTORY,
         Capability.SETTINGS,
         Capability.PLAYER_IDENTITY
      );
   }

   @NotNull
   @Override
   public Set<Category<?>> supportedCategories() {
      return Set.of(Categories.VIOLATION, Categories.SESSION, Categories.PLAYER_IDENTITY, Categories.SETTING);
   }

   @Override
   public void init(@NotNull BackendContext ctx) {
   }

   @NotNull
   @Override
   public CheckCatalogPersistence checkCatalog() {
      return this.checkCatalog;
   }

   @NotNull
   @Override
   public CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> legacyToCatalogCheckIds, String introducedVersionReplacement) {
      synchronized (this.writeMutex) {
         long violationsUpdated = this.repairViolationCheckIds(legacyToCatalogCheckIds);
         long versionsUpdated = this.repairStubCatalogVersions(introducedVersionReplacement);
         return new CheckCatalogRepairResult(legacyToCatalogCheckIds.size(), violationsUpdated, versionsUpdated);
      }
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() {
      synchronized (this.writeMutex) {
         this.violationsBySession.clear();
         this.violationsByPlayer.clear();
         this.sessions.clear();
         this.sessionsByPlayer.clear();
         this.identities.clear();
         this.identityByName.clear();
         this.settings.clear();
      }
   }

   @ApiStatus.Internal
   public Set<UUID> knownPlayerUuids() {
      synchronized (this.writeMutex) {
         return new LinkedHashSet<>(this.identities.keySet());
      }
   }

   @ApiStatus.Internal
   public void wipeAllForCopier() {
      synchronized (this.writeMutex) {
         this.violationsBySession.clear();
         this.violationsByPlayer.clear();
         this.sessions.clear();
         this.sessionsByPlayer.clear();
         this.identities.clear();
         this.identityByName.clear();
      }
   }

   @NotNull
   @Override
   public <E> StorageEventHandler<E> eventHandlerFor(@NotNull Category<E> cat) {
      if (cat == Categories.VIOLATION) {
         return (event, seq, endOfBatch) -> this.applyViolation(event);
      } else if (cat == Categories.SESSION) {
         return (event, seq, endOfBatch) -> this.applySession(event);
      } else if (cat == Categories.PLAYER_IDENTITY) {
         return (event, seq, endOfBatch) -> this.applyIdentity(event);
      } else if (cat == Categories.SETTING) {
         return (event, seq, endOfBatch) -> this.applySetting(event);
      } else {
         throw new IllegalArgumentException("unsupported category: " + cat.id());
      }
   }

   private void applyViolation(ViolationEvent v) {
      UUID id = v.id();
      ViolationRecord stored = new ViolationRecord(
         id, v.sessionId(), v.playerUuid(), v.checkId(), v.vl(), v.occurredEpochMs(), v.verboseData(), v.verboseFormat()
      );
      synchronized (this.writeMutex) {
         this.violationsBySession.computeIfAbsent(stored.sessionId(), k -> new ArrayList<>()).add(stored);
         this.violationsByPlayer.computeIfAbsent(stored.playerUuid(), k -> new ArrayList<>()).add(stored);
      }
   }

   private void applySession(SessionEvent e) {
      synchronized (this.writeMutex) {
         SessionRecord prev = this.sessions.get(e.sessionId());
         long closedAt = e.closedAtEpochMs();
         if (prev != null && prev.isClosed()) {
            closedAt = prev.closedAtEpochMs();
         }

         SessionRecord s = new SessionRecord(
            e.sessionId(),
            e.playerUuid(),
            e.serverName(),
            e.startedEpochMs(),
            e.lastActivityEpochMs(),
            closedAt,
            e.grimVersion(),
            e.clientBrand(),
            e.clientVersion(),
            e.serverVersionString(),
            e.instanceId(),
            List.copyOf(e.sessionBlobs())
         );
         this.sessions.put(s.sessionId(), s);
         if (prev != null) {
            List<SessionRecord> existing = this.sessionsByPlayer.get(prev.playerUuid());
            if (existing != null) {
               existing.removeIf(r -> r.sessionId().equals(s.sessionId()));
            }
         }

         this.sessionsByPlayer.computeIfAbsent(s.playerUuid(), k -> new ArrayList<>()).add(s);
      }
   }

   private void applyIdentity(PlayerIdentityEvent e) {
      synchronized (this.writeMutex) {
         PlayerIdentity prev = this.identities.get(e.uuid());
         long firstSeen = prev == null ? e.firstSeenEpochMs() : Math.min(prev.firstSeenEpochMs(), e.firstSeenEpochMs());
         long lastSeen = prev == null ? e.lastSeenEpochMs() : Math.max(prev.lastSeenEpochMs(), e.lastSeenEpochMs());
         PlayerIdentity merged = new PlayerIdentity(e.uuid(), e.currentName(), firstSeen, lastSeen);
         this.identities.put(e.uuid(), merged);
         if (e.currentName() != null) {
            this.identityByName.put(e.currentName().toLowerCase(Locale.ROOT), merged);
         }
      }
   }

   private void applySetting(SettingEvent e) {
      byte[] valueCopy = (byte[])e.value().clone();
      SettingRecord s = new SettingRecord(e.scope(), e.scopeKey(), e.key(), valueCopy, e.updatedEpochMs());
      synchronized (this.writeMutex) {
         this.settings.put(this.settingKey(s.scope().name(), s.scopeKey(), s.key()), s);
      }
   }

   @Override
   public <R> void bulkImport(@NotNull Category<?> cat, @NotNull List<R> records) throws BackendException {
      this.writeRecordsDirect(cat, records);
   }

   public synchronized void writeRecordsDirect(Category<?> cat, List<?> records) throws BackendException {
      synchronized (this.writeMutex) {
         for (Object r : records) {
            if (cat == Categories.VIOLATION) {
               this.applyViolationRecord((ViolationRecord)r);
            } else if (cat == Categories.SESSION) {
               this.applySessionRecord((SessionRecord)r);
            } else if (cat == Categories.PLAYER_IDENTITY) {
               this.applyIdentityRecord((PlayerIdentity)r);
            } else {
               if (cat != Categories.SETTING) {
                  throw new BackendException("unsupported category: " + cat.id());
               }

               this.applySettingRecord((SettingRecord)r);
            }
         }
      }
   }

   private void applyViolationRecord(ViolationRecord v) {
      this.violationsBySession.computeIfAbsent(v.sessionId(), k -> new ArrayList<>()).add(v);
      this.violationsByPlayer.computeIfAbsent(v.playerUuid(), k -> new ArrayList<>()).add(v);
   }

   private long repairViolationCheckIds(Map<Integer, Integer> legacyToCatalogCheckIds) {
      if (legacyToCatalogCheckIds.isEmpty()) {
         return 0L;
      } else {
         long updated = 0L;

         for (Entry<UUID, List<ViolationRecord>> entry : this.violationsBySession.entrySet()) {
            List<ViolationRecord> rows = entry.getValue();

            for (int i = 0; i < rows.size(); i++) {
               ViolationRecord v = rows.get(i);
               Integer catalogId = legacyToCatalogCheckIds.get(v.checkId());
               if (catalogId != null && catalogId != v.checkId()) {
                  rows.set(
                     i, new ViolationRecord(v.id(), v.sessionId(), v.playerUuid(), catalogId, v.vl(), v.occurredEpochMs(), v.verboseData(), v.verboseFormat())
                  );
                  updated++;
               }
            }
         }

         this.violationsByPlayer.clear();

         for (List<ViolationRecord> rows : this.violationsBySession.values()) {
            for (ViolationRecord v : rows) {
               this.violationsByPlayer.computeIfAbsent(v.playerUuid(), k -> new ArrayList<>()).add(v);
            }
         }

         return updated;
      }
   }

   private long repairStubCatalogVersions(String introducedVersionReplacement) {
      if (introducedVersionReplacement != null && !introducedVersionReplacement.isBlank()) {
         long updated = 0L;

         for (CheckCatalogRow row : this.checkCatalog.loadAll()) {
            if ("0.0.0-stub".equals(row.introducedVersion())) {
               this.checkCatalog
                  .upsert(
                     new CheckCatalogRow(row.checkId(), row.stableKey(), row.display(), row.description(), introducedVersionReplacement, row.introducedAt())
                  );
               updated++;
            }
         }

         return updated;
      } else {
         return 0L;
      }
   }

   private void applySessionRecord(SessionRecord s) {
      SessionRecord prev = this.sessions.put(s.sessionId(), s);
      if (prev != null) {
         List<SessionRecord> existing = this.sessionsByPlayer.get(prev.playerUuid());
         if (existing != null) {
            existing.removeIf(r -> r.sessionId().equals(s.sessionId()));
         }
      }

      this.sessionsByPlayer.computeIfAbsent(s.playerUuid(), k -> new ArrayList<>()).add(s);
   }

   private void applyIdentityRecord(PlayerIdentity id) {
      PlayerIdentity prev = this.identities.get(id.uuid());
      long firstSeen = prev == null ? id.firstSeenEpochMs() : Math.min(prev.firstSeenEpochMs(), id.firstSeenEpochMs());
      long lastSeen = prev == null ? id.lastSeenEpochMs() : Math.max(prev.lastSeenEpochMs(), id.lastSeenEpochMs());
      PlayerIdentity merged = new PlayerIdentity(id.uuid(), id.currentName(), firstSeen, lastSeen);
      this.identities.put(id.uuid(), merged);
      if (id.currentName() != null) {
         this.identityByName.put(id.currentName().toLowerCase(Locale.ROOT), merged);
      }
   }

   private void applySettingRecord(SettingRecord s) {
      this.settings.put(this.settingKey(s.scope().name(), s.scopeKey(), s.key()), s);
   }

   private String settingKey(String scope, String scopeKey, String key) {
      return scope + "::" + scopeKey + "::" + key;
   }

   @NotNull
   @Override
   public <R> Page<R> read(@NotNull Category<?> cat, @NotNull Query<R> query) throws BackendException {
      synchronized (this.writeMutex) {
         if (query instanceof Queries.ListSessionsByPlayer q) {
            List<SessionRecord> all = new ArrayList<>(this.sessionsByPlayer.getOrDefault(q.player(), List.of()));
            all.sort(Comparator.comparingLong(SessionRecord::startedEpochMs).reversed());
            int skip = decodeSkipCursor(q.cursor());
            int pageSize = q.pageSize();
            int end = Math.min(skip + pageSize, all.size());
            List<SessionRecord> slice = all.subList(skip, end);
            Cursor next = end < all.size() ? encodeSkipCursor(end) : null;
            return new Page<>((List<R>)slice, next);
         } else if (query instanceof Queries.GetSessionById q) {
            SessionRecord s = this.sessions.get(q.sessionId());
            return new Page<>(s == null ? List.of() : List.of((R)s), null);
         } else if (query instanceof Queries.ListViolationsInSession q) {
            List<ViolationRecord> all = new ArrayList<>(this.violationsBySession.getOrDefault(q.sessionId(), List.of()));
            all.sort(Comparator.comparingLong(ViolationRecord::occurredEpochMs).thenComparing(ViolationRecord::id));
            int skip = decodeSkipCursor(q.cursor());
            int end = Math.min(skip + q.pageSize(), all.size());
            List<ViolationRecord> slice = all.subList(skip, end);
            Cursor next = end < all.size() ? encodeSkipCursor(end) : null;
            return new Page<>((List<R>)slice, next);
         } else if (query instanceof Queries.GetPlayerIdentity q) {
            PlayerIdentity p = this.identities.get(q.uuid());
            return new Page<>(p == null ? List.of() : List.of((R)p), null);
         } else if (query instanceof Queries.GetPlayerIdentityByName q) {
            PlayerIdentity p = this.identityByName.get(q.name().toLowerCase(Locale.ROOT));
            return new Page<>(p == null ? List.of() : List.of((R)p), null);
         } else if (query instanceof Queries.ListPlayersByNamePrefix q) {
            String prefix = q.lowerPrefix();
            if (prefix != null && !prefix.isEmpty() && q.limit() > 0) {
               List<PlayerIdentity> out = new ArrayList<>();

               for (Entry<String, PlayerIdentity> e : this.identityByName.entrySet()) {
                  if (e.getKey().startsWith(prefix)) {
                     out.add(e.getValue());
                  }
               }

               out.sort(Comparator.comparingLong(PlayerIdentity::lastSeenEpochMs).reversed());
               if (out.size() > q.limit()) {
                  out = out.subList(0, q.limit());
               }

               return new Page<>((List<R>)out, null);
            } else {
               return Page.empty();
            }
         } else if (query instanceof Queries.GetSetting qx) {
            SettingRecord s = this.settings.get(this.settingKey(qx.scope().name(), qx.scopeKey(), qx.key()));
            return new Page<>(s == null ? List.of() : List.of((R)s), null);
         } else {
            throw new BackendException("unsupported query: " + query.getClass().getSimpleName());
         }
      }
   }

   private static int decodeSkipCursor(Cursor c) {
      if (c == null) {
         return 0;
      } else {
         try {
            return Integer.parseInt(c.token());
         } catch (NumberFormatException var2) {
            return 0;
         }
      }
   }

   private static Cursor encodeSkipCursor(int skip) {
      return new Cursor(Integer.toString(skip));
   }

   @Override
   public <E> void delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) {
      synchronized (this.writeMutex) {
         if (criteria instanceof Deletes.ByPlayer d) {
            UUID uuid = d.uuid();
            if (cat == Categories.VIOLATION) {
               List<ViolationRecord> v = this.violationsByPlayer.remove(uuid);
               if (v != null) {
                  for (ViolationRecord r : v) {
                     List<ViolationRecord> perSession = this.violationsBySession.get(r.sessionId());
                     if (perSession != null) {
                        perSession.removeIf(x -> x.id().equals(r.id()));
                     }
                  }
               }
            } else if (cat == Categories.SESSION) {
               List<SessionRecord> s = this.sessionsByPlayer.remove(uuid);
               if (s != null) {
                  for (SessionRecord rx : s) {
                     this.sessions.remove(rx.sessionId());
                  }
               }
            } else if (cat == Categories.PLAYER_IDENTITY) {
               PlayerIdentity p = this.identities.remove(uuid);
               if (p != null && p.currentName() != null) {
                  this.identityByName.remove(p.currentName().toLowerCase(Locale.ROOT));
               }
            } else if (cat == Categories.SETTING) {
               this.settings.keySet().removeIf(k -> k.startsWith("PLAYER::" + uuid + "::"));
            }
         } else if (criteria instanceof Deletes.OlderThan dx) {
            long cutoff = System.currentTimeMillis() - dx.maxAgeMs();
            if (cat == Categories.SESSION) {
               List<SessionRecord> toRemove = new ArrayList<>();

               for (SessionRecord s : this.sessions.values()) {
                  if (s.startedEpochMs() < cutoff) {
                     toRemove.add(s);
                  }
               }

               for (SessionRecord sx : toRemove) {
                  this.sessions.remove(sx.sessionId());
                  this.sessionsByPlayer.getOrDefault(sx.playerUuid(), List.of()).removeIf(x -> x.sessionId().equals(s.sessionId()));
                  this.violationsBySession.remove(sx.sessionId());
               }
            } else if (cat == Categories.VIOLATION) {
               for (List<ViolationRecord> list : this.violationsBySession.values()) {
                  list.removeIf(rx -> rx.occurredEpochMs() < cutoff);
               }

               for (List<ViolationRecord> list : this.violationsByPlayer.values()) {
                  list.removeIf(rx -> rx.occurredEpochMs() < cutoff);
               }
            }
         }
      }
   }

   @Override
   public long countViolationsInSession(@NotNull UUID sessionId) {
      synchronized (this.writeMutex) {
         return this.violationsBySession.getOrDefault(sessionId, List.of()).size();
      }
   }

   @Override
   public long countUniqueChecksInSession(@NotNull UUID sessionId) {
      synchronized (this.writeMutex) {
         List<ViolationRecord> list = this.violationsBySession.get(sessionId);
         if (list != null && !list.isEmpty()) {
            HashSet<Integer> seen = new HashSet<>();

            for (ViolationRecord v : list) {
               seen.add(v.checkId());
            }

            return seen.size();
         } else {
            return 0L;
         }
      }
   }

   @Override
   public long countSessionsByPlayer(@NotNull UUID player) {
      synchronized (this.writeMutex) {
         List<SessionRecord> list = this.sessionsByPlayer.get(player);
         return list == null ? 0L : list.size();
      }
   }

   @Override
   public long markCrashedSessions() {
      long affected = 0L;
      synchronized (this.writeMutex) {
         for (Entry<UUID, SessionRecord> e : this.sessions.entrySet()) {
            SessionRecord s = e.getValue();
            if (!s.isClosed()) {
               SessionRecord swept = new SessionRecord(
                  s.sessionId(),
                  s.playerUuid(),
                  s.serverName(),
                  s.startedEpochMs(),
                  s.lastActivityEpochMs(),
                  s.lastActivityEpochMs(),
                  s.grimVersion(),
                  s.clientBrand(),
                  s.clientVersion(),
                  s.serverVersionString(),
                  s.instanceId(),
                  s.sessionBlobs()
               );
               e.setValue(swept);
               List<SessionRecord> byPlayer = this.sessionsByPlayer.get(s.playerUuid());
               if (byPlayer != null) {
                  for (int i = 0; i < byPlayer.size(); i++) {
                     if (byPlayer.get(i).sessionId().equals(s.sessionId())) {
                        byPlayer.set(i, swept);
                        break;
                     }
                  }
               }

               affected++;
            }
         }

         return affected;
      }
   }

   public record Config() implements BackendConfig {
   }
}
