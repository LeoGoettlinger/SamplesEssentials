package ac.grim.grimac.internal.storage.verbose;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.api.storage.verbose.VerboseFormatter;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Map;

@ApiStatus.Internal
public interface VerboseRegistry {
   void register(@NotNull String var1, @NotNull VerboseSchema var2);

   void registerFormatter(@NotNull String var1, @NotNull VerboseFormatter var2);

   void registerTemplate(@NotNull String var1, @NotNull String var2, @Nullable String var3, @Nullable String var4, @NotNull Verbose var5);

   default void registerTemplates(@NotNull Runnable registration) {
      registration.run();
   }

   void onChange(@Nullable Runnable var1);

   @NotNull
   String render(@NotNull String var1, @NotNull byte[] var2, @NotNull VerboseRenderContext var3);

   @NotNull
   Map<Integer, Integer> checkIdVersions(@NotNull CheckRegistry var1);

   @Nullable
   VerboseFormatter codeFormatter(int var1, int var2, int var3);

   @Nullable
   VerboseSchema.Layout layout(int var1, int var2, int var3);
}
