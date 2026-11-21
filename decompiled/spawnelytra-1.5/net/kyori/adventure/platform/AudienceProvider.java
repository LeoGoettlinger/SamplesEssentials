package net.kyori.adventure.platform;

import java.util.UUID;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import org.jetbrains.annotations.NotNull;

public interface AudienceProvider extends AutoCloseable {
   @NotNull
   Audience all();

   @NotNull
   Audience console();

   @NotNull
   Audience players();

   @NotNull
   Audience player(@NotNull final UUID playerId);

   @NotNull
   default Audience permission(@NotNull final Key permission) {
      return this.permission(permission.namespace() + '.' + permission.value());
   }

   @NotNull
   Audience permission(@NotNull final String permission);

   @NotNull
   Audience world(@NotNull final Key world);

   @NotNull
   Audience server(@NotNull final String serverName);

   @NotNull
   ComponentFlattener flattener();

   @Override
   void close();

   public interface Builder<P extends AudienceProvider, B extends AudienceProvider.Builder<P, B>> {
      @NotNull
      B componentRenderer(@NotNull final ComponentRenderer<Pointered> componentRenderer);

      @NotNull
      B partition(@NotNull final Function<Pointered, ?> partitionFunction);

      @NotNull
      default <T> B componentRenderer(@NotNull final Function<Pointered, T> partition, @NotNull final ComponentRenderer<T> componentRenderer) {
         return this.partition(partition).componentRenderer(componentRenderer.mapContext(partition));
      }

      @NotNull
      P build();
   }
}
