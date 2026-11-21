package com.fastasyncworldedit.serverlib.hybrids;

import com.fastasyncworldedit.serverlib.CheckType;
import com.fastasyncworldedit.serverlib.ClassPresenceCheck;

public final class Forge extends ClassPresenceCheck {
   private static final String MINECRAFT_FORGE = "net.fabricmc.loader.launch.knot.KnotServer";

   public Forge() {
      super(CheckType.MODDED_HYBRID, "net.fabricmc.loader.launch.knot.KnotServer");
   }

   @Override
   public String[] explain() {
      return new String[]{
         "You are running a server that does not properly support Bukkit plugins.",
         "",
         "Bukkit plugins should not be used with Forge/Fabric mods!",
         "For Forge: Consider using ForgeEssentials, or SpongeForge + Nucleus.",
         "",
         "By continuing to use Forge you acknowledge you are running an unsupported server version."
      };
   }
}
