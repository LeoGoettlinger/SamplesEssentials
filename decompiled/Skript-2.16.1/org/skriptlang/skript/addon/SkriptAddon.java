package org.skriptlang.skript.addon;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

public interface SkriptAddon extends ViewProvider<SkriptAddon> {
   Class<?> source();

   String name();

   <R extends Registry<?>> void storeRegistry(Class<R> var1, R var2);

   void removeRegistry(Class<? extends Registry<?>> var1);

   boolean hasRegistry(Class<? extends Registry<?>> var1);

   <R extends Registry<?>> R registry(Class<R> var1);

   <R extends Registry<?>> R registry(Class<R> var1, Supplier<R> var2);

   SyntaxRegistry syntaxRegistry();

   Localizer localizer();

   default void loadModules(AddonModule... modules) {
      List<AddonModule> filtered = Arrays.stream(modules).filter(addonModule -> addonModule.canLoad(this)).toList();

      for (AddonModule module : filtered) {
         module.init(this);
      }

      for (AddonModule module : filtered) {
         module.load(this);
      }
   }

   @Contract("-> new")
   default SkriptAddon unmodifiableView() {
      return new SkriptAddonImpl.UnmodifiableAddon(this);
   }
}
