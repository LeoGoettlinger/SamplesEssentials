package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.scripting.LuaEngine;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NPCScriptManager {
   public static final String NAMESPACE = "elitemobs_npc";
   private static EliteMobsNPCScriptProvider provider;

   private NPCScriptManager() {
   }

   public static void initialize() {
      Path scriptDirectory = getScriptDirectory();

      try {
         Files.createDirectories(scriptDirectory);
      } catch (IOException var2) {
         Logger.warn("Failed to initialize NPC Lua script directory " + scriptDirectory + ".");
      }

      provider = new EliteMobsNPCScriptProvider(scriptDirectory);
      LuaEngine.registerScriptProvider(provider);
   }

   public static Path getScriptDirectory() {
      return MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("npc_scripts");
   }

   public static ScriptDefinition getDefinition(String fileName) {
      return LuaEngine.getDefinition("elitemobs_npc", fileName);
   }

   public static void shutdown() {
      if (provider != null) {
         LuaEngine.unregisterScriptProvider("elitemobs_npc");
         provider = null;
      }
   }
}
