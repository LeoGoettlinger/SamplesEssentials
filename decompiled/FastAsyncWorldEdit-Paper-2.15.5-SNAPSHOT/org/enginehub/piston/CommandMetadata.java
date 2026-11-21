package org.enginehub.piston;

import com.google.common.collect.ImmutableList;

public interface CommandMetadata {
   String getCalledName();

   ImmutableList<String> getArguments();
}
