package org.enginehub.piston.part;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandValue;
import org.enginehub.piston.inject.Key;

public interface ArgAcceptingCommandPart extends ArgConsumingCommandPart {
   default CommandValue value(CommandParameters parameters) {
      return parameters.valueOf(this);
   }

   ImmutableSet<Key<?>> getTypes();

   ImmutableList<String> getDefaults();
}
