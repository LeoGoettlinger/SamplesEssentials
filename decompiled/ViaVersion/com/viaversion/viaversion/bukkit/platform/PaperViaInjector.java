package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.bukkit.handlers.BukkitChannelInitializer;
import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;

public final class PaperViaInjector {
   public static final boolean PAPER_INJECTION_METHOD = hasPaperInjectionMethod();
   public static final boolean PAPER_PROTOCOL_METHOD = hasServerProtocolMethod();
   public static final boolean PAPER_IS_STOPPING_METHOD = hasIsStoppingMethod();

   private PaperViaInjector() {
   }

   public static int getServerProtocolVersion() {
      if (!PAPER_PROTOCOL_METHOD) {
         throw new UnsupportedOperationException("Paper method not available");
      } else {
         return Bukkit.getUnsafe().getProtocolVersion();
      }
   }

   public static void setPaperChannelInitializeListener() throws ReflectiveOperationException {
      Class<?> listenerClass = Class.forName("io.papermc.paper.network.ChannelInitializeListener");
      Object channelInitializeListener = Proxy.newProxyInstance(
         BukkitViaInjector.class.getClassLoader(), new Class[]{listenerClass}, (proxy, method, args) -> {
            if (method.getName().equals("afterInitChannel")) {
               BukkitChannelInitializer.afterChannelInitialize((Channel)args[0]);
               return null;
            } else {
               return method.invoke(proxy, args);
            }
         }
      );
      Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
      Method addListenerMethod = holderClass.getDeclaredMethod("addListener", Key.class, listenerClass);
      addListenerMethod.invoke(null, Key.key("viaversion", "injector"), channelInitializeListener);
   }

   public static void removePaperChannelInitializeListener() throws ReflectiveOperationException {
      Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
      Method addListenerMethod = holderClass.getDeclaredMethod("removeListener", Key.class);
      addListenerMethod.invoke(null, Key.key("viaversion", "injector"));
   }

   private static boolean hasServerProtocolMethod() {
      return hasMethod("org.bukkit.UnsafeValues", "getProtocolVersion");
   }

   private static boolean hasPaperInjectionMethod() {
      return hasClass("io.papermc.paper.network.ChannelInitializeListener");
   }

   private static boolean hasIsStoppingMethod() {
      return hasMethod(Bukkit.class, "isStopping");
   }

   public static boolean hasClass(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static boolean hasMethod(String className, String method) {
      try {
         Class.forName(className).getDeclaredMethod(method);
         return true;
      } catch (NoSuchMethodException | ClassNotFoundException var3) {
         return false;
      }
   }

   public static boolean hasMethod(Class<?> clazz, String method, Class<?>... params) {
      try {
         clazz.getDeclaredMethod(method, params);
         return true;
      } catch (NoSuchMethodException var4) {
         return false;
      }
   }

   public static boolean hasMethod(Class<?> clazz, Class<?> returnType, String method, Class<?>... params) {
      try {
         return clazz.getDeclaredMethod(method, params).getReturnType().equals(returnType);
      } catch (NoSuchMethodException var5) {
         return false;
      }
   }
}
