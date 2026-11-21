package dev.demora.demoralegendaryweapons.version;

import dev.demora.demoralegendaryweapons.util.MinecraftVersion;

public interface VersionModule {
   String name();

   boolean appliesTo(MinecraftVersion version);

   void enable();
}
