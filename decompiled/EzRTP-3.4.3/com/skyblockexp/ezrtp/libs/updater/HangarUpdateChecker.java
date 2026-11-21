package com.skyblockexp.ezrtp.libs.updater;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HangarUpdateChecker implements UpdateSource {
   private static final String DEFAULT_API_BASE_URL = "https://hangar.papermc.io/api/v1";
   private static final String DEFAULT_USER_AGENT = "ez-plugins/hangar-update-checker/1.0";
   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(8L);
   private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
   private static final Pattern CREATED_AT_PATTERN = Pattern.compile("\"createdAt\"\\s*:\\s*\"([^\"]+)\"");
   private final String author;
   private final String project;
   private final String currentVersion;
   private final String platform;
   private final String apiBaseUrl;
   private final String userAgent;
   private final Duration timeout;
   private final HttpClient httpClient;

   private HangarUpdateChecker(HangarUpdateChecker.Builder builder) {
      this.author = builder.author;
      this.project = builder.project;
      this.currentVersion = builder.currentVersion;
      this.platform = builder.platform;
      this.apiBaseUrl = builder.apiBaseUrl;
      this.userAgent = builder.userAgent;
      this.timeout = builder.timeout;
      this.httpClient = builder.httpClient;
   }

   public static HangarUpdateChecker.Builder builder(String author, String project, String currentVersion) {
      return new HangarUpdateChecker.Builder(author, project, currentVersion);
   }

   @Override
   public UpdateResult checkNow() {
      try {
         HangarUpdateChecker.HangarVersion latest = this.fetchLatestVersion();
         boolean hasUpdate = VersionComparator.isNewer(VersionComparator.normalize(this.currentVersion), VersionComparator.normalize(latest.name));
         return UpdateResult.success(hasUpdate, this.currentVersion, latest.name, latest.name, latest.releaseUrl, latest.publishedAt);
      } catch (UpdateCheckException var3) {
         return UpdateResult.failure(this.currentVersion, var3);
      }
   }

   @Override
   public CompletableFuture<UpdateResult> checkNowAsync() {
      return CompletableFuture.<UpdateResult>supplyAsync(() -> {
            try {
               HangarUpdateChecker.HangarVersion latest = this.fetchLatestVersion();
               boolean hasUpdate = VersionComparator.isNewer(VersionComparator.normalize(this.currentVersion), VersionComparator.normalize(latest.name));
               return UpdateResult.success(hasUpdate, this.currentVersion, latest.name, latest.name, latest.releaseUrl, latest.publishedAt);
            } catch (UpdateCheckException var3) {
               throw new CompletionException(var3);
            }
         })
         .exceptionally(
            ex -> {
               Throwable cause = ex.getCause() == null ? ex : ex.getCause();
               return cause instanceof UpdateCheckException
                  ? UpdateResult.failure(this.currentVersion, (UpdateCheckException)cause)
                  : UpdateResult.failure(
                     this.currentVersion,
                     new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Unexpected async Hangar check failure.", cause)
                  );
            }
         );
   }

   @Override
   public String getSourceName() {
      return "hangar";
   }

   private HangarUpdateChecker.HangarVersion fetchLatestVersion() throws UpdateCheckException {
      String url = String.format(
         "%s/projects/%s/%s/versions?limit=1&offset=0&platform=%s", this.apiBaseUrl, urlEncode(this.author), urlEncode(this.project), urlEncode(this.platform)
      );
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(url))
         .GET()
         .header("Accept", "application/json")
         .header("User-Agent", this.userAgent)
         .timeout(this.timeout)
         .build();

      HttpResponse<String> response;
      try {
         response = this.httpClient.send(request, BodyHandlers.ofString());
      } catch (HttpTimeoutException var11) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.TIMEOUT, "Hangar request timed out.", var11);
      } catch (IOException var12) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Hangar request failed.", var12);
      } catch (InterruptedException var13) {
         Thread.currentThread().interrupt();
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Hangar request interrupted.", var13);
      }

      if (response.statusCode() == 404) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NOT_FOUND, "Hangar project not found.");
      } else if (response.statusCode() == 429) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.RATE_LIMITED, "Hangar rate limit exceeded.");
      } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
         String body = response.body() == null ? "" : response.body();
         Matcher nameMatcher = NAME_PATTERN.matcher(body);
         if (!nameMatcher.find()) {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "No Hangar version in response.");
         } else {
            String name = nameMatcher.group(1);
            Matcher createdAtMatcher = CREATED_AT_PATTERN.matcher(body);
            Instant publishedAt = Instant.now();
            if (createdAtMatcher.find()) {
               try {
                  publishedAt = Instant.parse(createdAtMatcher.group(1));
               } catch (Exception var10) {
                  publishedAt = Instant.now();
               }
            }

            String releaseUrl = "https://hangar.papermc.io/" + this.author + "/" + this.project + "/versions/" + name;
            return new HangarUpdateChecker.HangarVersion(name, releaseUrl, publishedAt);
         }
      } else {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNKNOWN_HTTP_ERROR, "Unexpected Hangar HTTP status: " + response.statusCode());
      }
   }

   private static String urlEncode(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }

   public static final class Builder {
      private final String author;
      private final String project;
      private final String currentVersion;
      private String platform = "PAPER";
      private String apiBaseUrl = "https://hangar.papermc.io/api/v1";
      private String userAgent = "ez-plugins/hangar-update-checker/1.0";
      private Duration timeout = HangarUpdateChecker.DEFAULT_TIMEOUT;
      private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(HangarUpdateChecker.DEFAULT_TIMEOUT).build();

      private Builder(String author, String project, String currentVersion) {
         this.author = requireText(author, "author");
         this.project = requireText(project, "project");
         this.currentVersion = requireText(currentVersion, "currentVersion");
      }

      public HangarUpdateChecker.Builder platform(String platform) {
         this.platform = requireText(platform, "platform");
         return this;
      }

      public HangarUpdateChecker.Builder apiBaseUrl(String apiBaseUrl) {
         this.apiBaseUrl = requireText(apiBaseUrl, "apiBaseUrl");
         return this;
      }

      public HangarUpdateChecker.Builder userAgent(String userAgent) {
         this.userAgent = requireText(userAgent, "userAgent");
         return this;
      }

      public HangarUpdateChecker.Builder timeout(Duration timeout) {
         this.timeout = Objects.requireNonNull(timeout, "timeout");
         return this;
      }

      HangarUpdateChecker.Builder httpClient(HttpClient httpClient) {
         this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
         return this;
      }

      public HangarUpdateChecker build() {
         return new HangarUpdateChecker(this);
      }

      private static String requireText(String value, String field) {
         if (value != null && !value.isBlank()) {
            return value;
         } else {
            throw new IllegalArgumentException(field + " must not be blank.");
         }
      }
   }

   private static final class HangarVersion {
      private final String name;
      private final String releaseUrl;
      private final Instant publishedAt;

      private HangarVersion(String name, String releaseUrl, Instant publishedAt) {
         this.name = name;
         this.releaseUrl = releaseUrl;
         this.publishedAt = publishedAt;
      }
   }
}
