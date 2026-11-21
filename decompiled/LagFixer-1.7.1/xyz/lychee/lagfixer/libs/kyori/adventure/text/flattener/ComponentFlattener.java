package xyz.lychee.lagfixer.libs.kyori.adventure.text.flattener;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import xyz.lychee.lagfixer.libs.kyori.adventure.builder.AbstractBuilder;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.Buildable;

@ApiStatus.NonExtendable
public interface ComponentFlattener extends Buildable<ComponentFlattener, ComponentFlattener.Builder> {
   int NO_NESTING_LIMIT = -1;

   @NotNull
   static ComponentFlattener.Builder builder() {
      return new ComponentFlattenerImpl.BuilderImpl();
   }

   @NotNull
   static ComponentFlattener basic() {
      return ComponentFlattenerImpl.BASIC;
   }

   @NotNull
   static ComponentFlattener textOnly() {
      return ComponentFlattenerImpl.TEXT_ONLY;
   }

   void flatten(@NotNull final Component input, @NotNull final FlattenerListener listener);

   @ApiStatus.NonExtendable
   public interface Builder extends AbstractBuilder<ComponentFlattener>, Buildable.Builder<ComponentFlattener> {
      @NotNull
      <T extends Component> ComponentFlattener.Builder mapper(@NotNull final Class<T> type, @NotNull final Function<T, String> converter);

      @NotNull
      <T extends Component> ComponentFlattener.Builder complexMapper(@NotNull final Class<T> type, @NotNull final BiConsumer<T, Consumer<Component>> converter);

      @NotNull
      ComponentFlattener.Builder unknownMapper(@Nullable final Function<Component, String> converter);

      @NotNull
      ComponentFlattener.Builder nestingLimit(@Range(from = 1L,to = 2147483647L) final int limit);
   }
}
