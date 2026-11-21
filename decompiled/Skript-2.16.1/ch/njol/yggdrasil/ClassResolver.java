package ch.njol.yggdrasil;

import org.jetbrains.annotations.Nullable;

public interface ClassResolver {
   @Nullable
   Class<?> getClass(String var1);

   @Nullable
   String getID(Class<?> var1);
}
