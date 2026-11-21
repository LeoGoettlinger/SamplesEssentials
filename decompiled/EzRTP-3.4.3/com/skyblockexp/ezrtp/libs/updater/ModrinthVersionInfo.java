package com.skyblockexp.ezrtp.libs.updater;

import java.time.Instant;

final class ModrinthVersionInfo {
   private final String versionNumber;
   private final String versionType;
   private final String fileUrl;
   private final Instant publishedAt;

   ModrinthVersionInfo(String versionNumber, String versionType, String fileUrl, Instant publishedAt) {
      this.versionNumber = versionNumber;
      this.versionType = versionType;
      this.fileUrl = fileUrl;
      this.publishedAt = publishedAt;
   }

   String getVersionNumber() {
      return this.versionNumber;
   }

   String getVersionType() {
      return this.versionType;
   }

   String getFileUrl() {
      return this.fileUrl;
   }

   Instant getPublishedAt() {
      return this.publishedAt;
   }
}
