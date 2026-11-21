package ac.grim.grimac.api.storage.registry;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record StoreId(@NotNull String namespace, @NotNull String name) {
   public StoreId(@NotNull String namespace, @NotNull String name) {
      if (namespace == null || namespace.isEmpty()) {
         throw new IllegalArgumentException("namespace");
      } else if (name == null || name.isEmpty()) {
         throw new IllegalArgumentException("name");
      } else if (!isLegal(namespace)) {
         throw new IllegalArgumentException("namespace not [a-z0-9_-]: " + namespace);
      } else if (!isLegal(name)) {
         throw new IllegalArgumentException("name not [a-z0-9_-]: " + name);
      } else {
         this.namespace = namespace;
         this.name = name;
      }
   }

   @NotNull
   public static StoreId grim(@NotNull String name) {
      return new StoreId("grim", name);
   }

   @NotNull
   public String qualified() {
      return this.namespace + ":" + this.name;
   }

   @Override
   public String toString() {
      return this.qualified();
   }

   private static boolean isLegal(String s) {
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
            return false;
         }
      }

      return true;
   }
}
