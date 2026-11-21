package su.nightexpress.nightcore.bridge.key;

import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import su.nightexpress.nightcore.bridge.BukkitKeys;
import su.nightexpress.nightcore.util.LowerCase;

@NullMarked
public interface AdaptedKey {
   String NAMESPACE_SEPARATOR = ":";
   String PATH_SEPARATOR = "/";
   String MINECRAFT_NAMESPACE = "minecraft";

   default String buildChildValue(String... paths) {
      if (paths.length == 0) {
         return this.value();
      } else {
         String joined = LowerCase.internal(String.join("/", paths));
         return this.value() + "/" + joined;
      }
   }

   default AdaptedKey child(String... paths) {
      return BukkitKeys.create(this.namespace(), this.buildChildValue(paths));
   }

   default Optional<AdaptedKey> safeChild(String... paths) {
      return BukkitKeys.parse(this.namespace(), this.buildChildValue(paths));
   }

   NamespacedKey bukkit();

   String namespace();

   String value();

   String asString();

   String asMinimalString();
}
