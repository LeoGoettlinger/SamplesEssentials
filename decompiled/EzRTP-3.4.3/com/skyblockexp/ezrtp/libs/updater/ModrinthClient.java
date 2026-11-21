package com.skyblockexp.ezrtp.libs.updater;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ModrinthClient {
   private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
   private static final Pattern VERSION_TYPE_PATTERN = Pattern.compile("\"version_type\"\\s*:\\s*\"([^\"]+)\"");
   private static final Pattern DATE_PUBLISHED_PATTERN = Pattern.compile("\"date_published\"\\s*:\\s*\"([^\"]+)\"");
   private static final Pattern FILE_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"(https?://[^\"]+)\"");
   private final HttpClient httpClient;
   private final String apiBaseUrl;

   ModrinthClient(HttpClient httpClient, String apiBaseUrl) {
      this.httpClient = httpClient;
      this.apiBaseUrl = apiBaseUrl;
   }

   ModrinthVersionInfo fetchLatestVersion(
      String projectIdOrSlug,
      List<String> loaders,
      List<String> gameVersions,
      boolean includeChangelog,
      Optional<String> token,
      Duration timeout,
      String userAgent
   ) throws UpdateCheckException {
      String url = String.format("%s/project/%s/version?include_changelog=%s", this.apiBaseUrl, urlEncode(projectIdOrSlug), includeChangelog);
      if (!loaders.isEmpty()) {
         url = url + "&loaders=" + urlEncode(asJsonArray(loaders));
      }

      if (!gameVersions.isEmpty()) {
         url = url + "&game_versions=" + urlEncode(asJsonArray(gameVersions));
      }

      Builder requestBuilder = HttpRequest.newBuilder()
         .uri(URI.create(url))
         .GET()
         .header("Accept", "application/json")
         .header("User-Agent", userAgent)
         .timeout(timeout);
      token.ifPresent(value -> requestBuilder.header("Authorization", value));

      HttpResponse<String> response;
      try {
         response = this.httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
      } catch (HttpTimeoutException var12) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.TIMEOUT, "Modrinth API request timed out.", var12);
      } catch (IOException var13) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Modrinth API request failed.", var13);
      } catch (InterruptedException var14) {
         Thread.currentThread().interrupt();
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "Modrinth API request interrupted.", var14);
      }

      int statusCode = response.statusCode();
      if (statusCode == 404) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NOT_FOUND, "Modrinth project or versions not found.");
      } else if (statusCode == 401 || statusCode == 403) {
         if (response.headers().firstValue("X-Ratelimit-Remaining").map("0"::equals).orElse(false)) {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.RATE_LIMITED, "Modrinth rate limit exceeded.");
         } else {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.UNAUTHORIZED, "Unauthorized or forbidden by Modrinth.");
         }
      } else if (statusCode == 400) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.BAD_REQUEST, "Invalid Modrinth query parameters.");
      } else if (statusCode >= 200 && statusCode < 300) {
         return this.parseVersion(response.body());
      } else {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNKNOWN_HTTP_ERROR, "Unexpected HTTP status from Modrinth: " + statusCode);
      }
   }

   private ModrinthVersionInfo parseVersion(String body) throws UpdateCheckException {
      int firstObjectStart = body.indexOf(123);
      if (firstObjectStart < 0) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "No Modrinth versions in response.");
      } else {
         int firstObjectEnd = body.indexOf("},", firstObjectStart);
         if (firstObjectEnd < 0) {
            firstObjectEnd = body.lastIndexOf(125);
         }

         if (firstObjectEnd <= firstObjectStart) {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "Malformed Modrinth response.");
         } else {
            String firstItem = body.substring(firstObjectStart, firstObjectEnd + 1);
            String versionNumber = this.extractString(firstItem, VERSION_NUMBER_PATTERN);
            String versionType = this.extractString(firstItem, VERSION_TYPE_PATTERN);
            String datePublished = this.extractString(firstItem, DATE_PUBLISHED_PATTERN);
            String fileUrl = this.extractOptionalString(firstItem, FILE_URL_PATTERN).orElse(null);

            Instant publishedAt;
            try {
               publishedAt = Instant.parse(datePublished);
            } catch (DateTimeParseException var11) {
               throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "Invalid Modrinth date_published timestamp.", var11);
            }

            return new ModrinthVersionInfo(versionNumber, versionType, fileUrl, publishedAt);
         }
      }
   }

   private String extractString(String body, Pattern pattern) throws UpdateCheckException {
      Matcher matcher = pattern.matcher(body);
      if (!matcher.find()) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "Expected field missing in Modrinth response.");
      } else {
         return matcher.group(1);
      }
   }

   private Optional<String> extractOptionalString(String body, Pattern pattern) {
      Matcher matcher = pattern.matcher(body);
      return !matcher.find() ? Optional.empty() : Optional.of(matcher.group(1));
   }

   private static String asJsonArray(List<String> values) {
      StringBuilder builder = new StringBuilder("[");

      for (int i = 0; i < values.size(); i++) {
         if (i > 0) {
            builder.append(",");
         }

         builder.append("\"").append(values.get(i)).append("\"");
      }

      builder.append("]");
      return builder.toString();
   }

   private static String urlEncode(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }
}
