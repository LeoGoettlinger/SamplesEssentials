package net.luckperms.api.platform;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface PluginMetadata {
   @NonNull
   String getVersion();

   @NonNull
   String getApiVersion();
}
