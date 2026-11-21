package su.nightexpress.nightcore.manager;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.config.FileConfig;

public interface ConfigBacked extends FileBacked {
   @Deprecated
   @NonNull
   default FileConfig getConfig() {
      return this.loadConfig();
   }

   @NonNull
   default FileConfig loadConfig() {
      return FileConfig.load(this.getPath());
   }
}
