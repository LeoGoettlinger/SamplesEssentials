package org.skriptlang.skript.addon;

import java.util.SequencedCollection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public interface AddonModule {
   static AddonModule.ModuleOrigin origin(SkriptAddon addon, AddonModule module) {
      return new AddonModuleImpl.ModuleOriginImpl(addon, module);
   }

   static AddonModule.ModuleOrigin origin(SkriptAddon addon, AddonModule... modules) {
      return new AddonModuleImpl.ModuleOriginImpl(addon, modules);
   }

   default boolean canLoad(SkriptAddon addon) {
      return true;
   }

   default void init(SkriptAddon addon) {
   }

   void load(SkriptAddon var1);

   String name();

   default Origin origin(SkriptAddon addon) {
      return origin(addon, this);
   }

   default SyntaxRegistry moduleRegistry(SkriptAddon addon) {
      return SyntaxRegistry.withOrigin(addon.syntaxRegistry(), this.origin(addon));
   }

   default void register(SkriptAddon addon, AddonModule.Registrar... registrationMethods) {
      SyntaxRegistry registry = this.moduleRegistry(addon);

      for (AddonModule.Registrar func : registrationMethods) {
         func.register(registry);
      }
   }

   public sealed interface ModuleOrigin extends Origin.AddonOrigin permits AddonModuleImpl.ModuleOriginImpl {
      @Unmodifiable
      SequencedCollection<AddonModule> modules();

      @Deprecated(
         since = "2.15",
         forRemoval = true
      )
      default String moduleName() {
         return this.modules().stream().map(AddonModule::name).collect(Collectors.joining(", "));
      }
   }

   @FunctionalInterface
   public interface Registrar {
      void register(SyntaxRegistry var1);
   }
}
