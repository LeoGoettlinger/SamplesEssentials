package su.nightexpress.nightcore.language.entry;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.config.FileConfig;

@Deprecated
public interface LangElement {
   void write(@NonNull FileConfig var1);

   void load(@NonNull NightCorePlugin var1);

   void load(@NonNull FileConfig var1);
}
