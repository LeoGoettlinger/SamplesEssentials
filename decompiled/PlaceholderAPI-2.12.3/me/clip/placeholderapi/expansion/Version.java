package me.clip.placeholderapi.expansion;

@Deprecated
public final class Version {
   private final boolean isSpigot;
   private final String version;

   public Version(String version, boolean isSpigot) {
      this.version = version;
      this.isSpigot = isSpigot;
   }

   public String getVersion() {
      return this.version == null ? "unknown" : this.version;
   }

   public boolean isSpigot() {
      return this.isSpigot;
   }

   public boolean compareTo(String version) {
      return this.getVersion().equalsIgnoreCase(version);
   }
}
