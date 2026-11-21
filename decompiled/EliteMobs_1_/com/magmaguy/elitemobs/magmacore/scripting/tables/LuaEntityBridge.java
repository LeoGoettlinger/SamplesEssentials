package com.magmaguy.elitemobs.magmacore.scripting.tables;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

final class LuaEntityBridge {
   private static boolean eliteResolved = false;
   private static Method isEliteMobMethod;
   private static Method getEliteMobEntityMethod;
   private static Method eliteGetLevelMethod;
   private static Method eliteGetNameMethod;
   private static Method eliteGetHealthMethod;
   private static Method eliteGetMaxHealthMethod;
   private static Method eliteIsCustomBossMethod;
   private static Method eliteGetHealthMultiplierMethod;
   private static Method eliteGetDamageMultiplierMethod;
   private static Method eliteRemoveMethod;
   private static Object removalReasonOther;
   private static boolean fmmResolved = false;
   private static Method isDynamicEntityMethod;
   private static Method getDynamicEntityMethod;
   private static Class<?> modeledEntityClass;
   private static Method getLoadedMapMethod;
   private static Method getEntityIDMethod;
   private static Method playAnimationMethod;
   private static Method stopAnimationsMethod;
   private static Method modeledRemoveMethod;
   private static Class<?> propEntityClass;
   private static Method isPropEntityMethod;
   private static Method getPropEntityIDMethod;

   private LuaEntityBridge() {
   }

   static void addPluginFields(LuaTable table, Entity entity) {
      addEliteFields(table, entity);
      addFmmFields(table, entity);
   }

   private static void resolveElite() {
      if (!eliteResolved) {
         eliteResolved = true;
         if (Bukkit.getPluginManager().isPluginEnabled("EliteMobs")) {
            try {
               Class<?> trackerClass = Class.forName("com.magmaguy.elitemobs.entitytracker.EntityTracker");
               isEliteMobMethod = trackerClass.getMethod("isEliteMob", Entity.class);
               getEliteMobEntityMethod = trackerClass.getMethod("getEliteMobEntity", Entity.class);
               Class<?> eliteClass = Class.forName("com.magmaguy.elitemobs.mobconstructor.EliteEntity");
               eliteGetLevelMethod = eliteClass.getMethod("getLevel");
               eliteGetNameMethod = eliteClass.getMethod("getName");
               eliteGetHealthMethod = eliteClass.getMethod("getHealth");
               eliteGetMaxHealthMethod = eliteClass.getMethod("getMaxHealth");
               eliteIsCustomBossMethod = eliteClass.getMethod("isCustomBossEntity");
               eliteGetHealthMultiplierMethod = eliteClass.getMethod("getHealthMultiplier");
               eliteGetDamageMultiplierMethod = eliteClass.getMethod("getDamageMultiplier");
               Class<?> removalReasonClass = Class.forName("com.magmaguy.elitemobs.api.internal.RemovalReason");
               eliteRemoveMethod = eliteClass.getMethod("remove", removalReasonClass);
               removalReasonOther = Enum.valueOf(removalReasonClass.asSubclass(Enum.class), "OTHER");
            } catch (Exception var3) {
               Bukkit.getLogger().log(Level.WARNING, "[MagmaCore] Failed to resolve EliteMobs API via reflection", (Throwable)var3);
               isEliteMobMethod = null;
            }
         }
      }
   }

   private static void addEliteFields(LuaTable table, Entity entity) {
      resolveElite();
      if (isEliteMobMethod == null) {
         table.set("is_elite", LuaValue.FALSE);
         table.set("is_custom_boss", LuaValue.FALSE);
         table.set("is_significant_boss", LuaValue.FALSE);
      } else {
         try {
            boolean isElite = (Boolean)isEliteMobMethod.invoke(null, entity);
            table.set("is_elite", LuaValue.valueOf(isElite));
            if (!isElite) {
               table.set("is_custom_boss", LuaValue.FALSE);
               table.set("is_significant_boss", LuaValue.FALSE);
               return;
            }

            Object eliteEntity = getEliteMobEntityMethod.invoke(null, entity);
            if (eliteEntity == null) {
               table.set("is_custom_boss", LuaValue.FALSE);
               table.set("is_significant_boss", LuaValue.FALSE);
               return;
            }

            LuaTable eliteTable = new LuaTable();
            eliteTable.set("level", LuaValue.valueOf((Integer)eliteGetLevelMethod.invoke(eliteEntity)));
            Object nameObj = eliteGetNameMethod.invoke(eliteEntity);
            eliteTable.set("name", (LuaValue)(nameObj != null ? LuaValue.valueOf(nameObj.toString()) : LuaValue.NIL));
            eliteTable.set("health", LuaValue.valueOf((Double)eliteGetHealthMethod.invoke(eliteEntity)));
            eliteTable.set("max_health", LuaValue.valueOf((Double)eliteGetMaxHealthMethod.invoke(eliteEntity)));
            boolean isCustomBoss = (Boolean)eliteIsCustomBossMethod.invoke(eliteEntity);
            eliteTable.set("is_custom_boss", LuaValue.valueOf(isCustomBoss));
            table.set("is_custom_boss", LuaValue.valueOf(isCustomBoss));
            double healthMultiplier = (Double)eliteGetHealthMultiplierMethod.invoke(eliteEntity);
            double damageMultiplier = (Double)eliteGetDamageMultiplierMethod.invoke(eliteEntity);
            eliteTable.set("health_multiplier", LuaValue.valueOf(healthMultiplier));
            eliteTable.set("damage_multiplier", LuaValue.valueOf(damageMultiplier));
            boolean isSignificant = isCustomBoss && healthMultiplier > 1.0;
            table.set("is_significant_boss", LuaValue.valueOf(isSignificant));
            eliteTable.set("remove", LuaTableSupport.tableMethod(eliteTable, args -> {
               try {
                  eliteRemoveMethod.invoke(eliteEntity, removalReasonOther);
               } catch (Exception var3x) {
               }

               return LuaValue.NIL;
            }));
            table.set("elite", eliteTable);
         } catch (Exception var13) {
            table.set("is_elite", LuaValue.FALSE);
            table.set("is_custom_boss", LuaValue.FALSE);
            table.set("is_significant_boss", LuaValue.FALSE);
         }
      }
   }

