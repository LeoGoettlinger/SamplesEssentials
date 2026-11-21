package ch.njol.skript.lang.util.common;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

@FunctionalInterface
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public interface AnyNamed extends AnyProvider {
   @UnknownNullability
   String name();

   @UnknownNullability
   default Component nameComponent() {
      String name = this.name();
      return name == null ? null : Component.text(name);
   }

   default boolean supportsNameChange() {
      return false;
   }

   default void setName(String name) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
   }

   default void setName(Component name) throws UnsupportedOperationException {
      this.setName(TextComponentParser.instance().toString(name));
   }
}
