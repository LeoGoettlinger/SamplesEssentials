package ac.grim.grimac.internal.storage.history;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.category.EventStreamCategory;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.history.CheckBucket;
import ac.grim.grimac.api.storage.history.CheckCount;
import ac.grim.grimac.api.storage.history.HistoryService;
import ac.grim.grimac.api.storage.history.SessionDetail;
import ac.grim.grimac.api.storage.history.SessionSummary;
import ac.grim.grimac.api.storage.history.ViolationEntry;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseFormatter;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.api.storage.verbose.VerboseSink;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.verbose.GenericVerboseReader;
import ac.grim.grimac.internal.storage.verbose.VerboseManifest;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public final class HistoryServiceImpl implements HistoryService {
   private final DataStore store;
   private final CheckRegistry checks;
   private final int defaultPageSize;
   private final long defaultGroupIntervalMs;
   @Nullable
   private volatile EventStreamCategory<ViolationEvent, ViolationRecord> v2Violations;
   @Nullable
   private volatile Category<ServerStartupEvent> v2Startups;
   @Nullable
   private volatile VerboseRegistry verboseRegistry;
   private final Map<UUID, ServerStartupRecord> startupCache = new ConcurrentHashMap<>();
   private final Map<UUID, HistoryServiceImpl.CachedManifest> verboseManifestCache = new ConcurrentHashMap<>();

   public HistoryServiceImpl(@NotNull DataStore store, @NotNull CheckRegistry checks, int defaultPageSize, long defaultGroupIntervalMs) {
      this.store = store;
      this.checks = checks;
      this.defaultPageSize = defaultPageSize;
      this.defaultGroupIntervalMs = defaultGroupIntervalMs;
   }

   public HistoryServiceImpl(
      @NotNull DataStore store, @NotNull CheckRegistry checks, int defaultPageSize, long defaultGroupIntervalMs, @Nullable VerboseRegistry verboseRegistry
   ) {
      this(store, checks, defaultPageSize, defaultGroupIntervalMs);
      this.verboseRegistry = verboseRegistry;
   }

   public HistoryServiceImpl withV2Violations(@NotNull EventStreamCategory<ViolationEvent, ViolationRecord> v2) {
      this.v2Violations = v2;
      return this;
   }

   public HistoryServiceImpl withV2Startups(@NotNull Category<ServerStartupEvent> v2) {
      this.v2Startups = v2;
      return this;
   }

   public HistoryServiceImpl withVerboseRegistry(@NotNull VerboseRegistry registry) {
      this.verboseRegistry = registry;
      return this;
   }

   @NotNull
   @Override
   public CompletionStage<Page<SessionSummary>> listSessions(@NotNull UUID player, @Nullable Cursor cursor, int pageSize) {
      int ps = pageSize > 0 ? pageSize : this.defaultPageSize;
      return this.store
         .countSessionsByPlayer(player)
         .thenCompose(
            total -> this.store
               .query(Categories.SESSION, Queries.listSessionsByPlayer(player, ps, cursor))
               .thenCompose(p -> this.toSummaryPage((Page<SessionRecord>)p, total, 0L))
         );
   }

   @NotNull
   public CompletionStage<Page<SessionSummary>> listSessionsPaged(@NotNull UUID player, int pageIndex1Based, int pageSize) {
      int ps = pageSize > 0 ? pageSize : this.defaultPageSize;
      int page = Math.max(1, pageIndex1Based);
      return this.store
         .countSessionsByPlayer(player)
         .thenCompose(
            total -> {
               long sessionsBefore = (long)(page - 1) * ps;
               return sessionsBefore >= total
                  ? CompletableFuture.completedStage(new Page<>(List.of(), null))
                  : this.advanceCursor(player, page, ps)
                     .thenCompose(
                        cursor -> this.store
                           .query(Categories.SESSION, Queries.listSessionsByPlayer(player, ps, cursor))
                           .thenCompose(p -> this.toSummaryPage((Page<SessionRecord>)p, total, sessionsBefore))
                     );
            }
         );
   }

   private CompletionStage<Cursor> advanceCursor(UUID player, int pageIndex1Based, int pageSize) {
      if (pageIndex1Based <= 1) {
         return CompletableFuture.completedStage(null);
      } else {
         CompletionStage<Cursor> cur = CompletableFuture.completedStage(null);

         for (int i = 1; i < pageIndex1Based; i++) {
            cur = cur.thenCompose(c -> this.store.query(Categories.SESSION, Queries.listSessionsByPlayer(player, pageSize, c)).thenApply(Page::nextCursor));
         }

         return cur;
      }
   }

   private CompletionStage<Page<SessionSummary>> toSummaryPage(Page<SessionRecord> page, long total, long sessionsBefore) {
      List<SessionRecord> sessions = page.items();
      return (CompletionStage<Page<SessionSummary>>)(sessions.isEmpty()
         ? CompletableFuture.completedFuture(new Page<>(List.of(), page.nextCursor()))
         : this.resolveStartups(sessions)
            .thenCompose(
               startups -> {
                  EventStreamCategory<ViolationEvent, ViolationRecord> v2 = this.v2Violations;
                  return v2 != null
                     ? this.toSummaryPageV2(v2, sessions, (Map<UUID, ServerStartupRecord>)startups, page.nextCursor(), total, sessionsBefore)
                     : this.toSummaryPageLegacy(sessions, (Map<UUID, ServerStartupRecord>)startups, page.nextCursor(), total, sessionsBefore);
               }
            ));
   }

   private CompletionStage<Page<SessionSummary>> toSummaryPageV2(
      @NotNull EventStreamCategory<ViolationEvent, ViolationRecord> v2,
      @NotNull List<SessionRecord> sessions,
      @NotNull Map<UUID, ServerStartupRecord> startups,
      @Nullable Cursor nextCursor,
      long total,
      long sessionsBefore
   ) {
      List<UUID> sessionIds = new ArrayList<>(sessions.size());

      for (SessionRecord s : sessions) {
         sessionIds.add(s.sessionId());
      }

      CompletionStage<Map<UUID, Long>> countsByIdStage = this.store.execute(v2.countMany("session_id", sessionIds));
      return countsByIdStage.thenCompose(countsById -> {
         SessionSummary[] out = new SessionSummary[sessions.size()];
         CompletionStage<Long>[] uniqueStages = new CompletionStage[sessions.size()];

         for (int i = 0; i < sessions.size(); i++) {
            UUID sid = sessions.get(i).sessionId();
            uniqueStages[i] = this.store.execute(v2.countDistinct("session_id", sid, "check_id"));
         }

         CompletionStage<Void> all = CompletableFuture.completedStage(null);

         for (int i = 0; i < sessions.size(); i++) {
            SessionRecord sx = sessions.get(i);
            int ordinal = (int)(total - sessionsBefore - i);
            int slot = i;
            long violationCount = countsById.getOrDefault(sx.sessionId(), 0L);
            CompletionStage<Long> uniqueStage = uniqueStages[i];
            all = all.thenCombine(uniqueStage, (v, unique) -> {
               out[slot] = toSummary(sx, startups.get(sx.startupId()), ordinal, violationCount, unique.intValue());
               return null;
            });
         }

         return all.thenApply(v -> new Page<>(List.of(out), nextCursor));
      });
   }

   private CompletionStage<Page<SessionSummary>> toSummaryPageLegacy(
      @NotNull List<SessionRecord> sessions, @NotNull Map<UUID, ServerStartupRecord> startups, @Nullable Cursor nextCursor, long total, long sessionsBefore
   ) {
      SessionSummary[] out = new SessionSummary[sessions.size()];
      CompletionStage<Void> chain = CompletableFuture.completedStage(null);

      for (int i = 0; i < sessions.size(); i++) {
         SessionRecord s = sessions.get(i);
         int ordinal = (int)(total - sessionsBefore - i);
         int slot = i;
         chain = chain.thenCompose(
            v -> this.store
               .countViolationsInSession(s.sessionId())
               .thenCompose(
                  count -> this.store
                     .countUniqueChecksInSession(s.sessionId())
                     .thenAccept(unique -> out[slot] = toSummary(s, startups.get(s.startupId()), ordinal, count, unique.intValue()))
               )
         );
      }

      return chain.thenApply(v -> new Page<>(List.of(out), nextCursor));
   }

   private static SessionSummary toSummary(SessionRecord s, @Nullable ServerStartupRecord startup, int ordinal, long violationCount, int uniqueCheckCount) {
      return new SessionSummary(
         s.sessionId(),
         s.playerUuid(),
         ordinal,
         s.startedEpochMs(),
         s.lastActivityEpochMs(),
         s.closedAtEpochMs(),
         grimVersion(s, startup),
         serverName(s, startup),
         s.clientVersion(),
         s.clientBrand(),
         violationCount,
         uniqueCheckCount
      );
   }

   @NotNull
   @Override
   public CompletionStage<SessionDetail> getSessionDetail(@NotNull UUID player, @NotNull UUID sessionId) {
      return this.store
         .query(Categories.SESSION, Queries.getSessionById(sessionId))
         .thenCompose(
            sessionPage -> {
               if (sessionPage.items().isEmpty()) {
                  return CompletableFuture.completedStage(null);
               } else {
                  SessionRecord s = sessionPage.items().get(0);
                  return !s.playerUuid().equals(player)
                     ? CompletableFuture.completedStage(null)
                     : this.resolveStartup(s)
                        .thenCompose(
                           startup -> this.store
                              .query(Categories.VIOLATION, Queries.listViolationsInSession(sessionId, 10000, null))
                              .thenApply(vPage -> this.toDetail(s, startup, vPage.items(), 0))
                        );
               }
            }
         );
   }

   @NotNull
   public CompletionStage<SessionDetail> getSessionDetailByOrdinal(@NotNull UUID player, int sessionOrdinal) {
      return sessionOrdinal < 1
         ? CompletableFuture.completedStage(null)
         : this.store
            .countSessionsByPlayer(player)
            .thenCompose(
               total -> {
                  if (sessionOrdinal > total) {
                     return CompletableFuture.completedStage(null);
                  } else {
                     long descIndex = total - sessionOrdinal;
                     int pageSize = Math.max(1, this.defaultPageSize);
                     int pageIndex = (int)(descIndex / pageSize) + 1;
                     int inPage = (int)(descIndex % pageSize);
                     return this.advanceCursor(player, pageIndex, pageSize)
                        .thenCompose(
                           cursor -> this.store
                              .query(Categories.SESSION, Queries.listSessionsByPlayer(player, pageSize, cursor))
                              .thenCompose(
                                 p -> {
                                    if (inPage >= p.items().size()) {
                                       return CompletableFuture.completedStage(null);
                                    } else {
                                       SessionRecord s = p.items().get(inPage);
                                       return this.resolveStartup(s)
                                          .thenCompose(
                                             startup -> this.store
                                                .query(Categories.VIOLATION, Queries.listViolationsInSession(s.sessionId(), 10000, null))
                                                .thenApply(vPage -> this.toDetail(s, startup, vPage.items(), sessionOrdinal))
                                          );
                                    }
                                 }
                              )
                        );
                  }
               }
            );
   }

   @NotNull
   @Override
   public CompletionStage<Long> countSessions(@NotNull UUID player) {
      return this.store.countSessionsByPlayer(player);
   }

   private SessionDetail toDetail(SessionRecord s, @Nullable ServerStartupRecord startup, List<ViolationRecord> violations, int sessionOrdinal) {
      long bucketSize = Math.max(1L, this.defaultGroupIntervalMs);
      long start = s.startedEpochMs();
      List<ViolationEntry> entries = new ArrayList<>(violations.size());
      Map<Long, Map<Integer, Integer>> bucketCounts = new LinkedHashMap<>();
      Set<Integer> uniqueChecks = new LinkedHashSet<>();

      for (ViolationRecord v : violations) {
         long offset = v.occurredEpochMs() - start;
         String display = this.checks.displayFor(v.checkId()).orElseGet(() -> this.checks.stableKeyFor(v.checkId()).orElse("check#" + v.checkId()));
         String stable = this.checks.stableKeyFor(v.checkId()).orElse("check#" + v.checkId());
         String description = this.checks.descriptionFor(v.checkId()).orElse("");
         entries.add(
            new ViolationEntry(
               v.checkId(),
               stable,
               display,
               description,
               offset,
               v.vl(),
               this.renderVerbose(
                  v.verboseData(), v.verboseFormat(), startup, v.checkId(), new VerboseRenderContext(s.clientVersion(), serverVersion(s, startup))
               ),
               v.verboseFormat()
            )
         );
         long bucket = offset / bucketSize;
         bucketCounts.computeIfAbsent(bucket, k -> new LinkedHashMap<>()).merge(v.checkId(), 1, Integer::sum);
         uniqueChecks.add(v.checkId());
      }

      List<CheckBucket> buckets = new ArrayList<>(bucketCounts.size());

      for (Entry<Long, Map<Integer, Integer>> e : bucketCounts.entrySet()) {
         long bucketStart = e.getKey() * bucketSize;
         List<CheckCount> counts = new ArrayList<>(e.getValue().size());

         for (Entry<Integer, Integer> cc : e.getValue().entrySet()) {
            String display = this.checks.displayFor(cc.getKey()).orElseGet(() -> this.checks.stableKeyFor(cc.getKey()).orElse("check#" + cc.getKey()));
            String stable = this.checks.stableKeyFor(cc.getKey()).orElse("check#" + cc.getKey());
            String description = this.checks.descriptionFor(cc.getKey()).orElse("");
            counts.add(new CheckCount(cc.getKey(), stable, display, description, cc.getValue()));
         }

         buckets.add(new CheckBucket(bucketStart, counts));
      }

      return new SessionDetail(
         s.sessionId(),
         s.playerUuid(),
         sessionOrdinal,
         s.startedEpochMs(),
         s.lastActivityEpochMs(),
         grimVersion(s, startup),
         serverName(s, startup),
         s.clientVersion(),
         s.clientBrand(),
         bucketSize,
         uniqueChecks.size(),
         buckets,
         entries
      );
   }

   private CompletionStage<Map<UUID, ServerStartupRecord>> resolveStartups(@NotNull List<SessionRecord> sessions) {
      Category<ServerStartupEvent> cat = this.v2Startups;
      if (cat == null) {
         return CompletableFuture.completedStage(Map.of());
      } else {
         LinkedHashSet<UUID> missing = new LinkedHashSet<>();
         Map<UUID, ServerStartupRecord> resolved = new HashMap<>();

         for (SessionRecord s : sessions) {
            UUID id = s.startupId();
            if (id != null) {
               ServerStartupRecord cached = this.startupCache.get(id);
               if (cached != null) {
                  resolved.put(id, cached);
               } else {
                  missing.add(id);
               }
            }
         }

         if (missing.isEmpty()) {
            return CompletableFuture.completedStage(resolved);
         } else {
            CompletionStage<List<ServerStartupRecord>> fetched = this.store.execute(new EntityOps.GetManyOp<>(cat, missing));
            return fetched.thenApply(rows -> {
               for (ServerStartupRecord row : rows) {
                  this.startupCache.put(row.startupId(), row);
                  resolved.put(row.startupId(), row);
               }

               return resolved;
            });
         }
      }
   }

   private CompletionStage<ServerStartupRecord> resolveStartup(@NotNull SessionRecord session) {
      UUID startupId = session.startupId();
      if (startupId != null && this.v2Startups != null) {
         ServerStartupRecord cached = this.startupCache.get(startupId);
         return cached != null ? CompletableFuture.completedStage(cached) : this.resolveStartups(List.of(session)).thenApply(map -> map.get(startupId));
      } else {
         return CompletableFuture.completedStage(null);
      }
   }

   @Nullable
   private static String serverName(@NotNull SessionRecord session, @Nullable ServerStartupRecord startup) {
      return startup != null ? startup.serverName() : session.serverName();
   }

   @Nullable
   private static String grimVersion(@NotNull SessionRecord session, @Nullable ServerStartupRecord startup) {
      return startup != null ? startup.grimVersion() : session.grimVersion();
   }

   @Nullable
   private static String serverVersion(@NotNull SessionRecord session, @Nullable ServerStartupRecord startup) {
      String startupVersion = startup == null ? null : startup.serverVersionString();
      return startupVersion != null ? startupVersion : session.serverVersionString();
   }

   @Nullable
   String renderVerbose(
      @Nullable byte[] verboseData, @NotNull VerboseFormat verboseFormat, @Nullable ServerStartupRecord startup, int checkId, @NotNull VerboseRenderContext ctx
   ) {
      if (verboseData == null || verboseData.length == 0) {
         return null;
      } else if (verboseFormat == VerboseFormat.TEXT) {
         return renderText(verboseData);
      } else {
         VerboseManifest.Decoded manifest = startup == null ? null : this.decodedManifest(startup);
         if (manifest == null) {
            return renderText(verboseData);
         } else {
            int version = manifest.codecVersionOrText(checkId);
            if (manifest.supported() && version > 0) {
               int flavor = manifest.flavor();
               VerboseRegistry registry = this.verboseRegistry;
               if (registry != null) {
                  VerboseFormatter formatter = safeFormatter(registry, flavor, checkId, version);
                  if (formatter != null) {
                     try {
                        StringBuilder rendered = new StringBuilder();
                        formatter.render(VerboseBuf.wrap(verboseData), ctx, VerboseSink.into(rendered));
                        return rendered.toString();
                     } catch (Throwable var14) {
                        return placeholder(verboseData, version, "decode failed");
                     }
                  }

                  VerboseSchema.Layout layout = safeLayout(registry, flavor, checkId, version);
                  if (layout != null) {
                     String template = layout.template();
                     if (template != null) {
                        String rendered = Verbose.renderStored(template, verboseData, ctx);
                        if (rendered != null) {
                           return rendered;
                        }
                     }

                     try {
                        StringBuilder rendered = new StringBuilder();
                        GenericVerboseReader.render(layout, VerboseBuf.wrap(verboseData), ctx, VerboseSink.into(rendered));
                        return rendered.toString();
                     } catch (RuntimeException | GenericVerboseReader.UnderflowException var15) {
                        return placeholder(verboseData, version, "decode failed");
                     }
                  }
               }

               return placeholder(verboseData, version, "schema unavailable");
            } else {
               return renderText(verboseData);
            }
         }
      }
   }

   @Nullable
   private VerboseManifest.Decoded decodedManifest(@NotNull ServerStartupRecord startup) {
      byte[] manifest = startup.verboseManifest();
      if (manifest != null && manifest.length != 0) {
         int manifestHash = Arrays.hashCode(manifest);
         HistoryServiceImpl.CachedManifest cached = this.verboseManifestCache.get(startup.startupId());
         if (cached != null && cached.manifestHash() == manifestHash) {
            return cached.decoded();
         } else {
            VerboseManifest.Decoded decoded;
            try {
               decoded = VerboseManifest.decode(manifest);
            } catch (RuntimeException var7) {
               decoded = new VerboseManifest.Decoded(0, 0, Map.of(), false);
            }

            this.verboseManifestCache.put(startup.startupId(), new HistoryServiceImpl.CachedManifest(manifestHash, decoded));
            return decoded;
         }
      } else {
         return null;
      }
   }

   @Nullable
   private static VerboseFormatter safeFormatter(@NotNull VerboseRegistry registry, int flavor, int checkId, int version) {
      try {
         return registry.codeFormatter(flavor, checkId, version);
      } catch (RuntimeException var5) {
         return null;
      }
   }

   @Nullable
   private static VerboseSchema.Layout safeLayout(@NotNull VerboseRegistry registry, int flavor, int checkId, int version) {
      try {
         return registry.layout(flavor, checkId, version);
      } catch (RuntimeException var5) {
         return null;
      }
   }

   @NotNull
   private static String renderText(@NotNull byte[] verboseData) {
      return new String(verboseData, StandardCharsets.UTF_8);
   }

   @NotNull
   private static String placeholder(@NotNull byte[] verboseData, int version, @NotNull String reason) {
      return "[binary verbose v" + version + ", " + reason + "] " + hex(verboseData);
   }

   @NotNull
   private static String hex(@NotNull byte[] bytes) {
      char[] out = new char[2 + bytes.length * 2];
      out[0] = '0';
      out[1] = 'x';
      char[] digits = "0123456789abcdef".toCharArray();
      int j = 2;

      for (byte b : bytes) {
         out[j++] = digits[b >>> 4 & 15];
         out[j++] = digits[b & 15];
      }

      return new String(out);
   }

   private record CachedManifest(int manifestHash, @NotNull VerboseManifest.Decoded decoded) {
   }
}
