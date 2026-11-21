package net.luckperms.api.extension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface ExtensionManager {
   void loadExtension(Extension var1);

   @NonNull
   Extension loadExtension(Path var1) throws IOException;

   @NonNull
   @Unmodifiable
   Collection<Extension> getLoadedExtensions();
}
