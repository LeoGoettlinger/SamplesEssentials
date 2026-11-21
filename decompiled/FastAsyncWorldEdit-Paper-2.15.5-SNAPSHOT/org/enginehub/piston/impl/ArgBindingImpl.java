package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.part.CommandPart;

@AutoValue
abstract class ArgBindingImpl implements ArgBinding {
   static ArgBindingImpl.Builder builder() {
      return new AutoValue_ArgBindingImpl.Builder();
   }

   @Override
   public abstract String getInput();

   abstract ImmutableMap<CommandPart, Boolean> getPartsMap();

   @Override
   public boolean isExactMatch(CommandPart part) {
      Boolean result = (Boolean)this.getPartsMap().get(part);
      if (result == null) {
         throw new IllegalArgumentException("Part " + part + " is not in the parts map");
      } else {
         return result;
      }
   }

   @Override
   public final ImmutableSet<CommandPart> getParts() {
      return this.getPartsMap().keySet();
   }

   @com.google.auto.value.AutoValue.Builder
   interface Builder {
      ArgBindingImpl.Builder input(String var1);

      ArgBindingImpl.Builder partsMap(Map<CommandPart, Boolean> var1);

      ArgBindingImpl build();
   }
}
