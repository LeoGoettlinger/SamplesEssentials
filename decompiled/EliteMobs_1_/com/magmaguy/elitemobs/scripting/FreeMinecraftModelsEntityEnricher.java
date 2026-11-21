package com.magmaguy.elitemobs.scripting;

import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntity;
import com.magmaguy.freeminecraftmodels.customentity.PropEntity;
import java.util.Map;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

final class FreeMinecraftModelsEntityEnricher {
   private FreeMinecraftModelsEntityEnricher() {
   }

   static void enrich(LuaTable table, Entity entity) {
      Map<Entity, ModeledEntity> loaded = ModeledEntity.getLoadedModeledEntitiesWithUnderlyingEntities();
      ModeledEntity modeledEntity = loaded != null ? loaded.get(entity) : null;
      boolean isModeled = modeledEntity != null;
      table.set("is_modeled", LuaValue.valueOf(isModeled));
      boolean isProp = entity instanceof ArmorStand armorStand && PropEntity.isPropEntity(armorStand);
      table.set("is_prop", LuaValue.valueOf(isProp));
      if (isModeled) {
         LuaTable modelTable = new LuaTable();
         String modelId = modeledEntity.getEntityID();
         modelTable.set("model_id", (LuaValue)(modelId != null ? LuaValue.valueOf(modelId) : LuaValue.NIL));
         modelTable.set("play_animation", LuaTableSupport.tableMethod(modelTable, args -> {
            String animName = args.checkjstring(1);
            boolean blend = args.optboolean(2, false);
            boolean loop = args.optboolean(3, false);
            return LuaValue.valueOf(modeledEntity.playAnimation(animName, blend, loop));
         }));
         modelTable.set("stop_animations", LuaTableSupport.tableMethod(modelTable, args -> {
            modeledEntity.stopCurrentAnimations();
            return LuaValue.NIL;
         }));
         modelTable.set("remove", LuaTableSupport.tableMethod(modelTable, args -> {
            modeledEntity.remove();
            return LuaValue.NIL;
         }));
         modelTable.set("is_dynamic", LuaValue.valueOf(DynamicEntity.isDynamicEntity(entity)));
         table.set("model", modelTable);
      }
   }
}
