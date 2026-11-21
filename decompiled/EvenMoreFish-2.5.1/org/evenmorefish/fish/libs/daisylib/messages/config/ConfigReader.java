package org.evenmorefish.fish.libs.daisylib.messages.config;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ConfigReader<T> {
   @Nullable
   Object getObject(String var1);

   @Nullable
   String getString(String var1);

   @NonNull
   List<String> getStringList(String var1);

   @NonNull
   T getConfig();

   @Nullable
   ConfigReader<T> getSection(@NonNull String var1);
}
