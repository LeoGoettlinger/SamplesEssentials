package com.ranull.graves.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReflectionUtil {
   private ReflectionUtil() {
   }

   public static void swingMainHand(Player player) {
      try {
         Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
         Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
         Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getClass("Packet"));
         Object packetPlayOutAnimation = getClass("PacketPlayOutAnimation").getConstructor(getClass("Entity"), int.class).newInstance(entityPlayer, 0);
         sendPacket.invoke(playerConnection, packetPlayOutAnimation);
      } catch (InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | InstantiationException | IllegalAccessException var5) {
      }
   }

   public static Class<?> getClass(String clazz) throws ClassNotFoundException {
      String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
      return Class.forName("net.minecraft.server." + version + "." + clazz);
   }
}
