package me.clip.placeholderapi;

import java.util.OptionalInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ServerVersionResolver {
   private static final String PAPER_BUILD_INFO_PROVIDER = "me.clip.placeholderapi.PaperServerBuildInfoProvider";

   private ServerVersionResolver() {
   }

   @NotNull
   static ServerVersionResolver.ServerBuild resolve(@NotNull String bukkitVersion) {
      return resolve(bukkitVersion, resolvePaperBuildInfo());
   }

   @NotNull
   static ServerVersionResolver.ServerBuild resolve(@NotNull String bukkitVersion, @Nullable ServerVersionResolver.ServerBuild paperBuildInfo) {
      return paperBuildInfo != null ? paperBuildInfo : new ServerVersionResolver.ServerBuild(normalizeBukkitVersion(bukkitVersion), OptionalInt.empty());
   }

   @Nullable
   private static ServerVersionResolver.ServerBuild resolvePaperBuildInfo() {
      try {
         Class<?> provider = Class.forName("me.clip.placeholderapi.PaperServerBuildInfoProvider", true, ServerVersionResolver.class.getClassLoader());
         Object result = provider.getMethod("get").invoke(null);
         if (result instanceof ServerVersionResolver.ServerBuild) {
            return (ServerVersionResolver.ServerBuild)result;
         }
      } catch (LinkageError | ReflectiveOperationException var2) {
      }

      return null;
   }

   @NotNull
   static String normalizeBukkitVersion(@NotNull String bukkitVersion) {
      String version = bukkitVersion.split("-", 2)[0];
      int paperBuildMetadataIndex = version.indexOf(".build.");
      if (paperBuildMetadataIndex != -1) {
         version = version.substring(0, paperBuildMetadataIndex);
      }

      return version;
   }

   static final class ServerBuild {
      @NotNull
      private final String minecraftVersionId;
      @NotNull
      private final OptionalInt buildNumber;

      ServerBuild(@NotNull String minecraftVersionId, @NotNull OptionalInt buildNumber) {
         this.minecraftVersionId = minecraftVersionId;
         this.buildNumber = buildNumber;
      }

      @NotNull
      String getMinecraftVersionId() {
         return this.minecraftVersionId;
      }

      @NotNull
      OptionalInt getBuildNumber() {
         return this.buildNumber;
      }

      @NotNull
      String getLegacyVersion() {
         String version = this.minecraftVersionId;
         if (version.chars().filter(c -> c == 46).count() == 1L) {
            return 'v' + version.replace('.', '_') + "_R1";
         } else {
            String[] versionParts = version.split("\\.");
            int minor = 1;
            if (versionParts.length > 2 && !versionParts[2].isEmpty()) {
               try {
                  minor = Integer.parseInt(versionParts[2].charAt(0) + "");
               } catch (NumberFormatException var5) {
                  minor = 1;
               }
            }

            return 'v' + version.replace('.', '_').replace("_" + minor, "") + "_R" + (minor - 1);
         }
      }
   }
}
