package ac.grim.grimac.manager.config.update;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public interface MigrationContext {
   @NotNull
   YamlMap input();

   @NotNull
   YamlMap output();

   @NotNull
   YamlMap otherFile(@NotNull String var1);
}
