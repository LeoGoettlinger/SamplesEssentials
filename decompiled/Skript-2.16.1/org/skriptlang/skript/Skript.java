package org.skriptlang.skript;

import java.util.Collection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;

public interface Skript extends SkriptAddon {
   @Contract("_, _ -> new")
   static Skript of(Class<?> source, String name) {
      return new SkriptImpl(source, name);
   }

   @Contract("_, _ -> new")
   SkriptAddon registerAddon(Class<?> var1, String var2);

   @Unmodifiable
   Collection<SkriptAddon> addons();

   @Nullable
   default SkriptAddon addon(String name) {
      return this.addons().stream().filter(addon -> addon.name().equals(name)).findFirst().orElse(null);
   }

   @Contract("-> new")
   default Skript unmodifiableView() {
      return new SkriptImpl.UnmodifiableSkript(this, SkriptAddon.super.unmodifiableView());
   }
}
