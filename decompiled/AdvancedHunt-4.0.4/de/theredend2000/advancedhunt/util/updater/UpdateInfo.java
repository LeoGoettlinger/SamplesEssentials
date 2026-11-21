package de.theredend2000.advancedhunt.util.updater;

import java.time.Instant;
import java.util.Objects;

public final class UpdateInfo {
   private final String version;
   private final String sourceVersion;
   private final Instant publishedAt;

   public UpdateInfo(String version, String sourceVersion, Instant publishedAt) {
      this.version = version;
      this.sourceVersion = sourceVersion;
      this.publishedAt = publishedAt;
   }

   public String version() {
      return this.version;
   }

   public String sourceVersion() {
      return this.sourceVersion;
   }

   public Instant publishedAt() {
      return this.publishedAt;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof UpdateInfo)) {
         return false;
      } else {
         UpdateInfo that = (UpdateInfo)o;
         return Objects.equals(this.version, that.version)
            && Objects.equals(this.sourceVersion, that.sourceVersion)
            && Objects.equals(this.publishedAt, that.publishedAt);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.version, this.sourceVersion, this.publishedAt);
   }

   @Override
   public String toString() {
      return "UpdateInfo{version='" + this.version + '\'' + ", sourceVersion='" + this.sourceVersion + '\'' + ", publishedAt=" + this.publishedAt + '}';
   }
}
