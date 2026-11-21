package org.skriptlang.skript.common.function;

import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
@Internal
@Experimental
public interface Signature<T> {
   @Nullable
   Class<T> returnType();

   @NotNull
   @UnmodifiableView
   Parameters parameters();

   @Experimental
   Contract contract();

   @Experimental
   void addCall(FunctionReference<?> var1);

   default boolean isSingle() {
      return this.returnType() == null ? false : !this.returnType().isArray();
   }
}
