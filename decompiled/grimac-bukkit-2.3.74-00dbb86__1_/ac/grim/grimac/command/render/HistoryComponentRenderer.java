package ac.grim.grimac.command.render;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.history.CheckBucket;
import ac.grim.grimac.api.storage.history.CheckCount;
import ac.grim.grimac.api.storage.history.SessionDetail;
import ac.grim.grimac.api.storage.history.SessionSummary;
import ac.grim.grimac.api.storage.history.ViolationEntry;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent;
import ac.grim.grimac.shaded.kyori.adventure.text.event.HoverEvent;
import ac.grim.grimac.shaded.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import ac.grim.grimac.shaded.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Generated;

public final class HistoryComponentRenderer {
   private static final String SESSION_ROW_FALLBACK = "<hover:show_text:'&bSession &7%session_uuid%<newline>&7Click or run &e%detail_command%<newline>&7to view session details.'>&8[&b%grim_version%&8] &8[&b%server_name%&8] &8[&b%client_version%&8] &bSession &f%ordinal%&b duration &f%duration%&b with &c%violations%&b violations &8[&c%unique_checks%&8]%crashed_marker% &8(&7%timeago% ago&8)</hover>";
   private static final String GROUP_ROW_FALLBACK = "<hover:show_text:'&bViolations in &f%bucket_start%–%bucket_end%&b:%bucket_hover_entries%'>&7- %checks_list% &8(&b%offset%&8)</hover>";
   private static final Pattern LEGACY_FORMAT_PATTERN = Pattern.compile("[§&](?:[0-9a-fk-orxA-FK-ORX]|#[A-Fa-f0-9]{6}|x(?:[§&][A-Fa-f0-9]){6})");

   @NotNull
   public static List<Component> renderSessionList(
      @NotNull Sender sender,
      @NotNull UUID player,
      @NotNull String playerDisplayName,
      int page,
      int maxPages,
      @NotNull Page<SessionSummary> result,
      @Nullable UUID ongoingSessionId
   ) {
      ConfigManager cfg = GrimAPI.INSTANCE.getConfigManager().getConfig();
      String safePlayer = mmSafe(playerDisplayName);
      if (result.items().isEmpty()) {
         return List.of(
            parse(
                  sender,
                  cfg,
                  "grim-history-no-sessions",
                  "%prefix% &7No session history for &f%player%&7.",
                  Map.of("player", HistoryComponentRenderer.TemplateValue.raw(safePlayer))
               )
               .component()
         );
      } else {
         List<Component> out = new ArrayList<>(result.items().size() + 1);
         String maxPagesStr = Integer.toString(Math.max(1, maxPages));
         out.add(
            parse(
                  sender,
                  cfg,
                  "grim-history-header",
                  "%prefix% &bShowing session history for &f%player% &8[&f%page%&7/&f%max_pages%&8]",
                  Map.of(
                     "player",
                     HistoryComponentRenderer.TemplateValue.raw(safePlayer),
                     "page",
                     HistoryComponentRenderer.TemplateValue.raw(Integer.toString(page)),
                     "max_pages",
                     HistoryComponentRenderer.TemplateValue.raw(maxPagesStr),
                     "maxPages",
                     HistoryComponentRenderer.TemplateValue.raw(maxPagesStr)
                  )
               )
               .component()
         );

         for (SessionSummary s : result.items()) {
            boolean ongoing = ongoingSessionId != null && ongoingSessionId.equals(s.sessionId());
            out.add(renderSummaryLine(sender, cfg, s, ongoing, playerDisplayName));
         }

         return out;
      }
   }

