package ch.njol.skript.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.Nullable;

public class Experience implements YggdrasilSerializable {
   private final int xp;

   public Experience() {
      this.xp = -1;
   }

   public Experience(int xp) {
      this.xp = xp;
   }

   public int getXP() {
      return this.xp == -1 ? 1 : this.xp;
   }

   public int getInternalXP() {
      return this.xp;
   }

   @Override
   public String toString() {
      return this.xp == -1 ? "xp" : this.xp + " xp";
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + this.xp;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return !(obj instanceof Experience other) ? false : this.xp == other.xp;
      }
   }
}
