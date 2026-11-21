package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptInstance;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptableEntity;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ScriptableNPC extends ScriptableEntity {
   public static final ScriptHook ON_REMOVE = new ScriptHook("on_remove");
   public static final ScriptHook ON_INTERACT = new ScriptHook("on_npc_interact");
   public static final ScriptHook ON_PROXIMITY_ENTER = new ScriptHook("on_npc_proximity_enter");
   public static final ScriptHook ON_PROXIMITY_LEAVE = new ScriptHook("on_npc_proximity_leave");
   private static final Set<ScriptHook> SUPPORTED_HOOKS = Set.of(
      ScriptHook.ON_SPAWN,
      ScriptHook.ON_TICK,
      ScriptHook.ON_ZONE_ENTER,
      ScriptHook.ON_ZONE_LEAVE,
      ON_REMOVE,
      ON_INTERACT,
      ON_PROXIMITY_ENTER,
      ON_PROXIMITY_LEAVE
   );
   private final NPCEntity npcEntity;

   public ScriptableNPC(NPCEntity npcEntity) {
      this.npcEntity = npcEntity;
   }

   public NPCEntity getNpcEntity() {
      return this.npcEntity;
   }

   @Override
   public LuaTable buildContextTable(ScriptInstance instance) {
      return LuaNPCTable.build(this.npcEntity, instance);
   }

   @Override
   public String getContextKey() {
      return "npc";
   }

   @Override
   public Set<ScriptHook> getSupportedHooks() {
      return SUPPORTED_HOOKS;
   }

   @Override
   public Entity getBukkitEntity() {
      return this.npcEntity.getVillager();
   }

   @Override
   public Location getLocation() {
      return this.npcEntity.getVillager() != null && this.npcEntity.getVillager().isValid()
         ? this.npcEntity.getVillager().getLocation()
         : this.npcEntity.getSpawnLocation();
   }

   @Override
   public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
      if ("player".equals(key)) {
         LivingEntity actor = instance.getCurrentEventActor();
         if (actor != null) {
            return LuaLivingEntityTable.build(actor);
         }
      }

      return LuaValue.NIL;
   }
}
