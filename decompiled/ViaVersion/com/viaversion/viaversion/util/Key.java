package com.viaversion.viaversion.util;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract sealed class Key permits MinecraftKey, CustomKey {
   public static Key of(String namespace, String path) {
      return (Key)(!namespace.equals("minecraft") && !namespace.isEmpty() ? new CustomKey(namespace + ":" + path, namespace, path) : ofPath(path));
   }

   public static Key ofPath(String path) {
      return new MinecraftKey.CompactMinecraftKey(path);
   }

   public static Key of(String identifier) {
      int separatorIndex = identifier.indexOf(58);
      if (separatorIndex == -1) {
         return ofPath(identifier);
      } else {
         String path = identifier.substring(separatorIndex + 1);
         if (separatorIndex == 0) {
            return new MinecraftKey.ColonPrefixedMinecraftKey(identifier, path);
         } else {
            String namespace = identifier.substring(0, separatorIndex);
            return (Key)(namespace.equals("minecraft") ? new MinecraftKey.FullMinecraftKey(identifier, path) : new CustomKey(identifier, namespace, path));
         }
      }
   }

   @Nullable
   public static Key tryParse(String identifier) {
      try {
         return of(identifier);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public abstract String namespace();

   public abstract String path();

   public abstract String original();

   public abstract String minimized();

   public abstract boolean hasMinecraftNamespace();

   public Key withNamespace(String namespace) {
      return of(namespace, this.path());
   }

   public Key withPath(String path) {
      return of(this.namespace(), path);
   }

   public final boolean equals(String identifier) {
      return this.equals(of(identifier));
   }

   public static String stripNamespace(String identifier) {
      int index = identifier.indexOf(58);
      return index == -1 ? identifier : identifier.substring(index + 1);
   }

   public static String namespace(String identifier) {
      int index = identifier.indexOf(58);
      return index != -1 && index != 0 ? identifier.substring(0, index) : "minecraft";
   }

   public static String stripMinecraftNamespace(String identifier) {
      if (identifier.startsWith("minecraft:")) {
         return identifier.substring(MinecraftKey.MINECRAFT_NAMESPACE_LENGTH + 1);
      } else {
         return !identifier.isEmpty() && identifier.charAt(0) == ':' ? identifier.substring(1) : identifier;
      }
   }

   public static boolean equals(String firstIdentifier, String secondIdentifier) {
      return firstIdentifier != null && secondIdentifier != null && of(firstIdentifier).equals(of(secondIdentifier));
   }

   public static String namespaced(String identifier) {
      int index = identifier.indexOf(58);
      if (index == -1) {
         return "minecraft:" + identifier;
      } else {
         return index == 0 ? "minecraft" + identifier : identifier;
      }
   }

   public static boolean isValid(String identifier) {
      int separatorIndex = identifier.indexOf(58);
      if (separatorIndex == -1) {
         return MinecraftKey.isValidPath(identifier);
      } else if (separatorIndex == 0) {
         return MinecraftKey.isValidPath(identifier.substring(1));
      } else {
         String namespace = identifier.substring(0, separatorIndex);
         String path = identifier.substring(separatorIndex + 1);
         return MinecraftKey.isValidNamespace(namespace) && MinecraftKey.isValidPath(path);
      }
   }
}
