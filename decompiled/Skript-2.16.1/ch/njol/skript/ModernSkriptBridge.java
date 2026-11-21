package ch.njol.skript;

import java.util.Collection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;

final class ModernSkriptBridge {
   private ModernSkriptBridge() {
   }

   public static final class SpecialUnmodifiableSkript implements org.skriptlang.skript.Skript {
      private final org.skriptlang.skript.Skript skript;
      private final org.skriptlang.skript.Skript unmodifiableSkript;

      public SpecialUnmodifiableSkript(org.skriptlang.skript.Skript skript) {
         this.skript = skript;
         this.unmodifiableSkript = skript.unmodifiableView();
      }

      @Override
      public org.skriptlang.skript.addon.SkriptAddon registerAddon(Class<?> source, String name) {
         return this.skript.registerAddon(source, name);
      }

      @Unmodifiable
      @Override
      public Collection<org.skriptlang.skript.addon.SkriptAddon> addons() {
         return this.unmodifiableSkript.addons();
      }

      @Override
      public Class<?> source() {
         return this.unmodifiableSkript.source();
      }

      @Override
      public String name() {
         return this.unmodifiableSkript.name();
      }

      @Override
      public <R extends Registry<?>> void storeRegistry(Class<R> registryClass, R registry) {
         this.unmodifiableSkript.storeRegistry(registryClass, registry);
      }

      @Override
      public void removeRegistry(Class<? extends Registry<?>> registryClass) {
         this.unmodifiableSkript.removeRegistry(registryClass);
      }

      @Override
      public boolean hasRegistry(Class<? extends Registry<?>> registryClass) {
         return this.unmodifiableSkript.hasRegistry(registryClass);
      }

      @Override
      public <R extends Registry<?>> R registry(Class<R> registryClass) {
         return this.unmodifiableSkript.registry(registryClass);
      }

      @Override
      public <R extends Registry<?>> R registry(Class<R> registryClass, Supplier<R> putIfAbsent) {
         return this.unmodifiableSkript.registry(registryClass, putIfAbsent);
      }

      @Override
      public SyntaxRegistry syntaxRegistry() {
         return this.unmodifiableSkript.syntaxRegistry();
      }

      @Override
      public Localizer localizer() {
         return this.unmodifiableSkript.localizer();
      }
   }
}
