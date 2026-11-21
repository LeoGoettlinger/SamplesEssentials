package com.skyblockexp.ezrtp.config.teleport;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

public final class TeleportMessages {
   private final String teleporting;
   private final String teleportSuccess;
   private final String teleportFailure;
   private final String worldMissing;
   private final String joinSearching;
   private final String queueQueued;
   private final String queueFull;
   private final String insufficientFunds;
   private final String countdownStart;
   private final String countdownTick;
   private final String teleportFailureBiome;
   private final String teleportFallbackSuccess;
   private final String teleportFallbackNoCache;
   private final String teleportFailedSearch;

   public TeleportMessages(
      String teleporting,
      String teleportSuccess,
      String teleportFailure,
      String worldMissing,
      String joinSearching,
      String queueQueued,
      String queueFull,
      String insufficientFunds,
      String countdownStart,
      String countdownTick,
      String teleportFailureBiome,
      String teleportFallbackSuccess,
      String teleportFallbackNoCache,
      String teleportFailedSearch
   ) {
      this.teleporting = teleporting;
      this.teleportSuccess = teleportSuccess;
      this.teleportFailure = teleportFailure;
      this.worldMissing = worldMissing;
      this.joinSearching = joinSearching;
      this.queueQueued = queueQueued;
      this.queueFull = queueFull;
      this.insufficientFunds = insufficientFunds;
      this.countdownStart = countdownStart;
      this.countdownTick = countdownTick;
      this.teleportFailureBiome = teleportFailureBiome;
      this.teleportFallbackSuccess = teleportFallbackSuccess;
      this.teleportFallbackNoCache = teleportFallbackNoCache;
      this.teleportFailedSearch = teleportFailedSearch;
   }

   public Component teleporting() {
      return MessageUtil.parseMiniMessage(this.teleporting);
   }

   public Component teleportSuccess(int x, int z, String world) {
      String processed = this.teleportSuccess.replace("<x>", Integer.toString(x)).replace("<z>", Integer.toString(z)).replace("<world>", world);
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component teleportFailure() {
      return MessageUtil.parseMiniMessage(this.teleportFailure);
   }

   public Component teleportFailureBiome() {
      return MessageUtil.parseMiniMessage(this.teleportFailureBiome);
   }

   public Component worldMissing(String world) {
      String processed = this.worldMissing.replace("<world>", world);
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component joinSearching() {
      return MessageUtil.parseMiniMessage(this.joinSearching);
   }

   public Component queued(int position) {
      String processed = this.queueQueued.replace("<position>", Integer.toString(Math.max(position, 1)));
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component queueFull(int maxSize) {
      String processed = this.queueFull.replace("<size>", Integer.toString(Math.max(maxSize, 0)));
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component insufficientFunds(double cost) {
      String processed = this.insufficientFunds.replace("<cost>", String.format(Locale.US, "%.2f", Math.max(cost, 0.0)));
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component countdownStart(int seconds) {
      String template = this.countdownStart != null ? this.countdownStart : "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>";
      String processed = template.replace("<seconds>", Integer.toString(seconds));
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component countdownTick(int seconds) {
      String template = this.countdownTick != null ? this.countdownTick : "<gray><seconds>...</gray>";
      String processed = template.replace("<seconds>", Integer.toString(seconds));
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component teleportFallbackSuccess(int x, int z) {
      String template = this.teleportFallbackSuccess != null
         ? this.teleportFallbackSuccess
         : "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>";
      String processed = template.replace("<x>", Integer.toString(x)).replace("<z>", Integer.toString(z));
      return MessageUtil.parseMiniMessage(processed);
   }

   public Component teleportFallbackNoCache() {
      String template = this.teleportFallbackNoCache != null
         ? this.teleportFallbackNoCache
         : "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>";
      return MessageUtil.parseMiniMessage(template);
   }

   public Component teleportFailedSearch() {
      String template = this.teleportFailedSearch != null ? this.teleportFailedSearch : "<red>Search failed: No valid locations found.</red>";
      return MessageUtil.parseMiniMessage(template);
   }

   public static TeleportMessages fromConfiguration(ConfigurationSection section) {
      return section == null
         ? defaultMessages()
         : new TeleportMessages(
            section.getString("teleporting", "<gray>Searching for a safe location...</gray>"),
            section.getString("teleport-success", "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>"),
            section.getString("teleport-failed", "<red>Unable to find a safe location. Please try again.</red>"),
            section.getString("world-missing", "<red>The configured world '<white><world></white>' is not available.</red>"),
            section.getString("join-searching", "<gray>Finding you a safe place to explore...</gray>"),
            section.getString("queue-queued", "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>"),
            section.getString("queue-full", "<red>The random teleport queue is currently full. Please try again soon.</red>"),
            section.getString("insufficient-funds", "<red>You need <white><cost></white> to use random teleport.</red>"),
            section.getString("countdown-start", "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"),
            section.getString("countdown-tick", "<gray><seconds>...</gray>"),
            section.getString("teleport-failed-biome", "<red>No valid biome was found. Please try again.</red>"),
            section.getString(
               "teleport-fallback-success",
               "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>"
            ),
            section.getString(
               "teleport-fallback-no-cache", "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>"
            ),
            section.getString("teleport-failed-search", "<red>Search failed: No valid locations found.</red>")
         );
   }

   public static TeleportMessages defaultMessages() {
      return new TeleportMessages(
         "<gray>Searching for a safe location...</gray>",
         "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>",
         "<red>Unable to find a safe location. Please try again.</red>",
         "<red>The configured world '<white><world></white>' is not available.</red>",
         "<gray>Finding you a safe place to explore...</gray>",
         "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>",
         "<red>The random teleport queue is currently full. Please try again soon.</red>",
         "<red>You need <white><cost></white> to use random teleport.</red>",
         "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>",
         "<gray><seconds>...</gray>",
         "<red>No valid biome was found. Please try again.</red>",
         "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>",
         "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>",
         "<red>Search failed: No valid locations found.</red>"
      );
   }
}
