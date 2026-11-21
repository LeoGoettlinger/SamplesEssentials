package com.viaversion.viaversion.util;

abstract sealed class MinecraftKey extends Key permits MinecraftKey.CompactMinecraftKey, MinecraftKey.ColonPrefixedMinecraftKey, MinecraftKey.FullMinecraftKey {
   static final String MINECRAFT_NAMESPACE = "minecraft";
   static final String MINECRAFT_NAMESPACE_WITH_COLON = "minecraft:";
   static final int MINECRAFT_NAMESPACE_LENGTH = "minecraft".length();
   private final String path;

   protected MinecraftKey(String path) {
      if (!isValidPath(path)) {
         throw new IllegalArgumentException("Invalid path: " + path);
      } else {
         this.path = path;
      }
   }

   @Override
   public String namespace() {
      return "minecraft";
   }

   @Override
   public String path() {
      return this.path;
   }

   @Override
   public String minimized() {
      return this.path;
   }

   @Override
   public boolean hasMinecraftNamespace() {
      return true;
   }

   @Override
   public final boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof MinecraftKey key ? this.path.equals(key.path) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.path.hashCode();
   }

   static boolean isValidNamespace(String namespace) {
      int i = 0;

      for (int length = namespace.length(); i < length; i++) {
         char c = namespace.charAt(i);
         if ((c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_' && c != '-' && c != '.') {
            return false;
         }
      }

      return true;
   }

   static boolean isValidPath(String path) {
      int i = 0;

      for (int length = path.length(); i < length; i++) {
         char c = path.charAt(i);
         if ((char)(c - 'a') > 25 && (char)(c - '-') > '\f' && c != '_') {
            return false;
         }
      }

      return true;
   }

   static final class ColonPrefixedMinecraftKey extends MinecraftKey {
      private final String original;
      private String identifier;

      ColonPrefixedMinecraftKey(String original, String path) {
         super(path);
         this.original = original;
      }

      @Override
      public String original() {
         return this.original;
      }

      @Override
      public String toString() {
         if (this.identifier == null) {
            this.identifier = "minecraft:" + this.path();
         }

         return this.identifier;
      }
   }

   static final class CompactMinecraftKey extends MinecraftKey {
      private String identifier;

      CompactMinecraftKey(String path) {
         super(path);
      }

      @Override
      public String original() {
         return this.path();
      }

      @Override
      public String toString() {
         if (this.identifier == null) {
            this.identifier = "minecraft:" + this.path();
         }

         return this.identifier;
      }
   }

   static final class FullMinecraftKey extends MinecraftKey {
      private final String identifier;

      FullMinecraftKey(String identifier, String path) {
         super(path);
         this.identifier = identifier;
      }

      @Override
      public String original() {
         return this.identifier;
      }

      @Override
      public String toString() {
         return this.identifier;
      }
   }
}
