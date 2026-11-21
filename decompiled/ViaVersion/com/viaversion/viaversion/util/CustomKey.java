package com.viaversion.viaversion.util;

final class CustomKey extends Key {
   private final String original;
   private final String namespace;
   private final String path;

   CustomKey(String original, String namespace, String path) {
      if (!MinecraftKey.isValidNamespace(namespace)) {
         throw new IllegalArgumentException("Invalid namespace: " + namespace);
      } else if (!MinecraftKey.isValidPath(path)) {
         throw new IllegalArgumentException("Invalid path: " + path);
      } else {
         this.original = original;
         this.namespace = namespace;
         this.path = path;
      }
   }

   @Override
   public String namespace() {
      return this.namespace;
   }

   @Override
   public String path() {
      return this.path;
   }

   @Override
   public String original() {
      return this.original;
   }

   @Override
   public String minimized() {
      return this.original;
   }

   @Override
   public boolean hasMinecraftNamespace() {
      return false;
   }

   @Override
   public final boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof CustomKey key ? this.original.equals(key.original) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.original.hashCode();
   }

   @Override
   public String toString() {
      return this.original;
   }
}
