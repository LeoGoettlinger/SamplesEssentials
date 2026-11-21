package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandPart;

@AutoValue
abstract class CommandParametersImpl implements CommandParameters {
   static CommandParametersImpl.Builder builder() {
      return new AutoValue_CommandParametersImpl.Builder();
   }

   abstract ImmutableSet<CommandPart> presentParts();

   abstract ImmutableMap<CommandPart, CommandValue> values();

   abstract InjectedValueAccess injectedValues();

   abstract CommandMetadata metadata();

   abstract ArgumentConverterAccess converters();

   @Override
   public final boolean has(CommandPart part) {
      return this.presentParts().contains(part);
   }

   @Override
   public final CommandValue valueOf(ArgAcceptingCommandPart part) {
      CommandValue value = (CommandValue)this.values().get(part);
      if (value == null) {
         throw new NoSuchElementException("No value for " + part);
      } else {
         return value;
      }
   }

   @Override
   public CommandMetadata getMetadata() {
      return this.metadata();
   }

   @Override
   public ArgumentConverterAccess getConverters() {
      return this.converters();
   }

   @Override
   public final <T> Optional<T> injectedValue(Key<T> key, InjectedValueAccess context) {
      return this.injectedValues().injectedValue(key, context);
   }

   @com.google.auto.value.AutoValue.Builder
   interface Builder {
      default CommandParametersImpl.Builder addPresentPart(CommandPart part) {
         this.presentPartsBuilder().add(part);
         return this;
      }

      com.google.common.collect.ImmutableSet.Builder<CommandPart> presentPartsBuilder();

      default CommandParametersImpl.Builder addValue(CommandPart part, CommandValue value) {
         this.valuesBuilder().put(part, value);
         return this;
      }

      com.google.common.collect.ImmutableMap.Builder<CommandPart, CommandValue> valuesBuilder();

      CommandParametersImpl.Builder injectedValues(InjectedValueAccess var1);

      CommandParametersImpl.Builder metadata(CommandMetadata var1);

      CommandParametersImpl.Builder converters(ArgumentConverterAccess var1);

      CommandParametersImpl build();
   }
}
