package me.clip.placeholderapi.libs.kyori.adventure.pointer;

import java.util.stream.Stream;
import me.clip.placeholderapi.libs.kyori.adventure.key.Key;
import me.clip.placeholderapi.libs.kyori.examination.Examinable;
import me.clip.placeholderapi.libs.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;

public interface Pointer<V> extends Examinable {
   @NotNull
   static <V> Pointer<V> pointer(@NotNull final Class<V> type, @NotNull final Key key) {
      return new PointerImpl<>(type, key);
   }

   @NotNull
   Class<V> type();

   @NotNull
   Key key();

   @NotNull
   @Override
   default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("type", this.type()), ExaminableProperty.of("key", this.key()));
   }
}
