package xyz.lychee.lagfixer.libs.kyori.adventure.pointer;

import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

@ApiStatus.NonExtendable
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
