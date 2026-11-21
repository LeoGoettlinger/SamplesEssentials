package me.clip.placeholderapi.expansion.manager;

import java.util.Arrays;
import java.util.Objects;

public final class MethodSignature {
   private final String name;
   private final Class<?>[] params;

   protected MethodSignature(String name, Class<?>[] params) {
      this.name = name;
      this.params = params;
   }

   public String getName() {
      return this.name;
   }

   public Class<?>[] getParams() {
      return this.params;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MethodSignature that = (MethodSignature)o;
         return Objects.equals(this.name, that.name) && Arrays.equals((Object[])this.params, (Object[])that.params);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(this.name);
      return 31 * result + Arrays.hashCode((Object[])this.params);
   }
}
