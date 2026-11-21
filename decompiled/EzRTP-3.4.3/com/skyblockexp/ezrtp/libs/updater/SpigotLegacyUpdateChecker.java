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

public final class SpigotLegacyUpdateChecker implements UpdateSource {
   private static final String DEFAULT_BASE_URL = "https://api.spigotmc.org/legacy/update.php";
   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(8L);
   private static final String DEFAULT_USER_AGENT = "ez-plugins/spigot-update-checker/1.0";
   private final int resourceId;
   private final String currentVersion;
   private final String baseUrl;
   private final String userAgent;
   private final Duration timeout;
   private final HttpClient httpClient;

   private SpigotLegacyUpdateChecker(SpigotLegacyUpdateChecker.Builder builder) {
      this.resourceId = builder.resourceId;
      this.currentVersion = builder.currentVersion;
      this.baseUrl = builder.baseUrl;
      this.userAgent = builder.userAgent;
      this.timeout = builder.timeout;
      this.httpClient = builder.httpClient;
   }

   public static SpigotLegacyUpdateChecker.Builder builder(int resourceId, String currentVersion) {
      return new SpigotLegacyUpdateChecker.Builder(resourceId, currentVersion);
   }

   @Override
   public UpdateResult checkNow() {
      try {
         String latest = this.fetchLatestVersion();
         boolean hasUpdate = VersionComparator.isNewer(VersionComparator.normalize(this.currentVersion), VersionComparator.normalize(latest));
         return UpdateResult.success(hasUpdate, this.currentVersion, latest, latest, null, Instant.now());
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
               return UpdateResult.success(hasUpdate, this.currentVersion, latest, latest, null, Instant.now());
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
                     new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Unexpected async Spigot check failure.", cause)
                  );
            }
         );
   }

   @Override
   public String getSourceName() {
      return "spigot-legacy";
   }

   private String fetchLatestVersion() throws UpdateCheckException {
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(this.baseUrl + "?resource=" + this.resourceId))
         .GET()
         .header("User-Agent", this.userAgent)
         .timeout(this.timeout)
         .build();

      HttpResponse<String> response;
      try {
         response = this.httpClient.send(request, BodyHandlers.ofString());
      } catch (HttpTimeoutException var4) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.TIMEOUT, "Spigot request timed out.", var4);
      } catch (IOException var5) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Spigot request failed.", var5);
      } catch (InterruptedException var6) {
         Thread.currentThread().interrupt();
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Spigot request interrupted.", var6);
      }

      if (response.statusCode() == 404) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NOT_FOUND, "Spigot resource not found.");
      } else if (response.statusCode() == 429) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.RATE_LIMITED, "Spigot rate limit exceeded.");
      } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
         String body = response.body() == null ? "" : response.body().trim();
         if (body.isEmpty()) {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "Spigot returned empty version.");
         } else {
            return VersionComparator.normalize(body);
         }
      } else {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNKNOWN_HTTP_ERROR, "Unexpected Spigot HTTP status: " + response.statusCode());
      }
   }

   public static final class Builder {
      private final int resourceId;
      private final String currentVersion;
      private String baseUrl = "https://api.spigotmc.org/legacy/update.php";
      private String userAgent = "ez-plugins/spigot-update-checker/1.0";
      private Duration timeout = SpigotLegacyUpdateChecker.DEFAULT_TIMEOUT;
      private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(SpigotLegacyUpdateChecker.DEFAULT_TIMEOUT).build();

      private Builder(int resourceId, String currentVersion) {
         if (resourceId <= 0) {
            throw new IllegalArgumentException("resourceId must be > 0");
         } else {
            this.resourceId = resourceId;
            this.currentVersion = requireText(currentVersion, "currentVersion");
         }
      }

      public SpigotLegacyUpdateChecker.Builder baseUrl(String baseUrl) {
         this.baseUrl = requireText(baseUrl, "baseUrl");
         return this;
      }

      public SpigotLegacyUpdateChecker.Builder userAgent(String userAgent) {
         this.userAgent = requireText(userAgent, "userAgent");
         return this;
      }

      public SpigotLegacyUpdateChecker.Builder timeout(Duration timeout) {
         this.timeout = Objects.requireNonNull(timeout, "timeout");
         return this;
      }

      SpigotLegacyUpdateChecker.Builder httpClient(HttpClient httpClient) {
         this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
         return this;
      }

      public SpigotLegacyUpdateChecker build() {
         return new SpigotLegacyUpdateChecker(this);
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
