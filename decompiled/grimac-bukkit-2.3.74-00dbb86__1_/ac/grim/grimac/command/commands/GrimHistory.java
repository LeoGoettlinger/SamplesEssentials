package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.history.HistoryService;
import ac.grim.grimac.api.storage.history.SessionDetail;
import ac.grim.grimac.api.storage.history.SessionSummary;
import ac.grim.grimac.api.storage.history.ViolationEntry;
import ac.grim.grimac.api.storage.identity.NameResolver;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.command.render.HistoryComponentRenderer;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.history.HistoryServiceImpl;
import ac.grim.grimac.manager.datastore.DataStoreLifecycle;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.incendo.cloud.Command;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.description.Description;
import ac.grim.grimac.shaded.incendo.cloud.parser.standard.IntegerParser;
import ac.grim.grimac.shaded.incendo.cloud.parser.standard.StringParser;
import ac.grim.grimac.shaded.incendo.cloud.suggestion.Suggestion;
import ac.grim.grimac.shaded.incendo.cloud.suggestion.SuggestionProvider;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.TextComponent;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GrimHistory implements BuildableCommand {
   private static final AtomicBoolean REPAIR_RUNNING = new AtomicBoolean();
   private static final int MAX_SUGGESTIONS = 30;
   private static final int MAX_PLAYER_SUGGESTIONS = 25;
   private static final String LATEST_ALIAS = "latest";
   private static final Predicate<ViolationEntry> FILTER_ERROR = v -> false;

   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      SuggestionProvider<Sender> listPageNumberSuggestions = listPageSuggestions();
      SuggestionProvider<Sender> sessionOrdinalSuggestions = sessionSuggestions();
      SuggestionProvider<Sender> violationPageSuggestions = violationPageSuggestions();
      SuggestionProvider<Sender> targetSuggestions = targetSuggestions(arguments);
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"})
            .literal("history", "hist")
            .literal("repair")
            .literal("check-ids")
            .permission("grim.history.repair")
            .handler(this::handleRepairCheckIds)
      );
      this.registerHistoryViewBranches(commandManager, false, targetSuggestions, listPageNumberSuggestions, sessionOrdinalSuggestions, violationPageSuggestions);
      this.registerHistoryViewBranches(commandManager, true, targetSuggestions, listPageNumberSuggestions, sessionOrdinalSuggestions, violationPageSuggestions);
   }

   private void registerHistoryViewBranches(
      CommandManager<Sender> commandManager,
      boolean withPlayerLiteral,
      SuggestionProvider<Sender> targetSuggestions,
      SuggestionProvider<Sender> listPageNumberSuggestions,
      SuggestionProvider<Sender> sessionOrdinalSuggestions,
      SuggestionProvider<Sender> violationPageSuggestions
   ) {
      Supplier<Command.Builder<Sender>> base = () -> {
         Command.Builder<Sender> b = commandManager.commandBuilder("grim", new String[]{"grimac"}).literal("history", "hist").permission("grim.history");
         if (withPlayerLiteral) {
            b = b.literal("player");
         }

         return b.required("target", StringParser.stringParser(), targetSuggestions);
      };
      commandManager.command(applyFilterFlags(commandManager, base.get()).handler(this::handleListPage1));
      commandManager.command(
         applyFilterFlags(commandManager, base.get().literal("page").required("page_number", IntegerParser.integerParser(1), listPageNumberSuggestions))
            .handler(this::handleListPageN)
      );
      commandManager.command(base.get().literal("session").handler(ctx -> this.handleSessionHelp(ctx, withPlayerLiteral)));
      commandManager.command(
         applyFilterFlags(
               commandManager,
               base.get()
                  .literal("session")
                  .required("session", StringParser.stringParser(), sessionOrdinalSuggestions)
                  .flag(
                     commandManager.flagBuilder("detailed")
                        .withAliases("d")
                        .withDescription(Description.of("Show each violation as its own row instead of time-bucketed groups."))
                  )
                  .flag(
                     commandManager.flagBuilder("verbose")
                        .withAliases("v")
                        .withDescription(Description.of("Include the raw verbose text inline on each line (also always available on hover)."))
                  )
            )
            .handler(this::handleDetailDefaultPage)
      );
      commandManager.command(
         applyFilterFlags(
               commandManager,
               base.get()
                  .literal("session")
                  .required("session", StringParser.stringParser(), sessionOrdinalSuggestions)
                  .literal("page")
                  .required("page_number", IntegerParser.integerParser(1), violationPageSuggestions)
                  .flag(
                     commandManager.flagBuilder("detailed")
                        .withAliases("d")
                        .withDescription(Description.of("Show each violation as its own row instead of time-bucketed groups."))
                  )
                  .flag(
                     commandManager.flagBuilder("verbose")
                        .withAliases("v")
                        .withDescription(Description.of("Include the raw verbose text inline on each line (also always available on hover)."))
                  )
            )
            .handler(this::handleDetailPageN)
      );
   }

   private static Command.Builder<Sender> applyFilterFlags(CommandManager<Sender> commandManager, Command.Builder<Sender> b) {
      return b.flag(
            commandManager.flagBuilder("name")
               .withComponent(StringParser.stringParser())
               .withDescription(Description.of("Filter to violations whose check display name matches this regex."))
         )
         .flag(
            commandManager.flagBuilder("match")
               .withComponent(StringParser.stringParser())
               .withDescription(Description.of("Filter to violations whose verbose text matches this regex."))
         )
         .flag(
            commandManager.flagBuilder("grep")
               .withComponent(StringParser.stringParser())
               .withDescription(Description.of("Filter to violations whose display name OR verbose text matches this regex."))
         );
   }

   private void handleListPage1(CommandContext<Sender> context) {
      Sender sender = context.sender();
      String target = context.get("target");
      Predicate<ViolationEntry> filter = parseFilterFromContext(sender, context);
      if (filter != FILTER_ERROR) {
         this.runWithPrelude(
            sender, target, (uuid, displayName, lifecycle, history) -> this.renderList(sender, lifecycle, history, uuid, displayName, 1, filter)
         );
      }
   }

   private void handleListPageN(CommandContext<Sender> context) {
      Sender sender = context.sender();
      String target = context.get("target");
      int page = context.<Integer>get("page_number");
      Predicate<ViolationEntry> filter = parseFilterFromContext(sender, context);
      if (filter != FILTER_ERROR) {
         this.runWithPrelude(
            sender,
            target,
            (uuid, displayName, lifecycle, history) -> this.renderList(sender, lifecycle, history, uuid, displayName, Math.max(1, page), filter)
         );
      }
   }

   private void handleDetailDefaultPage(CommandContext<Sender> context) {
      Sender sender = context.sender();
      String target = context.get("target");
      String sessionRaw = context.get("session");
      boolean detailed = context.flags().hasFlag("detailed");
      boolean verbose = context.flags().hasFlag("verbose");
      Predicate<ViolationEntry> filter = parseFilterFromContext(sender, context);
      if (filter != FILTER_ERROR) {
         this.runWithPrelude(
            sender,
            target,
            (uuid, displayName, lifecycle, history) -> {
               Integer ordinal = resolveSessionOrdinal(sessionRaw, uuid, history);
               return ordinal == null
                  ? List.of(
                     this.message(
                        "grim-history-session-not-found",
                        "%prefix% &cSession &f%ordinal%&c not found for &f%player%&c.",
                        Map.of("player", displayName, "ordinal", sessionRaw)
                     )
                  )
                  : this.renderDetail(sender, lifecycle, history, uuid, displayName, ordinal, detailed, verbose, null, filter);
            }
         );
      }
   }

   private void handleDetailPageN(CommandContext<Sender> context) {
      Sender sender = context.sender();
      String target = context.get("target");
      String sessionRaw = context.get("session");
      int page = context.<Integer>get("page_number");
      boolean detailed = context.flags().hasFlag("detailed");
      boolean verbose = context.flags().hasFlag("verbose");
      Predicate<ViolationEntry> filter = parseFilterFromContext(sender, context);
      if (filter != FILTER_ERROR) {
         this.runWithPrelude(
            sender,
            target,
            (uuid, displayName, lifecycle, history) -> {
               Integer ordinal = resolveSessionOrdinal(sessionRaw, uuid, history);
               return ordinal == null
                  ? List.of(
                     this.message(
                        "grim-history-session-not-found",
                        "%prefix% &cSession &f%ordinal%&c not found for &f%player%&c.",
                        Map.of("player", displayName, "ordinal", sessionRaw)
                     )
                  )
                  : this.renderDetail(sender, lifecycle, history, uuid, displayName, ordinal, detailed, verbose, Math.max(1, page), filter);
            }
         );
      }
   }

   private void handleSessionHelp(CommandContext<Sender> ctx, boolean viaPlayer) {
      Sender sender = ctx.sender();
      String target = ctx.get("target");
      String addressPrefix = "/grim history " + (viaPlayer ? "player " : "");
      sender.sendMessage(
         Component.text()
            .append(Component.text(addressPrefix, NamedTextColor.GRAY))
            .append(Component.text(target, NamedTextColor.WHITE))
            .append(Component.text(" session ", NamedTextColor.GRAY))
            .append(Component.text("<N | latest>", NamedTextColor.AQUA))
            .append(Component.text(" — show violations for a specific session.", NamedTextColor.GRAY))
            .build()
      );
      sender.sendMessage(Component.text("  N is the session ordinal (1 = oldest). 'latest' / 'last' / 'l' = most recent.", NamedTextColor.GRAY));
      sender.sendMessage(Component.text("Optional:", NamedTextColor.GRAY));
      sender.sendMessage(
         Component.text("  page <P>", NamedTextColor.YELLOW)
            .append(Component.text("                  page through this session's violations", NamedTextColor.GRAY))
      );
      sender.sendMessage(
         Component.text("  --detailed / -d", NamedTextColor.YELLOW)
            .append(Component.text("           raw per-violation rows (no time-bucketing)", NamedTextColor.GRAY))
      );
      sender.sendMessage(
         Component.text("  --verbose / -v", NamedTextColor.YELLOW)
            .append(Component.text("            inline verbose text (also on hover)", NamedTextColor.GRAY))
      );
      sender.sendMessage(
         Component.text("  --name <regex>", NamedTextColor.YELLOW).append(Component.text("            filter by check display name", NamedTextColor.GRAY))
      );
      sender.sendMessage(
         Component.text("  --match <regex>", NamedTextColor.YELLOW).append(Component.text("           filter by verbose text", NamedTextColor.GRAY))
      );
      sender.sendMessage(
         Component.text("  --grep <regex>", NamedTextColor.YELLOW)
            .append(Component.text("            filter by either name or verbose (AND-composes with others)", NamedTextColor.GRAY))
      );
      sender.sendMessage(Component.text("Examples:", NamedTextColor.GRAY));
      sender.sendMessage(
         ((TextComponent)Component.text("  " + addressPrefix, NamedTextColor.GRAY).append(Component.text(target, NamedTextColor.WHITE)))
            .append(Component.text(" session latest -d -v", NamedTextColor.GRAY))
      );
      sender.sendMessage(
         ((TextComponent)Component.text("  " + addressPrefix, NamedTextColor.GRAY).append(Component.text(target, NamedTextColor.WHITE)))
            .append(Component.text(" session 1 --grep reach", NamedTextColor.GRAY))
      );
   }

   @Nullable
   private static Predicate<ViolationEntry> parseFilterFromContext(Sender sender, CommandContext<Sender> ctx) {
      Pattern namePat;
      Pattern verbosePat;
      Pattern grepPat;
      try {
         namePat = compileFlag(ctx, "name");
         verbosePat = compileFlag(ctx, "match");
         grepPat = compileFlag(ctx, "grep");
      } catch (GrimHistory.BadRegexException var8) {
         sender.sendMessage(MessageUtil.miniMessage("<red>Invalid regex for --" + var8.flag + ": " + var8.detail));
         return FILTER_ERROR;
      }

      return namePat == null && verbosePat == null && grepPat == null ? null : v -> {
         if (namePat != null && !namePat.matcher(v.displayName()).find()) {
            return false;
         } else {
            if (verbosePat != null) {
               String vb = v.verbose();
               if (vb == null || !verbosePat.matcher(vb).find()) {
                  return false;
               }
            }

            if (grepPat != null && !grepPat.matcher(v.displayName()).find()) {
               String vb = v.verbose();
               if (vb == null || !grepPat.matcher(vb).find()) {
                  return false;
               }
            }

            return true;
         }
      };
   }

   @Nullable
   private static Pattern compileFlag(CommandContext<Sender> ctx, String flag) {
      String raw = ctx.flags().get(flag);
      if (raw == null) {
         return null;
      } else {
         try {
            return Pattern.compile(raw, 2);
         } catch (PatternSyntaxException var4) {
            throw new GrimHistory.BadRegexException(flag, var4.getDescription());
         }
      }
   }

   @Nullable
   private static Integer resolveSessionOrdinal(String raw, UUID uuid, HistoryService history) throws Exception {
      if (raw == null) {
         return null;
      } else {
         String trimmed = raw.trim().toLowerCase(Locale.ROOT);
         if (!trimmed.equals("latest") && !trimmed.equals("last") && !trimmed.equals("l")) {
            try {
               int n = Integer.parseInt(trimmed);
               return n >= 1 ? n : null;
            } catch (NumberFormatException var6) {
               return null;
            }
         } else {
            long total = history.countSessions(uuid).toCompletableFuture().get(5L, TimeUnit.SECONDS);
            return total >= 1L ? (int)Math.min(2147483647L, total) : null;
         }
      }
   }

   private void runWithPrelude(Sender sender, String target, GrimHistory.HistoryAction action) {
      DataStoreLifecycle lifecycle = GrimAPI.INSTANCE.getDataStoreLifecycle();
      if (lifecycle == null || !lifecycle.isEnabled()) {
         sender.sendMessage(this.message("grim-history-disabled", "%prefix% &cHistory subsystem is disabled!", Map.of()));
      } else if (!lifecycle.isLoaded()) {
         sender.sendMessage(
            this.message("grim-history-load-failure", "%prefix% &cHistory subsystem failed to load! Check server console for errors.", Map.of())
         );
      } else {
         UUID onlineUuid = onlineUuid(target);
         GrimAPI.INSTANCE
            .getScheduler()
            .getAsyncScheduler()
            .runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> this.runWithPreludeAsync(sender, target, onlineUuid, lifecycle, action));
      }
   }

   private void runWithPreludeAsync(Sender sender, String target, @Nullable UUID onlineUuid, DataStoreLifecycle lifecycle, GrimHistory.HistoryAction action) {
      try {
         HistoryService history = lifecycle.historyService();
         if (history == null) {
            sendHistoryMessage(sender, this.message("grim-history-disabled", "%prefix% &cHistory subsystem is disabled!", Map.of()));
            return;
         }

         UUID targetUuid = resolveUuid(target, lifecycle, onlineUuid);
         if (targetUuid == null) {
            sendHistoryMessage(sender, this.message("grim-history-unknown-player", "%prefix% &cUnknown player: &f%player%", Map.of("player", target)));
            return;
         }

         sendHistoryMessages(sender, action.run(targetUuid, target, lifecycle, history));
      } catch (Exception var8) {
         sendHistoryMessage(
            sender,
            this.message(
               "grim-history-failed",
               "%prefix% &cFailed to load history: &7%error%",
               Map.of("error", var8.getMessage() == null ? var8.getClass().getSimpleName() : var8.getMessage())
            )
         );
      }
   }

   private List<Component> renderList(
      Sender sender, DataStoreLifecycle lifecycle, HistoryService history, UUID uuid, String displayName, int page, @Nullable Predicate<ViolationEntry> filter
   ) throws Exception {
      int entriesPerPage = lifecycle.config().history().entriesPerPage();
      long totalSessions = history.countSessions(uuid).toCompletableFuture().get(5L, TimeUnit.SECONDS);
      int maxPages = Math.max(1, (int)((totalSessions + entriesPerPage - 1L) / Math.max(1, entriesPerPage)));
      if (page > maxPages) {
         page = maxPages;
      }

      Cursor cursor = this.advanceToPage(history, uuid, entriesPerPage, page);
      Page<SessionSummary> result = history.listSessions(uuid, cursor, entriesPerPage).toCompletableFuture().get(10L, TimeUnit.SECONDS);
      if (filter != null) {
         result = filterSessionsByDetail(history, uuid, result, filter);
      }

      UUID ongoingSessionId = ongoingSessionIdFor(lifecycle, uuid);
      return HistoryComponentRenderer.renderSessionList(sender, uuid, displayName, page, maxPages, result, ongoingSessionId);
   }

   private static Page<SessionSummary> filterSessionsByDetail(HistoryService history, UUID uuid, Page<SessionSummary> result, Predicate<ViolationEntry> filter) {
      List<SessionSummary> kept = new ArrayList<>();

      for (SessionSummary s : result.items()) {
         try {
            SessionDetail d = history.getSessionDetail(uuid, s.sessionId()).toCompletableFuture().get(2L, TimeUnit.SECONDS);
            if (d != null && d.violations().stream().anyMatch(filter)) {
               kept.add(s);
            }
         } catch (Exception var8) {
         }
      }

      return new Page<>(kept, result.nextCursor());
   }

   private List<Component> renderDetail(
      Sender sender,
      DataStoreLifecycle lifecycle,
      HistoryService history,
      UUID uuid,
      String displayName,
      int sessionOrdinal,
      boolean detailed,
      boolean verbose,
      @Nullable Integer violationPage,
      @Nullable Predicate<ViolationEntry> filter
   ) throws Exception {
      SessionDetail detail = null;
      if (history instanceof HistoryServiceImpl impl) {
         detail = impl.getSessionDetailByOrdinal(uuid, sessionOrdinal).toCompletableFuture().get(10L, TimeUnit.SECONDS);
      }

      if (detail == null) {
         return List.of(
            this.message(
               "grim-history-session-not-found",
               "%prefix% &cSession &f%ordinal%&c not found for &f%player%&c.",
               Map.of("player", displayName, "ordinal", Integer.toString(sessionOrdinal))
            )
         );
      } else {
         if (filter != null) {
            detail = HistoryComponentRenderer.applyFilter(detail, filter);
         }

         int pageSize = lifecycle.config().history().entriesPerPage();
         UUID ongoingSessionId = ongoingSessionIdFor(lifecycle, uuid);
         boolean isOngoing = ongoingSessionId != null && ongoingSessionId.equals(detail.sessionId());
         return HistoryComponentRenderer.renderSessionDetail(sender, displayName, detail, detailed, verbose, violationPage, pageSize, isOngoing);
      }
   }

   private void handleRepairCheckIds(CommandContext<Sender> context) {
      Sender sender = context.sender();
      DataStoreLifecycle lifecycle = GrimAPI.INSTANCE.getDataStoreLifecycle();
      if (lifecycle != null && lifecycle.isLoaded() && lifecycle.config() != null) {
         String backendId = lifecycle.config().routing().get(Categories.VIOLATION);
         if (backendId != null && !backendId.equalsIgnoreCase("none")) {
            Backend backend = lifecycle.allBackendsForCommands().get(backendId);
            if (backend == null) {
               sender.sendMessage(Component.text("Violation backend '" + backendId + "' is not active.", NamedTextColor.RED));
            } else if (!REPAIR_RUNNING.compareAndSet(false, true)) {
               sender.sendMessage(Component.text("A history check-id repair is already running.", NamedTextColor.YELLOW));
            } else {
               List<GrimHistory.CheckDefinition> liveChecks = snapshotLiveCheckDefinitions();
               logBoth(sender, Component.text("Repairing history check ids on backend '" + backendId + "' in the background...", NamedTextColor.AQUA));
               GrimAPI.INSTANCE
                  .getScheduler()
                  .getAsyncScheduler()
                  .runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> runRepairAsync(sender, lifecycle, backend, liveChecks));
            }
         } else {
            sender.sendMessage(Component.text("No violation backend is routed; nothing to repair.", NamedTextColor.YELLOW));
         }
      } else {
         sender.sendMessage(Component.text("History storage is disabled (see database.yml).", NamedTextColor.RED));
      }
   }

   private static void runRepairAsync(Sender sender, DataStoreLifecycle lifecycle, Backend backend, List<GrimHistory.CheckDefinition> liveChecks) {
      try {
         int prewarmed = prewarmCatalog(lifecycle, liveChecks);
         GrimHistory.RepairPlan plan = buildRepairPlan(backend);
         CheckCatalogRepairResult result = backend.repairCheckCatalog(plan.legacyToCatalogIds(), currentGrimVersion());
         runOnGlobalThread(() -> reportRepairComplete(sender, prewarmed, plan, result));
      } catch (Exception var10) {
         runOnGlobalThread(() -> logBoth(sender, Component.text("Repair failed: " + var10.getMessage(), NamedTextColor.RED)));
         LogUtil.error("v1 check-id repair failed via /grim history repair check-ids", var10);
      } finally {
         REPAIR_RUNNING.set(false);
      }
   }

   private static void reportRepairComplete(Sender sender, int prewarmed, GrimHistory.RepairPlan plan, CheckCatalogRepairResult result) {
      logBoth(
         sender,
         Component.text()
            .append(Component.text("Repair complete: ", NamedTextColor.GREEN))
            .append(Component.text(prewarmed + " live check definitions prewarmed, "))
            .append(Component.text(result.mappingsApplied() + " id mapping(s), "))
            .append(Component.text(result.violationsUpdated() + " violation row(s) rewritten, "))
            .append(Component.text(result.catalogVersionsUpdated() + " stub version row(s) fixed"))
            .build()
      );
      if (plan.ambiguousHashes() > 0 || plan.catalogIdCollisions() > 0) {
         logBoth(
            sender,
            Component.text()
               .append(Component.text("Skipped ", NamedTextColor.YELLOW))
               .append(Component.text(plan.ambiguousHashes() + " ambiguous hash mapping(s), "))
               .append(Component.text(plan.catalogIdCollisions() + " catalog-id collision(s)."))
               .build()
         );
      }
   }

   private static List<GrimHistory.CheckDefinition> snapshotLiveCheckDefinitions() {
      List<GrimHistory.CheckDefinition> definitions = new ArrayList<>();
      Set<String> seenStableKeys = new HashSet<>();

      for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
         for (AbstractCheck check : player.checkManager.allChecks.values()) {
            String stableKey = check.getStableKey();
            if (stableKey != null && !stableKey.isBlank() && seenStableKeys.add(stableKey)) {
               definitions.add(new GrimHistory.CheckDefinition(stableKey, check.getCheckName(), check.getDescription()));
            }
         }
      }

      return definitions;
   }

   private static int prewarmCatalog(DataStoreLifecycle lifecycle, List<GrimHistory.CheckDefinition> definitions) {
      CheckRegistry registry = lifecycle.checkRegistryForCommands();
      if (registry == null) {
         return 0;
      } else {
         int prewarmed = 0;
         String version = currentGrimVersion();

         for (GrimHistory.CheckDefinition definition : definitions) {
            registry.intern(definition.stableKey(), definition.display(), definition.description(), version);
            prewarmed++;
         }

         return prewarmed;
      }
   }

   private static GrimHistory.RepairPlan buildRepairPlan(Backend backend) {
      Set<Integer> catalogIds = new HashSet<>();
      Map<Integer, List<CheckCatalogRow>> byLegacyHash = new LinkedHashMap<>();

      for (CheckCatalogRow row : backend.checkCatalog().loadAll()) {
         catalogIds.add(row.checkId());
         byLegacyHash.computeIfAbsent(row.stableKey().hashCode(), k -> new ArrayList<>()).add(row);
      }

      Map<Integer, Integer> repairIds = new LinkedHashMap<>();
      int ambiguousHashes = 0;
      int catalogIdCollisions = 0;

      for (Entry<Integer, List<CheckCatalogRow>> entry : byLegacyHash.entrySet()) {
         int legacyId = entry.getKey();
         List<CheckCatalogRow> owners = entry.getValue();
         if (owners.size() != 1) {
            ambiguousHashes += owners.size();
         } else {
            CheckCatalogRow row = owners.get(0);
            if (legacyId != row.checkId()) {
               if (catalogIds.contains(legacyId)) {
                  catalogIdCollisions++;
               } else {
                  repairIds.put(legacyId, row.checkId());
               }
            }
         }
      }

      return new GrimHistory.RepairPlan(repairIds, ambiguousHashes, catalogIdCollisions);
   }

   private static String currentGrimVersion() {
      return GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
   }

   private static void sendHistoryMessage(Sender sender, Component message) {
      sendHistoryMessages(sender, List.of(message));
   }

   private static void sendHistoryMessages(Sender sender, List<Component> messages) {
      if (!messages.isEmpty()) {
         runOnGlobalThread(() -> {
            for (Component message : messages) {
               sender.sendMessage(message);
            }
         });
      }
   }

   private static void runOnGlobalThread(Runnable task) {
      GrimAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().run(GrimAPI.INSTANCE.getGrimPlugin(), task);
   }

   private static void logBoth(Sender sender, Component msg) {
      sender.sendMessage(msg);
      LogUtil.info(plain(msg));
   }

   private static String plain(Component c) {
      StringBuilder sb = new StringBuilder();
      flatten(c, sb);
      return sb.toString();
   }

   private static void flatten(Component c, StringBuilder sb) {
      if (c instanceof TextComponent tc) {
         sb.append(tc.content());
      }

      for (Component child : c.children()) {
         flatten(child, sb);
      }
   }

   private Cursor advanceToPage(HistoryService history, UUID uuid, int pageSize, int page) throws Exception {
      if (page <= 1) {
         return null;
      } else {
         Cursor cursor = null;

         for (int i = 1; i < page; i++) {
            Page<SessionSummary> r = history.listSessions(uuid, cursor, pageSize).toCompletableFuture().get(5L, TimeUnit.SECONDS);
            cursor = r.nextCursor();
            if (cursor == null) {
               break;
            }
         }

         return cursor;
      }
   }

   @Nullable
   private static UUID ongoingSessionIdFor(DataStoreLifecycle lifecycle, UUID player) {
      return lifecycle.sessionTracker().currentSessionId(player);
   }

   private static SuggestionProvider<Sender> targetSuggestions(CloudPlatformCommandArguments arguments) {
      SuggestionProvider<Sender> onlineProvider = arguments.onlinePlayerSuggestions();
      return SuggestionProvider.blocking(
         (ctx, in) -> {
            String partial = in.remainingInput();
            String partialLower = partial == null ? "" : partial.toLowerCase(Locale.ROOT);

            List<Suggestion> onlineSuggestions;
            try {
               Iterable<? extends Suggestion> onlineIt = (Iterable<? extends Suggestion>)onlineProvider.suggestionsFuture(ctx, in)
                  .toCompletableFuture()
                  .get(500L, TimeUnit.MILLISECONDS);
               onlineSuggestions = new ArrayList<>();
               onlineIt.forEach(onlineSuggestions::add);
            } catch (Exception var12) {
               onlineSuggestions = List.of();
            }

            if (partialLower.isEmpty()) {
               return onlineSuggestions;
            } else {
               Set<String> seen = new HashSet<>();
               List<Suggestion> out = new ArrayList<>();

               for (Suggestion s : onlineSuggestions) {
                  if (seen.add(s.suggestion().toLowerCase(Locale.ROOT))) {
                     out.add(s);
                     if (out.size() >= 25) {
                        return out;
                     }
                  }
               }

               DataStoreLifecycle dsl = GrimAPI.INSTANCE.getDataStoreLifecycle();
               if (dsl != null && dsl.isLoaded() && dsl.dataStore() != null) {
                  try {
                     Page<PlayerIdentity> page = dsl.dataStore()
                        .query(Categories.PLAYER_IDENTITY, Queries.listPlayersByNamePrefix(partialLower, 25))
                        .toCompletableFuture()
                        .get(1L, TimeUnit.SECONDS);

                     for (PlayerIdentity id : page.items()) {
                        if (id.currentName() != null && seen.add(id.currentName().toLowerCase(Locale.ROOT))) {
                           out.add(Suggestion.suggestion(id.currentName()));
                           if (out.size() >= 25) {
                              return out;
                           }
                        }
                     }
                  } catch (Exception var13) {
                  }

                  return out;
               } else {
                  return out;
               }
            }
         }
      );
   }

   private static SuggestionProvider<Sender> listPageSuggestions() {
      return SuggestionProvider.blocking((ctx, in) -> {
         UUID uuid = resolveTargetUuid(ctx);
         if (uuid == null) {
            return List.of();
         } else {
            DataStoreLifecycle dsl = GrimAPI.INSTANCE.getDataStoreLifecycle();
            if (dsl != null && dsl.isLoaded() && dsl.historyService() != null) {
               try {
                  long total = dsl.historyService().countSessions(uuid).toCompletableFuture().get(1L, TimeUnit.SECONDS);
                  int entriesPerPage = dsl.config().history().entriesPerPage();
                  int maxPages = Math.max(1, (int)((total + entriesPerPage - 1L) / Math.max(1, entriesPerPage)));
                  return rangeSuggestions(1, Math.min(maxPages, 30));
               } catch (Exception var8) {
                  return List.of();
               }
            } else {
               return List.of();
            }
         }
      });
   }

   private static SuggestionProvider<Sender> sessionSuggestions() {
      return SuggestionProvider.blocking((ctx, in) -> {
         UUID uuid = resolveTargetUuid(ctx);
         if (uuid == null) {
            return List.of(Suggestion.suggestion("latest"));
         } else {
            DataStoreLifecycle dsl = GrimAPI.INSTANCE.getDataStoreLifecycle();
            if (dsl != null && dsl.isLoaded() && dsl.historyService() != null) {
               try {
                  long total = dsl.historyService().countSessions(uuid).toCompletableFuture().get(1L, TimeUnit.SECONDS);
                  int max = (int)Math.min(total, 30L);
                  List<Suggestion> out = new ArrayList<>(max + 1);
                  out.add(Suggestion.suggestion("latest"));

                  for (int i = 1; i <= max; i++) {
                     out.add(Suggestion.suggestion(Integer.toString(i)));
                  }

                  return out;
               } catch (Exception var9) {
                  return List.of(Suggestion.suggestion("latest"));
               }
            } else {
               return List.of(Suggestion.suggestion("latest"));
            }
         }
      });
   }

   private static SuggestionProvider<Sender> violationPageSuggestions() {
      return SuggestionProvider.blocking((ctx, in) -> {
         UUID uuid = resolveTargetUuid(ctx);
         if (uuid == null) {
            return List.of();
         } else {
            DataStoreLifecycle dsl = GrimAPI.INSTANCE.getDataStoreLifecycle();
            if (dsl != null && dsl.isLoaded() && dsl.historyService() != null) {
               String sessionRaw = ctx.getOrDefault("session", null);
               if (sessionRaw == null) {
                  return List.of();
               } else {
                  try {
                     Integer ordinal = resolveSessionOrdinal(sessionRaw, uuid, dsl.historyService());
                     if (ordinal == null) {
                        return List.of();
                     } else if (dsl.historyService() instanceof HistoryServiceImpl impl) {
                        SessionDetail detail = impl.getSessionDetailByOrdinal(uuid, ordinal).toCompletableFuture().get(1L, TimeUnit.SECONDS);
                        if (detail == null) {
                           return List.of();
                        } else {
                           int entriesPerPage = dsl.config().history().entriesPerPage();
                           int rows = Math.max(detail.violations().size(), detail.buckets().size());
                           int maxPages = Math.max(1, (rows + entriesPerPage - 1) / Math.max(1, entriesPerPage));
                           return rangeSuggestions(1, Math.min(maxPages, 30));
                        }
                     } else {
                        return List.of();
                     }
                  } catch (Exception var11) {
                     return List.of();
                  }
               }
            } else {
               return List.of();
            }
         }
      });
   }

   @Nullable
   private static UUID resolveTargetUuid(CommandContext<Sender> ctx) {
      String target = ctx.getOrDefault("target", null);
      return target == null ? null : resolveUuid(target, GrimAPI.INSTANCE.getDataStoreLifecycle(), null);
   }

   @Nullable
   private static UUID resolveUuid(String name, @Nullable DataStoreLifecycle lifecycle) {
      return resolveUuid(name, lifecycle, onlineUuid(name));
   }

   @Nullable
   private static UUID resolveUuid(String name, @Nullable DataStoreLifecycle lifecycle, @Nullable UUID onlineUuid) {
      if (onlineUuid != null) {
         return onlineUuid;
      } else {
         if (lifecycle != null && lifecycle.isLoaded()) {
            NameResolver resolver = lifecycle.nameResolver();
            if (resolver != null) {
               try {
                  Optional<UUID> hit = resolver.resolveByName(name).toCompletableFuture().get(2L, TimeUnit.SECONDS);
                  if (hit.isPresent()) {
                     return hit.get();
                  }
               } catch (Exception var6) {
               }
            }
         }

         try {
            return UUID.fromString(name);
         } catch (IllegalArgumentException var5) {
            return null;
         }
      }
   }

   @Nullable
   private static UUID onlineUuid(String name) {
      PlatformPlayer online = GrimAPI.INSTANCE.getPlatformPlayerFactory().getFromName(name);
      return online == null ? null : online.getUniqueId();
   }

   private static List<Suggestion> rangeSuggestions(int fromInclusive, int toInclusive) {
      if (toInclusive < fromInclusive) {
         return List.of();
      } else {
         List<Suggestion> out = new ArrayList<>(toInclusive - fromInclusive + 1);

         for (int i = fromInclusive; i <= toInclusive; i++) {
            out.add(Suggestion.suggestion(Integer.toString(i)));
         }

         return out;
      }
   }

   private Component message(String key, String fallback, Map<String, String> vars) {
      String raw = GrimAPI.INSTANCE.getConfigManager().getConfig().getStringElse(key, fallback);

      for (Entry<String, String> e : vars.entrySet()) {
         raw = raw.replace("%" + e.getKey() + "%", e.getValue());
      }

      return MessageUtil.miniMessage(raw);
   }

   private static final class BadRegexException extends RuntimeException {
      final String flag;
      final String detail;

      BadRegexException(String flag, String detail) {
         super(detail);
         this.flag = flag;
         this.detail = detail;
      }
   }

   private record CheckDefinition(String stableKey, String display, String description) {
   }

   @FunctionalInterface
   private interface HistoryAction {
      List<Component> run(UUID var1, String var2, DataStoreLifecycle var3, HistoryService var4) throws Exception;
   }

   private record RepairPlan(Map<Integer, Integer> legacyToCatalogIds, int ambiguousHashes, int catalogIdCollisions) {
   }
}