   private static Component renderSummaryLine(Sender sender, ConfigManager cfg, SessionSummary s, boolean ongoing, String playerDisplayName) {
      long elapsedNow = Math.max(0L, System.currentTimeMillis() - s.startedEpochMs());
      String durationText = ongoing ? "current" : formatDuration(s.durationMs());
      String crashedMarker = !ongoing && s.endedUnexpectedly() ? cfg.getStringElse("grim-history-crashed-marker", " &8(&ccrashed&8)") : "";
      String detailCommand = "/grim history player " + playerDisplayName + " session " + s.sessionOrdinal();
      HistoryComponentRenderer.RenderedTemplate rendered = parse(
         sender,
         cfg,
         "grim-history-session",
         "<hover:show_text:'&bSession &7%session_uuid%<newline>&7Click or run &e%detail_command%<newline>&7to view session details.'>&8[&b%grim_version%&8] &8[&b%server_name%&8] &8[&b%client_version%&8] &bSession &f%ordinal%&b duration &f%duration%&b with &c%violations%&b violations &8[&c%unique_checks%&8]%crashed_marker% &8(&7%timeago% ago&8)</hover>",
         Map.ofEntries(
            Map.entry("player", HistoryComponentRenderer.TemplateValue.raw(mmSafe(playerDisplayName))),
            Map.entry("grim_version", HistoryComponentRenderer.TemplateValue.raw(nullToUnknown(s.grimVersion()))),
            Map.entry("server_name", HistoryComponentRenderer.TemplateValue.raw(nullToUnknown(s.serverName()))),
            Map.entry("client_version", HistoryComponentRenderer.TemplateValue.raw(clientVersionDisplay(s.clientVersion()))),
            Map.entry("client_brand", HistoryComponentRenderer.TemplateValue.raw(mmSafe(nullToUnknown(s.clientBrand())))),
            Map.entry("ordinal", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(s.sessionOrdinal()))),
            Map.entry("duration", HistoryComponentRenderer.TemplateValue.raw(durationText)),
            Map.entry("violations", HistoryComponentRenderer.TemplateValue.raw(Long.toString(s.violationCount()))),
            Map.entry("unique_checks", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(s.uniqueCheckCount()))),
            Map.entry("crashed_marker", HistoryComponentRenderer.TemplateValue.raw(crashedMarker)),
            Map.entry("timeago", HistoryComponentRenderer.TemplateValue.raw(formatDuration(elapsedNow))),
            Map.entry("session_uuid", HistoryComponentRenderer.TemplateValue.text(s.sessionId().toString())),
            Map.entry("detail_command", HistoryComponentRenderer.TemplateValue.text(detailCommand))
         )
      );
      Component line = rendered.component();
      if (!hasInlineHover(rendered.raw())) {
         line = line.hoverEvent(HoverEvent.showText(sessionHover(sender, detailCommand, s.sessionId().toString())));
      }

      return line.clickEvent(ClickEvent.runCommand(detailCommand));
   }

   @NotNull
   public static List<Component> renderSessionDetail(
      @NotNull Sender sender,
      @NotNull String playerDisplayName,
      @NotNull SessionDetail d,
      boolean detailed,
      boolean verbose,
      @Nullable Integer pageArg,
      int pageSize,
      boolean isOngoing
   ) {
      ConfigManager cfg = GrimAPI.INSTANCE.getConfigManager().getConfig();
      List<Component> out = new ArrayList<>();
      int perPage = Math.max(1, pageSize);
      long elapsedNow = Math.max(0L, System.currentTimeMillis() - d.startedEpochMs());
      String durationText = isOngoing ? "current" : formatDuration(d.durationMs());
      int totalRows = detailed ? d.violations().size() : d.buckets().size();
      int maxPages = Math.max(1, (totalRows + perPage - 1) / perPage);
      int page = pageArg == null ? maxPages : Math.max(1, Math.min(pageArg, maxPages));
      Map<String, HistoryComponentRenderer.TemplateValue> metaVars = Map.ofEntries(
         Map.entry("player", HistoryComponentRenderer.TemplateValue.raw(mmSafe(playerDisplayName))),
         Map.entry("ordinal", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(d.sessionOrdinal()))),
         Map.entry("grim_version", HistoryComponentRenderer.TemplateValue.raw(nullToUnknown(d.grimVersion()))),
         Map.entry("server_name", HistoryComponentRenderer.TemplateValue.raw(nullToUnknown(d.serverName()))),
         Map.entry("client_version", HistoryComponentRenderer.TemplateValue.raw(clientVersionDisplay(d.clientVersion()))),
         Map.entry("client_brand", HistoryComponentRenderer.TemplateValue.raw(mmSafe(nullToUnknown(d.clientBrand())))),
         Map.entry("duration", HistoryComponentRenderer.TemplateValue.raw(durationText)),
         Map.entry("timeago", HistoryComponentRenderer.TemplateValue.raw(formatDuration(elapsedNow))),
         Map.entry("violations", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(d.violations().size()))),
         Map.entry("unique_checks", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(d.uniqueCheckCount()))),
         Map.entry("bucket_size", HistoryComponentRenderer.TemplateValue.raw(formatDuration(d.bucketSizeMs()))),
         Map.entry("page", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(page))),
         Map.entry("max_pages", HistoryComponentRenderer.TemplateValue.raw(Integer.toString(maxPages)))
      );
      out.add(parse(sender, cfg, "grim-history-detail-header", "%prefix% &bShowing &f%player%&b's session &f%ordinal%&b details:", metaVars).component());
      out.add(
         parse(
               sender,
               cfg,
               "grim-history-detail-meta1",
               "&bGrim: &f%grim_version%&b, Server: &f%server_name%&b, Duration: &f%duration%&b, Date: &7%timeago% ago",
               metaVars
            )
            .component()
      );
      out.add(parse(sender, cfg, "grim-history-detail-meta2", "&bClient: &f%client_version%&b, Brand: &f%client_brand%", metaVars).component());
      out.add(
         parse(
               sender,
               cfg,
               "grim-history-detail-violations-header",
               "&bViolations: &8(%violations% total, %unique_checks% unique) &8[&f%page%&7/&f%max_pages%&8]",
               metaVars
            )
            .component()
      );
      if (d.violations().isEmpty()) {
         out.add(parse(sender, cfg, "grim-history-detail-empty", "&7- (none)", Map.of()).component());
         return out;
      } else {
         int from = (page - 1) * perPage;
         int to = Math.min(from + perPage, totalRows);
         if (detailed) {
            for (int i = from; i < to; i++) {
               out.add(renderViolationLine(sender, cfg, d.violations().get(i), verbose));
            }
         } else {
            for (int i = from; i < to; i++) {
               out.add(renderBucketLine(sender, cfg, d.buckets().get(i), d, verbose));
            }
         }

         return out;
      }
   }

   private static Component renderBucketLine(Sender sender, ConfigManager cfg, CheckBucket bucket, SessionDetail d, boolean verbose) {
      StringBuilder checksList = new StringBuilder();
      boolean first = true;

      for (CheckCount c : bucket.checks()) {
         if (!first) {
            checksList.append("&7, ");
         }

         first = false;
         checksList.append(
            cfg.getStringElse("grim-history-check-count", "&f%check_name%&7 x&c%count%")
               .replace("%check_name%", mmSafe(c.displayName()))
               .replace("%count%", Integer.toString(c.count()))
         );
      }

      Component bucketHoverEntries = buildBucketHoverEntries(sender, cfg, d, bucket, verbose);
      String bucketStart = formatDuration(bucket.bucketStartOffsetMs());
      String bucketEnd = formatDuration(bucket.bucketStartOffsetMs() + d.bucketSizeMs());
      HistoryComponentRenderer.RenderedTemplate rendered = parse(
         sender,
         cfg,
         "grim-history-detail-group",
         "<hover:show_text:'&bViolations in &f%bucket_start%–%bucket_end%&b:%bucket_hover_entries%'>&7- %checks_list% &8(&b%offset%&8)</hover>",
         Map.of(
            "checks_list",
            HistoryComponentRenderer.TemplateValue.raw(checksList.toString()),
            "offset",
            HistoryComponentRenderer.TemplateValue.raw(bucketStart),
            "bucket_start",
            HistoryComponentRenderer.TemplateValue.raw(bucketStart),
            "bucket_end",
            HistoryComponentRenderer.TemplateValue.raw(bucketEnd),
            "bucket_hover_entries",
            HistoryComponentRenderer.TemplateValue.component(bucketHoverEntries)
         )
      );
      Component line = rendered.component();
      if (!hasInlineHover(rendered.raw())) {
         line = line.hoverEvent(HoverEvent.showText(bucketHover(sender, cfg, bucketStart, bucketEnd, bucketHoverEntries)));
      }

      return line;
   }

   private static Component buildBucketHoverEntries(Sender sender, ConfigManager cfg, SessionDetail d, CheckBucket bucket, boolean verbose) {
      long bucketStart = bucket.bucketStartOffsetMs();
      long bucketEnd = bucketStart + d.bucketSizeMs();
      Component body = Component.empty();

      for (ViolationEntry v : d.violations()) {
         if (v.offsetFromSessionStartMs() >= bucketStart && v.offsetFromSessionStartMs() < bucketEnd) {
            Component description = (Component)(v.description().isBlank()
               ? Component.empty()
               : parse(
                     sender,
                     cfg,
                     "grim-history-hover-description",
                     " — &f%description%",
                     Map.of("description", HistoryComponentRenderer.TemplateValue.text(v.description()))
                  )
                  .component());
            String verboseText = v.verbose() == null ? "" : v.verbose();
            Component verboseComponent = (Component)(verbose && !verboseText.isBlank()
               ? parse(
                     sender,
                     cfg,
                     "grim-history-detail-group-hover-verbose",
                     " — &7%verbose%",
                     Map.of("verbose", HistoryComponentRenderer.TemplateValue.text(verboseText))
                  )
                  .component()
               : Component.empty());
            body = body.append(
               parse(
                     sender,
                     cfg,
                     "grim-history-detail-group-hover-entry",
                     "<newline>&8  %offset% &b%check%%description%%verbose%",
                     Map.of(
                        "offset",
                        HistoryComponentRenderer.TemplateValue.text(formatDuration(v.offsetFromSessionStartMs())),
                        "check",
                        HistoryComponentRenderer.TemplateValue.text(v.displayName()),
                        "description",
                        HistoryComponentRenderer.TemplateValue.component(description),
                        "verbose",
                        HistoryComponentRenderer.TemplateValue.component(verboseComponent)
                     )
                  )
                  .component()
            );
         }
      }

      return body;
   }

   private static Component bucketHover(Sender sender, ConfigManager cfg, String bucketStart, String bucketEnd, Component bucketHoverEntries) {
      return parse(
            sender,
            cfg,
            "grim-history-detail-group-hover",
            "&bViolations in &f%bucket_start%–%bucket_end%&b:%bucket_hover_entries%",
            Map.of(
               "bucket_start",
               HistoryComponentRenderer.TemplateValue.text(bucketStart),
               "bucket_end",
               HistoryComponentRenderer.TemplateValue.text(bucketEnd),
               "bucket_hover_entries",
               HistoryComponentRenderer.TemplateValue.component(bucketHoverEntries)
            )
         )
         .component();
   }

   private static Component renderViolationLine(Sender sender, ConfigManager cfg, ViolationEntry v, boolean verbose) {
      String verboseText = v.verbose() == null ? "" : v.verbose();
      HistoryComponentRenderer.RenderedTemplate rendered = parse(
         sender,
         cfg,
         "grim-history-detail-entry",
         "&7- &f%check% &8(&b%offset%&8)&7 %verbose%",
         Map.of(
            "check",
            HistoryComponentRenderer.TemplateValue.raw(mmSafe(v.displayName())),
            "description",
            HistoryComponentRenderer.TemplateValue.raw(mmSafe(v.description())),
            "offset",
            HistoryComponentRenderer.TemplateValue.raw(formatDuration(v.offsetFromSessionStartMs())),
            "vl",
            HistoryComponentRenderer.TemplateValue.raw(Double.toString(v.vl())),
            "verbose",
            HistoryComponentRenderer.TemplateValue.raw(verbose ? mmSafe(verboseText) : "")
         )
      );
      Component line = rendered.component();
      boolean hasDescription = !v.description().isBlank();
      if (!hasInlineHover(rendered.raw()) && (!verboseText.isBlank() || hasDescription)) {
         line = line.hoverEvent(HoverEvent.showText(violationHover(sender, cfg, v, verboseText, hasDescription)));
      }

      return line;
   }

   private static Component violationHover(Sender sender, ConfigManager cfg, ViolationEntry v, String verboseText, boolean hasDescription) {
      Component description = (Component)(hasDescription
         ? parse(
               sender,
               cfg,
               "grim-history-hover-description",
               " — &f%description%",
               Map.of("description", HistoryComponentRenderer.TemplateValue.text(v.description()))
            )
            .component()
         : Component.empty());
      Component verbose = (Component)(!verboseText.isBlank()
         ? parse(
               sender,
               cfg,
               "grim-history-detail-entry-hover-verbose",
               "<newline>&7%verbose%",
               Map.of("verbose", HistoryComponentRenderer.TemplateValue.text(verboseText))
            )
            .component()
         : Component.empty());
      return parse(
            sender,
            cfg,
            "grim-history-detail-entry-hover",
            "&b%check%%description%<newline>&8@ %offset% — vl %vl%%verbose%",
            Map.of(
               "check",
               HistoryComponentRenderer.TemplateValue.text(v.displayName()),
               "description",
               HistoryComponentRenderer.TemplateValue.component(description),
               "offset",
               HistoryComponentRenderer.TemplateValue.text(formatDuration(v.offsetFromSessionStartMs())),
               "vl",
               HistoryComponentRenderer.TemplateValue.text(Double.toString(v.vl())),
               "verbose",
               HistoryComponentRenderer.TemplateValue.component(verbose)
            )
         )
         .component();
   }

   private static Component sessionHover(Sender sender, String detailCommand, String sessionId) {
      return parseRaw(
         sender,
         "&bSession &7%session_uuid%<newline>&7Click or run &e%detail_command%<newline>&7to view session details.",
         Map.of(
            "session_uuid",
            HistoryComponentRenderer.TemplateValue.text(sessionId),
            "detail_command",
            HistoryComponentRenderer.TemplateValue.text(detailCommand)
         )
      );
   }

   @NotNull
   public static SessionDetail applyFilter(@NotNull SessionDetail d, @NotNull Predicate<ViolationEntry> filter) {
      List<ViolationEntry> survivors = new ArrayList<>();

      for (ViolationEntry v : d.violations()) {
         if (filter.test(v)) {
            survivors.add(v);
         }
      }

      TreeMap<Long, Map<String, int[]>> agg = new TreeMap<>();
      Map<String, ViolationEntry> checkMeta = new LinkedHashMap<>();
      long bucketSize = Math.max(1L, d.bucketSizeMs());

      for (ViolationEntry vx : survivors) {
         long bucketStart = vx.offsetFromSessionStartMs() / bucketSize * bucketSize;
         agg.computeIfAbsent(bucketStart, k -> new LinkedHashMap<>()).computeIfAbsent(vx.displayName(), k -> new int[]{0})[0]++;
         checkMeta.putIfAbsent(vx.displayName(), vx);
      }

      List<CheckBucket> newBuckets = new ArrayList<>(agg.size());

      for (Entry<Long, Map<String, int[]>> e : agg.entrySet()) {
         List<CheckCount> ccs = new ArrayList<>(e.getValue().size());

         for (Entry<String, int[]> ce : e.getValue().entrySet()) {
            ViolationEntry meta = checkMeta.get(ce.getKey());
            ccs.add(new CheckCount(meta.checkId(), meta.stableKey(), meta.displayName(), meta.description(), ce.getValue()[0]));
         }

         newBuckets.add(new CheckBucket(e.getKey(), ccs));
      }

      int uniqueChecks = checkMeta.size();
      return new SessionDetail(
         d.sessionId(),
         d.playerUuid(),
         d.sessionOrdinal(),
         d.startedEpochMs(),
         d.lastActivityEpochMs(),
         d.grimVersion(),
         d.serverName(),
         d.clientVersion(),
         d.clientBrand(),
         d.bucketSizeMs(),
         uniqueChecks,
         newBuckets,
         survivors
      );
   }

   private static HistoryComponentRenderer.RenderedTemplate parse(
      Sender sender, ConfigManager cfg, String key, String fallback, Map<String, HistoryComponentRenderer.TemplateValue> vars
   ) {
      String raw = cfg.getStringElse(key, fallback);
      return new HistoryComponentRenderer.RenderedTemplate(parseRaw(sender, raw, vars), raw);
   }

   private static Component parseRaw(Sender sender, String raw, Map<String, HistoryComponentRenderer.TemplateValue> vars) {
      TagResolver.Builder resolver = TagResolver.builder();

      for (Entry<String, HistoryComponentRenderer.TemplateValue> e : vars.entrySet()) {
         HistoryComponentRenderer.TemplateValue value = e.getValue();
         if (value.component() == null) {
            raw = raw.replace("%" + e.getKey() + "%", value.raw());
         } else {
            raw = raw.replace("%" + e.getKey() + "%", "<" + e.getKey() + ">");
            resolver.resolver(Placeholder.component(e.getKey(), value.component()));
         }
      }

      raw = MessageUtil.replacePlaceholders(sender, raw);
      return MessageUtil.miniMessage(raw, resolver.build());
   }

   private static boolean hasInlineHover(String raw) {
      return raw.toLowerCase(Locale.ROOT).contains("<hover:");
   }

   private static String mmSafe(@Nullable String raw) {
      if (raw != null && !raw.isEmpty()) {
         String stripped = LEGACY_FORMAT_PATTERN.matcher(raw.replace("%", "")).replaceAll("");
         return stripped.replace("\\", "\\\\").replace("<", "\\<");
      } else {
         return "";
      }
   }

   @NotNull
   public static String formatDuration(long ms) {
      if (ms < 0L) {
         ms = 0L;
      }

      long days = TimeUnit.MILLISECONDS.toDays(ms);
      ms -= TimeUnit.DAYS.toMillis(days);
      long hours = TimeUnit.MILLISECONDS.toHours(ms);
      ms -= TimeUnit.HOURS.toMillis(hours);
      long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
      ms -= TimeUnit.MINUTES.toMillis(minutes);
      long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);
      StringBuilder sb = new StringBuilder();
      if (days > 0L) {
         sb.append(days).append("d ");
      }

      if (hours > 0L) {
         sb.append(hours).append("h ");
      }

      if (minutes > 0L) {
         sb.append(minutes).append("m ");
      }

      if (seconds > 0L || sb.length() == 0) {
         sb.append(seconds).append("s");
      }

      return sb.toString().trim();
   }

   @NotNull
   public static String formatAbsolute(long epochMs) {
      return new Date(epochMs).toString();
   }

   @NotNull
   private static String nullToUnknown(@Nullable String s) {
      return s == null ? "unknown" : s;
   }

   @NotNull
   static String clientVersionDisplay(int pvn) {
      if (pvn <= 0) {
         return "unknown";
      } else {
         try {
            ClientVersion cv = ClientVersion.getById(pvn);
            if (cv != null && cv != ClientVersion.UNKNOWN) {
               String name = cv.getReleaseName();
               return name == null ? "pvn:" + pvn : name;
            } else {
               return "unknown";
            }
         } catch (RuntimeException var3) {
            return "pvn:" + pvn;
         }
      }
   }

   @Generated
   private HistoryComponentRenderer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   private record RenderedTemplate(Component component, String raw) {
   }

   private record TemplateValue(@Nullable String raw, @Nullable Component component) {
      static HistoryComponentRenderer.TemplateValue raw(@Nullable String value) {
         return new HistoryComponentRenderer.TemplateValue(value == null ? "" : value, null);
      }

      static HistoryComponentRenderer.TemplateValue text(@Nullable String value) {
         return new HistoryComponentRenderer.TemplateValue(null, Component.text(value == null ? "" : value));
      }

      static HistoryComponentRenderer.TemplateValue component(@Nullable Component component) {
         return new HistoryComponentRenderer.TemplateValue(null, (Component)(component == null ? Component.empty() : component));
      }
   }
}
