package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.magmaguy.elitemobs.magmacore.dialog.DialogManager;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.magmacore.util.VersionChecker;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class NightbreakChangelogManager implements Listener {
   private static final long RETRY_TICKS = 18000L;
   private static final int MAX_DIALOG_RELEASES = 20;
   private static final int MAX_RELEASE_TEXT_LENGTH = 6000;
   private static NightbreakChangelogManager active;
   private static JavaPlugin pendingContentPlugin;
   private static Supplier<? extends List<? extends NightbreakManagedContent>> pendingContentSupplier;
   private final JavaPlugin plugin;
   private final NightbreakPluginSpec spec;
   private final NightbreakChangelogMessages messages;
   private final File stateFile;
   private final YamlConfiguration state;
   private final String currentVersion;
   private final String trackingFloorVersion;
   private final boolean bootstrapCurrentRelease;
   private String commandLabel;
   private final AtomicBoolean fetching = new AtomicBoolean();
   private volatile List<NightbreakAccount.PluginChangelog> trackedPluginReleases = List.of();
   private volatile boolean pluginHistoryComplete;
   private volatile Map<String, List<NightbreakAccount.PluginChangelog>> trackedContentReleases = Map.of();
   private volatile Map<String, Boolean> trackedContentHistoryComplete = Map.of();
   private volatile List<NightbreakChangelogManager.TrackedContent> trackedContent = List.of();
   private final Map<UUID, String> lastNotifiedDigest = new HashMap<>();
   private int retryTaskId = -1;
   private int retryAttempts;
   private volatile boolean closed;
   private SimpleCommandMap registeredCommandMap;
   private NightbreakChangelogManager.ChangelogCommand registeredCommand;

   private NightbreakChangelogManager(JavaPlugin plugin, NightbreakPluginSpec spec) {
      this.plugin = plugin;
      this.spec = spec;
      this.messages = spec.resolveChangelogMessages();
      this.currentVersion = normalizeVersion(plugin.getDescription().getVersion());
      this.commandLabel = sanitizeCommandLabel(spec.rootCommand()) + "changelog";
      this.stateFile = new File(plugin.getDataFolder(), "nightbreak-changelogs.yml");
      this.state = YamlConfiguration.loadConfiguration(this.stateFile);
      String configuredFloor = normalizeVersion(this.state.getString("trackingSinceVersion"));
      boolean missingTrackingFloor = configuredFloor == null;
      if (configuredFloor == null) {
         configuredFloor = this.currentVersion;
         this.state.set("trackingSinceVersion", this.currentVersion);
         this.state.set("bootstrapCurrentRelease", this.currentVersion);
      }

      this.bootstrapCurrentRelease = missingTrackingFloor || sameVersion(this.state.getString("bootstrapCurrentRelease"), this.currentVersion);
      this.trackingFloorVersion = configuredFloor;
      this.state.set("lastObservedVersion", this.currentVersion);
      this.trackedPluginReleases = this.readCachedReleases("cache.plugin", this.currentVersion);
      this.pluginHistoryComplete = sameVersion(this.state.getString("cache.plugin.targetVersion"), this.currentVersion)
         && this.state.getBoolean("cache.plugin.complete", false);
      this.saveState();
   }

   public static synchronized void initialize(JavaPlugin plugin, NightbreakPluginSpec spec) {
      shutdown(plugin);
      if (plugin != null && spec != null) {
         active = new NightbreakChangelogManager(plugin, spec);
         Bukkit.getPluginManager().registerEvents(active, plugin);
         active.registerCommand();
         if (pendingContentPlugin == plugin && pendingContentSupplier != null) {
            active.registerPendingContent();
         }

         if (active.bootstrapCurrentRelease || !sameVersion(active.trackingFloorVersion, active.currentVersion)) {
            active.fetchAsync();
         }
      }
   }

   public static synchronized void shutdown(JavaPlugin plugin) {
      if (active != null && (plugin == null || active.plugin == plugin)) {
         active.closed = true;
         HandlerList.unregisterAll(active);
         if (active.retryTaskId != -1) {
            Bukkit.getScheduler().cancelTask(active.retryTaskId);
         }

         active.unregisterCommand();
         active.lastNotifiedDigest.clear();
         if (pendingContentPlugin == active.plugin) {
            pendingContentPlugin = null;
            pendingContentSupplier = null;
         }

         active = null;
      }
   }

   public static <T extends NightbreakManagedContent> void registerContentSupplier(JavaPlugin plugin, Supplier<List<T>> supplier) {
      NightbreakChangelogManager manager = active;
      if (plugin != null && supplier != null) {
         pendingContentPlugin = plugin;
         pendingContentSupplier = supplier;
         if (manager != null && !manager.closed && manager.plugin == plugin) {
            manager.registerPendingContent();
         }
      }
   }

   private void registerPendingContent() {
      try {
         List<? extends NightbreakManagedContent> supplied = (List<? extends NightbreakManagedContent>)pendingContentSupplier.get();
         this.registerContent(supplied);
      } catch (RuntimeException var2) {
         this.plugin.getLogger().warning("Could not register DLC changelog tracking: " + var2.getMessage());
      }
   }

   private void registerContent(List<? extends NightbreakManagedContent> packages) {
      List<NightbreakChangelogManager.TrackedContent> content = new ArrayList<>();

      for (NightbreakManagedContent contentPackage : packages) {
         if (contentPackage != null && contentPackage.isInstalled()) {
            String slug = contentPackage.getNightbreakSlug();
            if (slug != null && !slug.isBlank()) {
               String version = Integer.toString(contentPackage.getLocalVersion());
               String path = "content." + stateKey(slug);
               String floor = normalizeVersion(this.state.getString(path + ".trackingSinceVersion"));
               boolean missingFloor = floor == null;
               if (floor == null) {
                  floor = version;
                  this.state.set(path + ".trackingSinceVersion", version);
                  this.state.set(path + ".bootstrapCurrentRelease", version);
               }

               this.state.set(path + ".lastObservedVersion", version);
               boolean bootstrap = missingFloor || sameVersion(this.state.getString(path + ".bootstrapCurrentRelease"), version);
               content.add(new NightbreakChangelogManager.TrackedContent(slug, contentPackage.getDisplayName(), version, floor, bootstrap));
            }
         }
      }

      this.trackedContent = List.copyOf(content);
      Map<String, List<NightbreakAccount.PluginChangelog>> cachedContent = new LinkedHashMap<>();
      Map<String, Boolean> cachedCompleteness = new LinkedHashMap<>();

      for (NightbreakChangelogManager.TrackedContent tracked : this.trackedContent) {
         String cachePath = "cache.content." + stateKey(tracked.slug);
         List<NightbreakAccount.PluginChangelog> cached = this.readCachedReleases(cachePath, tracked.currentVersion);
         if (!cached.isEmpty()) {
            cachedContent.put(tracked.slug, cached);
         }

         cachedCompleteness.put(
            tracked.slug,
            sameVersion(this.state.getString(cachePath + ".targetVersion"), tracked.currentVersion) && this.state.getBoolean(cachePath + ".complete", false)
         );
      }

      this.trackedContentReleases = Map.copyOf(cachedContent);
      this.trackedContentHistoryComplete = Map.copyOf(cachedCompleteness);
      this.saveState();
      if (!this.trackedContent.isEmpty()) {
         this.fetchAsync();
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      if (this.isAdmin(event.getPlayer()) && !this.isPermanentlyDisabled(event.getPlayer())) {
         this.lastNotifiedDigest.remove(event.getPlayer().getUniqueId());
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.notifyIfPending(event.getPlayer()), 40L);
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.lastNotifiedDigest.remove(event.getPlayer().getUniqueId());
   }

   private void fetchAsync() {
      if (!this.closed && this.fetching.compareAndSet(false, true)) {
         Bukkit.getScheduler()
            .runTaskAsynchronously(
               this.plugin,
               () -> {
                  List<NightbreakAccount.PluginChangelog> selected = this.trackedPluginReleases;
                  boolean selectedPluginComplete = this.pluginHistoryComplete;
                  boolean historyServiceUnavailable = false;
                  boolean pluginNeedsHistory = this.bootstrapCurrentRelease
                     || NightbreakPluginUpdater.compareVersions(this.currentVersion, this.trackingFloorVersion) > 0;
                  if (pluginNeedsHistory && !selectedPluginComplete) {
                     NightbreakAccount.ChangelogFetch history = NightbreakAccount.fetchPluginChangelogs(this.spec.pluginSlug());
                     List<NightbreakAccount.PluginChangelog> releases = history.releases();
                     historyServiceUnavailable = history.serviceUnavailable();
                     NightbreakChangelogManager.RangeSelection selection = this.bootstrapCurrentRelease
                        ? this.selectCurrentRelease(releases, this.currentVersion)
                        : this.selectRange(releases, this.trackingFloorVersion, this.currentVersion);
                     if (selection.releases.size() >= selected.size()) {
                        selected = selection.releases;
                     }

                     selectedPluginComplete = selection.complete;
                     if (selected.isEmpty()) {
                        selected = this.latestReleaseFallback();
                     }

                     if (this.bootstrapCurrentRelease && !selected.isEmpty()) {
                        selectedPluginComplete = true;
                     }
                  }

                  Map<String, List<NightbreakAccount.PluginChangelog>> selectedContent = new LinkedHashMap<>(this.trackedContentReleases);
                  Map<String, Boolean> selectedContentCompleteness = new LinkedHashMap<>(this.trackedContentHistoryComplete);

                  for (NightbreakChangelogManager.TrackedContent content : this.trackedContent) {
                     boolean contentNeedsHistory = content.bootstrapCurrentRelease
                        || NightbreakPluginUpdater.compareVersions(content.currentVersion, content.floorVersion) > 0;
                     if (!contentNeedsHistory) {
                        selectedContentCompleteness.put(content.slug, true);
                     } else if (!selectedContentCompleteness.getOrDefault(content.slug, false) && !historyServiceUnavailable) {
                        NightbreakAccount.ChangelogFetch historyx = NightbreakAccount.fetchDlcChangelogs(content.slug);
                        List<NightbreakAccount.PluginChangelog> contentHistory = historyx.releases();
                        historyServiceUnavailable = historyx.serviceUnavailable();
                        NightbreakChangelogManager.RangeSelection selectionx = content.bootstrapCurrentRelease
                           ? this.selectCurrentRelease(contentHistory, content.currentVersion)
                           : this.selectRange(contentHistory, content.floorVersion, content.currentVersion);
                        List<NightbreakAccount.PluginChangelog> contentRange = selectionx.releases;
                        List<NightbreakAccount.PluginChangelog> previousRange = selectedContent.getOrDefault(content.slug, List.of());
                        if (previousRange.size() > contentRange.size()) {
                           contentRange = previousRange;
                        }

                        if (contentRange.isEmpty()) {
                           contentRange = this.latestContentReleaseFallback(content);
                        }

                        if (!contentRange.isEmpty()) {
                           selectedContent.put(content.slug, contentRange);
                        }

                        selectedContentCompleteness.put(content.slug, selectionx.complete || content.bootstrapCurrentRelease && !contentRange.isEmpty());
                     }
                  }

                  List<NightbreakAccount.PluginChangelog> finalSelected = selected;
                  boolean finalPluginComplete = selectedPluginComplete;
                  if (!this.closed && this.plugin.isEnabled()) {
                     try {
                        Bukkit.getScheduler()
                           .runTask(
                              this.plugin,
                              () -> this.applyFetchedResults(
                                 finalSelected, finalPluginComplete, selectedContent, selectedContentCompleteness, pluginNeedsHistory
                              )
                           );
                     } catch (RuntimeException var15) {
                        this.fetching.set(false);
                        if (!this.closed) {
                           this.plugin.getLogger().warning("Could not apply fetched changelogs: " + var15.getMessage());
                        }
                     }
                  } else {
                     this.fetching.set(false);
                  }
               }
            );
      }
   }

   private void applyFetchedResults(
      List<NightbreakAccount.PluginChangelog> selectedPlugin,
      boolean selectedPluginComplete,
      Map<String, List<NightbreakAccount.PluginChangelog>> selectedContent,
      Map<String, Boolean> selectedContentCompleteness,
      boolean pluginNeedsHistory
   ) {
      this.fetching.set(false);
      if (!this.closed && this.plugin.isEnabled()) {
         this.trackedPluginReleases = selectedPlugin;
         this.pluginHistoryComplete = selectedPluginComplete;
         this.trackedContentReleases = Map.copyOf(selectedContent);
         this.trackedContentHistoryComplete = Map.copyOf(selectedContentCompleteness);
         if (!selectedPlugin.isEmpty()) {
            this.writeCachedReleases("cache.plugin", this.currentVersion, selectedPlugin, selectedPluginComplete);
         }

         if (this.bootstrapCurrentRelease && selectedPluginComplete) {
            this.state.set("bootstrapCurrentRelease", null);
         }

         for (NightbreakChangelogManager.TrackedContent content : this.trackedContent) {
            List<NightbreakAccount.PluginChangelog> contentReleases = selectedContent.get(content.slug);
            if (contentReleases != null && !contentReleases.isEmpty()) {
               this.writeCachedReleases(
                  "cache.content." + stateKey(content.slug),
                  content.currentVersion,
                  contentReleases,
                  selectedContentCompleteness.getOrDefault(content.slug, false)
               );
            }

            if (content.bootstrapCurrentRelease && selectedContentCompleteness.getOrDefault(content.slug, false)) {
               this.state.set("content." + stateKey(content.slug) + ".bootstrapCurrentRelease", null);
            }
         }

         this.saveState();
         boolean expectedContentMissing = this.trackedContent
            .stream()
            .anyMatch(
               contentx -> (contentx.bootstrapCurrentRelease || NightbreakPluginUpdater.compareVersions(contentx.currentVersion, contentx.floorVersion) > 0)
                  && !selectedContentCompleteness.getOrDefault(contentx.slug, false)
            );
         if ((!pluginNeedsHistory || selectedPluginComplete) && !expectedContentMissing) {
            if (this.retryTaskId != -1) {
               Bukkit.getScheduler().cancelTask(this.retryTaskId);
            }

            this.retryTaskId = -1;
            this.retryAttempts = 0;
         } else {
            this.scheduleRetry();
         }

         Bukkit.getOnlinePlayers().forEach(this::notifyIfPending);
      }
   }

   private List<NightbreakAccount.PluginChangelog> latestReleaseFallback() {
      NightbreakAccount.VersionInfo latest = NightbreakAccount.getPublicPluginVersion(this.spec.pluginSlug(), false);
      return latest != null && sameVersion(latest.version, this.currentVersion) && latest.changelog != null && !latest.changelog.isBlank()
         ? List.of(new NightbreakAccount.PluginChangelog(this.currentVersion, latest.changelog, null))
         : List.of();
   }

   private List<NightbreakAccount.PluginChangelog> latestContentReleaseFallback(NightbreakChangelogManager.TrackedContent content) {
      NightbreakAccount.VersionInfo latest = NightbreakAccount.getPublicDlcVersion(content.slug, false);
      return latest != null && sameVersion(latest.version, content.currentVersion) && latest.changelog != null && !latest.changelog.isBlank()
         ? List.of(new NightbreakAccount.PluginChangelog(content.currentVersion, latest.changelog, null))
         : List.of();
   }

   private NightbreakChangelogManager.RangeSelection selectCurrentRelease(List<NightbreakAccount.PluginChangelog> releases, String targetVersion) {
      int targetIndex = indexOfVersion(releases, targetVersion);
      return targetIndex < 0
         ? new NightbreakChangelogManager.RangeSelection(List.of(), false)
         : new NightbreakChangelogManager.RangeSelection(List.of(releases.get(targetIndex)), true);
   }

   private NightbreakChangelogManager.RangeSelection selectRange(List<NightbreakAccount.PluginChangelog> releases, String floorVersion, String targetVersion) {
      if (NightbreakPluginUpdater.compareVersions(targetVersion, floorVersion) <= 0) {
         return new NightbreakChangelogManager.RangeSelection(List.of(), true);
      } else {
         int floorIndex = indexOfVersion(releases, floorVersion);
         int currentIndex = indexOfVersion(releases, targetVersion);
         if (currentIndex < 0) {
            return new NightbreakChangelogManager.RangeSelection(List.of(), false);
         } else {
            int start = floorIndex >= 0 ? floorIndex + 1 : Math.max(0, currentIndex - 20 + 1);
            return start > currentIndex
               ? new NightbreakChangelogManager.RangeSelection(List.of(), true)
               : new NightbreakChangelogManager.RangeSelection(List.copyOf(releases.subList(start, currentIndex + 1)), true);
         }
      }
   }

   private void scheduleRetry() {
      if (!this.closed && this.retryTaskId == -1) {
         long delay = 18000L * (1L << Math.min(this.retryAttempts++, 4));
         this.retryTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            this.retryTaskId = -1;
            this.fetchAsync();
         }, delay);
      }
   }

   private void notifyIfPending(Player player) {
      if (!this.closed
         && this.registeredCommand != null
         && player != null
         && player.isOnline()
         && this.isAdmin(player)
         && !this.isPermanentlyDisabled(player)
         && this.hasPending(player)) {
         String digest = this.pendingDigest(player);
         if (!digest.equals(this.lastNotifiedDigest.put(player.getUniqueId(), digest))) {
            player.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(this.format(this.messages.pendingNotice())),
                     SpigotMessage.commandHoverMessage(this.format(this.messages.viewButton()), this.format(this.messages.viewHover()), "/" + this.commandLabel)
                  }
               );
         }
      }
   }

   private String pendingDigest(Player player) {
      StringBuilder digest = new StringBuilder(this.currentVersion);
      this.pendingPluginFor(player).forEach(release -> digest.append('|').append(release.version()));
      this.pendingContentFor(player).forEach((content, releases) -> {
         digest.append('|').append(content.slug);
         releases.forEach(release -> digest.append(':').append(release.version()));
      });
      return digest.toString();
   }

   private void open(Player player) {
      List<NightbreakAccount.PluginChangelog> pending = this.pendingPluginFor(player);
      Map<NightbreakChangelogManager.TrackedContent, List<NightbreakAccount.PluginChangelog>> pendingContent = this.pendingContentFor(player);
      if (pending.isEmpty() && pendingContent.isEmpty()) {
         player.sendMessage(this.format(this.messages.noUnread()));
      } else {
         NightbreakChangelogManager.PendingView view = this.buildPendingView(pending, pendingContent);
         if (VersionChecker.serverVersionOlderThan(21, 6)) {
            this.openChatFallback(player, view);
         } else {
            DialogManager.MultiActionDialogBuilder dialog = new DialogManager.MultiActionDialogBuilder()
               .title(this.format(this.messages.dialogTitle()))
               .externalTitle(this.format(this.messages.dialogExternalTitle()))
               .columns(1)
               .afterAction(DialogManager.AfterAction.CLOSE)
               .canCloseWithEscape(true);
            if (view.omitted > 0) {
               dialog.addBody(
                  DialogManager.PlainMessageBody.of(this.format(this.messages.omittedDialog(), "$count", Integer.toString(view.omitted))).width(420)
               );
            }

            for (NightbreakChangelogManager.RenderedRelease release : view.releases) {
               dialog.addBody(DialogManager.PlainMessageBody.of(this.releaseBody(release)).width(420));
            }

            dialog.addAction(
               DialogManager.ActionButton.of(
                     this.format(this.messages.dismissButton()), new DialogManager.RunCommandAction("/" + this.commandLabel + " dismiss")
                  )
                  .width(205)
            );
            dialog.addAction(
               DialogManager.ActionButton.of(
                     this.format(this.messages.disableButton()), new DialogManager.RunCommandAction("/" + this.commandLabel + " disable")
                  )
                  .tooltip(this.format(this.messages.disableTooltip()))
                  .width(205)
            );
            DialogManager.sendDialog(player, dialog);
         }
      }
   }

   private NightbreakChangelogManager.PendingView buildPendingView(
      List<NightbreakAccount.PluginChangelog> pluginReleases,
      Map<NightbreakChangelogManager.TrackedContent, List<NightbreakAccount.PluginChangelog>> contentReleases
   ) {
      int total = pluginReleases.size() + contentReleases.values().stream().mapToInt(List::size).sum();
      List<NightbreakChangelogManager.PendingSection> sections = new ArrayList<>();
      if (!pluginReleases.isEmpty()) {
         sections.add(new NightbreakChangelogManager.PendingSection("@plugin", this.spec.displayName(), pluginReleases));
      }

      contentReleases.forEach((content, releases) -> {
         if (!releases.isEmpty()) {
            sections.add(new NightbreakChangelogManager.PendingSection(content.slug, content.displayName, (List<NightbreakAccount.PluginChangelog>)releases));
         }
      });
      List<NightbreakChangelogManager.RenderedRelease> rendered = new ArrayList<>();

      for (int depth = 1; rendered.size() < 20; depth++) {
         boolean added = false;

         for (NightbreakChangelogManager.PendingSection section : sections) {
            int index = section.releases.size() - depth;
            if (index >= 0) {
               rendered.add(new NightbreakChangelogManager.RenderedRelease(section.componentKey, section.componentName, section.releases.get(index)));
               added = true;
               if (rendered.size() >= 20) {
                  break;
               }
            }
         }

         if (!added) {
            break;
         }
      }

      return new NightbreakChangelogManager.PendingView(List.copyOf(rendered), Math.max(0, total - rendered.size()));
   }

   private JsonElement releaseBody(NightbreakChangelogManager.RenderedRelease rendered) {
      NightbreakAccount.PluginChangelog release = rendered.release;
      JsonArray components = new JsonArray();
      JsonObject header = new JsonObject();
      header.addProperty("text", this.formattedReleaseHeader(rendered));
      header.addProperty("color", "gold");
      header.addProperty("bold", true);
      components.add(header);
      String body = displayReleaseText(rendered);
      String[] lines = body.split("\n", -1);

      for (int index = 0; index < lines.length; index++) {
         if (index > 0) {
            components.add(literalComponent("\n", null));
         }

         String line = lines[index];
         String tagColor = tagColor(line);
         if (tagColor == null) {
            components.add(literalComponent(line, "white"));
         } else {
            String tag = line.substring(0, line.indexOf(93) + 1);
            components.add(literalComponent(tag, tagColor));
            components.add(literalComponent(line.substring(tag.length()), "white"));
         }
      }

      return components;
   }

   private static JsonObject literalComponent(String text, String color) {
      JsonObject component = new JsonObject();
      component.addProperty("text", text);
      if (color != null) {
         component.addProperty("color", color);
      }

      return component;
   }

   private static String tagColor(String line) {
      String tagged = !line.startsWith("- ") && !line.startsWith("* ") ? line : line.substring(2);
      if (tagged.startsWith("[New]")) {
         return "green";
      } else if (tagged.startsWith("[Fix]")) {
         return "red";
      } else if (tagged.startsWith("[Hotfix]")) {
         return "dark_red";
      } else {
         return tagged.startsWith("[Tweak]") ? "yellow" : null;
      }
   }

   private static String displayReleaseText(NightbreakChangelogManager.RenderedRelease rendered) {
      String body = sanitizeRemoteText(rendered.release.changelog());
      int newline = body.indexOf(10);
      String firstLine = (newline < 0 ? body : body.substring(0, newline)).trim();
      String expectedHeader = (rendered.componentName + " " + rendered.release.version()).trim();
      if (firstLine.endsWith(":")) {
         firstLine = firstLine.substring(0, firstLine.length() - 1).trim();
      }

      if (firstLine.equalsIgnoreCase(expectedHeader)) {
         return newline < 0 ? "" : body.substring(newline + 1).stripLeading();
      } else {
         return body;
      }
   }

   private static String sanitizeRemoteText(String changelog) {
      String body = changelog == null ? "" : changelog.replace("\r", "").trim();
      body = body.replaceAll("(?i)§[0-9A-FK-ORX]", "");
      body = body.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
      if (body.length() > 6000) {
         body = body.substring(0, 6000) + "\n…";
      }

      return body;
   }

   private String formattedReleaseHeader(NightbreakChangelogManager.RenderedRelease rendered) {
      String formatted = this.format(
         this.messages.releaseHeader(), "$component", sanitizeHeaderValue(rendered.componentName), "$version", sanitizeHeaderValue(rendered.release.version())
      );
      return formatted.replaceAll("(?i)[§&][0-9A-FK-ORX]", "");
   }

   private static String sanitizeHeaderValue(String value) {
      return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').replaceAll("(?i)[§&][0-9A-FK-ORX]", "").replaceAll("[\\p{Cntrl}]", "").trim();
   }

   private void openChatFallback(Player player, NightbreakChangelogManager.PendingView view) {
      player.sendMessage(this.format(this.messages.chatHeader()));
      if (view.omitted > 0) {
         player.sendMessage(this.format(this.messages.omittedChat(), "$count", Integer.toString(view.omitted)));
      }

      for (NightbreakChangelogManager.RenderedRelease rendered : view.releases) {
         player.sendMessage("§6§l" + this.formattedReleaseHeader(rendered).stripTrailing());

         for (String line : displayReleaseText(rendered).split("\n", -1)) {
            player.sendMessage("§f" + line);
         }
      }

      player.sendMessage(this.format(this.messages.chatInstructions()));
   }

   private boolean hasPending(Player player) {
      return !this.pendingPluginFor(player).isEmpty() || !this.pendingContentFor(player).isEmpty();
   }

   private List<NightbreakAccount.PluginChangelog> pendingPluginFor(Player player) {
      if (this.trackedPluginReleases.isEmpty()) {
         return List.of();
      } else {
         List<NightbreakAccount.PluginChangelog> readableReleases = this.trackedPluginReleases
            .stream()
            .filter(NightbreakChangelogManager::hasChangelogText)
            .toList();
         if (readableReleases.isEmpty()) {
            return List.of();
         } else {
            String path = this.playerPath(player) + ".dismissedPluginVersions";
            List<String> dismissedVersions = this.state.getStringList(path);
            String dismissedThrough = normalizeVersion(this.state.getString(this.playerPath(player) + ".dismissed.plugin"));
            List<NightbreakAccount.PluginChangelog> pending = readableReleases;
            if (dismissedThrough != null) {
               int dismissedIndex = indexOfVersion(readableReleases, dismissedThrough);
               if (dismissedIndex < 0 && sameVersion(dismissedThrough, this.currentVersion)) {
                  pending = List.of();
               } else if (dismissedIndex >= 0) {
                  pending = dismissedIndex + 1 >= readableReleases.size() ? List.of() : readableReleases.subList(dismissedIndex + 1, readableReleases.size());
               }
            }

            return dismissedVersions.isEmpty() ? pending : filterDismissed(pending, dismissedVersions);
         }
      }
   }

   private Map<NightbreakChangelogManager.TrackedContent, List<NightbreakAccount.PluginChangelog>> pendingContentFor(Player player) {
      Map<NightbreakChangelogManager.TrackedContent, List<NightbreakAccount.PluginChangelog>> result = new LinkedHashMap<>();

      for (NightbreakChangelogManager.TrackedContent content : this.trackedContent) {
         List<NightbreakAccount.PluginChangelog> releases = this.trackedContentReleases.get(content.slug);
         if (releases != null && !releases.isEmpty()) {
            releases = releases.stream().filter(NightbreakChangelogManager::hasChangelogText).toList();
            if (!releases.isEmpty()) {
               List<String> dismissedVersions = this.state.getStringList(this.playerPath(player) + ".dismissedContentVersions." + stateKey(content.slug));
               String dismissed = normalizeVersion(this.state.getString(this.playerPath(player) + ".dismissed.content." + stateKey(content.slug)));
               int dismissedIndex = dismissed == null ? -1 : indexOfVersion(releases, dismissed);
               List<NightbreakAccount.PluginChangelog> pending;
               if (dismissed == null) {
                  pending = releases;
               } else if (dismissedIndex < 0 && sameVersion(dismissed, content.currentVersion)) {
                  pending = List.of();
               } else if (dismissedIndex < 0) {
                  pending = releases;
               } else if (dismissedIndex + 1 >= releases.size()) {
                  pending = List.of();
               } else {
                  pending = releases.subList(dismissedIndex + 1, releases.size());
               }

               if (!dismissedVersions.isEmpty()) {
                  pending = filterDismissed(pending, dismissedVersions);
               }

               if (!pending.isEmpty()) {
                  result.put(content, pending);
               }
            }
         }
      }

      return result;
   }

   private static boolean hasChangelogText(NightbreakAccount.PluginChangelog release) {
      return release.changelog() != null && !release.changelog().isBlank();
   }

   private static List<NightbreakAccount.PluginChangelog> filterDismissed(List<NightbreakAccount.PluginChangelog> releases, List<String> dismissedVersions) {
      return releases.stream().filter(release -> dismissedVersions.stream().noneMatch(version -> sameVersion(version, release.version()))).toList();
   }

   private void dismiss(Player player) {
      NightbreakChangelogManager.PendingView displayed = this.buildPendingView(this.pendingPluginFor(player), this.pendingContentFor(player));
      List<NightbreakAccount.PluginChangelog> displayedPlugin = displayed.releases
         .stream()
         .filter(release -> release.componentKey.equals("@plugin"))
         .map(NightbreakChangelogManager.RenderedRelease::release)
         .toList();
      this.state
         .set(
            this.playerPath(player) + ".dismissedPluginVersions",
            mergeDismissedVersions(this.state.getStringList(this.playerPath(player) + ".dismissedPluginVersions"), displayedPlugin)
         );

      for (NightbreakChangelogManager.TrackedContent content : this.trackedContent) {
         String path = this.playerPath(player) + ".dismissedContentVersions." + stateKey(content.slug);
         List<NightbreakAccount.PluginChangelog> displayedContent = displayed.releases
            .stream()
            .filter(release -> release.componentKey.equals(content.slug))
            .map(NightbreakChangelogManager.RenderedRelease::release)
            .toList();
         this.state.set(path, mergeDismissedVersions(this.state.getStringList(path), displayedContent));
      }

      this.saveState();
      if (this.hasPending(player)) {
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage(this.format(this.messages.dismissedPage())),
                  SpigotMessage.commandHoverMessage(
                     this.format(this.messages.viewNextButton()), this.format(this.messages.viewNextHover()), "/" + this.commandLabel
                  )
               }
            );
      } else {
         player.sendMessage(this.format(this.messages.dismissedAll()));
      }
   }

   private static List<String> mergeDismissedVersions(List<String> existing, List<NightbreakAccount.PluginChangelog> releases) {
      List<String> merged = new ArrayList<>(existing);

      for (NightbreakAccount.PluginChangelog release : releases) {
         if (merged.stream().noneMatch(version -> sameVersion(version, release.version()))) {
            merged.add(release.version());
         }
      }

      return List.copyOf(merged.subList(Math.max(0, merged.size() - 200), merged.size()));
   }

   private void disable(Player player) {
      this.state.set(this.playerPath(player) + ".disabled", true);
      this.state.set(this.playerPath(player) + ".dismissed.plugin", this.currentVersion);

      for (NightbreakChangelogManager.TrackedContent content : this.trackedContent) {
         this.state.set(this.playerPath(player) + ".dismissed.content." + stateKey(content.slug), content.currentVersion);
      }

      this.saveState();
      player.sendMessage(this.format(this.messages.disabled()));
   }

   private boolean isPermanentlyDisabled(Player player) {
      return this.state.getBoolean(this.playerPath(player) + ".disabled", false);
   }

   private String playerPath(Player player) {
      return "players." + player.getUniqueId();
   }

   private boolean isAdmin(Player player) {
      String wildcard = this.plugin.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "") + ".*";
      return player.isOp()
         || hasPermission(player, wildcard)
         || hasPermission(player, this.spec.adminPermission())
         || hasPermission(player, this.spec.setupPermission())
         || hasPermission(player, this.spec.initializePermission());
   }

   private static boolean hasPermission(Player player, String permission) {
      return permission != null && !permission.isBlank() && player.hasPermission(permission);
   }

   private void registerCommand() {
      try {
         Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         field.setAccessible(true);
         SimpleCommandMap commandMap = (SimpleCommandMap)field.get(Bukkit.getServer());
         String desiredLabel = this.commandLabel;
         String defaultFallbackPrefix = sanitizeCommandLabel(this.plugin.getName());
         NightbreakChangelogManager.RegisteredChangelogCommand existing = this.findExistingChangelogCommand(commandMap, desiredLabel, defaultFallbackPrefix);
         if (existing != null) {
            this.commandLabel = existing.label();
            existing.command()
               .reconfigure(
                  this.format(this.messages.commandDescription()), this.format(this.messages.commandUsage()), this.format(this.messages.unavailable())
               );
            existing.command().register(commandMap);
            this.registeredCommand = existing.command();
            this.registeredCommandMap = commandMap;
            return;
         }

         String fallbackPrefix = firstAvailableFallbackPrefix(commandMap, defaultFallbackPrefix, desiredLabel);
         if (fallbackPrefix == null) {
            this.plugin.getLogger().warning("Could not register /" + desiredLabel + ": no collision-free namespaced label was available.");
            return;
         }

         String actualLabel = commandMap.getCommand(desiredLabel) == null ? desiredLabel : fallbackPrefix + ":" + desiredLabel;
         this.commandLabel = actualLabel;
         NightbreakChangelogManager.ChangelogCommand candidate = new NightbreakChangelogManager.ChangelogCommand(
            desiredLabel, this.format(this.messages.commandDescription()), this.format(this.messages.commandUsage()), this.format(this.messages.unavailable())
         );
         commandMap.register(fallbackPrefix, candidate);
         if (commandMap.getCommand(actualLabel) != candidate) {
            this.plugin.getLogger().warning("Could not register /" + actualLabel + ".");
            this.commandLabel = desiredLabel;
            return;
         }

         this.registeredCommand = candidate;
         this.registeredCommandMap = commandMap;
      } catch (ReflectiveOperationException var9) {
         this.plugin.getLogger().warning("Could not register /" + this.commandLabel + ": " + var9.getMessage());
      }
   }

   private NightbreakChangelogManager.RegisteredChangelogCommand findExistingChangelogCommand(
      SimpleCommandMap commandMap, String desiredLabel, String defaultFallbackPrefix
   ) {
      if (commandMap.getCommand(desiredLabel) instanceof NightbreakChangelogManager.ChangelogCommand changelogCommand) {
         return new NightbreakChangelogManager.RegisteredChangelogCommand(desiredLabel, changelogCommand);
      } else {
         String defaultNamespacedLabel = defaultFallbackPrefix + ":" + desiredLabel;
         if (commandMap.getCommand(defaultNamespacedLabel) instanceof NightbreakChangelogManager.ChangelogCommand changelogCommand) {
            return new NightbreakChangelogManager.RegisteredChangelogCommand(defaultNamespacedLabel, changelogCommand);
         } else {
            try {
               Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
               knownCommandsField.setAccessible(true);
               Map<String, Command> knownCommands = (Map<String, Command>)knownCommandsField.get(commandMap);
               return knownCommands.entrySet()
                  .stream()
                  .filter(entry -> entry.getKey().endsWith(":" + desiredLabel))
                  .filter(entry -> entry.getValue() instanceof NightbreakChangelogManager.ChangelogCommand)
                  .map(
                     entry -> new NightbreakChangelogManager.RegisteredChangelogCommand(
                        entry.getKey(), (NightbreakChangelogManager.ChangelogCommand)entry.getValue()
                     )
                  )
                  .findFirst()
                  .orElse(null);
            } catch (ReflectiveOperationException var9) {
               return null;
            }
         }
      }
   }

   private static String firstAvailableFallbackPrefix(SimpleCommandMap commandMap, String basePrefix, String desiredLabel) {
      for (int suffix = 0; suffix < 100; suffix++) {
         String candidate = suffix == 0 ? basePrefix : basePrefix + suffix;
         if (commandMap.getCommand(candidate + ":" + desiredLabel) == null) {
            return candidate;
         }
      }

      return null;
   }

   private void unregisterCommand() {
      if (this.registeredCommand != null && this.registeredCommandMap != null) {
         NightbreakChangelogManager.ChangelogCommand command = this.registeredCommand;
         SimpleCommandMap commandMap = this.registeredCommandMap;

         try {
            command.unregister(commandMap);
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>)knownCommandsField.get(commandMap);
            List<String> registeredLabels = new ArrayList<>();
            knownCommands.forEach((label, registered) -> {
               if (registered == command) {
                  registeredLabels.add(label);
               }
            });
            registeredLabels.forEach(label -> knownCommands.remove(label, command));
         } catch (UnsupportedOperationException | ReflectiveOperationException var9) {
            this.plugin.getLogger().warning("Could not fully unregister /" + this.commandLabel + ": " + var9.getMessage());
         } finally {
            this.registeredCommand = null;
            this.registeredCommandMap = null;
         }
      }
   }

   private void saveState() {
      try {
         if (!this.plugin.getDataFolder().isDirectory() && !this.plugin.getDataFolder().mkdirs()) {
            throw new IOException("could not create plugin data folder");
         }

         this.state.save(this.stateFile);
      } catch (IOException var2) {
         this.plugin.getLogger().warning("Could not save changelog state: " + var2.getMessage());
      }
   }

   private List<NightbreakAccount.PluginChangelog> readCachedReleases(String path, String targetVersion) {
      if (!sameVersion(this.state.getString(path + ".targetVersion"), targetVersion)) {
         return List.of();
      } else {
         List<NightbreakAccount.PluginChangelog> releases = new ArrayList<>();

         for (Map<?, ?> entry : this.state.getMapList(path + ".releases")) {
            Object version = entry.get("version");
            Object changelog = entry.get("changelog");
            if (version != null && changelog != null) {
               Object releasedAt = entry.get("releasedAt");
               String boundedChangelog = changelog.toString();
               if (boundedChangelog.length() > 12000) {
                  boundedChangelog = boundedChangelog.substring(0, 12000);
               }

               releases.add(new NightbreakAccount.PluginChangelog(version.toString(), boundedChangelog, releasedAt == null ? null : releasedAt.toString()));
               if (releases.size() >= 100) {
                  break;
               }
            }
         }

         return List.copyOf(releases);
      }
   }

   private void writeCachedReleases(String path, String targetVersion, List<NightbreakAccount.PluginChangelog> releases, boolean complete) {
      List<Map<String, String>> serialized = new ArrayList<>();

      for (NightbreakAccount.PluginChangelog release : releases) {
         Map<String, String> entry = new LinkedHashMap<>();
         entry.put("version", release.version());
         String changelog = release.changelog();
         entry.put("changelog", changelog.length() > 12000 ? changelog.substring(0, 12000) : changelog);
         if (release.releasedAt() != null) {
            entry.put("releasedAt", release.releasedAt());
         }

         serialized.add(entry);
      }

      this.state.set(path + ".targetVersion", targetVersion);
      this.state.set(path + ".releases", serialized);
      this.state.set(path + ".complete", complete);
   }

   private static int indexOfVersion(List<NightbreakAccount.PluginChangelog> releases, String version) {
      for (int index = 0; index < releases.size(); index++) {
         if (sameVersion(releases.get(index).version(), version)) {
            return index;
         }
      }

      return -1;
   }

   private static String normalizeVersion(String version) {
      return version != null && !version.isBlank() ? version.trim().replaceFirst("^[vV]", "") : null;
   }

   private String format(String template, String... replacements) {
      String pluginName = this.spec.displayName() == null ? "" : this.spec.displayName();
      String prefix = this.messages.prefix().replace("$plugin", pluginName).replace("$command", this.commandLabel);
      String formatted = (template == null ? "" : template).replace("$plugin", pluginName).replace("$command", this.commandLabel).replace("$prefix", prefix);

      for (int index = 0; index + 1 < replacements.length; index += 2) {
         formatted = formatted.replace(replacements[index], replacements[index + 1] == null ? "" : replacements[index + 1]);
      }

      return formatted;
   }

   private static boolean sameVersion(String left, String right) {
      String normalizedLeft = normalizeVersion(left);
      String normalizedRight = normalizeVersion(right);
      return normalizedLeft != null && normalizedLeft.equalsIgnoreCase(normalizedRight);
   }

   private static String sanitizeCommandLabel(String rootCommand) {
      String value = rootCommand == null ? "nightbreak" : rootCommand.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
      return value.isBlank() ? "nightbreak" : value;
   }

   private static String stateKey(String slug) {
      return slug.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
   }

   private static final class ChangelogCommand extends Command {
      private String unavailableMessage;

      private ChangelogCommand(String name, String description, String usage, String unavailableMessage) {
         super(name, description, usage, List.of());
         this.unavailableMessage = unavailableMessage;
      }

      private void reconfigure(String description, String usage, String unavailableMessage) {
         this.setDescription(description);
         this.setUsage(usage);
         this.unavailableMessage = unavailableMessage;
      }

      public boolean execute(CommandSender sender, String label, String[] args) {
         NightbreakChangelogManager manager = NightbreakChangelogManager.active;
         if (manager != null && !manager.closed) {
            if (sender instanceof Player player && manager.isAdmin(player)) {
               if (args.length == 0) {
                  manager.open(player);
               } else if (args[0].equalsIgnoreCase("dismiss")) {
                  manager.dismiss(player);
               } else if (args[0].equalsIgnoreCase("disable")) {
                  manager.disable(player);
               } else {
                  sender.sendMessage(this.getUsage());
               }

               return true;
            } else {
               sender.sendMessage(manager.format(manager.messages.adminOnly()));
               return true;
            }
         } else {
            sender.sendMessage(manager == null ? this.unavailableMessage : manager.format(manager.messages.unavailable()));
            return true;
         }
      }
   }

   private record PendingSection(String componentKey, String componentName, List<NightbreakAccount.PluginChangelog> releases) {
   }

   private record PendingView(List<NightbreakChangelogManager.RenderedRelease> releases, int omitted) {
   }

   private record RangeSelection(List<NightbreakAccount.PluginChangelog> releases, boolean complete) {
   }

   private record RegisteredChangelogCommand(String label, NightbreakChangelogManager.ChangelogCommand command) {
   }

   private record RenderedRelease(String componentKey, String componentName, NightbreakAccount.PluginChangelog release) {
   }

   private record TrackedContent(String slug, String displayName, String currentVersion, String floorVersion, boolean bootstrapCurrentRelease) {
   }
}
