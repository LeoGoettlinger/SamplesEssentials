package org.skriptlang.skript.addon;

import java.util.List;
import java.util.SequencedCollection;

class AddonModuleImpl {
   public record ModuleOriginImpl(SkriptAddon addon, SequencedCollection<AddonModule> modules) implements AddonModule.ModuleOrigin {
      public ModuleOriginImpl(SkriptAddon addon, AddonModule... modules) {
         this(addon.unmodifiableView(), List.of(modules));
      }
   }
}
