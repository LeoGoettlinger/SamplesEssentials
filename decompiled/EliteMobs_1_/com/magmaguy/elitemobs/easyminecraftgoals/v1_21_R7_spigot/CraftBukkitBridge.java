package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot;

import java.lang.reflect.Method;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftBukkitBridge {
   private static final boolean IS_PAPER = detectPaper();
   private static final String CB_PACKAGE = detectCraftBukkitPackage();
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
   private static Method craftItemStackAsBukkitCopy;
   private static Method craftBlockDataGetState;
   private static Method displayTeleportDurationMethod = null;
   private static Class<?> craftChatMessageClass;
   private static Method fromStringOrNullMethod;
   private static Method craftWorldCreateEntityMethod = null;

   private static boolean detectPaper() {
      try {
         Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   private static String detectCraftBukkitPackage() {
      if (IS_PAPER) {
         return "org.bukkit.craftbukkit";
      } else {
         try {
            Class.forName("org.bukkit.craftbukkit.v1_21_R7.CraftServer");
            return "org.bukkit.craftbukkit.v1_21_R7";
         } catch (ClassNotFoundException var1) {
            return "org.bukkit.craftbukkit";
         }
      }
   }

   private static void initializeClasses() {
      try {
         craftWorldClass = Class.forName(CB_PACKAGE + ".CraftWorld");
         craftPlayerClass = Class.forName(CB_PACKAGE + ".entity.CraftPlayer");
         craftEntityClass = Class.forName(CB_PACKAGE + ".entity.CraftEntity");
         craftLivingEntityClass = Class.forName(CB_PACKAGE + ".entity.CraftLivingEntity");
         craftItemStackClass = Class.forName(CB_PACKAGE + ".inventory.CraftItemStack");
         craftBlockDataClass = Class.forName(CB_PACKAGE + ".block.data.CraftBlockData");
         craftWorldGetHandle = craftWorldClass.getMethod("getHandle");
         craftPlayerGetHandle = craftPlayerClass.getMethod("getHandle");
         craftEntityGetHandle = craftEntityClass.getMethod("getHandle");
         craftLivingEntityGetHandle = craftLivingEntityClass.getMethod("getHandle");
         craftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
         craftItemStackAsBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
         craftBlockDataGetState = craftBlockDataClass.getMethod("getState");
      } catch (Exception var1) {
         throw new RuntimeException("Failed to initialize CraftBukkit bridge for " + (IS_PAPER ? "Paper" : "Spigot"), var1);
      }
   }

   public static boolean isPaper() {
      return IS_PAPER;
   }

   public static WorldServer getServerLevel(World world) {
      try {
         return (WorldServer)craftWorldGetHandle.invoke(world);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get ServerLevel from World", var2);
      }
   }

   public static WorldServer getServerLevel(Location location) {
      return getServerLevel(location.getWorld());
   }

   public static EntityPlayer getServerPlayer(Player player) {
      try {
         return (EntityPlayer)craftPlayerGetHandle.invoke(player);
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

   public static EntityLiving getNMSLivingEntity(LivingEntity entity) {
      try {
         return (EntityLiving)craftLivingEntityGetHandle.invoke(entity);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get NMS LivingEntity from Bukkit LivingEntity", var2);
      }
   }

   public static ItemStack asBukkitCopy(net.minecraft.world.item.ItemStack itemStack) {
      try {
         return (ItemStack)craftItemStackAsBukkitCopy.invoke(null, itemStack);
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

   public static IBlockData getBlockState(BlockData blockData) {
      try {
         return (IBlockData)craftBlockDataGetState.invoke(blockData);
      } catch (Exception var2) {
         throw new RuntimeException("Failed to get BlockState from BlockData", var2);
      }
   }

   public static World getBukkitWorld(net.minecraft.world.level.World level) {
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
            displayTeleportDurationMethod = findDisplayTeleportDurationMethod();
         }

         displayTeleportDurationMethod.invoke(display, duration);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   private static Method findDisplayTeleportDurationMethod() throws NoSuchMethodException {
      Class<?> displayClass = Display.class;

      try {
         Method method = displayClass.getDeclaredMethod("setPosRotInterpolationDuration", int.class);
         method.setAccessible(true);
         return method;
      } catch (NoSuchMethodException var2) {
         Method methodx = displayClass.getDeclaredMethod("d", int.class);
         methodx.setAccessible(true);
         return methodx;
      }
   }

   public static String getEntityDimensionsFieldName() {
      return "dimensions";
   }

   public static net.minecraft.world.entity.Entity createNMSEntity(EntityType bukkitType, WorldServer level, Location location) {
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
               nmsEntity.a_(location.getX(), location.getY(), location.getZ());
               if (location.getYaw() != 0.0F) {
                  nmsEntity.v(location.getYaw());
               }

               if (location.getPitch() != 0.0F) {
                  nmsEntity.w(location.getPitch());
               }
            }

            return nmsEntity;
         }
      } catch (Exception var6) {
         throw new RuntimeException("Failed to create NMS entity for type: " + bukkitType, var6);
      }
   }

   public static IChatBaseComponent fromLegacyText(String text) {
      if (text != null && !text.isEmpty()) {
         try {
            if (craftChatMessageClass == null) {
               craftChatMessageClass = Class.forName(CB_PACKAGE + ".util.CraftChatMessage");
               fromStringOrNullMethod = craftChatMessageClass.getMethod("fromStringOrNull", String.class);
            }

            IChatBaseComponent component = (IChatBaseComponent)fromStringOrNullMethod.invoke(null, text);
            if (component != null) {
               return component;
            }
         } catch (Exception var2) {
         }

         return IChatBaseComponent.b(text);
      } else {
         return IChatBaseComponent.b("");
      }
   }

   static {
      initializeClasses();
   }
}
