package ch.njol.skript.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.Nullable;

public class GameruleValue<T> implements YggdrasilSerializable {
   private T gameruleValue;

   public GameruleValue(T gameruleValue) {
      this.gameruleValue = gameruleValue;
   }

   public T getGameruleValue() {
      return this.gameruleValue;
   }

   @Override
   public String toString() {
      return this.gameruleValue.toString();
   }

   @Override
   public boolean equals(@Nullable Object other) {
      return !(other instanceof GameruleValue) ? false : this.gameruleValue.equals(((GameruleValue)other).gameruleValue);
   }
}
