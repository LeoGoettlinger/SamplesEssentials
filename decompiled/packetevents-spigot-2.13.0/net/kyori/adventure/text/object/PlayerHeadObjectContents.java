package net.kyori.adventure.text.object;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.PlatformAPI;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface PlayerHeadObjectContents extends ObjectContents {
   boolean DEFAULT_HAT = true;

   @Nullable
   String name();

   @Nullable
   UUID id();

   @NotNull
   @Unmodifiable
   List<PlayerHeadObjectContents.ProfileProperty> profileProperties();

   boolean hat();

   @Nullable
   Key texture();

   @Contract(
      value = "-> new",
      pure = true
   )
   @NotNull
   PlayerHeadObjectContents.Builder toBuilder();

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static PlayerHeadObjectContents.ProfileProperty property(@NotNull final String name, @NotNull final String value) {
      return new PlayerHeadObjectContentsImpl.ProfilePropertyImpl(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"), null);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static PlayerHeadObjectContents.ProfileProperty property(@NotNull final String name, @NotNull final String value, @Nullable final String signature) {
      return new PlayerHeadObjectContentsImpl.ProfilePropertyImpl(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"), signature);
   }

   @NotNull
   @Override
   default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(
         ExaminableProperty.of("name", this.name()),
         ExaminableProperty.of("id", this.id()),
         ExaminableProperty.of("profileProperties", this.profileProperties()),
         ExaminableProperty.of("hat", this.hat()),
         ExaminableProperty.of("texture", this.texture())
      );
   }

   public interface Builder {
      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder name(@Nullable final String name);

      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder id(@Nullable final UUID id);

      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder profileProperty(@NotNull final PlayerHeadObjectContents.ProfileProperty property);

      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder profileProperties(@NotNull final Collection<PlayerHeadObjectContents.ProfileProperty> properties);

      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder skin(@NotNull final PlayerHeadObjectContents.SkinSource skinSource);

      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder hat(final boolean hat);

      @Contract("_ -> this")
      @NotNull
      PlayerHeadObjectContents.Builder texture(@Nullable final Key texture);

      @Contract(
         value = "-> new",
         pure = true
      )
      @NotNull
      PlayerHeadObjectContents build();
   }

   public interface ProfileProperty extends Examinable {
      @NotNull
      String name();

      @NotNull
      String value();

      @Nullable
      String signature();

      @NotNull
      @Override
      default Stream<? extends ExaminableProperty> examinableProperties() {
         return Stream.of(
            ExaminableProperty.of("name", this.name()), ExaminableProperty.of("value", this.value()), ExaminableProperty.of("signature", this.signature())
         );
      }
   }

   public interface SkinSource {
      @PlatformAPI
      @Internal
      void applySkinToPlayerHeadContents(@NotNull PlayerHeadObjectContents.Builder builder);
   }
}
