package su.nightexpress.nightcore.bridge.key;

import java.util.Optional;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import su.nightexpress.nightcore.bridge.BukkitKeys;
import su.nightexpress.nightcore.bridge.key.exception.InvalidKeyException;
import su.nightexpress.nightcore.util.LowerCase;

@NullMarked
public final class KeyDomain {
   private final String namespace;
   private final String basePath;

   private KeyDomain(String namespace, String basePath) {
      this.namespace = namespace;
      this.basePath = basePath;
   }

   public static KeyDomain of(String namespace) {
      return of(namespace, "");
   }

   public static KeyDomain of(String namespace, String basePath) {
      if (!BukkitKeys.isValidNamespace(namespace)) {
         throw new IllegalArgumentException("Invalid namespace format: " + namespace);
      } else if (!basePath.isEmpty() && !BukkitKeys.isValidValue(basePath)) {
         throw new IllegalArgumentException("Invalid base path format: " + basePath);
      } else {
         return new KeyDomain(namespace, basePath);
      }
   }

   public static KeyDomain of(Plugin plugin) {
      return of(plugin, "");
   }

   public static KeyDomain of(Plugin plugin, String basePath) {
      String namespace = LowerCase.internal(plugin.getName());
      String safePath = LowerCase.internal(basePath);
      return of(namespace, safePath);
   }

   public Optional<AdaptedKey> resolve(String input) {
      if (input.isBlank()) {
         return Optional.empty();
      } else if (input.contains(":")) {
         return BukkitKeys.parse(input);
      } else {
         String safeInput = LowerCase.internal(input);
         return this.basePath.isEmpty() || !safeInput.equals(this.basePath) && !safeInput.startsWith(this.basePath + "/")
            ? this.makeSafe(input)
            : BukkitKeys.parse(this.namespace, safeInput);
      }
   }

   private String buildPath(String... paths) {
      return this.buildPath(this.basePath, paths);
   }

   private String buildPath(String basePath, String... paths) {
      if (paths.length == 0) {
         return basePath;
      } else {
         String joined = LowerCase.internal(String.join("/", paths));
         return basePath.isEmpty() ? joined : basePath + "/" + joined;
      }
   }

   private String[] combineKeyAndPath(AdaptedKey prefixKey, String... paths) {
      String[] combined = new String[paths.length + 2];
      combined[0] = prefixKey.namespace();
      combined[1] = prefixKey.value();
      if (paths.length > 0) {
         System.arraycopy(paths, 0, combined, 2, paths.length);
      }

      return combined;
   }

   public AdaptedKey make(String... paths) throws InvalidKeyException {
      return BukkitKeys.create(this.namespace, this.buildPath(paths));
   }

   public Optional<AdaptedKey> makeSafe(String... paths) {
      return BukkitKeys.parse(this.namespace, this.buildPath(paths));
   }

   public AdaptedKey combine(AdaptedKey prefixKey, String... paths) throws InvalidKeyException {
      return this.make(this.combineKeyAndPath(prefixKey, paths));
   }

   public Optional<AdaptedKey> combineSafe(AdaptedKey prefixKey, String... paths) {
      return this.makeSafe(this.combineKeyAndPath(prefixKey, paths));
   }

   public String getNamespace() {
      return this.namespace;
   }

   public String getBasePath() {
      return this.basePath;
   }
}
