package com.magmaguy.elitemobs.easyminecraftgoals.v26;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftBukkitBridge {
   private static final String CB_PACKAGE = "org.bukkit.craftbukkit";
   private static Class<?> craftWorldClass;
   private static Class<?> craftPlayerClass;
   private static Class<?> craftEntityClass;
   private static Class<?> craftLivingEntityClass;
   private static Class<?> craftItemStackClass;
   private static Class<?> craftBlockDataClass;
   private static Method craftWorldGetHandle;
   private static Method craftPlayerGetHandle;
   private static Method craftEntityGetHandle;
   private static Method craftLivingEntityGetHandle;
   private static Method craftItemStackAsNMSCopy;
   private static Method craftBlockDataGetState;
   private static Method displayTeleportDurationMethod = null;
   private static Class<?> craftChatMessageClass;
   private static Method fromStringOrNullMethod;
   private static Method craftWorldCreateEntityMethod = null;

   private static void initializeClasses() {
      try {
         craftWorldClass = Class.forName("org.bukkit.craftbukkit.CraftWorld");
         craftPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
         craftEntityClass = Class.forName("org.bukkit.craftbukkit.entity.CraftEntity");
         craftLivingEntityClass = Class.forName("org.bukkit.craftbukkit.entity.CraftLivingEntity");
         craftItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
         craftBlockDataClass = Class.forName("org.bukkit.craftbukkit.block.data.CraftBlockData");
         craftWorldGetHandle = craftWorldClass.getMethod("getHandle");
         craftPlayerGetHandle = craftPlayerClass.getMethod("getHandle");
         craftEntityGetHandle = craftEntityClass.getMethod("getHandle");
         craftLivingEntityGetHandle = craftLivingEntityClass.getMethod("getHandle");
         craftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
         craftBlockDataGetState = craftBlockDataClass.getMethod("getState");
      } catch (Exception var1) {
         throw new RuntimeException("Failed to initialize CraftBukkit bridge", var1);
      }
   }

   public static boolean isPaper() {
      try {
         Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   public static ServerLevel getServerLevel(World world) {
      try {
         return (ServerLevel)craftWorldGetHandle.invoke(world);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get ServerLevel from World", var2);
      }
   }

   public static ServerLevel getServerLevel(Location location) {
      return getServerLevel(location.getWorld());
   }

   public static ServerPlayer getServerPlayer(Player player) {
      try {
         return (ServerPlayer)craftPlayerGetHandle.invoke(player);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get ServerPlayer from Player", var2);
      }
   }

   public static net.minecraft.world.entity.Entity getNMSEntity(Entity entity) {
      try {
         return (net.minecraft.world.entity.Entity)craftEntityGetHandle.invoke(entity);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get NMS Entity from Bukkit Entity", var2);
      }
   }

   public static LivingEntity getNMSLivingEntity(org.bukkit.entity.LivingEntity entity) {
      try {
         return (LivingEntity)craftLivingEntityGetHandle.invoke(entity);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get NMS LivingEntity from Bukkit LivingEntity", var2);
      }
   }

   public static ItemStack asBukkitCopy(net.minecraft.world.item.ItemStack itemStack) {
      try {
         return (ItemStack)CraftBukkitBridge.BukkitItemCopy.METHOD.invoke(null, itemStack);
      } catch (ReflectiveOperationException var2) {
         throw new IllegalStateException("Failed to convert native item to Bukkit", var2);
      }
   }

   public static net.minecraft.world.item.ItemStack asNMSCopy(ItemStack itemStack) {
      try {
         return (net.minecraft.world.item.ItemStack)craftItemStackAsNMSCopy.invoke(null, itemStack);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to convert ItemStack to NMS", var2);
      }
   }

   public static BlockState getBlockState(BlockData blockData) {
      try {
         return (BlockState)craftBlockDataGetState.invoke(blockData);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get BlockState from BlockData", var2);
      }
   }

   public static World getBukkitWorld(Level level) {
      try {
         Method getWorld = level.getClass().getMethod("getWorld");
         return (World)getWorld.invoke(level);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get Bukkit World from Level", var2);
      }
   }

   public static Entity getBukkitEntity(net.minecraft.world.entity.Entity nmsEntity) {
      try {
         Method getBukkitEntity = nmsEntity.getClass().getMethod("getBukkitEntity");
         return (Entity)getBukkitEntity.invoke(nmsEntity);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get Bukkit Entity from NMS Entity", var2);
      }
   }

   public static void setDisplayTeleportDuration(Display display, int duration) {
      try {
         if (displayTeleportDurationMethod == null) {
            displayTeleportDurationMethod = Display.class.getDeclaredMethod("setPosRotInterpolationDuration", int.class);
            displayTeleportDurationMethod.setAccessible(true);
         }

         displayTeleportDurationMethod.invoke(display, duration);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public static String getEntityDimensionsFieldName() {
      return "dimensions";
   }

   public static net.minecraft.world.entity.Entity createNMSEntity(EntityType bukkitType, ServerLevel level, Location location) {
      try {
         if (craftWorldCreateEntityMethod == null) {
            craftWorldCreateEntityMethod = craftWorldClass.getMethod("createEntity", Location.class, Class.class);
         }

         Class<? extends Entity> entityClass = bukkitType.getEntityClass();
         if (entityClass == null) {
            throw new RuntimeException("No entity class for type: " + bukkitType);
         } else {
            Entity bukkitEntity = (Entity)craftWorldCreateEntityMethod.invoke(location.getWorld(), location, entityClass);
            net.minecraft.world.entity.Entity nmsEntity = getNMSEntity(bukkitEntity);
            if (nmsEntity != null) {
               nmsEntity.setPos(location.getX(), location.getY(), location.getZ());
               if (location.getYaw() != 0.0F) {
                  nmsEntity.setYRot(location.getYaw());
               }

               if (location.getPitch() != 0.0F) {
                  nmsEntity.setXRot(location.getPitch());
               }
            }

            return nmsEntity;
         }
      } catch (Exception var6) {
         throw new RuntimeException("Failed to create NMS entity for type: " + bukkitType, var6);
      }
   }

   public static Component fromLegacyText(String text) {
      if (text != null && !text.isEmpty()) {
         try {
            if (craftChatMessageClass == null) {
               craftChatMessageClass = Class.forName("org.bukkit.craftbukkit.util.CraftChatMessage");
               fromStringOrNullMethod = craftChatMessageClass.getMethod("fromStringOrNull", String.class);
            }

            Component component = (Component)fromStringOrNullMethod.invoke(null, text);
            if (component != null) {
               return component;
            }
         } catch (Exception var2) {
         }

         return Component.literal(text);
      } else {
         return Component.literal("");
      }
   }

   static {
      initializeClasses();
   }

   private static final class BukkitItemCopy {
      private static final Method METHOD = resolveMethod();

      private static Method resolveMethod() {
         Method selected = null;
         boolean ambiguous = false;

         for (Method method : CraftBukkitBridge.craftItemStackClass.getMethods()) {
            if (method.getName().equals("asBukkitCopy")
               && Modifier.isStatic(method.getModifiers())
               && method.getParameterCount() == 1
               && method.getParameterTypes()[0].isAssignableFrom(net.minecraft.world.item.ItemStack.class)
               && ItemStack.class.isAssignableFrom(method.getReturnType())) {
               if (method.getParameterTypes()[0] == net.minecraft.world.item.ItemStack.class) {
                  return method;
               }

               ambiguous |= selected != null;
               selected = method;
            }
         }

         if (ambiguous) {
            throw new IllegalStateException("Ambiguous public CraftItemStack.asBukkitCopy overloads for " + net.minecraft.world.item.ItemStack.class.getName());
         } else if (selected == null) {
            throw new IllegalStateException("No public CraftItemStack.asBukkitCopy overload accepts " + net.minecraft.world.item.ItemStack.class.getName());
         } else {
            return selected;
         }
      }
   }
}
