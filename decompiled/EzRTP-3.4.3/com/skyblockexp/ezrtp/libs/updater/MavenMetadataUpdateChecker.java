package com.skyblockexp.ezrtp.libs.updater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MavenMetadataUpdateChecker implements UpdateSource {
   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(8L);
   private static final String DEFAULT_USER_AGENT = "ez-plugins/maven-metadata-checker/1.0";
   private static final Pattern LATEST_PATTERN = Pattern.compile("<latest>([^<]+)</latest>");
   private static final Pattern RELEASE_PATTERN = Pattern.compile("<release>([^<]+)</release>");
   private static final Pattern VERSION_PATTERN = Pattern.compile("<version>([^<]+)</version>");
   private final String metadataUrl;
   private final String currentVersion;
   private final String userAgent;
   private final Duration timeout;
   private final HttpClient httpClient;

   private MavenMetadataUpdateChecker(MavenMetadataUpdateChecker.Builder builder) {
      this.metadataUrl = builder.metadataUrl;
      this.currentVersion = builder.currentVersion;
      this.userAgent = builder.userAgent;
      this.timeout = builder.timeout;
      this.httpClient = builder.httpClient;
   }

   public static MavenMetadataUpdateChecker.Builder builder(String metadataUrl, String currentVersion) {
      return new MavenMetadataUpdateChecker.Builder(metadataUrl, currentVersion);
   }

   @Override
   public UpdateResult checkNow() {
      try {
         String latest = this.fetchLatestVersion();
         boolean hasUpdate = VersionComparator.isNewer(VersionComparator.normalize(this.currentVersion), VersionComparator.normalize(latest));
         return UpdateResult.success(hasUpdate, this.currentVersion, latest, latest, this.metadataUrl, Instant.now());
      } catch (UpdateCheckException var3) {
         return UpdateResult.failure(this.currentVersion, var3);
      }
   }

   @Override
   public CompletableFuture<UpdateResult> checkNowAsync() {
      return CompletableFuture.<UpdateResult>supplyAsync(() -> {
            try {
               String latest = this.fetchLatestVersion();
               boolean hasUpdate = VersionComparator.isNewer(VersionComparator.normalize(this.currentVersion), VersionComparator.normalize(latest));
               return UpdateResult.success(hasUpdate, this.currentVersion, latest, latest, this.metadataUrl, Instant.now());
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
                     new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Unexpected async Maven metadata check failure.", cause)
                  );
            }
         );
   }

   @Override
   public String getSourceName() {
      return "maven-metadata";
   }

   private String fetchLatestVersion() throws UpdateCheckException {
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.metadataUrl)).GET().header("User-Agent", this.userAgent).timeout(this.timeout).build();

      HttpResponse<String> response;
      try {
         response = this.httpClient.send(request, BodyHandlers.ofString());
      } catch (HttpTimeoutException var5) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.TIMEOUT, "Maven metadata request timed out.", var5);
      } catch (IOException var6) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Maven metadata request failed.", var6);
      } catch (InterruptedException var7) {
         Thread.currentThread().interrupt();
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Maven metadata request interrupted.", var7);
      }

      if (response.statusCode() == 404) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NOT_FOUND, "maven-metadata.xml not found.");
      } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
         String body = response.body() == null ? "" : response.body();
         String latest = findFirst(body, LATEST_PATTERN);
         if (latest == null) {
            latest = findFirst(body, RELEASE_PATTERN);
         }

         if (latest == null) {
            latest = findLast(body, VERSION_PATTERN);
         }

         if (latest != null && !latest.isBlank()) {
            return VersionComparator.normalize(latest);
         } else {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "No version found in maven-metadata.xml");
         }
      } else {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNKNOWN_HTTP_ERROR, "Unexpected Maven metadata HTTP status: " + response.statusCode());
      }
   }

   private static String findFirst(String body, Pattern pattern) {
      Matcher matcher = pattern.matcher(body);
      return matcher.find() ? matcher.group(1).trim() : null;
   }

   private static String findLast(String body, Pattern pattern) {
      Matcher matcher = pattern.matcher(body);
      String value = null;

      while (matcher.find()) {
         value = matcher.group(1).trim();
      }

      return value;
   }

   public static final class Builder {
      private final String metadataUrl;
      private final String currentVersion;
      private String userAgent = "ez-plugins/maven-metadata-checker/1.0";
      private Duration timeout = MavenMetadataUpdateChecker.DEFAULT_TIMEOUT;
      private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(MavenMetadataUpdateChecker.DEFAULT_TIMEOUT).build();

      private Builder(String metadataUrl, String currentVersion) {
         this.metadataUrl = requireText(metadataUrl, "metadataUrl");
         this.currentVersion = requireText(currentVersion, "currentVersion");
      }

      public MavenMetadataUpdateChecker.Builder userAgent(String userAgent) {
         this.userAgent = requireText(userAgent, "userAgent");
         return this;
      }

      public MavenMetadataUpdateChecker.Builder timeout(Duration timeout) {
         this.timeout = Objects.requireNonNull(timeout, "timeout");
         return this;
      }

      MavenMetadataUpdateChecker.Builder httpClient(HttpClient httpClient) {
         this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
         return this;
      }

      public MavenMetadataUpdateChecker build() {
         return new MavenMetadataUpdateChecker(this);
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
