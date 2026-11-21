package org.enginehub.piston;

import com.google.auto.value.AutoValue;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nullable;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandPart;

@AutoValue
public abstract class NoInputCommandParameters implements CommandParameters {
   public static NoInputCommandParameters.Builder builder() {
      return new AutoValue_NoInputCommandParameters.Builder().injectedValues(InjectedValueAccess.EMPTY).converters(ArgumentConverterAccess.EMPTY);
   }

   NoInputCommandParameters() {
   }

   abstract InjectedValueAccess injectedValues();

   @Nullable
   abstract CommandMetadata metadata();

   abstract ArgumentConverterAccess converters();

   @Override
   public boolean has(CommandPart part) {
      return false;
   }

   @Override
   public CommandValue valueOf(ArgAcceptingCommandPart part) {
      throw new NoSuchElementException();
   }

   @Nullable
   @Override
   public CommandMetadata getMetadata() {
      return this.metadata();
   }

   @Override
   public ArgumentConverterAccess getConverters() {
      return this.converters();
   }

   @Override
   public <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
      return this.injectedValues().injectedValue(key, context);
   }

   @com.google.auto.value.AutoValue.Builder
   public interface Builder {
      NoInputCommandParameters.Builder injectedValues(InjectedValueAccess var1);

      NoInputCommandParameters.Builder metadata(@Nullable CommandMetadata var1);

      NoInputCommandParameters.Builder converters(ArgumentConverterAccess var1);

      NoInputCommandParameters build();
   }
}
