package com.github.jikoo.openinv.lib.planarwrappers.util.version;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public final class BukkitVersions {
   public static final Version MINECRAFT;
   @Deprecated
   public static final Version CRAFTBUKKIT_PACKAGE;

   @VisibleForTesting
   @Contract("_ -> new")
   @NotNull
   static Version parseMinecraftVersion(Server server) {
      Pattern semVerRelease = Pattern.compile("^(\\d+)\\.(\\d+)(\\.(\\d+))?-.*$");

      String versionString;
      try {
         MethodHandle getMcVer = MethodHandles.publicLookup().findVirtual(Server.class, "getMinecraftVersion", MethodType.methodType(String.class));
         versionString = (String)getMcVer.invoke((Server)server);
      } catch (Throwable var4) {
         versionString = Bukkit.getBukkitVersion();
      }

      Matcher matcher = semVerRelease.matcher(versionString);
      return (Version)(matcher.find()
         ? new IntVersion(
            Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(4))
         )
         : Version.of(stripExtraData(versionString)));
   }

   @VisibleForTesting
   @NotNull
   static Version parseCraftbukkitVersion(@NotNull String craftbukkitPackage) {
      Pattern packageVer = Pattern.compile("^org\\.bukkit\\.craftbukkit\\.v(\\d+)_(\\d+)_R?(\\d+)");
      Matcher matcher = packageVer.matcher(craftbukkitPackage);
      return (Version)(matcher.find()
         ? new IntVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)))
         : MINECRAFT);
   }

   @NotNull
   private static String stripExtraData(@NotNull String version) {
      int preReleaseStart = version.indexOf(45);
      int metadataStart = version.indexOf(43);
      if (preReleaseStart > -1) {
         return metadataStart > -1 ? version.substring(0, Math.min(preReleaseStart, metadataStart)) : version.substring(0, preReleaseStart);
      } else {
         return metadataStart > -1 ? version.substring(0, metadataStart) : version;
      }
   }

   private BukkitVersions() {
      throw new IllegalStateException("Cannot instantiate static utility classes!");
   }

   static {
      Server server = Bukkit.getServer();
      MINECRAFT = parseMinecraftVersion(server);
      String packageString = server.getClass().getPackage().toString();
      CRAFTBUKKIT_PACKAGE = parseCraftbukkitVersion(packageString);
   }
}
