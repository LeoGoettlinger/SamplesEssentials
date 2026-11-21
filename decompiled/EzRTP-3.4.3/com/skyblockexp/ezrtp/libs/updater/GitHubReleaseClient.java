package com.skyblockexp.ezrtp.libs.updater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GitHubReleaseClient {
   private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
   private static final Pattern URL_PATTERN = Pattern.compile("\"html_url\"\\s*:\\s*\"([^\"]+)\"");
   private static final Pattern PUBLISHED_PATTERN = Pattern.compile("\"published_at\"\\s*:\\s*\"([^\"]+)\"");
   private final HttpClient httpClient;
   private final String apiBaseUrl;

   GitHubReleaseClient(HttpClient httpClient, String apiBaseUrl) {
      this.httpClient = httpClient;
      this.apiBaseUrl = apiBaseUrl;
   }

   GitHubReleaseInfo fetchLatestRelease(String owner, String repo, Optional<String> githubToken, Duration timeout, String userAgent) throws UpdateCheckException {
      String url = String.format("%s/repos/%s/%s/releases/latest", this.apiBaseUrl, owner, repo);
      Builder requestBuilder = HttpRequest.newBuilder()
         .uri(URI.create(url))
         .GET()
         .header("Accept", "application/vnd.github+json")
         .header("X-GitHub-Api-Version", "2022-11-28")
         .header("User-Agent", userAgent)
         .timeout(timeout);
      githubToken.ifPresent(token -> requestBuilder.header("Authorization", "Bearer " + token));

      HttpResponse<String> response;
      try {
         response = this.httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
      } catch (HttpTimeoutException var10) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.TIMEOUT, "GitHub API request timed out.", var10);
      } catch (IOException var11) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "GitHub API request failed.", var11);
      } catch (InterruptedException var12) {
         Thread.currentThread().interrupt();
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NETWORK_ERROR, "GitHub API request interrupted.", var12);
      }

      int statusCode = response.statusCode();
      if (statusCode == 404) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.NOT_FOUND, "Repository or release not found: " + owner + "/" + repo);
      } else if (statusCode == 401) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNAUTHORIZED, "Unauthorized GitHub API token.");
      } else if (statusCode == 403) {
         if (response.headers().firstValue("X-RateLimit-Remaining").map("0"::equals).orElse(false)) {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.RATE_LIMITED, "GitHub API rate limit exceeded.");
         } else {
            throw new UpdateCheckException(UpdateCheckException.ErrorType.UNAUTHORIZED, "Forbidden by GitHub API.");
         }
      } else if (statusCode >= 200 && statusCode < 300) {
         return this.parseRelease(response.body());
      } else {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.UNKNOWN_HTTP_ERROR, "Unexpected HTTP status from GitHub API: " + statusCode);
      }
   }

   private GitHubReleaseInfo parseRelease(String body) throws UpdateCheckException {
      String tag = this.extractString(body, TAG_PATTERN);
      String htmlUrl = this.extractString(body, URL_PATTERN);
      String published = this.extractString(body, PUBLISHED_PATTERN);

      Instant publishedAt;
      try {
         publishedAt = Instant.parse(published);
      } catch (DateTimeParseException var7) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "Invalid published_at timestamp in GitHub response.", var7);
      }

      return new GitHubReleaseInfo(tag, htmlUrl, publishedAt);
   }

   private String extractString(String body, Pattern pattern) throws UpdateCheckException {
      Matcher matcher = pattern.matcher(body);
      if (!matcher.find()) {
         throw new UpdateCheckException(UpdateCheckException.ErrorType.INVALID_RESPONSE, "Expected field missing in GitHub release response.");
      } else {
         return matcher.group(1);
      }
   }
}
