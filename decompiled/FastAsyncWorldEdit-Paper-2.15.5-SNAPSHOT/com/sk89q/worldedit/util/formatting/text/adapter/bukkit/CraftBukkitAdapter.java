package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.google.gson.JsonDeserializer;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class CraftBukkitAdapter implements Adapter {
   private static final CraftBukkitAdapter.Binding REFLECTION_BINDINGS = load();
   private static final boolean ALIVE = REFLECTION_BINDINGS.valid();

   private static CraftBukkitAdapter.Binding load() {
      try {
         Class<?> server = Bukkit.getServer().getClass();
         if (!isCompatibleServer(server)) {
            throw new UnsupportedOperationException("Incompatible server version");
         } else {
            String serverVersion = maybeVersion(server.getPackage().getName().substring("org.bukkit.craftbukkit".length()));
            Class<?> craftPlayerClass = craftBukkitClass(serverVersion, "entity.CraftPlayer");
            Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
            Class<?> entityPlayerClass = getHandleMethod.getReturnType();
            Field playerConnectionField = entityPlayerClass.getField("playerConnection");
            Class<?> playerConnectionClass = playerConnectionField.getType();
            Class<?> packetClass = minecraftClass(serverVersion, "Packet");
            Method sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);
            Class<?> baseComponentClass = minecraftClass(serverVersion, "IChatBaseComponent");
            Class<?> chatMessageTypeClass = optionalMinecraftClass(serverVersion, "ChatMessageType");
            Object chatMessageTypeSystem = enumValue(chatMessageTypeClass, "SYSTEM", 1);
            Class<?> chatPacketClass = minecraftClass(serverVersion, "PacketPlayOutChat");
            boolean longConstructor = false;

            Constructor<?> chatPacketConstructor;
            try {
               chatPacketConstructor = chatPacketClass.getConstructor(baseComponentClass);
            } catch (NoSuchMethodException var21) {
               if (chatMessageTypeClass == null) {
                  throw var21;
               }

               longConstructor = true;
               chatPacketConstructor = chatPacketClass.getConstructor(baseComponentClass, chatMessageTypeClass, UUID.class);
            }

            Constructor<?> legacyChatPacketConstructor = optionalConstructor(chatPacketClass, baseComponentClass, byte.class);
            Class<?> titlePacketClass = optionalMinecraftClass(serverVersion, "PacketPlayOutTitle");
            Object titleActionActionBar;
            Constructor<?> titlePacketConstructor;
            if (titlePacketClass != null) {
               Class<?> titlePacketClassAction = minecraftClass(serverVersion, "PacketPlayOutTitle$EnumTitleAction");
               titleActionActionBar = enumValue(titlePacketClassAction, "ACTIONBAR", Integer.MAX_VALUE);
               titlePacketConstructor = titlePacketClass.getConstructor(titlePacketClassAction, baseComponentClass);
            } else {
               titleActionActionBar = null;
               titlePacketConstructor = null;
            }

            Class<?> chatSerializerClass = Arrays.stream(baseComponentClass.getClasses())
               .filter(JsonDeserializer.class::isAssignableFrom)
               .findAny()
               .orElseGet(() -> {
                  try {
                     return minecraftClass(serverVersion, "ChatSerializer");
                  } catch (ClassNotFoundException var2x) {
                     throw new RuntimeException(var2x);
                  }
               });
            Method serializeMethod = Arrays.stream(chatSerializerClass.getMethods())
               .filter(m -> Modifier.isStatic(m.getModifiers()))
               .filter(m -> baseComponentClass.isAssignableFrom(m.getReturnType()))
               .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(String.class))
               .min(Comparator.comparing(Method::getName))
               .orElseThrow(() -> new RuntimeException("Unable to find serialize method"));
            return new CraftBukkitAdapter.AliveBinding(
               getHandleMethod,
               playerConnectionField,
               sendPacketMethod,
               chatMessageTypeSystem,
               legacyChatPacketConstructor,
               chatPacketConstructor,
               titleActionActionBar,
               titlePacketConstructor,
               serializeMethod,
               longConstructor
            );
         }
      } catch (Throwable var22) {
         return new CraftBukkitAdapter.DeadBinding();
      }
   }

   private static boolean isCompatibleServer(final Class<?> serverClass) {
      return serverClass.getPackage().getName().startsWith("org.bukkit.craftbukkit") && serverClass.getSimpleName().equals("CraftServer");
   }

   private static Class<?> craftBukkitClass(final String version, final String name) throws ClassNotFoundException {
      return Class.forName("org.bukkit.craftbukkit." + version + name);
   }

   private static Class<?> minecraftClass(final String version, final String name) throws ClassNotFoundException {
      return Class.forName("net.minecraft.server." + version + name);
   }

   private static String maybeVersion(final String version) {
      if (version.isEmpty()) {
         return "";
      } else if (version.charAt(0) == '.') {
         return version.substring(1) + '.';
      } else {
         throw new IllegalArgumentException("Unknown version " + version);
      }
   }

   private static Class<?> optionalMinecraftClass(final String version, final String name) {
      try {
         return minecraftClass(version, name);
      } catch (ClassNotFoundException var3) {
         return null;
      }
   }

   @Override
   public void sendMessage(final List<? extends CommandSender> viewers, final Component component) {
      if (ALIVE) {
         send(viewers, component, REFLECTION_BINDINGS::createMessagePacket);
      }
   }

   @Override
   public void sendActionBar(final List<? extends CommandSender> viewers, final Component component) {
      if (ALIVE) {
         send(viewers, component, REFLECTION_BINDINGS::createActionBarPacket);
      }
   }

   private static void send(final List<? extends CommandSender> viewers, final Component component, final Function<Component, Object> function) {
      Object packet = null;
      Iterator<? extends CommandSender> iterator = viewers.iterator();

      while (iterator.hasNext()) {
         CommandSender sender = iterator.next();
         if (sender instanceof Player) {
            try {
               Player player = (Player)sender;
               if (packet == null) {
                  packet = function.apply(component);
               }

               REFLECTION_BINDINGS.sendPacket(packet, player);
               iterator.remove();
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      }
   }

   @Nullable
   private static Object enumValue(@Nullable final Class<?> klass, String name, int ordinal) {
      if (klass == null) {
         return null;
      } else if (!Enum.class.isAssignableFrom(klass)) {
         return null;
      } else {
         try {
            return Enum.valueOf(klass.asSubclass(Enum.class), name);
         } catch (IllegalArgumentException var5) {
            Object[] constants = klass.getEnumConstants();
            return constants.length > ordinal ? constants[ordinal] : null;
         }
      }
   }

   @Nullable
   private static <T> Constructor<T> optionalConstructor(@Nullable Class<T> klass, @NonNull Class<?>... args) {
      if (klass == null) {
         return null;
      } else {
         try {
            return klass.getConstructor(args);
         } catch (NoSuchMethodException var3) {
            return null;
         }
      }
   }

   private static final class AliveBinding extends CraftBukkitAdapter.Binding {
      private static final UUID NIL_UUID = new UUID(0L, 0L);
      private static final byte LEGACY_CHAT_MESSAGE_TYPE_ACTION_BAR = 2;
      private final Method getHandleMethod;
      private final Field playerConnectionField;
      private final Method sendPacketMethod;
      private final Object chatMessageTypeSystem;
      private final Constructor<?> legacyChatPacketConstructor;
      private final Constructor<?> chatPacketConstructor;
      private final Object titlePacketActionActionBar;
      private final Constructor<?> titlePacketConstructor;
      private final boolean canMakeTitle;
      private final Method serializeMethod;
      private final boolean longChatPacketConstructor;

      AliveBinding(
         final Method getHandleMethod,
         final Field playerConnectionField,
         final Method sendPacketMethod,
         final Object chatMessageTypeSystem,
         final Constructor<?> legacyChatPacketConstructor,
         final Constructor<?> chatPacketConstructor,
         final Object titlePacketActionActionBar,
         final Constructor<?> titlePacketConstructor,
         final Method serializeMethod,
         final boolean longChatPacketConstructor
      ) {
         this.getHandleMethod = getHandleMethod;
         this.playerConnectionField = playerConnectionField;
         this.sendPacketMethod = sendPacketMethod;
         this.chatMessageTypeSystem = chatMessageTypeSystem;
         this.legacyChatPacketConstructor = legacyChatPacketConstructor;
         this.chatPacketConstructor = chatPacketConstructor;
         this.titlePacketConstructor = titlePacketConstructor;
         this.titlePacketActionActionBar = titlePacketActionActionBar;
         this.canMakeTitle = this.titlePacketConstructor != null && this.titlePacketActionActionBar != null;
         this.serializeMethod = serializeMethod;
         this.longChatPacketConstructor = longChatPacketConstructor;
      }

      @Override
      boolean valid() {
         return true;
      }

      @Override
      Object createMessagePacket(final Component component) {
         String json = GsonComponentSerializer.INSTANCE.serialize(component);

         try {
            Object mc = this.serializeMethod.invoke(null, json);
            return this.longChatPacketConstructor
               ? this.chatPacketConstructor.newInstance(mc, this.chatMessageTypeSystem, NIL_UUID)
               : this.chatPacketConstructor.newInstance(mc);
         } catch (Exception var4) {
            throw new UnsupportedOperationException("An exception was encountered while creating a packet for a component", var4);
         }
      }

      @Override
      Object createActionBarPacket(final Component component) {
         if (this.canMakeTitle) {
            try {
               String json = GsonComponentSerializer.INSTANCE.serialize(component);
               return this.titlePacketConstructor.newInstance(this.titlePacketActionActionBar, this.serializeMethod.invoke(null, json));
            } catch (Exception var5) {
               throw new UnsupportedOperationException("An exception was encountered while creating a packet for a component", var5);
            }
         } else if (this.legacyChatPacketConstructor != null) {
            String legacy = LegacyComponentSerializer.legacy().serialize(component);
            String json = GsonComponentSerializer.INSTANCE.serialize(TextComponent.of(legacy));

            try {
               return this.legacyChatPacketConstructor.newInstance(this.serializeMethod.invoke(null, json), (byte)2);
            } catch (Exception var6) {
               throw new UnsupportedOperationException("An exception was encountered while creating a packet for a component", var6);
            }
         } else {
            return this.createMessagePacket(component);
         }
      }

      @Override
      void sendPacket(final Object packet, final Player player) {
         try {
            Object connection = this.playerConnectionField.get(this.getHandleMethod.invoke(player));
            this.sendPacketMethod.invoke(connection, packet);
         } catch (Exception var4) {
            throw new UnsupportedOperationException("An exception was encountered while sending a packet for a component", var4);
         }
      }
   }

   private abstract static class Binding {
      private Binding() {
      }

      abstract boolean valid();

      abstract Object createMessagePacket(final Component component);

      abstract Object createActionBarPacket(final Component component);

      abstract void sendPacket(final Object packet, final Player player);
   }

   private static final class DeadBinding extends CraftBukkitAdapter.Binding {
      private DeadBinding() {
      }

      @Override
      boolean valid() {
         return false;
      }

      @Override
      Object createMessagePacket(final Component component) {
         throw new UnsupportedOperationException();
      }

      @Override
      Object createActionBarPacket(final Component component) {
         throw new UnsupportedOperationException();
      }

      @Override
      void sendPacket(final Object packet, final Player player) {
         throw new UnsupportedOperationException();
      }
   }
}
