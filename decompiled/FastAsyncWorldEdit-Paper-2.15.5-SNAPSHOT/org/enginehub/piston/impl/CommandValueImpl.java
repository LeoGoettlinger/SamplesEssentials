package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.exception.ConversionFailedException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandPart;

@AutoValue
abstract class CommandValueImpl implements CommandValue {
   static CommandValueImpl.Builder builder() {
      return new $AutoValue_CommandValueImpl.Builder();
   }

   abstract ArgumentConverterAccess manager();

   abstract Supplier<CommandParseResult> commandContextSupplier();

   @Memoized
   CommandParseResult commandContext() {
      return this.commandContextSupplier().get();
   }

   abstract CommandPart partContext();

   abstract InjectedValueAccess injectedValues();

   abstract ImmutableList<String> values();

   @Override
   public ImmutableList<String> asStrings() {
      return this.values();
   }

   @Override
   public <T> ImmutableList<T> asMultiple(Key<T> key) {
      com.google.common.collect.ImmutableList.Builder<T> values = ImmutableList.builder();
      UnmodifiableIterator var3 = this.values().iterator();

      while (var3.hasNext()) {
         String value = (String)var3.next();
         if (!value.trim().isEmpty()) {
            Optional<ArgumentConverter<T>> converter = this.manager().getConverter(key);
            Preconditions.checkState(converter.isPresent(), "No converter for %s", key);
            ConversionResult<T> convert = converter.get().convert(value, this.injectedValues());
            if (!convert.isSuccessful()) {
               throw new ConversionFailedException(
                  this.commandContext(), this.partContext().getTextRepresentation(), converter.get(), (FailedConversion<?>)convert
               );
            }

            values.addAll(convert.get());
         }
      }

      return values.build();
   }

   @com.google.auto.value.AutoValue.Builder
   interface Builder {
      CommandValueImpl.Builder manager(ArgumentConverterAccess var1);

      CommandValueImpl.Builder commandContextSupplier(Supplier<CommandParseResult> var1);

      CommandValueImpl.Builder partContext(CommandPart var1);

      CommandValueImpl.Builder injectedValues(InjectedValueAccess var1);

      default CommandValueImpl.Builder value(String value) {
         return this.values(ImmutableList.of(value));
      }

      CommandValueImpl.Builder values(Collection<String> var1);

      CommandValueImpl build();
   }
}
