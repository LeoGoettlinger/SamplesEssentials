package org.enginehub.piston.impl;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.function.Supplier;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverterAccess;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.CommandPart;

final class AutoValue_CommandValueImpl extends $AutoValue_CommandValueImpl {
   @LazyInit
   private transient volatile CommandParseResult commandContext;

   AutoValue_CommandValueImpl(
      ArgumentConverterAccess manager,
      Supplier<CommandParseResult> commandContextSupplier,
      CommandPart partContext,
      InjectedValueAccess injectedValues,
      ImmutableList<String> values
   ) {
      super(manager, commandContextSupplier, partContext, injectedValues, values);
   }

   @Override
   CommandParseResult commandContext() {
      if (this.commandContext == null) {
         synchronized (this) {
            if (this.commandContext == null) {
               this.commandContext = super.commandContext();
               if (this.commandContext == null) {
                  throw new NullPointerException("commandContext() cannot return null");
               }
            }
         }
      }

      return this.commandContext;
   }
}
