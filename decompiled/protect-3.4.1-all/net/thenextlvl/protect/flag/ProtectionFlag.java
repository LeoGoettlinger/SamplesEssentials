package net.thenextlvl.protect.flag;

import org.jetbrains.annotations.Contract;

public interface ProtectionFlag<T> extends Flag<T> {
   @Contract(
      pure = true
   )
   T protectedValue();
}
