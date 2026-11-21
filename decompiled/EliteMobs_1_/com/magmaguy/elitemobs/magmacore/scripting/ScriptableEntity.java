package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class ScriptableEntity {
   private final HashMap<String, Long> globalCooldowns = new HashMap<>();
   private final HashMap<String, Long> localCooldowns = new HashMap<>();

   public boolean handleScriptError(String context, Exception failure) {
      return false;
   }

   public abstract LuaTable buildContextTable(ScriptInstance var1);

   public abstract String getContextKey();

   public abstract Set<ScriptHook> getSupportedHooks();

   public abstract Entity getBukkitEntity();

   public abstract Location getLocation();

   public boolean isScriptOwnerActive() {
      Entity entity = this.getBukkitEntity();
      return entity != null && !entity.isDead();
   }

   public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
      return LuaValue.NIL;
   }

   public Map<String, Long> getGlobalCooldownStore() {
      return this.globalCooldowns;
   }

   public Map<String, Long> getLocalCooldownStore(ScriptDefinition definition) {
      return this.localCooldowns;
   }

   public void onShutdown() {
   }
}