   private static void resolveFmm() {
      if (!fmmResolved) {
         fmmResolved = true;
         if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
            try {
               modeledEntityClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.ModeledEntity");
               getLoadedMapMethod = modeledEntityClass.getMethod("getLoadedModeledEntitiesWithUnderlyingEntities");
               getEntityIDMethod = modeledEntityClass.getMethod("getEntityID");
               playAnimationMethod = modeledEntityClass.getMethod("playAnimation", String.class, boolean.class, boolean.class);
               stopAnimationsMethod = modeledEntityClass.getMethod("stopCurrentAnimations");
               modeledRemoveMethod = modeledEntityClass.getMethod("remove");
               Class<?> dynamicClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.DynamicEntity");
               isDynamicEntityMethod = dynamicClass.getMethod("isDynamicEntity", Entity.class);
               getDynamicEntityMethod = dynamicClass.getMethod("getDynamicEntity", Entity.class);
               propEntityClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.PropEntity");
               isPropEntityMethod = propEntityClass.getMethod("isPropEntity", ArmorStand.class);
               getPropEntityIDMethod = propEntityClass.getMethod("getPropEntityID", ArmorStand.class);
            } catch (Exception var1) {
               Bukkit.getLogger().log(Level.WARNING, "[MagmaCore] Failed to resolve FreeMinecraftModels API via reflection", (Throwable)var1);
               modeledEntityClass = null;
            }
         }
      }
   }

   private static void addFmmFields(LuaTable table, Entity entity) {
      resolveFmm();
      if (modeledEntityClass == null) {
         table.set("is_modeled", LuaValue.FALSE);
         table.set("is_prop", LuaValue.FALSE);
      } else {
         try {
            HashMap<Entity, ?> loadedMap = (HashMap<Entity, ?>)getLoadedMapMethod.invoke(null);
            Object modeledEntity = loadedMap != null ? loadedMap.get(entity) : null;
            boolean isModeled = modeledEntity != null;
            table.set("is_modeled", LuaValue.valueOf(isModeled));
            boolean isProp = false;
            if (entity instanceof ArmorStand armorStand) {
               try {
                  isProp = (Boolean)isPropEntityMethod.invoke(null, armorStand);
               } catch (Exception var9) {
               }
            }

            table.set("is_prop", LuaValue.valueOf(isProp));
            if (!isModeled) {
               return;
            }

            LuaTable modelTable = new LuaTable();
            String modelId = (String)getEntityIDMethod.invoke(modeledEntity);
            modelTable.set("model_id", (LuaValue)(modelId != null ? LuaValue.valueOf(modelId) : LuaValue.NIL));
            modelTable.set("play_animation", LuaTableSupport.tableMethod(modelTable, args -> {
               String animName = args.checkjstring(1);
               boolean blend = args.optboolean(2, false);
               boolean loop = args.optboolean(3, false);

               try {
                  boolean result = (Boolean)playAnimationMethod.invoke(modeledEntity, animName, blend, loop);
                  return LuaValue.valueOf(result);
               } catch (Exception var6x) {
                  return LuaValue.FALSE;
               }
            }));
            modelTable.set("stop_animations", LuaTableSupport.tableMethod(modelTable, args -> {
               try {
                  stopAnimationsMethod.invoke(modeledEntity);
               } catch (Exception var3x) {
               }

               return LuaValue.NIL;
            }));
            modelTable.set("remove", LuaTableSupport.tableMethod(modelTable, args -> {
               try {
                  modeledRemoveMethod.invoke(modeledEntity);
               } catch (Exception var3x) {
               }

               return LuaValue.NIL;
            }));
            table.set("model", modelTable);
         } catch (Exception var10) {
            table.set("is_modeled", LuaValue.FALSE);
            table.set("is_prop", LuaValue.FALSE);
         }
      }
   }
}
