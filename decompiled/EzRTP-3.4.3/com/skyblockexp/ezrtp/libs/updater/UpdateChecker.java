package com.skyblockexp.ezrtp.libs.updater;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class UpdateChecker implements UpdateSource {
   private static final String DEFAULT_API_BASE_URL = "https://api.github.com";
   private static final String DEFAULT_USER_AGENT = "ezplugins-github-update-checker";
   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(8L);
   private final String owner;
   private final String repo;
   private final String currentVersion;
   private final Optional<String> githubToken;
   private final Duration timeout;
   private final String userAgent;
   private final GitHubReleaseClient releaseClient;

   private UpdateChecker(UpdateChecker.Builder builder) {
      this.owner = builder.owner;
      this.repo = builder.repo;
      this.currentVersion = builder.currentVersion;
      this.githubToken = Optional.ofNullable(builder.githubToken).filter(s -> !s.isBlank());
      this.timeout = builder.timeout;
      this.userAgent = builder.userAgent;
      this.releaseClient = builder.releaseClient;
   }

   public static UpdateChecker.Builder builder(String owner, String repo, String currentVersion) {
      return new UpdateChecker.Builder(owner, repo, currentVersion);
   }

   @Override
   public UpdateResult checkNow() {
      try {
         GitHubReleaseInfo latest = this.releaseClient.fetchLatestRelease(this.owner, this.repo, this.githubToken, this.timeout, this.userAgent);
         String latestVersion = VersionComparator.normalize(latest.getTagName());
         String normalizedCurrent = VersionComparator.normalize(this.currentVersion);
         boolean hasUpdate = VersionComparator.isNewer(normalizedCurrent, latestVersion);
         return UpdateResult.success(hasUpdate, this.currentVersion, latestVersion, latest.getTagName(), latest.getHtmlUrl(), latest.getPublishedAt());
      } catch (UpdateCheckException var5) {
         return UpdateResult.failure(this.currentVersion, var5);
      }
   }

   @Override
   public CompletableFuture<UpdateResult> checkNowAsync() {
      return CompletableFuture.<UpdateResult>supplyAsync(() -> {
            try {
               GitHubReleaseInfo latest = this.releaseClient.fetchLatestRelease(this.owner, this.repo, this.githubToken, this.timeout, this.userAgent);
               String latestVersion = VersionComparator.normalize(latest.getTagName());
               String normalizedCurrent = VersionComparator.normalize(this.currentVersion);
               boolean hasUpdate = VersionComparator.isNewer(normalizedCurrent, latestVersion);
               return UpdateResult.success(hasUpdate, this.currentVersion, latestVersion, latest.getTagName(), latest.getHtmlUrl(), latest.getPublishedAt());
            } catch (UpdateCheckException var5) {
               throw new CompletionException(var5);
            }
         })
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
      return "github";
   }

   public static final class Builder {
      private final String owner;
      private final String repo;
      private final String currentVersion;
      private String githubToken;
      private Duration timeout = UpdateChecker.DEFAULT_TIMEOUT;
      private String userAgent = "ezplugins-github-update-checker";
      private String apiBaseUrl = "https://api.github.com";
      private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(UpdateChecker.DEFAULT_TIMEOUT).build();
      private GitHubReleaseClient releaseClient;

      private Builder(String owner, String repo, String currentVersion) {
         this.owner = requireText(owner, "owner");
         this.repo = requireText(repo, "repo");
         this.currentVersion = requireText(currentVersion, "currentVersion");
      }

      public UpdateChecker.Builder githubToken(String githubToken) {
         this.githubToken = githubToken;
         return this;
      }

      public UpdateChecker.Builder timeout(Duration timeout) {
         this.timeout = Objects.requireNonNull(timeout, "timeout");
         return this;
      }

      public UpdateChecker.Builder userAgent(String userAgent) {
         this.userAgent = requireText(userAgent, "userAgent");
         return this;
      }

      public UpdateChecker.Builder apiBaseUrl(String apiBaseUrl) {
         this.apiBaseUrl = requireText(apiBaseUrl, "apiBaseUrl");
         return this;
      }

      UpdateChecker.Builder httpClient(HttpClient httpClient) {
         this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
         return this;
      }

      public UpdateChecker build() {
         if (this.releaseClient == null) {
            this.releaseClient = new GitHubReleaseClient(this.httpClient, this.apiBaseUrl);
         }

         return new UpdateChecker(this);
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
