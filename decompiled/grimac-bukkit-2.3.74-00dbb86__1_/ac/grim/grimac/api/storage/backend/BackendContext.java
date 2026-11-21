package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.nio.file.Path;
import java.util.logging.Logger;

@ApiStatus.Experimental
public interface BackendContext {
   BackendConfig config();

   Logger logger();

   Path dataDirectory();
}
