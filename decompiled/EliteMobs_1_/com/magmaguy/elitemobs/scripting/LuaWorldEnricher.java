package com.magmaguy.elitemobs.scripting;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaWorldTable;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import org.bukkit.Location;

public final class LuaWorldEnricher {
   private static boolean registered;

   private LuaWorldEnricher() {
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;
         LuaWorldTable.registerEnricher(
            (table, world) -> {
               table.set(
                  "lightning_effect_at_location",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        Location location = LuaTableSupport.tableToLocation(args.checktable(1), world);
                        if (location != null
                           && location.getWorld() != null
                           && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                           LightningSpawnBypass.strikeLightningIgnoreProtections(location);
                        }

                        return LuaValue.NIL;
                     }
                  )
               );
               table.set(
                  "spawn_npc_at_location",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        Location location = LuaTableSupport.tableToLocation(args.checktable(2), world);
                        if (location != null
                           && location.getWorld() != null
                           && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                           NPCEntity npc = new NPCEntity(location, args.checkjstring(1));
                           if (!npc.isValid()) {
                              npc.remove(RemovalReason.OTHER);
                              return LuaValue.NIL;
                           } else {
                              LuaTable handle = new LuaTable();
                              handle.set("remove", LuaTableSupport.tableMethod(handle, ignored -> {
                                 npc.remove(RemovalReason.OTHER);
                                 return LuaValue.NIL;
                              }));
                              return handle;
                           }
                        } else {
                           return LuaValue.NIL;
                        }
                     }
                  )
               );
               table.set(
                  "spawn_boss_at_location",
                  LuaTableSupport.tableMethod(
                     table,
                     args -> {
                        Location location = LuaTableSupport.tableToLocation(args.checktable(2), world);
                        int level = args.checkint(3);
                        if (location != null
                           && location.getWorld() != null
                           && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                           CustomBossEntity boss = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
                           if (boss == null) {
                              return LuaValue.NIL;
                           } else {
                              boss.spawn(location, level, false);
                              return (Varargs)(boss.getLivingEntity() != null && boss.getLivingEntity().isValid()
                                 ? LuaLivingEntityTable.build(boss.getLivingEntity())
                                 : LuaValue.NIL);
                           }
                        } else {
                           return LuaValue.NIL;
                        }
                     }
                  )
               );
            }
         );
      }
   }
}
