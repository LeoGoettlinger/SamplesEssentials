package xyz.lychee.lagfixer.libs.kyori.option;

import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.option.value.ValueSource;

@ApiStatus.NonExtendable
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

   @ApiStatus.NonExtendable
   public interface Builder {
      <V> OptionState.Builder value(final Option<V> option, @Nullable final V value);

      OptionState.Builder values(final OptionState existing);

      OptionState.Builder values(final ValueSource source);

      OptionState build();
   }

   @ApiStatus.NonExtendable
   public interface Versioned extends OptionState {
      Map<Integer, OptionState> childStates();

      OptionState.Versioned at(final int version);
   }

   @ApiStatus.NonExtendable
   public interface VersionedBuilder {
      OptionState.VersionedBuilder version(final int version, final Consumer<OptionState.Builder> versionBuilder);

      OptionState.Versioned build();
   }
}
