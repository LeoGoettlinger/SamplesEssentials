package su.nightexpress.nightcore.bridge.key;

import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.LowerCase;

@NullMarked
public final class KeyPathResolver {
   private static final String DEFAULT_FOLDER = "default";
   private final Path targetDir;
   @Nullable
   private final String defaultNamespace;
   @Nullable
   private final String prefixToRemove;

   private KeyPathResolver(Path targetDir, @Nullable String defaultNamespace, @Nullable String baseFolder) {
      this.targetDir = targetDir;
      this.defaultNamespace = defaultNamespace;
      if (baseFolder != null && !baseFolder.isBlank()) {
         this.prefixToRemove = baseFolder + "/";
      } else {
         this.prefixToRemove = null;
      }
   }

   public static KeyPathResolver of(JavaPlugin plugin, Path targetDir, @Nullable String baseFolder) {
      String namespace = LowerCase.internal(plugin.getName());
      return new KeyPathResolver(targetDir, namespace, baseFolder);
   }

   public static KeyPathResolver of(JavaPlugin plugin, Path targetDir) {
      return of(plugin, targetDir, null);
   }

   public static KeyPathResolver of(Path targetDir, KeyDomain domain) {
      return new KeyPathResolver(targetDir, domain.getNamespace(), domain.getBasePath());
   }

   public static KeyPathResolver of(Path targetDir, @Nullable String defaultNamespace, @Nullable String baseFolder) {
      return new KeyPathResolver(targetDir, defaultNamespace, baseFolder);
   }

   public static KeyPathResolver of(Path targetDir, @Nullable String defaultNamespace) {
      return of(targetDir, defaultNamespace, null);
   }

   private Path resolveBase(AdaptedKey key, boolean isFile) {
      String folderName;
      if (this.defaultNamespace != null && key.namespace().equals(this.defaultNamespace)) {
         folderName = "default";
      } else {
         folderName = key.namespace();
      }

      String value = key.value();
      String prefix = this.prefixToRemove;
      if (prefix != null && value.startsWith(prefix)) {
         value = value.substring(prefix.length());
      }

      if (isFile) {
         value = value + ".yml";
      }

      return this.targetDir.resolve(folderName).resolve(value).normalize();
   }

   public Path resolveDirectory(AdaptedKey key) {
      return this.resolveBase(key, false);
   }

   @Deprecated
   public Path resolve(AdaptedKey key) {
      return this.resolveFile(key);
   }

   public Path resolveFile(AdaptedKey key) {
      return this.resolveBase(key, true);
   }
}
