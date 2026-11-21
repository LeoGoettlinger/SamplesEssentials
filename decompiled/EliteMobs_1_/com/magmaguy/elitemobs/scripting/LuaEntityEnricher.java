package com.magmaguy.elitemobs.scripting;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatEnemyAuthorization;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class LuaEntityEnricher {
   private static boolean registered = false;

   private LuaEntityEnricher() {
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;
         LuaEntityTable.registerEnricher(LuaEntityEnricher::enrich);
      }
   }

   private static void enrich(LuaTable table, Entity entity) {
      addEliteFields(table, entity);
      if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         try {
            FreeMinecraftModelsEntityEnricher.enrich(table, entity);
         } catch (NoClassDefFoundError var3) {
         }
      }
   }

   private static void addEliteFields(LuaTable table, Entity entity) {
      if (entity instanceof Player player) {
         LuaTableSupport.lazyField(table, "elite_player", () -> {
            ElitePlayerInventory inventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());
            if (inventory == null) {
               return LuaValue.NIL;
            } else {
               LuaTable values = new LuaTable();
               values.set("tier", inventory.getFullPlayerTier(false));
               return values;
            }
         });
      }

      table.set(
         "can_receive_hostile_effect",
         LuaTableSupport.tableMethod(
            table,
            args -> LuaValue.valueOf(
               Bukkit.getEntity(UUID.fromString(args.checkjstring(1))) instanceof Player player
                  && LuaEntityTable.isHostileEffectTarget(player, entity)
                  && AdvancedCombatEnemyAuthorization.canTargetWithMagicWeapon(player, (LivingEntity)entity)
            )
         )
      );
      boolean isElite = EntityTracker.isEliteMob(entity);
      table.set("is_elite", LuaValue.valueOf(isElite));
      if (!isElite) {
         table.set("is_custom_boss", LuaValue.FALSE);
         table.set("is_significant_boss", LuaValue.FALSE);
      } else {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
         if (eliteEntity == null) {
            table.set("is_custom_boss", LuaValue.FALSE);
            table.set("is_significant_boss", LuaValue.FALSE);
         } else {
            LuaTable eliteTable = new LuaTable();
            eliteTable.set("level", LuaValue.valueOf(eliteEntity.getLevel()));
            String name = eliteEntity.getName();
            eliteTable.set("name", (LuaValue)(name != null ? LuaValue.valueOf(name) : LuaValue.NIL));
            LuaTableSupport.lazyField(eliteTable, "health", () -> LuaValue.valueOf(eliteEntity.getHealth()));
            LuaTableSupport.lazyField(eliteTable, "max_health", () -> LuaValue.valueOf(eliteEntity.getMaxHealth()));
            boolean isCustomBoss = eliteEntity.isCustomBossEntity();
            eliteTable.set("is_custom_boss", LuaValue.valueOf(isCustomBoss));
            table.set("is_custom_boss", LuaValue.valueOf(isCustomBoss));
            double healthMultiplier = eliteEntity.getHealthMultiplier();
            double damageMultiplier = eliteEntity.getDamageMultiplier();
            eliteTable.set("health_multiplier", LuaValue.valueOf(healthMultiplier));
            eliteTable.set("damage_multiplier", LuaValue.valueOf(damageMultiplier));
            table.set("is_significant_boss", LuaValue.valueOf(isCustomBoss && healthMultiplier > 1.0));
            eliteTable.set("remove", LuaTableSupport.tableMethod(eliteTable, args -> {
               eliteEntity.remove(RemovalReason.OTHER);
               return LuaValue.NIL;
            }));
            table.set("elite", eliteTable);
         }
      }
   }
}
