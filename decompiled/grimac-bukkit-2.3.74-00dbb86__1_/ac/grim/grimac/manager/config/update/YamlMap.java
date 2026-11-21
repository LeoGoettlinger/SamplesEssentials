package ac.grim.grimac.manager.config.update;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;

public interface YamlMap {
   @Nullable
   Object get(@NotNull String var1);

   @Nullable
   String getString(@NotNull String var1);

   @Nullable
   Integer getInt(@NotNull String var1);

   @Nullable
   Long getLong(@NotNull String var1);

   @Nullable
   Double getDouble(@NotNull String var1);

   @Nullable
   Boolean getBool(@NotNull String var1);

   @Nullable
   List<?> getList(@NotNull String var1);

   @Nullable
   Map<String, Object> getMap(@NotNull String var1);

   boolean has(@NotNull String var1);

   @NotNull
   YamlMap put(@NotNull String var1, @NotNull Object var2);

   @NotNull
   YamlMap remove(@NotNull String var1);

   @NotNull
   YamlMap rename(@NotNull String var1, @NotNull String var2);
}
