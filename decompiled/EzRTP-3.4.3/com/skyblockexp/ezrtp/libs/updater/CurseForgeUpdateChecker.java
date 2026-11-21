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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CurseForgeUpdateChecker implements UpdateSource {
   private static final String DEFAULT_API_BASE_URL = "https://api.curseforge.com/v1";
   private static final String DEFAULT_USER_AGENT = "ez-plugins/curseforge-update-checker/1.0";
   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(8L);
   private static final Pattern FILE_VERSION_PATTERN = Pattern.compile("\"displayName\"\\s*:\\s*\"([^\"]+)\"");
   private final int modId;
   private final String currentVersion;
   private final String apiBaseUrl;
   private final String userAgent;
   private final Duration timeout;
   private final Optional<String> apiKey;
   private final HttpClient httpClient;

   private CurseForgeUpdateChecker(CurseForgeUpdateChecker.Builder builder) {
      this.modId = builder.modId;
      this.currentVersion = builder.currentVersion;
      this.apiBaseUrl = builder.apiBaseUrl;
      this.userAgent = builder.userAgent;
      this.timeout = builder.timeout;
      this.apiKey = Optional.ofNullable(builder.apiKey).filter(v -> !v.isBlank());
      this.httpClient = builder.httpClient;
   }

   public static CurseForgeUpdateChecker.Builder builder(int modId, String currentVersion) {
      return new CurseForgeUpdateChecker.Builder(modId, currentVersion);
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
                     new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Unexpected async CurseForge check failure.", cause)
                  );
            }
         );
   }

   @Override
   public String getSourceName() {
      return "curseforge";
   }

   private String fetchLatestVersion() throws UpdateCheckException {
      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
         .uri(URI.create(this.apiBaseUrl + "/mods/" + this.modId + "/files"))
         .GET()
         .header("Accept", "application/json")
         .header("User-Agent", this.userAgent)
         .timeout(this.timeout);
      this.apiKey.ifPresent(key -> requestBuilder.header("x-api-key", key));

      HttpResponse<String> response;
      try {
         response = this.httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
      } catch (HttpTimeoutException var4) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.TIMEOUT, "CurseForge request timed out.", var4);
      } catch (IOException var5) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "CurseForge request failed.", var5);
      } catch (InterruptedException var6) {
         Thread.currentThread().interrupt();
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "CurseForge request interrupted.", var6);
      }

      if (response.statusCode() == 401 || response.statusCode() == 403) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNAUTHORIZED, "CurseForge rejected authentication.");
      } else if (response.statusCode() == 404) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NOT_FOUND, "CurseForge project not found.");
      } else if (response.statusCode() == 429) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.RATE_LIMITED, "CurseForge rate limit exceeded.");
      } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
         Matcher matcher = FILE_VERSION_PATTERN.matcher((CharSequence)(response.body() == null ? "" : response.body()));
         if (!matcher.find()) {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "No CurseForge file version found in response.");
         } else {
            return VersionComparator.normalize(matcher.group(1));
         }
      } else {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNKNOWN_HTTP_ERROR, "Unexpected CurseForge HTTP status: " + response.statusCode());
      }
   }

   public static final class Builder {
      private final int modId;
      private final String currentVersion;
      private String apiBaseUrl = "https://api.curseforge.com/v1";
      private String userAgent = "ez-plugins/curseforge-update-checker/1.0";
      private Duration timeout = CurseForgeUpdateChecker.DEFAULT_TIMEOUT;
      private String apiKey;
      private HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CurseForgeUpdateChecker.DEFAULT_TIMEOUT).build();

      private Builder(int modId, String currentVersion) {
         if (modId <= 0) {
            throw new IllegalArgumentException("modId must be > 0");
         } else {
            this.modId = modId;
            this.currentVersion = requireText(currentVersion, "currentVersion");
         }
      }

      public CurseForgeUpdateChecker.Builder apiBaseUrl(String apiBaseUrl) {
         this.apiBaseUrl = requireText(apiBaseUrl, "apiBaseUrl");
         return this;
      }

      public CurseForgeUpdateChecker.Builder userAgent(String userAgent) {
         this.userAgent = requireText(userAgent, "userAgent");
         return this;
      }

      public CurseForgeUpdateChecker.Builder timeout(Duration timeout) {
         this.timeout = Objects.requireNonNull(timeout, "timeout");
         return this;
      }

      public CurseForgeUpdateChecker.Builder apiKey(String apiKey) {
         this.apiKey = apiKey;
         return this;
      }

      CurseForgeUpdateChecker.Builder httpClient(HttpClient httpClient) {
         this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
         return this;
      }

      public CurseForgeUpdateChecker build() {
         return new CurseForgeUpdateChecker(this);
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
