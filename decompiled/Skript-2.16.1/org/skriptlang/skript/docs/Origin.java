package org.skriptlang.skript.docs;

import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.addon.SkriptAddon;

public sealed interface Origin permits OriginImpl.UnknownOrigin, Origin.AddonOrigin {
   Origin UNKNOWN = new OriginImpl.UnknownOrigin();

   @Contract("_ -> new")
   static Origin of(SkriptAddon addon) {
      return new OriginImpl.AddonOriginImpl(addon);
   }

   String name();

   public non-sealed interface AddonOrigin extends Origin {
      SkriptAddon addon();

      @Override
      default String name() {
         return this.addon().name();
      }
   }
}
