package me.clip.placeholderapi.expansion.cloud;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import me.clip.placeholderapi.util.TimeUtil;

public class CloudExpansion {
   private String name;
   private String author;
   private String latest_version;
   private String description;
   private String source_url;
   private String dependency_url;
   private boolean hasExpansion;
   private boolean shouldUpdate;
   private long last_update;
   private long ratings_count;
   private double average_rating;
   private List<String> placeholders;
   private List<CloudExpansion.Version> versions;

   public String getTimeSinceLastUpdate() {
      int time = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.getLastUpdate());
      return TimeUtil.getTime(time);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public CloudExpansion.Version getVersion() {
      return this.getLatestVersion() == null ? null : this.getVersion(this.getLatestVersion());
   }

   public CloudExpansion.Version getVersion(String version) {
      return this.versions == null ? null : this.versions.stream().filter(v -> v.getVersion().equals(version)).findFirst().orElse(null);
   }

   public List<String> getAvailableVersions() {
      return this.versions.stream().map(CloudExpansion.Version::getVersion).collect(Collectors.toList());
   }

   public String getLatestVersion() {
      return this.latest_version;
   }

   public void setLatestVersion(String latest_version) {
      this.latest_version = latest_version;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getSourceUrl() {
      return this.source_url;
   }

   public void setSourceUrl(String source_url) {
      this.source_url = source_url;
   }

   public String getDependencyUrl() {
      return this.dependency_url;
   }

   public void setDependencyUrl(String dependency_url) {
      this.dependency_url = dependency_url;
   }

   public boolean hasExpansion() {
      return this.hasExpansion;
   }

   public void setHasExpansion(boolean hasExpansion) {
      this.hasExpansion = hasExpansion;
   }

   public boolean shouldUpdate() {
      return this.shouldUpdate;
   }

   public void setShouldUpdate(boolean shouldUpdate) {
      this.shouldUpdate = shouldUpdate;
   }

   public long getLastUpdate() {
      return this.last_update;
   }

   public void setLastUpdate(long last_update) {
      this.last_update = last_update;
   }

   public long getRatingsCount() {
      return this.ratings_count;
   }

   public double getAverage_rating() {
      return this.average_rating;
   }

   public List<String> getPlaceholders() {
      return this.placeholders;
   }

   public void setPlaceholders(List<String> placeholders) {
      this.placeholders = placeholders;
   }

   public List<CloudExpansion.Version> getVersions() {
      return this.versions;
   }

   public void setVersions(List<CloudExpansion.Version> versions) {
      this.versions = versions;
   }

   public static class Version {
      private String url;
      private String version;
      private String release_notes;
      private boolean verified;

      public String getUrl() {
         return this.url;
      }

      public void setUrl(String url) {
         this.url = url;
      }

      public String getVersion() {
         return this.version;
      }

      public void setVersion(String version) {
         this.version = version;
      }

      public String getReleaseNotes() {
         return this.release_notes;
      }

      public void setReleaseNotes(String release_notes) {
         this.release_notes = release_notes;
      }

      public boolean isVerified() {
         return this.verified;
      }

      public void setVerified(boolean verified) {
         this.verified = verified;
      }
   }
}
