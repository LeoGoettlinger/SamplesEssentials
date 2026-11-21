package com.skyblockexp.ezrtp.libs.updater;

import java.time.Instant;

final class GitHubReleaseInfo {
   private final String tagName;
   private final String htmlUrl;
   private final Instant publishedAt;

   GitHubReleaseInfo(String tagName, String htmlUrl, Instant publishedAt) {
      this.tagName = tagName;
      this.htmlUrl = htmlUrl;
      this.publishedAt = publishedAt;
   }

   String getTagName() {
      return this.tagName;
   }

   String getHtmlUrl() {
      return this.htmlUrl;
   }

   Instant getPublishedAt() {
      return this.publishedAt;
   }
}
