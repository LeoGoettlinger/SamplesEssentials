package org.evenmorefish.fish.libs.boostedyaml.settings.updater;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.evenmorefish.fish.libs.boostedyaml.block.Block;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ValueMapper {
   @Nullable
   default Object map(@NotNull Section section, @NotNull Route key) {
      return this.map(section.getBlock(key));
   }

   @Nullable
   default Object map(@NotNull Block<?> block) {
      return this.map(block.getStoredValue());
   }

   @Nullable
   default Object map(@Nullable Object value) {
      return value;
   }

   static ValueMapper section(final BiFunction<Section, Route, Object> mapper) {
      return new ValueMapper() {
         @Override
         public Object map(@NotNull Section section, @NotNull Route key) {
            return mapper.apply(section, key);
         }
      };
   }

   static ValueMapper block(final Function<Block<?>, Object> mapper) {
      return new ValueMapper() {
         @Override
         public Object map(@NotNull Block<?> block) {
            return mapper.apply(block);
         }
      };
   }

   static ValueMapper value(final Function<Object, Object> mapper) {
      return new ValueMapper() {
         @Override
         public Object map(@Nullable Object value) {
            return mapper.apply(value);
         }
      };
   }
}
