package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public interface Debuggable {
   String toString(@Nullable Event var1, boolean var2);

   @Override
   String toString();
}
