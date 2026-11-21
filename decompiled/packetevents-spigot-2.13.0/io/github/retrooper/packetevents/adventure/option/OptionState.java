package io.github.retrooper.packetevents.adventure.option;

import io.github.retrooper.packetevents.adventure.option.value.ValueSource;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.Nullable;

@NonExtendable
public interface OptionState {
   @Deprecated
   static OptionState emptyOptionState() {
      return OptionSchema.globalSchema().emptyState();
   }

   @Deprecated
   static OptionState.Builder optionState() {
      return OptionSchema.globalSchema().stateBuilder();
   }

   @Deprecated
   static OptionState.VersionedBuilder versionedOptionState() {
      return OptionSchema.globalSchema().versionedStateBuilder();
   }

   OptionSchema schema();

   boolean has(final Option<?> option);

   @Nullable
   <V> V value(final Option<V> option);

   @NonExtendable
   public interface Builder {
      <V> OptionState.Builder value(final Option<V> option, @Nullable final V value);

      OptionState.Builder values(final OptionState existing);

      OptionState.Builder values(final ValueSource source);

      OptionState build();
   }

   @NonExtendable
   public interface Versioned extends OptionState {
      Map<Integer, OptionState> childStates();

      OptionState.Versioned at(final int version);
   }

   @NonExtendable
   public interface VersionedBuilder {
      OptionState.VersionedBuilder version(final int version, final Consumer<OptionState.Builder> versionBuilder);

      OptionState.Versioned build();
   }
}
