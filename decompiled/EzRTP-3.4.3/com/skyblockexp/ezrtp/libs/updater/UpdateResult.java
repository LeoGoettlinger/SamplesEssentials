package com.skyblockexp.ezrtp.libs.updater;

import java.time.Instant;
import java.util.Optional;

public final class UpdateResult {
   private final boolean updateAvailable;
   private final String currentVersion;
   private final String latestVersion;
   private final String latestTag;
   private final String releaseUrl;
   private final Instant publishedAt;
   private final UpdateCheckException error;

   private UpdateResult(
      boolean updateAvailable,
      String currentVersion,
      String latestVersion,
      String latestTag,
      String releaseUrl,
      Instant publishedAt,
      UpdateCheckException error
   ) {
      this.updateAvailable = updateAvailable;
      this.currentVersion = currentVersion;
      this.latestVersion = latestVersion;
      this.latestTag = latestTag;
      this.releaseUrl = releaseUrl;
      this.publishedAt = publishedAt;
      this.error = error;
   }

   public static UpdateResult success(
      boolean updateAvailable, String currentVersion, String latestVersion, String latestTag, String releaseUrl, Instant publishedAt
   ) {
      return new UpdateResult(updateAvailable, currentVersion, latestVersion, latestTag, releaseUrl, publishedAt, null);
   }

   public static UpdateResult failure(String currentVersion, UpdateCheckException error) {
      return new UpdateResult(false, currentVersion, null, null, null, null, error);
   }

   public boolean isUpdateAvailable() {
      return this.updateAvailable;
   }

   public String getCurrentVersion() {
      return this.currentVersion;
   }

   public Optional<String> getLatestVersion() {
      return Optional.ofNullable(this.latestVersion);
   }

   public Optional<String> getLatestTag() {
      return Optional.ofNullable(this.latestTag);
   }

   public Optional<String> getReleaseUrl() {
      return Optional.ofNullable(this.releaseUrl);
   }

   public Optional<Instant> getPublishedAt() {
      return Optional.ofNullable(this.publishedAt);
   }

   public Optional<UpdateCheckException> getError() {
      return Optional.ofNullable(this.error);
   }

   public boolean hasError() {
      return this.error != null;
   }
}
