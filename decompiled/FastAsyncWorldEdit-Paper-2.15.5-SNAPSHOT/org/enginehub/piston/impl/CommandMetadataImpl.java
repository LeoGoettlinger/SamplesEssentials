package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.enginehub.piston.CommandMetadata;

@AutoValue
abstract class CommandMetadataImpl implements CommandMetadata {
   static CommandMetadataImpl.Builder builder() {
      return new AutoValue_CommandMetadataImpl.Builder();
   }

   @Override
   public abstract String getCalledName();

   @Override
   public abstract ImmutableList<String> getArguments();

   @com.google.auto.value.AutoValue.Builder
   interface Builder {
      CommandMetadataImpl.Builder calledName(String var1);

      CommandMetadataImpl.Builder arguments(Collection<String> var1);

      CommandMetadataImpl build();
   }
}
