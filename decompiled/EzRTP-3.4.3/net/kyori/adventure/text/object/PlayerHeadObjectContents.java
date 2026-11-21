package net.kyori.adventure.text.object;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.PlatformAPI;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

public sealed interface PlayerHeadObjectContents extends ObjectContents permits PlayerHeadObjectContentsImpl {
   boolean DEFAULT_HAT = true;
   @RegExp
   String NAME_REGEX = "^[!-~]{0,16}$";

   static boolean isValidName(final String name) {
      return name.length() > 16 ? false : name.chars().filter(c -> c <= 32 || c >= 126).findAny().isEmpty();
   }

   @Nullable
   String name();

   @Nullable
   UUID id();

   @Unmodifiable
   List<PlayerHeadObjectContents.ProfileProperty> profileProperties();

   boolean hat();

   @Nullable
   Key texture();

   @Contract(
      value = "-> new",
      pure = true
   )
   PlayerHeadObjectContents.Builder toBuilder();

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static PlayerHeadObjectContents.ProfileProperty property(final String name, final String value) {
      return new PlayerHeadObjectContentsImpl.ProfilePropertyImpl(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"), null);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static PlayerHeadObjectContents.ProfileProperty property(final String name, final String value, @Nullable final String signature) {
      return new PlayerHeadObjectContentsImpl.ProfilePropertyImpl(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"), signature);
   }

   public sealed interface Builder permits PlayerHeadObjectContentsImpl.BuilderImpl {
      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder name(@Nullable final String name);

      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder id(@Nullable final UUID id);

      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder profileProperty(final PlayerHeadObjectContents.ProfileProperty property);

      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder profileProperties(final Collection<? extends PlayerHeadObjectContents.ProfileProperty> properties);

      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder skin(final PlayerHeadObjectContents.SkinSource skinSource);

      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder hat(final boolean hat);

      @Contract("_ -> this")
      PlayerHeadObjectContents.Builder texture(@Nullable final Key texture);

      @Contract(
         value = "-> new",
         pure = true
      )
      PlayerHeadObjectContents build();
   }

   public interface ProfileProperty {
      String name();

      String value();

      @Nullable
      String signature();
   }

   public interface SkinSource extends ObjectContentsLike {
      @PlatformAPI
      @Internal
      void applySkinToPlayerHeadContents(PlayerHeadObjectContents.Builder builder);

      @Override
      default ObjectContents asObjectContents() {
         return ObjectContents.playerHead(this);
      }
   }
}
