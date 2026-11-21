package su.nightexpress.nightcore.bridge.spigot.key;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.key.AdaptedKey;

@NullMarked
public class SpigotKey implements AdaptedKey {
   private final NamespacedKey key;

   public SpigotKey(NamespacedKey key) {
      this.key = key;
   }

   @Override
   public String asMinimalString() {
      return this.namespace().equals("minecraft") ? this.value() : this.asString();
   }

   @Override
   public String asString() {
      return this.key.toString();
   }

   @Override
   public NamespacedKey bukkit() {
      return this.key;
   }

   @Override
   public String namespace() {
      return this.key.getNamespace();
   }

   @Override
   public String value() {
      return this.key.getKey();
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + (this.key == null ? 0 : this.key.hashCode());
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         SpigotKey other = (SpigotKey)obj;
         return this.key.equals(other.key);
      }
   }

   @Override
   public String toString() {
      return this.key.toString();
   }
}
