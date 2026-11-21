package com.skyblockexp.ezrtp.libs.updater;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class ModrinthUpdateChecker implements UpdateSource {
   private static final String DEFAULT_API_BASE_URL = "https://api.modrinth.com/v2";
   private static final String DEFAULT_USER_AGENT = "ez-plugins/modrinth-update-checker/1.0";
   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(8L);
   private final String projectIdOrSlug;
   private final String currentVersion;
   private final List<String> loaders;
   private final List<String> gameVersions;
   private final boolean includeChangelog;
   private final Optional<String> authToken;
   private final Duration timeout;
   private final String userAgent;
   private final ModrinthClient modrinthClient;

   private ModrinthUpdateChecker(ModrinthUpdateChecker.Builder builder) {
      this.projectIdOrSlug = builder.projectIdOrSlug;
      this.currentVersion = builder.currentVersion;
      this.loaders = List.copyOf(builder.loaders);
      this.gameVersions = List.copyOf(builder.gameVersions);
      this.includeChangelog = builder.includeChangelog;
      this.authToken = Optional.ofNullable(builder.authToken).filter(s -> !s.isBlank());
      this.timeout = builder.timeout;
      this.userAgent = builder.userAgent;
      this.modrinthClient = builder.modrinthClient;
   }

   public static ModrinthUpdateChecker.Builder builder(String projectIdOrSlug, String currentVersion) {
      return new ModrinthUpdateChecker.Builder(projectIdOrSlug, currentVersion);
   }

   @Override
   public UpdateResult checkNow() {
      try {
         ModrinthVersionInfo latest = this.modrinthClient
            .fetchLatestVersion(this.projectIdOrSlug, this.loaders, this.gameVersions, this.includeChangelog, this.authToken, this.timeout, this.userAgent);
         String latestVersion = VersionComparator.normalize(latest.getVersionNumber());
         String normalizedCurrent = VersionComparator.normalize(this.currentVersion);
         boolean hasUpdate = VersionComparator.isNewer(normalizedCurrent, latestVersion);
         return UpdateResult.success(hasUpdate, this.currentVersion, latestVersion, latest.getVersionType(), latest.getFileUrl(), latest.getPublishedAt());
      } catch (UpdateCheckException var5) {
         return UpdateResult.failure(this.currentVersion, var5);
      }
   }

   @Override
   public CompletableFuture<UpdateResult> checkNowAsync() {
      return CompletableFuture.<UpdateResult>supplyAsync(
            () -> {
               try {
                  ModrinthVersionInfo latest = this.modrinthClient
                     .fetchLatestVersion(
                        this.projectIdOrSlug, this.loaders, this.gameVersions, this.includeChangelog, this.authToken, this.timeout, this.userAgent
                     );
                  String latestVersion = VersionComparator.normalize(latest.getVersionNumber());
                  String normalizedCurrent = VersionComparator.normalize(this.currentVersion);
                  boolean hasUpdate = VersionComparator.isNewer(normalizedCurrent, latestVersion);
                  return UpdateResult.success(
                     hasUpdate, this.currentVersion, latestVersion, latest.getVersionType(), latest.getFileUrl(), latest.getPublishedAt()
                  );
               } catch (UpdateCheckException var5) {
                  throw new CompletionException(var5);
               }
            }
         )
         .exceptionally(
            ex -> {
               Throwable cause = ex.getCause() == null ? ex : ex.getCause();
               return cause instanceof UpdateCheckException
                  ? UpdateResult.failure(this.currentVersion, (UpdateCheckException)cause)
                  : UpdateResult.failure(
                     this.currentVersion,
                     new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Unexpected async update check failure.", cause)
                  );
            }
         );
   }

   @Override
   public String getSourceName() {
      return "modrinth";
   }

   public static final class Builder {
      private final String projectIdOrSlug;
      private final String currentVersion;
      private final List<String> loaders = new ArrayList<>();
      private final List<String> gameVersions = new ArrayList<>();
      private boolean includeChangelog;
      private String authToken;
      private Duration timeout = ModrinthUpdateChecker.DEFAULT_TIMEOUT;
      private String userAgent = "ez-plugins/modrinth-update-checker/1.0";
      private String apiBaseUrl = "https://api.modrinth.com/v2";
      private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(ModrinthUpdateChecker.DEFAULT_TIMEOUT).build();
      private ModrinthClient modrinthClient;

      private Builder(String projectIdOrSlug, String currentVersion) {
         this.projectIdOrSlug = requireText(projectIdOrSlug, "projectIdOrSlug");
         this.currentVersion = requireText(currentVersion, "currentVersion");
      }

      public ModrinthUpdateChecker.Builder loaders(List<String> loaders) {
         this.loaders.clear();
         this.loaders.addAll(Objects.requireNonNull(loaders, "loaders"));
         return this;
      }

      public ModrinthUpdateChecker.Builder gameVersions(List<String> gameVersions) {
         this.gameVersions.clear();
         this.gameVersions.addAll(Objects.requireNonNull(gameVersions, "gameVersions"));
         return this;
      }

      public ModrinthUpdateChecker.Builder includeChangelog(boolean includeChangelog) {
         this.includeChangelog = includeChangelog;
         return this;
      }

      public ModrinthUpdateChecker.Builder authToken(String authToken) {
         this.authToken = authToken;
         return this;
      }

      public ModrinthUpdateChecker.Builder timeout(Duration timeout) {
         this.timeout = Objects.requireNonNull(timeout, "timeout");
         return this;
      }

      public ModrinthUpdateChecker.Builder userAgent(String userAgent) {
         this.userAgent = requireText(userAgent, "userAgent");
         return this;
      }

      public ModrinthUpdateChecker.Builder apiBaseUrl(String apiBaseUrl) {
         this.apiBaseUrl = requireText(apiBaseUrl, "apiBaseUrl");
         return this;
      }

      ModrinthUpdateChecker.Builder httpClient(HttpClient httpClient) {
         this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
         return this;
      }

      public ModrinthUpdateChecker build() {
         if (this.loaders.isEmpty()) {
            this.loaders.add("paper");
         }

         if (this.modrinthClient == null) {
            this.modrinthClient = new ModrinthClient(this.httpClient, this.apiBaseUrl);
         }

         return new ModrinthUpdateChecker(this);
      }

      private static String requireText(String value, String field) {
         if (value != null && !value.isBlank()) {
            return value;
         } else {
            throw new IllegalArgumentException(field + " must not be blank.");
         }
      }
   }
}
