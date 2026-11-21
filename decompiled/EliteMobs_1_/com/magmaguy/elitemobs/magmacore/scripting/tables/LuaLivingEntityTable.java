package com.magmaguy.elitemobs.magmacore.scripting.tables;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class LuaLivingEntityTable {
   public static LuaTable build(LivingEntity entity) {
      LuaTable table = LuaEntityTable.build(entity);
      if (entity == null) {
         return table;
      } else {
         LuaTableSupport.lazyField(table, "health", () -> LuaValue.valueOf(entity.getHealth()));
         LuaTableSupport.lazyField(table, "maximum_health", () -> LuaValue.valueOf(entity.getMaxHealth()));
         LuaTableSupport.lazyField(table, "name", () -> {
            String n = entity.getName();
            return (LuaValue)(n != null ? LuaValue.valueOf(n) : LuaValue.NIL);
         });
         LuaTableSupport.lazyField(table, "is_alive", () -> LuaValue.valueOf(!entity.isDead()));
         table.set("set_ai", LuaTableSupport.tableMethod(table, args -> {
            entity.setAI(args.checkboolean(1));
            return LuaValue.NIL;
         }));
         table.set("set_owner", LuaTableSupport.tableMethod(table, args -> {
            if (entity instanceof Tameable tameable) {
               Player owner = Bukkit.getPlayer(UUID.fromString(args.checkjstring(1)));
               if (owner == null) {
                  return LuaValue.FALSE;
               } else {
                  tameable.setOwner(owner);
                  if (entity instanceof Wolf wolf) {
                     wolf.setAngry(false);
                  }

                  return LuaValue.TRUE;
               }
            } else {
               return LuaValue.FALSE;
            }
         }));
         table.set("damage", LuaTableSupport.tableMethod(table, args -> {
            entity.damage(args.checkdouble(1));
            return LuaValue.NIL;
         }));
         table.set("push", LuaTableSupport.tableMethod(table, args -> {
            entity.setVelocity(entity.getVelocity().add(new Vector(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3))));
            return LuaValue.NIL;
         }));
         table.set("set_facing", LuaTableSupport.tableMethod(table, args -> {
            entity.teleport(entity.getLocation().setDirection(new Vector(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3))));
            return LuaValue.NIL;
         }));
         table.set("has_potion_effect", LuaTableSupport.tableMethod(table, args -> {
            PotionEffectType type = PotionEffectType.getByName(args.checkjstring(1));
            return LuaValue.valueOf(type != null && entity.hasPotionEffect(type));
         }));
         table.set("add_potion_effect", LuaTableSupport.tableMethod(table, args -> {
            PotionEffectType type = PotionEffectType.getByName(args.checkjstring(1));
            if (type != null) {
               entity.addPotionEffect(new PotionEffect(type, args.checkint(2), args.checkint(3)));
            }

            return LuaValue.NIL;
         }));
         table.set("remove_potion_effect", LuaTableSupport.tableMethod(table, args -> {
            PotionEffectType type = PotionEffectType.getByName(args.checkjstring(1));
            if (type != null) {
               entity.removePotionEffect(type);
            }

            return LuaValue.NIL;
         }));
         table.set("get_scale", LuaTableSupport.tableMethod(table, args -> {
            AttributeInstance attr = resolveScaleAttribute(entity);
            return LuaValue.valueOf(attr != null ? attr.getValue() : 1.0);
         }));
         table.set("set_scale", LuaTableSupport.tableMethod(table, args -> {
            AttributeInstance attr = resolveScaleAttribute(entity);
            if (attr != null) {
               attr.setBaseValue(args.checkdouble(1));
            }

            return LuaValue.NIL;
         }));
         if (entity instanceof Player player) {
            table.set("chat", LuaTableSupport.tableMethod(table, args -> {
               player.chat(ChatColorConverter.convert(args.checkjstring(1)));
               return LuaValue.NIL;
            }));
            table.set("send_message", LuaTableSupport.tableMethod(table, args -> {
               player.sendMessage(ChatColorConverter.convert(args.checkjstring(1)));
               return LuaValue.NIL;
            }));
            table.set(
               "get_held_item",
               LuaTableSupport.tableMethod(
                  table,
                  args -> {
                     ItemStack item = player.getInventory().getItemInMainHand();
                     if (item.getType() != Material.AIR && item.getAmount() > 0) {
                        LuaTable itemTable = new LuaTable();
                        itemTable.set("type", item.getType().name());
                        itemTable.set("amount", item.getAmount());
                        itemTable.set(
                           "display_name",
                           item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name()
                        );
                        return itemTable;
                     } else {
                        return LuaValue.NIL;
                     }
                  }
               )
            );
            table.set("consume_held_item", LuaTableSupport.tableMethod(table, args -> {
               int amount = args.narg() >= 1 && !args.arg(1).isnil() ? args.checkint(1) : 1;
               Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                  ItemStack item = player.getInventory().getItemInMainHand();
                  if (item.getType() != Material.AIR) {
                     int newAmount = item.getAmount() - amount;
                     if (newAmount <= 0) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                     } else {
                        item.setAmount(newAmount);
                     }
                  }
               });
               return LuaValue.NIL;
            }));
            table.set("has_item", LuaTableSupport.tableMethod(table, args -> {
               String materialName = args.checkjstring(1);
               int amount = args.narg() >= 2 && !args.arg(2).isnil() ? args.checkint(2) : 1;
               Material material = Material.matchMaterial(materialName);
               return material == null ? LuaValue.FALSE : LuaValue.valueOf(player.getInventory().contains(material, amount));
            }));
            table.set("game_mode", player.getGameMode().name().toLowerCase());
            table.set(
               "get_target_entity",
               LuaTableSupport.tableMethod(
                  table,
                  args -> {
                     double range = args.optdouble(1, 50.0);
                     Location eyeLoc = player.getEyeLocation();
                     RayTraceResult result = player.getWorld()
                        .rayTrace(eyeLoc, eyeLoc.getDirection(), range, FluidCollisionMode.NEVER, true, 0.5, e -> !e.equals(player));
                     if (result != null && result.getHitEntity() != null) {
                        Entity hitEntity = result.getHitEntity();
                        return hitEntity instanceof LivingEntity le ? build(le) : LuaEntityTable.build(hitEntity);
                     } else {
                        return LuaValue.NIL;
                     }
                  }
               )
            );
            table.set("get_eye_location", LuaTableSupport.tableMethod(table, args -> LuaTableSupport.locationToTable(player.getEyeLocation())));
            table.set("get_look_direction", LuaTableSupport.tableMethod(table, args -> LuaTableSupport.vectorToTable(player.getEyeLocation().getDirection())));
            table.set("send_block_change", LuaTableSupport.tableMethod(table, args -> {
               int x = args.checkint(1);
               int y = args.checkint(2);
               int z = args.checkint(3);
               String materialName = args.checkjstring(4).toUpperCase(Locale.ROOT);
               int ticks = args.narg() >= 5 && !args.arg(5).isnil() ? args.checkint(5) : -1;
               Material material = Material.matchMaterial(materialName);
               if (material != null && material.isBlock()) {
                  Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                     Location loc = new Location(player.getWorld(), x, y, z);
                     player.sendBlockChange(loc, material.createBlockData());
                     if (ticks > 0) {
                        Bukkit.getScheduler().runTaskLater(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                           Block real = player.getWorld().getBlockAt(x, y, z);
                           player.sendBlockChange(loc, real.getBlockData());
                        }, ticks);
                     }
                  });
                  return LuaValue.TRUE;
               } else {
                  return LuaValue.FALSE;
               }
            }));
            table.set("reset_block", LuaTableSupport.tableMethod(table, args -> {
               int x = args.checkint(1);
               int y = args.checkint(2);
               int z = args.checkint(3);
               Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                  Block real = player.getWorld().getBlockAt(x, y, z);
                  player.sendBlockChange(new Location(player.getWorld(), x, y, z), real.getBlockData());
               });
               return LuaValue.TRUE;
            }));
            table.set("sleep", LuaTableSupport.tableMethod(table, args -> {
               double x = args.checkdouble(1);
               double y = args.checkdouble(2);
               double z = args.checkdouble(3);
               Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                  Location bedLoc = new Location(player.getWorld(), (int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
                  Block block = bedLoc.getBlock();
                  BlockData originalData = block.getBlockData().clone();
                  block.setType(Material.WHITE_BED, false);
                  if (block.getBlockData() instanceof Bed bedData) {
                     Vector toPlayer = player.getLocation().toVector().subtract(bedLoc.toVector()).setY(0).normalize();
                     float angle = (float)Math.toDegrees(Math.atan2(-toPlayer.getX(), toPlayer.getZ()));
                     if (angle < 0.0F) {
                        angle += 360.0F;
                     }

                     if (angle >= 315.0F || angle < 45.0F) {
                        bedData.setFacing(BlockFace.SOUTH);
                     } else if (angle < 135.0F) {
                        bedData.setFacing(BlockFace.WEST);
                     } else if (angle < 225.0F) {
                        bedData.setFacing(BlockFace.NORTH);
                     } else {
                        bedData.setFacing(BlockFace.EAST);
                     }

                     block.setBlockData(bedData, false);
                  }

                  try {
                     player.sleep(bedLoc, true);
                  } catch (Exception var13) {
                  }

                  BukkitTask[] cleanupTask = new BukkitTask[1];
                  cleanupTask[0] = Bukkit.getScheduler().runTaskTimer(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                     if (!player.isSleeping() || !player.isOnline()) {
                        block.setBlockData(originalData, false);
                        cleanupTask[0].cancel();
                     }
                  }, 20L, 10L);
               });
               return LuaValue.NIL;
            }));
            table.set("wake_up", LuaTableSupport.tableMethod(table, args -> {
               Bukkit.getScheduler().runTask(MagmaCore.getInstance().getRequestingPlugin(), () -> {
                  if (player.isSleeping()) {
                     player.wakeup(true);
                  }
               });
               return LuaValue.NIL;
            }));
            LuaPlayerUITable.addTo(table, player);
         }

         return table;
      }
   }

   private static AttributeInstance resolveScaleAttribute(LivingEntity entity) {
      try {
         Attribute attribute = (Attribute)Registry.ATTRIBUTE.get(NamespacedKey.minecraft("scale"));
         if (attribute == null) {
            attribute = (Attribute)Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.scale"));
         }

         return attribute == null ? null : entity.getAttribute(attribute);
      } catch (Throwable var2) {
         return null;
      }
   }
}
