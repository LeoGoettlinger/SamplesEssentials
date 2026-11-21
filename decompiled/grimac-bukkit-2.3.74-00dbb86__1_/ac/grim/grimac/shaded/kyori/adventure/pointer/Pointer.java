package ac.grim.grimac.shaded.kyori.adventure.pointer;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.key.Key;
import ac.grim.grimac.shaded.kyori.examination.Examinable;
import ac.grim.grimac.shaded.kyori.examination.ExaminableProperty;
import java.util.stream.Stream;

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
