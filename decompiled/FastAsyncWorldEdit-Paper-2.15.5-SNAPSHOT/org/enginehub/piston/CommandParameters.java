package org.enginehub.piston;

import javax.annotation.Nullable;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.CommandPart;

public interface CommandParameters extends InjectedValueAccess {
   boolean has(CommandPart var1);

   CommandValue valueOf(ArgAcceptingCommandPart var1);

   @Nullable
   CommandMetadata getMetadata();

   ArgumentConverterAccess getConverters();
}
