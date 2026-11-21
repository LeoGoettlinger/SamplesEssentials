package com.sk89q.worldedit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.Nullable;

public class WorldEditManifest {
   public static final String WORLD_EDIT_VERSION = "WorldEdit-Version";
   public static final String WORLD_EDIT_KIND = "WorldEdit-Kind";
   private final String worldEditVersion;
   private final WorldEditManifest.Kind worldEditKind;

   public static WorldEditManifest load() {
      Attributes attributes = readAttributes();

      WorldEditManifest.Kind kind;
      try {
         kind = WorldEditManifest.Kind.valueOf(readAttribute(attributes, "WorldEdit-Kind", () -> "UNKNOWN"));
      } catch (IllegalArgumentException var3) {
         kind = WorldEditManifest.Kind.UNKNOWN;
      }

      return new WorldEditManifest(readAttribute(attributes, "WorldEdit-Version", () -> "(unknown)"), kind);
   }

   @Nullable
   public static Attributes readAttributes() {
      Class<WorldEditManifest> clazz = WorldEditManifest.class;
      String className = clazz.getSimpleName() + ".class";
      String classPath = clazz.getResource(className).toString();
      if (!classPath.startsWith("jar")) {
         return null;
      } else {
         try {
            JarURLConnection jarConnection = (JarURLConnection)URI.create(classPath).toURL().openConnection();
            Manifest manifest = jarConnection.getManifest();
            return manifest.getMainAttributes();
         } catch (IOException var5) {
            throw new UncheckedIOException(var5);
         }
      }
   }

   private static String readAttribute(@Nullable Attributes attributes, String name, Supplier<String> defaultAction) {
      if (attributes == null) {
         return defaultAction.get();
      } else {
         String value = attributes.getValue(name);
         return value != null ? value : defaultAction.get();
      }
   }

   private WorldEditManifest(String worldEditVersion, WorldEditManifest.Kind worldEditKind) {
      this.worldEditVersion = worldEditVersion;
      this.worldEditKind = worldEditKind;
   }

   public String getWorldEditVersion() {
      return this.worldEditVersion;
   }

   public WorldEditManifest.Kind getWorldEditKind() {
      return this.worldEditKind;
   }

   public static enum Kind {
      MOD("mods"),
      PLUGIN("plugins"),
      UNKNOWN("mods/plugins");

      public final String folderName;

      private Kind(String folderName) {
         this.folderName = folderName;
      }
   }
}
