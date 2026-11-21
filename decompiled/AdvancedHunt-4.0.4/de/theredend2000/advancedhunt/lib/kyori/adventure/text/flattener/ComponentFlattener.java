package de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public sealed interface ComponentFlattener permits ComponentFlattenerImpl {
   int NO_NESTING_LIMIT = -1;

   static ComponentFlattener.Builder builder() {
      return new ComponentFlattenerImpl.BuilderImpl();
   }

   static ComponentFlattener basic() {
      return ComponentFlattenerImpl.BASIC;
   }

   static ComponentFlattener textOnly() {
      return ComponentFlattenerImpl.TEXT_ONLY;
   }

   void flatten(final Component input, final FlattenerListener listener);

   ComponentFlattener.Builder toBuilder();

   public sealed interface Builder extends AbstractBuilder<ComponentFlattener> permits ComponentFlattenerImpl.BuilderImpl {
      <T extends Component> ComponentFlattener.Builder mapper(final Class<T> type, final Function<T, String> converter);

      <T extends Component> ComponentFlattener.Builder complexMapper(final Class<T> type, final BiConsumer<T, Consumer<Component>> converter);

      ComponentFlattener.Builder unknownMapper(@Nullable final Function<Component, String> converter);

      ComponentFlattener.Builder nestingLimit(@Range(from = 1L,to = 2147483647L) final int limit);
   }
}
