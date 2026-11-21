package su.nightexpress.nightcore.manager;

import java.io.File;
import java.nio.file.Path;
import org.jspecify.annotations.NonNull;

public interface FileBacked {
   @Deprecated
   @NonNull
   default File getFile() {
      return this.getPath().toFile();
   }

   @NonNull
   Path getPath();
}
