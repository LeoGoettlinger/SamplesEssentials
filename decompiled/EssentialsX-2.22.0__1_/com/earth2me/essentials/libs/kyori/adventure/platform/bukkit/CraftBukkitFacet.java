package com.earth2me.essentials.libs.kyori.adventure.platform.bukkit;

import com.earth2me.essentials.libs.kyori.adventure.audience.MessageType;
import com.earth2me.essentials.libs.kyori.adventure.chat.ChatType;
import com.earth2me.essentials.libs.kyori.adventure.identity.Identity;
import com.earth2me.essentials.libs.kyori.adventure.nbt.BinaryTagIO;
import com.earth2me.essentials.libs.kyori.adventure.nbt.BinaryTagTypes;
import com.earth2me.essentials.libs.kyori.adventure.nbt.CompoundBinaryTag;
import com.earth2me.essentials.libs.kyori.adventure.nbt.ListBinaryTag;
import com.earth2me.essentials.libs.kyori.adventure.nbt.StringBinaryTag;
import com.earth2me.essentials.libs.kyori.adventure.platform.facet.Facet;
import com.earth2me.essentials.libs.kyori.adventure.platform.facet.FacetBase;
import com.earth2me.essentials.libs.kyori.adventure.platform.facet.FacetComponentFlattener;
import com.earth2me.essentials.libs.kyori.adventure.platform.facet.Knob;
import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.TextComponent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CraftBukkitFacet<V extends CommandSender> extends FacetBase<V> {
   private static final Class<?> CLASS_NMS_ENTITY = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("Entity"), MinecraftReflection.findMcClassName("world.entity.Entity")
   );
   private static final Class<?> CLASS_CRAFT_ENTITY = MinecraftReflection.findCraftClass("entity.CraftEntity");
   private static final MethodHandle CRAFT_ENTITY_GET_HANDLE = MinecraftReflection.findMethod(CLASS_CRAFT_ENTITY, "getHandle", CLASS_NMS_ENTITY);
   @Nullable
   static final Class<? extends Player> CLASS_CRAFT_PLAYER = MinecraftReflection.findCraftClass("entity.CraftPlayer", Player.class);
   @Nullable
   static final MethodHandle CRAFT_PLAYER_GET_HANDLE;
   @Nullable
   private static final MethodHandle ENTITY_PLAYER_GET_CONNECTION;
   @Nullable
   private static final MethodHandle PLAYER_CONNECTION_SEND_PACKET;
   private static final boolean SUPPORTED;
   @Nullable
   private static final Class<?> CLASS_CHAT_COMPONENT;
   @Nullable
   private static final Class<?> CLASS_MESSAGE_TYPE;
   @Nullable
   private static final Object MESSAGE_TYPE_CHAT;
   @Nullable
   private static final Object MESSAGE_TYPE_SYSTEM;
   @Nullable
   private static final Object MESSAGE_TYPE_ACTIONBAR;
   @Nullable
   private static final MethodHandle LEGACY_CHAT_PACKET_CONSTRUCTOR;
   @Nullable
   private static final MethodHandle CHAT_PACKET_CONSTRUCTOR;
   @Nullable
   private static final Class<?> CLASS_TITLE_PACKET;
   @Nullable
   private static final Class<?> CLASS_TITLE_ACTION;
   private static final MethodHandle CONSTRUCTOR_TITLE_MESSAGE;
   @Nullable
   private static final MethodHandle CONSTRUCTOR_TITLE_TIMES;
   @Nullable
   private static final Object TITLE_ACTION_TITLE;
   @Nullable
   private static final Object TITLE_ACTION_SUBTITLE;
   @Nullable
   private static final Object TITLE_ACTION_ACTIONBAR;
   @Nullable
   private static final Object TITLE_ACTION_CLEAR;
   @Nullable
   private static final Object TITLE_ACTION_RESET;

   protected CraftBukkitFacet(@Nullable final Class<? extends V> viewerClass) {
      super(viewerClass);
   }

   @Override
   public boolean isSupported() {
      return super.isSupported() && SUPPORTED;
   }

   static {
      Class<?> craftPlayerClass = MinecraftReflection.findCraftClass("entity.CraftPlayer");
      Class<?> packetClass = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("Packet"), MinecraftReflection.findMcClassName("network.protocol.Packet")
      );
      MethodHandle craftPlayerGetHandle = null;
      MethodHandle entityPlayerGetConnection = null;
      MethodHandle playerConnectionSendPacket = null;
      if (craftPlayerClass != null && packetClass != null) {
         try {
            Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
            Class<?> entityPlayerClass = getHandleMethod.getReturnType();
            craftPlayerGetHandle = MinecraftReflection.lookup().unreflect(getHandleMethod);
            Field playerConnectionField = MinecraftReflection.findField(entityPlayerClass, "playerConnection", "connection");
            Class<?> playerConnectionClass = null;
            if (playerConnectionField != null) {
               entityPlayerGetConnection = MinecraftReflection.lookup().unreflectGetter(playerConnectionField);
               playerConnectionClass = playerConnectionField.getType();
            } else {
               Class<?> serverGamePacketListenerImpl = MinecraftReflection.findClass(
                  MinecraftReflection.findNmsClassName("PlayerConnection"),
                  MinecraftReflection.findMcClassName("server.network.PlayerConnection"),
                  MinecraftReflection.findMcClassName("server.network.ServerGamePacketListenerImpl")
               );

               for (Field field : entityPlayerClass.getDeclaredFields()) {
                  int modifiers = field.getModifiers();
                  if (Modifier.isPublic(modifiers)
                     && !Modifier.isFinal(modifiers)
                     && (serverGamePacketListenerImpl == null || field.getType().equals(serverGamePacketListenerImpl))) {
                     entityPlayerGetConnection = MinecraftReflection.lookup().unreflectGetter(field);
                     playerConnectionClass = field.getType();
                  }
               }
            }

            Class<?> serverCommonPacketListenerImpl = MinecraftReflection.findClass(
               MinecraftReflection.findMcClassName("server.network.ServerCommonPacketListenerImpl")
            );
            if (serverCommonPacketListenerImpl != null) {
               playerConnectionClass = serverCommonPacketListenerImpl;
            }

            playerConnectionSendPacket = MinecraftReflection.searchMethod(playerConnectionClass, 1, new String[]{"sendPacket", "send"}, void.class, packetClass);
         } catch (Throwable var16) {
            Knob.logError(var16, "Failed to initialize CraftBukkit sendPacket");
         }
      }

      CRAFT_PLAYER_GET_HANDLE = craftPlayerGetHandle;
      ENTITY_PLAYER_GET_CONNECTION = entityPlayerGetConnection;
      PLAYER_CONNECTION_SEND_PACKET = playerConnectionSendPacket;
      SUPPORTED = Knob.isEnabled("craftbukkit", true)
         && MinecraftComponentSerializer.isSupported()
         && CRAFT_PLAYER_GET_HANDLE != null
         && ENTITY_PLAYER_GET_CONNECTION != null
         && PLAYER_CONNECTION_SEND_PACKET != null;
      CLASS_CHAT_COMPONENT = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("IChatBaseComponent"),
         MinecraftReflection.findMcClassName("network.chat.IChatBaseComponent"),
         MinecraftReflection.findMcClassName("network.chat.Component")
      );
      CLASS_MESSAGE_TYPE = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("ChatMessageType"),
         MinecraftReflection.findMcClassName("network.chat.ChatMessageType"),
         MinecraftReflection.findMcClassName("network.chat.ChatType")
      );
      if (CLASS_MESSAGE_TYPE != null && !CLASS_MESSAGE_TYPE.isEnum()) {
         MESSAGE_TYPE_CHAT = 0;
         MESSAGE_TYPE_SYSTEM = 1;
         MESSAGE_TYPE_ACTIONBAR = 2;
      } else {
         MESSAGE_TYPE_CHAT = MinecraftReflection.findEnum(CLASS_MESSAGE_TYPE, "CHAT", 0);
         MESSAGE_TYPE_SYSTEM = MinecraftReflection.findEnum(CLASS_MESSAGE_TYPE, "SYSTEM", 1);
         MESSAGE_TYPE_ACTIONBAR = MinecraftReflection.findEnum(CLASS_MESSAGE_TYPE, "GAME_INFO", 2);
      }

      MethodHandle legacyChatPacketConstructor = null;
      MethodHandle chatPacketConstructor = null;

      try {
         if (CLASS_CHAT_COMPONENT != null) {
            Class<?> chatPacketClass = MinecraftReflection.needClass(
               MinecraftReflection.findNmsClassName("PacketPlayOutChat"),
               MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutChat"),
               MinecraftReflection.findMcClassName("network.protocol.game.ClientboundChatPacket"),
               MinecraftReflection.findMcClassName("network.protocol.game.ClientboundSystemChatPacket")
            );
            if (MESSAGE_TYPE_CHAT == 0) {
               chatPacketConstructor = MinecraftReflection.findConstructor(chatPacketClass, CLASS_CHAT_COMPONENT, boolean.class);
            }

            if (chatPacketConstructor == null) {
               chatPacketConstructor = MinecraftReflection.findConstructor(chatPacketClass, CLASS_CHAT_COMPONENT, int.class);
            }

            if (chatPacketConstructor == null) {
               chatPacketConstructor = MinecraftReflection.findConstructor(chatPacketClass, CLASS_CHAT_COMPONENT);
            }

            if (chatPacketConstructor == null) {
               if (CLASS_MESSAGE_TYPE != null) {
                  chatPacketConstructor = MinecraftReflection.findConstructor(chatPacketClass, CLASS_CHAT_COMPONENT, CLASS_MESSAGE_TYPE, UUID.class);
               }
            } else if (MESSAGE_TYPE_CHAT == 0) {
               if (chatPacketConstructor.type().parameterType(1).equals(boolean.class)) {
                  chatPacketConstructor = MethodHandles.insertArguments(chatPacketConstructor, 1, Boolean.FALSE);
                  chatPacketConstructor = MethodHandles.dropArguments(chatPacketConstructor, 1, Integer.class, UUID.class);
               } else {
                  chatPacketConstructor = MethodHandles.dropArguments(chatPacketConstructor, 2, UUID.class);
               }
            } else {
               chatPacketConstructor = MethodHandles.dropArguments(
                  chatPacketConstructor, 1, CLASS_MESSAGE_TYPE == null ? Object.class : CLASS_MESSAGE_TYPE, UUID.class
               );
            }

            legacyChatPacketConstructor = MinecraftReflection.findConstructor(chatPacketClass, CLASS_CHAT_COMPONENT, byte.class);
            if (legacyChatPacketConstructor == null) {
               legacyChatPacketConstructor = MinecraftReflection.findConstructor(chatPacketClass, CLASS_CHAT_COMPONENT, int.class);
            }
         }
      } catch (Throwable var15) {
         Knob.logError(var15, "Failed to initialize ClientboundChatPacket constructor");
      }

      CHAT_PACKET_CONSTRUCTOR = chatPacketConstructor;
      LEGACY_CHAT_PACKET_CONSTRUCTOR = legacyChatPacketConstructor;
      CLASS_TITLE_PACKET = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("PacketPlayOutTitle"), MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutTitle")
      );
      CLASS_TITLE_ACTION = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("PacketPlayOutTitle$EnumTitleAction"),
         MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutTitle$EnumTitleAction")
      );
      CONSTRUCTOR_TITLE_MESSAGE = MinecraftReflection.findConstructor(CLASS_TITLE_PACKET, CLASS_TITLE_ACTION, CLASS_CHAT_COMPONENT);
      CONSTRUCTOR_TITLE_TIMES = MinecraftReflection.findConstructor(CLASS_TITLE_PACKET, int.class, int.class, int.class);
      TITLE_ACTION_TITLE = MinecraftReflection.findEnum(CLASS_TITLE_ACTION, "TITLE", 0);
      TITLE_ACTION_SUBTITLE = MinecraftReflection.findEnum(CLASS_TITLE_ACTION, "SUBTITLE", 1);
      TITLE_ACTION_ACTIONBAR = MinecraftReflection.findEnum(CLASS_TITLE_ACTION, "ACTIONBAR");
      TITLE_ACTION_CLEAR = MinecraftReflection.findEnum(CLASS_TITLE_ACTION, "CLEAR");
      TITLE_ACTION_RESET = MinecraftReflection.findEnum(CLASS_TITLE_ACTION, "RESET");
   }

   protected abstract static class AbstractBook extends CraftBukkitFacet.PacketFacet<Player> implements Facet.Book<Player, Object, ItemStack> {
      protected static final int HAND_MAIN = 0;
      private static final Material BOOK_TYPE = (Material)MinecraftReflection.findEnum(Material.class, "WRITTEN_BOOK");
      private static final ItemStack BOOK_STACK = BOOK_TYPE == null ? null : new ItemStack(BOOK_TYPE);
      private static final String BOOK_TITLE = "title";
      private static final String BOOK_AUTHOR = "author";
      private static final String BOOK_PAGES = "pages";
      private static final String BOOK_RESOLVED = "resolved";
      private static final Class<?> CLASS_NBT_TAG_COMPOUND = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("NBTTagCompound"),
         MinecraftReflection.findMcClassName("nbt.CompoundTag"),
         MinecraftReflection.findMcClassName("nbt.NBTTagCompound")
      );
      private static final Class<?> CLASS_NBT_IO = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("NBTCompressedStreamTools"),
         MinecraftReflection.findMcClassName("nbt.NbtIo"),
         MinecraftReflection.findMcClassName("nbt.NBTCompressedStreamTools")
      );
      private static final MethodHandle NBT_IO_DESERIALIZE;
      private static final Class<?> CLASS_CRAFT_ITEMSTACK;
      private static final Class<?> CLASS_MC_ITEMSTACK;
      private static final MethodHandle MC_ITEMSTACK_SET_TAG;
      private static final MethodHandle CRAFT_ITEMSTACK_NMS_COPY;
      private static final MethodHandle CRAFT_ITEMSTACK_CRAFT_MIRROR;

      protected abstract void sendOpenPacket(@NotNull final Player viewer) throws Throwable;

      @Override
      public boolean isSupported() {
         return super.isSupported()
            && NBT_IO_DESERIALIZE != null
            && MC_ITEMSTACK_SET_TAG != null
            && CRAFT_ITEMSTACK_CRAFT_MIRROR != null
            && CRAFT_ITEMSTACK_NMS_COPY != null
            && BOOK_STACK != null;
      }

      @NotNull
      public String createMessage(@NotNull final Player viewer, @NotNull final Component message) {
         return BukkitComponentSerializer.gson().serialize(message);
      }

      @NotNull
      public ItemStack createBook(@NotNull final String title, @NotNull final String author, @NotNull final Iterable<Object> pages) {
         return this.applyTag(BOOK_STACK, tagFor(title, author, pages));
      }

      @Deprecated
      public void openBook(@NotNull final Player viewer, @NotNull final ItemStack book) {
         PlayerInventory inventory = viewer.getInventory();
         ItemStack current = inventory.getItemInHand();

         try {
            inventory.setItemInHand(book);
            this.sendOpenPacket(viewer);
         } catch (Throwable var9) {
            Knob.logError(var9, "Failed to send openBook packet: %s", book);
         } finally {
            inventory.setItemInHand(current);
         }
      }

      private static CompoundBinaryTag tagFor(@NotNull final String title, @NotNull final String author, @NotNull final Iterable<Object> pages) {
         ListBinaryTag.Builder<StringBinaryTag> builder = ListBinaryTag.builder(BinaryTagTypes.STRING);

         for (Object page : pages) {
            builder.add(StringBinaryTag.of((String)page));
         }

         return CompoundBinaryTag.builder()
            .putString("title", title)
            .putString("author", author)
            .put("pages", builder.build())
            .putByte("resolved", (byte)1)
            .build();
      }

      @NotNull
      private Object createTag(@NotNull final CompoundBinaryTag tag) throws IOException {
         CraftBukkitFacet.AbstractBook.TrustedByteArrayOutputStream output = new CraftBukkitFacet.AbstractBook.TrustedByteArrayOutputStream();
         BinaryTagIO.writer().write(tag, output);

         try {
            DataInputStream dis = new DataInputStream(output.toInputStream());

            Object var4;
            try {
               var4 = (Object)NBT_IO_DESERIALIZE.invoke((DataInputStream)dis);
            } catch (Throwable var7) {
               try {
                  dis.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            dis.close();
            return var4;
         } catch (Throwable var8) {
            throw new IOException(var8);
         }
      }

      private ItemStack applyTag(@NotNull final ItemStack input, final CompoundBinaryTag binTag) {
         if (CRAFT_ITEMSTACK_NMS_COPY != null && MC_ITEMSTACK_SET_TAG != null && CRAFT_ITEMSTACK_CRAFT_MIRROR != null) {
            try {
               Object stack = (Object)CRAFT_ITEMSTACK_NMS_COPY.invoke((ItemStack)input);
               Object tag = this.createTag(binTag);
               MC_ITEMSTACK_SET_TAG.invoke((Object)stack, (Object)tag);
               return (ItemStack)CRAFT_ITEMSTACK_CRAFT_MIRROR.invoke((Object)stack);
            } catch (Throwable var5) {
               Knob.logError(var5, "Failed to apply NBT tag to ItemStack: %s %s", input, binTag);
               return input;
            }
         } else {
            return input;
         }
      }

      static {
         MethodHandle nbtIoDeserialize = null;
         if (CLASS_NBT_IO != null) {
            for (Method method : CLASS_NBT_IO.getDeclaredMethods()) {
               if (Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(CLASS_NBT_TAG_COMPOUND) && method.getParameterCount() == 1) {
                  Class<?> firstParam = method.getParameterTypes()[0];
                  if (firstParam.equals(DataInputStream.class) || firstParam.equals(DataInput.class)) {
                     try {
                        nbtIoDeserialize = MinecraftReflection.lookup().unreflect(method);
                     } catch (IllegalAccessException var7) {
                     }
                     break;
                  }
               }
            }
         }

         NBT_IO_DESERIALIZE = nbtIoDeserialize;
         CLASS_CRAFT_ITEMSTACK = MinecraftReflection.findCraftClass("inventory.CraftItemStack");
         CLASS_MC_ITEMSTACK = MinecraftReflection.findClass(
            MinecraftReflection.findNmsClassName("ItemStack"), MinecraftReflection.findMcClassName("world.item.ItemStack")
         );
         MC_ITEMSTACK_SET_TAG = MinecraftReflection.searchMethod(CLASS_MC_ITEMSTACK, 1, "setTag", void.class, CLASS_NBT_TAG_COMPOUND);
         CRAFT_ITEMSTACK_NMS_COPY = MinecraftReflection.findStaticMethod(CLASS_CRAFT_ITEMSTACK, "asNMSCopy", CLASS_MC_ITEMSTACK, ItemStack.class);
         CRAFT_ITEMSTACK_CRAFT_MIRROR = MinecraftReflection.findStaticMethod(CLASS_CRAFT_ITEMSTACK, "asCraftMirror", CLASS_CRAFT_ITEMSTACK, CLASS_MC_ITEMSTACK);
      }

      private static final class TrustedByteArrayOutputStream extends ByteArrayOutputStream {
         private TrustedByteArrayOutputStream() {
         }

         public InputStream toInputStream() {
            return new ByteArrayInputStream(this.buf, 0, this.count);
         }
      }
   }

   static class ActionBar extends CraftBukkitFacet.PacketFacet<Player> implements Facet.ActionBar<Player, Object> {
      @Override
      public boolean isSupported() {
         return super.isSupported() && CraftBukkitFacet.TITLE_ACTION_ACTIONBAR != null;
      }

      @Nullable
      public Object createMessage(@NotNull final Player viewer, @NotNull final Component message) {
         try {
            return (Object)CraftBukkitFacet.CONSTRUCTOR_TITLE_MESSAGE
               .invoke((Object)CraftBukkitFacet.TITLE_ACTION_ACTIONBAR, (Object)super.createMessage((V)viewer, message));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to invoke PacketPlayOutTitle constructor: %s", message);
            return null;
         }
      }
   }

   static class ActionBarLegacy extends CraftBukkitFacet.PacketFacet<Player> implements Facet.ActionBar<Player, Object> {
      @Override
      public boolean isSupported() {
         return super.isSupported() && CraftBukkitFacet.LEGACY_CHAT_PACKET_CONSTRUCTOR != null;
      }

      @Nullable
      public Object createMessage(@NotNull final Player viewer, @NotNull final Component message) {
         TextComponent legacyMessage = Component.text(BukkitComponentSerializer.legacy().serialize(message));

         try {
            return (Object)CraftBukkitFacet.LEGACY_CHAT_PACKET_CONSTRUCTOR.invoke((Object)super.createMessage((V)viewer, legacyMessage), (byte)2);
         } catch (Throwable var5) {
            Knob.logError(var5, "Failed to invoke PacketPlayOutChat constructor: %s", legacyMessage);
            return null;
         }
      }
   }

   static class ActionBar_1_17 extends CraftBukkitFacet.PacketFacet<Player> implements Facet.ActionBar<Player, Object> {
      @Nullable
      private static final Class<?> CLASS_SET_ACTION_BAR_TEXT_PACKET = MinecraftReflection.findMcClass(
         "network.protocol.game.ClientboundSetActionBarTextPacket"
      );
      @Nullable
      private static final MethodHandle CONSTRUCTOR_ACTION_BAR = MinecraftReflection.findConstructor(
         CLASS_SET_ACTION_BAR_TEXT_PACKET, CraftBukkitFacet.CLASS_CHAT_COMPONENT
      );

      @Override
      public boolean isSupported() {
         return super.isSupported() && CONSTRUCTOR_ACTION_BAR != null;
      }

      @Nullable
      public Object createMessage(@NotNull final Player viewer, @NotNull final Component message) {
         try {
            return (Object)CONSTRUCTOR_ACTION_BAR.invoke((Object)super.createMessage((V)viewer, message));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to invoke PacketPlayOutTitle constructor: %s", message);
            return null;
         }
      }
   }

   static final class Book1_13 extends CraftBukkitFacet.AbstractBook {
      private static final Class<?> CLASS_BYTE_BUF = MinecraftReflection.findClass("io.netty.buffer.ByteBuf");
      private static final Class<?> CLASS_PACKET_CUSTOM_PAYLOAD = MinecraftReflection.findNmsClass("PacketPlayOutCustomPayload");
      private static final Class<?> CLASS_FRIENDLY_BYTE_BUF = MinecraftReflection.findNmsClass("PacketDataSerializer");
      private static final Class<?> CLASS_RESOURCE_LOCATION = MinecraftReflection.findNmsClass("MinecraftKey");
      private static final Object PACKET_TYPE_BOOK_OPEN;
      private static final MethodHandle NEW_PACKET_CUSTOM_PAYLOAD = MinecraftReflection.findConstructor(
         CLASS_PACKET_CUSTOM_PAYLOAD, CLASS_RESOURCE_LOCATION, CLASS_FRIENDLY_BYTE_BUF
      );
      private static final MethodHandle NEW_FRIENDLY_BYTE_BUF = MinecraftReflection.findConstructor(CLASS_FRIENDLY_BYTE_BUF, CLASS_BYTE_BUF);

      @Override
      public boolean isSupported() {
         return super.isSupported() && CLASS_BYTE_BUF != null && NEW_PACKET_CUSTOM_PAYLOAD != null && PACKET_TYPE_BOOK_OPEN != null;
      }

      @Override
      protected void sendOpenPacket(@NotNull final Player viewer) throws Throwable {
         ByteBuf data = Unpooled.buffer();
         data.writeByte(0);
         Object packetByteBuf = (Object)NEW_FRIENDLY_BYTE_BUF.invoke((ByteBuf)data);
         this.sendMessage(viewer, (Object)NEW_PACKET_CUSTOM_PAYLOAD.invoke((Object)PACKET_TYPE_BOOK_OPEN, (Object)packetByteBuf));
      }

      static {
         Object packetType = null;
         if (CLASS_RESOURCE_LOCATION != null) {
            try {
               packetType = CLASS_RESOURCE_LOCATION.getConstructor(String.class).newInstance("minecraft:book_open");
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException var2) {
            }
         }

         PACKET_TYPE_BOOK_OPEN = packetType;
      }
   }

   static final class BookPost1_13 extends CraftBukkitFacet.AbstractBook {
      private static final Class<?> CLASS_ENUM_HAND = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("EnumHand"),
         MinecraftReflection.findMcClassName("world.EnumHand"),
         MinecraftReflection.findMcClassName("world.InteractionHand")
      );
      private static final Object HAND_MAIN = MinecraftReflection.findEnum(CLASS_ENUM_HAND, "MAIN_HAND", 0);
      private static final Class<?> PACKET_OPEN_BOOK = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("PacketPlayOutOpenBook"),
         MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutOpenBook"),
         MinecraftReflection.findMcClassName("network.protocol.game.ClientboundOpenBookPacket")
      );
      private static final MethodHandle NEW_PACKET_OPEN_BOOK = MinecraftReflection.findConstructor(PACKET_OPEN_BOOK, CLASS_ENUM_HAND);

      @Override
      public boolean isSupported() {
         return super.isSupported() && HAND_MAIN != null && NEW_PACKET_OPEN_BOOK != null;
      }

      @Override
      protected void sendOpenPacket(@NotNull final Player viewer) throws Throwable {
         this.sendMessage(viewer, (Object)NEW_PACKET_OPEN_BOOK.invoke((Object)HAND_MAIN));
      }
   }

   static final class BookPre1_13 extends CraftBukkitFacet.AbstractBook {
      private static final String PACKET_TYPE_BOOK_OPEN = "MC|BOpen";
      private static final Class<?> CLASS_BYTE_BUF = MinecraftReflection.findClass("io.netty.buffer.ByteBuf");
      private static final Class<?> CLASS_PACKET_CUSTOM_PAYLOAD = MinecraftReflection.findNmsClass("PacketPlayOutCustomPayload");
      private static final Class<?> CLASS_PACKET_DATA_SERIALIZER = MinecraftReflection.findNmsClass("PacketDataSerializer");
      private static final MethodHandle NEW_PACKET_CUSTOM_PAYLOAD = MinecraftReflection.findConstructor(
         CLASS_PACKET_CUSTOM_PAYLOAD, String.class, CLASS_PACKET_DATA_SERIALIZER
      );
      private static final MethodHandle NEW_PACKET_BYTE_BUF = MinecraftReflection.findConstructor(CLASS_PACKET_DATA_SERIALIZER, CLASS_BYTE_BUF);

      @Override
      public boolean isSupported() {
         return super.isSupported() && CLASS_BYTE_BUF != null && CLASS_PACKET_CUSTOM_PAYLOAD != null && NEW_PACKET_CUSTOM_PAYLOAD != null;
      }

      @Override
      protected void sendOpenPacket(@NotNull final Player viewer) throws Throwable {
         ByteBuf data = Unpooled.buffer();
         data.writeByte(0);
         Object packetByteBuf = (Object)NEW_PACKET_BYTE_BUF.invoke((ByteBuf)data);
         this.sendMessage(viewer, (Object)NEW_PACKET_CUSTOM_PAYLOAD.invoke((String)"MC|BOpen", (Object)packetByteBuf));
      }
   }

   static final class Book_1_20_5 extends CraftBukkitFacet.PacketFacet<Player> implements Facet.Book<Player, Object, ItemStack> {
      @Override
      public boolean isSupported() {
         return super.isSupported() && CraftBukkitAccess.Book_1_20_5.isSupported();
      }

      @Nullable
      public ItemStack createBook(@NotNull final String title, @NotNull final String author, @NotNull final Iterable<Object> pages) {
         try {
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            List<Object> pageList = new ArrayList<>();

            for (Object page : pages) {
               pageList.add((Object)CraftBukkitAccess.Book_1_20_5.CREATE_FILTERABLE.invoke((Object)page));
            }

            Object bookContent = (Object)CraftBukkitAccess.Book_1_20_5.NEW_BOOK_CONTENT
               .invoke(
                  (Object)(Object)CraftBukkitAccess.Book_1_20_5.CREATE_FILTERABLE.invoke((String)title), (String)author, (int)0, (List)pageList, (boolean)true
               );
            Object stack = (Object)CraftBukkitAccess.Book_1_20_5.CRAFT_ITEMSTACK_NMS_COPY.invoke((ItemStack)item);
            CraftBukkitAccess.Book_1_20_5.MC_ITEMSTACK_SET
               .invoke((Object)stack, (Object)CraftBukkitAccess.Book_1_20_5.WRITTEN_BOOK_COMPONENT_TYPE, (Object)bookContent);
            return (ItemStack)CraftBukkitAccess.Book_1_20_5.CRAFT_ITEMSTACK_CRAFT_MIRROR.invoke((Object)stack);
         } catch (Throwable var8) {
            Knob.logError(var8, "Failed to apply written_book_content component to ItemStack");
            return null;
         }
      }

      public void openBook(@NotNull final Player viewer, @NotNull final ItemStack book) {
         PlayerInventory inventory = viewer.getInventory();
         ItemStack current = inventory.getItemInHand();

         try {
            inventory.setItemInHand(book);
            this.sendMessage(viewer, (Object)CraftBukkitAccess.Book_1_20_5.NEW_PACKET_OPEN_BOOK.invoke((Object)CraftBukkitAccess.Book_1_20_5.HAND_MAIN));
         } catch (Throwable var9) {
            Knob.logError(var9, "Failed to send openBook packet: %s", book);
         } finally {
            inventory.setItemInHand(current);
         }
      }
   }

   static final class BossBar extends BukkitFacet.BossBar {
      private static final Class<?> CLASS_CRAFT_BOSS_BAR = MinecraftReflection.findCraftClass("boss.CraftBossBar");
      private static final Class<?> CLASS_BOSS_BAR_ACTION;
      private static final Object BOSS_BAR_ACTION_TITLE;
      private static final MethodHandle CRAFT_BOSS_BAR_HANDLE;
      private static final MethodHandle NMS_BOSS_BATTLE_SET_NAME;
      private static final MethodHandle NMS_BOSS_BATTLE_SEND_UPDATE;

      private BossBar(@NotNull final Collection<Player> viewers) {
         super(viewers);
      }

      @Override
      public void bossBarNameChanged(
         @NotNull final com.earth2me.essentials.libs.kyori.adventure.bossbar.BossBar bar, @NotNull final Component oldName, @NotNull final Component newName
      ) {
         try {
            Object handle = (Object)CRAFT_BOSS_BAR_HANDLE.invoke((org.bukkit.boss.BossBar)this.bar);
            Object text = MinecraftComponentSerializer.get().serialize(newName);
            NMS_BOSS_BATTLE_SET_NAME.invoke((Object)handle, (Object)text);
            NMS_BOSS_BATTLE_SEND_UPDATE.invoke((Object)handle, (Object)BOSS_BAR_ACTION_TITLE);
         } catch (Throwable var6) {
            Knob.logError(var6, "Failed to set CraftBossBar name: %s %s", this.bar, newName);
            super.bossBarNameChanged(bar, oldName, newName);
         }
      }

      static {
         Class<?> classBossBarAction = null;
         Object bossBarActionTitle = null;
         classBossBarAction = MinecraftReflection.findClass(
            MinecraftReflection.findNmsClassName("PacketPlayOutBoss$Action"),
            MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutBoss$Action"),
            MinecraftReflection.findMcClassName("network.protocol.game.ClientboundBossEventPacket$Operation")
         );
         if (classBossBarAction != null && classBossBarAction.isEnum()) {
            bossBarActionTitle = MinecraftReflection.findEnum(classBossBarAction, "UPDATE_NAME", 3);
         } else {
            classBossBarAction = null;
            Class<?> packetClass = MinecraftReflection.findClass(
               MinecraftReflection.findNmsClassName("PacketPlayOutBoss"),
               MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutBoss"),
               MinecraftReflection.findMcClassName("network.protocol.game.ClientboundBossEventPacket")
            );
            Class<?> bossEventClass = MinecraftReflection.findClass(
               MinecraftReflection.findNmsClassName("BossBattle"),
               MinecraftReflection.findMcClassName("world.BossBattle"),
               MinecraftReflection.findMcClassName("world.BossEvent")
            );
            if (packetClass != null && bossEventClass != null) {
               try {
                  MethodType methodType = MethodType.methodType(packetClass, bossEventClass);

                  String methodName;
                  try {
                     packetClass.getDeclaredMethod("createUpdateNamePacket", bossEventClass);
                     methodName = "createUpdateNamePacket";
                  } catch (NoSuchMethodException var11) {
                     methodName = "c";
                  }

                  MethodHandle factoryMethod = MinecraftReflection.lookup().findStatic(packetClass, methodName, methodType);
                  bossBarActionTitle = (Object)LambdaMetafactory.metafactory(
                        MinecraftReflection.lookup(), "apply", MethodType.methodType(Function.class), methodType.generic(), factoryMethod, methodType
                     )
                     .getTarget()
                     .invoke();
                  classBossBarAction = Function.class;
               } catch (Throwable var12) {
                  Knob.logError(var12, "Failed to initialize CraftBossBar constructor");
               }
            }
         }

         CLASS_BOSS_BAR_ACTION = classBossBarAction;
         BOSS_BAR_ACTION_TITLE = bossBarActionTitle;
         MethodHandle craftBossBarHandle = null;
         MethodHandle nmsBossBattleSetName = null;
         MethodHandle nmsBossBattleSendUpdate = null;
         if (CLASS_CRAFT_BOSS_BAR != null && CraftBukkitFacet.CLASS_CHAT_COMPONENT != null && BOSS_BAR_ACTION_TITLE != null) {
            try {
               Field craftBossBarHandleField = MinecraftReflection.needField(CLASS_CRAFT_BOSS_BAR, "handle");
               craftBossBarHandle = MinecraftReflection.lookup().unreflectGetter(craftBossBarHandleField);
               Class<?> nmsBossBattleType = craftBossBarHandleField.getType();

               for (Field field : nmsBossBattleType.getFields()) {
                  if (field.getType().equals(CraftBukkitFacet.CLASS_CHAT_COMPONENT)) {
                     nmsBossBattleSetName = MinecraftReflection.lookup().unreflectSetter(field);
                     break;
                  }
               }

               nmsBossBattleSendUpdate = MinecraftReflection.findMethod(
                  nmsBossBattleType, new String[]{"sendUpdate", "a", "broadcast"}, void.class, CLASS_BOSS_BAR_ACTION
               );
            } catch (Throwable var13) {
               Knob.logError(var13, "Failed to initialize CraftBossBar constructor");
            }
         }

         CRAFT_BOSS_BAR_HANDLE = craftBossBarHandle;
         NMS_BOSS_BATTLE_SET_NAME = nmsBossBattleSetName;
         NMS_BOSS_BATTLE_SEND_UPDATE = nmsBossBattleSendUpdate;
      }

      public static class Builder extends CraftBukkitFacet<Player> implements Facet.BossBar.Builder<Player, CraftBukkitFacet.BossBar> {
         protected Builder() {
            super(Player.class);
         }

         @Override
         public boolean isSupported() {
            return super.isSupported()
               && CraftBukkitFacet.BossBar.CLASS_CRAFT_BOSS_BAR != null
               && CraftBukkitFacet.BossBar.CRAFT_BOSS_BAR_HANDLE != null
               && CraftBukkitFacet.BossBar.NMS_BOSS_BATTLE_SET_NAME != null
               && CraftBukkitFacet.BossBar.NMS_BOSS_BATTLE_SEND_UPDATE != null;
         }

         @NotNull
         public CraftBukkitFacet.BossBar createBossBar(@NotNull final Collection<Player> viewers) {
            return new CraftBukkitFacet.BossBar(viewers);
         }
      }
   }

   static final class BossBarWither extends CraftBukkitFacet.FakeEntity<Wither> implements Facet.BossBarEntity<Player, Location> {
      private volatile boolean initialized = false;

      private BossBarWither(@NotNull final Collection<Player> viewers) {
         super(Wither.class, viewers.iterator().next().getWorld().getSpawnLocation());
         this.invisible(true);
         this.metadata(20, 890);
      }

      @Override
      public void bossBarInitialized(@NotNull final com.earth2me.essentials.libs.kyori.adventure.bossbar.BossBar bar) {
         Facet.BossBarEntity.super.bossBarInitialized(bar);
         this.initialized = true;
      }

      @NotNull
      @Override
      public Location createPosition(@NotNull final Player viewer) {
         Location position = super.createPosition(viewer);
         position.setPitch(position.getPitch() - 30.0F);
         position.setYaw(position.getYaw() + 0.0F);
         position.add(position.getDirection().multiply(40));
         return position;
      }

      @Override
      public boolean isEmpty() {
         return !this.initialized || this.viewers.isEmpty();
      }

      public static class Builder extends CraftBukkitFacet<Player> implements Facet.BossBar.Builder<Player, CraftBukkitFacet.BossBarWither> {
         protected Builder() {
            super(Player.class);
         }

         @NotNull
         public CraftBukkitFacet.BossBarWither createBossBar(@NotNull final Collection<Player> viewers) {
            return new CraftBukkitFacet.BossBarWither(viewers);
         }
      }
   }

   static class Chat extends CraftBukkitFacet.PacketFacet<CommandSender> implements Facet.Chat<CommandSender, Object> {
      @Override
      public boolean isSupported() {
         return super.isSupported() && CraftBukkitFacet.CHAT_PACKET_CONSTRUCTOR != null;
      }

      public void sendMessage(@NotNull final CommandSender viewer, @NotNull final Identity source, @NotNull final Object message, @NotNull final Object type) {
         Object messageType = type == MessageType.CHAT ? CraftBukkitFacet.MESSAGE_TYPE_CHAT : CraftBukkitFacet.MESSAGE_TYPE_SYSTEM;

         try {
            this.sendMessage(viewer, (Object)CraftBukkitFacet.CHAT_PACKET_CONSTRUCTOR.invoke((Object)message, (Object)messageType, (UUID)source.uuid()));
         } catch (Throwable var7) {
            Knob.logError(var7, "Failed to invoke PacketPlayOutChat constructor: %s %s", message, messageType);
         }
      }
   }

   static class Chat1_19_3 extends CraftBukkitFacet.Chat {
      @Override
      public boolean isSupported() {
         return super.isSupported() && CraftBukkitAccess.Chat1_19_3.isSupported();
      }

      @Override
      public void sendMessage(@NotNull final CommandSender viewer, @NotNull final Identity source, @NotNull final Object message, @NotNull final Object type) {
         if (!(type instanceof ChatType.Bound)) {
            super.sendMessage(viewer, source, message, type);
         } else {
            ChatType.Bound bound = (ChatType.Bound)type;

            try {
               Object nameComponent = this.createMessage(viewer, bound.name());
               Object targetComponent = bound.target() != null ? this.createMessage(viewer, bound.target()) : null;
               Object registryAccess = (Object)CraftBukkitAccess.Chat1_19_3.ACTUAL_GET_REGISTRY_ACCESS
                  .invoke(
                     (Object)(Object)CraftBukkitAccess.Chat1_19_3.SERVER_PLAYER_GET_LEVEL
                        .invoke((Object)(Object)CRAFT_PLAYER_GET_HANDLE.invoke((CommandSender)viewer))
                  );
               Object chatTypeRegistry = (Optional)CraftBukkitAccess.Chat1_19_3.REGISTRY_ACCESS_GET_REGISTRY_OPTIONAL
                  .invoke((Object)registryAccess, (Object)CraftBukkitAccess.Chat1_19_3.CHAT_TYPE_RESOURCE_KEY)
                  .orElseThrow(NoSuchElementException::new);
               Object typeResourceLocation = (Object)CraftBukkitAccess.NEW_RESOURCE_LOCATION
                  .invoke((String)bound.type().key().namespace(), (String)bound.type().key().value());
               Object boundNetwork;
               if (CraftBukkitAccess.Chat1_19_3.CHAT_TYPE_BOUND_NETWORK_CONSTRUCTOR != null) {
                  Object chatTypeObject = (Optional)CraftBukkitAccess.Chat1_19_3.REGISTRY_GET_OPTIONAL
                     .invoke((Object)chatTypeRegistry, (Object)typeResourceLocation)
                     .orElseThrow(NoSuchElementException::new);
                  int networkId = (int)CraftBukkitAccess.Chat1_19_3.REGISTRY_GET_ID.invoke((Object)chatTypeRegistry, (Object)chatTypeObject);
                  if (networkId < 0) {
                     throw new IllegalArgumentException("Could not get a valid network id from " + type);
                  }

                  boundNetwork = (Object)CraftBukkitAccess.Chat1_19_3.CHAT_TYPE_BOUND_NETWORK_CONSTRUCTOR
                     .invoke((int)networkId, (Object)nameComponent, (Object)targetComponent);
               } else {
                  Object chatTypeHolder = (Optional)CraftBukkitAccess.Chat1_19_3.REGISTRY_GET_HOLDER
                     .invoke((Object)chatTypeRegistry, (Object)typeResourceLocation)
                     .orElseThrow(NoSuchElementException::new);
                  boundNetwork = (Object)CraftBukkitAccess.Chat1_19_3.CHAT_TYPE_BOUND_CONSTRUCTOR
                     .invoke((Object)chatTypeHolder, (Object)nameComponent, (Optional)Optional.ofNullable(targetComponent));
               }

               this.sendMessage(viewer, (Object)CraftBukkitAccess.Chat1_19_3.DISGUISED_CHAT_PACKET_CONSTRUCTOR.invoke((Object)message, (Object)boundNetwork));
            } catch (Throwable var14) {
               Knob.logError(var14, "Failed to send a 1.19.3+ message: %s %s", message, type);
            }
         }
      }
   }

   static class EntitySound extends CraftBukkitFacet.PacketFacet<Player> implements CraftBukkitFacet.PartialEntitySound {
      private static final Class<?> CLASS_CLIENTBOUND_CUSTOM_SOUND = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("PacketPlayOutCustomSoundEffect"),
         MinecraftReflection.findMcClassName("network.protocol.game.ClientboundCustomSoundPacket"),
         MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutCustomSoundEffect")
      );
      private static final Class<?> CLASS_VEC3 = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("Vec3D"),
         MinecraftReflection.findMcClassName("world.phys.Vec3D"),
         MinecraftReflection.findMcClassName("world.phys.Vec3")
      );
      private static final MethodHandle NEW_CLIENTBOUND_ENTITY_SOUND;
      private static final MethodHandle NEW_CLIENTBOUND_CUSTOM_SOUND;
      private static final MethodHandle NEW_VEC3 = MinecraftReflection.findConstructor(CLASS_VEC3, double.class, double.class, double.class);
      private static final MethodHandle NEW_RESOURCE_LOCATION = MinecraftReflection.findConstructor(
         CraftBukkitAccess.CLASS_RESOURCE_LOCATION, String.class, String.class
      );
      private static final MethodHandle REGISTRY_GET_OPTIONAL = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY, 1, "getOptional", Optional.class, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      private static final Object REGISTRY_SOUND_EVENT;

      @Override
      public boolean isSupported() {
         return super.isSupported()
            && NEW_CLIENTBOUND_ENTITY_SOUND != null
            && NEW_RESOURCE_LOCATION != null
            && REGISTRY_SOUND_EVENT != null
            && REGISTRY_GET_OPTIONAL != null
            && CraftBukkitFacet.CRAFT_ENTITY_GET_HANDLE != null
            && CraftBukkitAccess.EntitySound.isSupported();
      }

      @Override
      public Object createForEntity(final com.earth2me.essentials.libs.kyori.adventure.sound.Sound sound, final Entity entity) {
         try {
            Object nmsEntity = this.toNativeEntity(entity);
            if (nmsEntity == null) {
               return null;
            }

            Object soundCategory = this.toVanilla(sound.source());
            if (soundCategory == null) {
               return null;
            }

            Object nameRl = (Object)NEW_RESOURCE_LOCATION.invoke((String)sound.name().namespace(), (String)sound.name().value());
            Optional<?> event = (Optional)REGISTRY_GET_OPTIONAL.invoke((Object)REGISTRY_SOUND_EVENT, (Object)nameRl);
            long seed = sound.seed().orElseGet(() -> ThreadLocalRandom.current().nextLong());
            if (event.isPresent()) {
               return (Object)NEW_CLIENTBOUND_ENTITY_SOUND.invoke(
                  (Object)event.get(), (Object)soundCategory, (Object)nmsEntity, (float)sound.volume(), (float)sound.pitch(), (long)seed
               );
            }

            if (NEW_CLIENTBOUND_CUSTOM_SOUND != null && NEW_VEC3 != null) {
               Location loc = entity.getLocation();
               return (Object)NEW_CLIENTBOUND_CUSTOM_SOUND.invoke(
                  (Object)nameRl,
                  (Object)soundCategory,
                  (Object)(Object)NEW_VEC3.invoke((double)loc.getX(), (double)loc.getY(), (double)loc.getZ()),
                  (float)sound.volume(),
                  (float)sound.pitch(),
                  (long)seed
               );
            }
         } catch (Throwable var10) {
            Knob.logError(var10, "Failed to send sound tracking an entity");
         }

         return null;
      }

      public void playSound(@NotNull final Player viewer, final Object message) {
         this.sendPacket(viewer, message);
      }

      static {
         MethodHandle entitySoundPacketConstructor = MinecraftReflection.findConstructor(
            CraftBukkitAccess.EntitySound.CLASS_CLIENTBOUND_ENTITY_SOUND,
            CraftBukkitAccess.EntitySound.CLASS_SOUND_EVENT,
            CraftBukkitAccess.EntitySound.CLASS_SOUND_SOURCE,
            CraftBukkitFacet.CLASS_NMS_ENTITY,
            float.class,
            float.class,
            long.class
         );
         if (entitySoundPacketConstructor == null) {
            entitySoundPacketConstructor = MinecraftReflection.findConstructor(
               CraftBukkitAccess.EntitySound.CLASS_CLIENTBOUND_ENTITY_SOUND,
               CraftBukkitAccess.EntitySound.CLASS_SOUND_EVENT,
               CraftBukkitAccess.EntitySound.CLASS_SOUND_SOURCE,
               CraftBukkitFacet.CLASS_NMS_ENTITY,
               float.class,
               float.class
            );
            if (entitySoundPacketConstructor != null) {
               entitySoundPacketConstructor = MethodHandles.dropArguments(entitySoundPacketConstructor, 5, long.class);
            }
         }

         NEW_CLIENTBOUND_ENTITY_SOUND = entitySoundPacketConstructor;
         entitySoundPacketConstructor = MinecraftReflection.findConstructor(
            CLASS_CLIENTBOUND_CUSTOM_SOUND,
            CraftBukkitAccess.CLASS_RESOURCE_LOCATION,
            CraftBukkitAccess.EntitySound.CLASS_SOUND_SOURCE,
            CLASS_VEC3,
            float.class,
            float.class,
            long.class
         );
         if (entitySoundPacketConstructor == null) {
            entitySoundPacketConstructor = MinecraftReflection.findConstructor(
               CLASS_CLIENTBOUND_CUSTOM_SOUND,
               CraftBukkitAccess.CLASS_RESOURCE_LOCATION,
               CraftBukkitAccess.EntitySound.CLASS_SOUND_SOURCE,
               CLASS_VEC3,
               float.class,
               float.class
            );
            if (entitySoundPacketConstructor != null) {
               entitySoundPacketConstructor = MethodHandles.dropArguments(entitySoundPacketConstructor, 5, long.class);
            }
         }

         NEW_CLIENTBOUND_CUSTOM_SOUND = entitySoundPacketConstructor;
         entitySoundPacketConstructor = null;
         if (CraftBukkitAccess.CLASS_REGISTRY != null) {
            try {
               Field soundEventField = MinecraftReflection.findField(CraftBukkitAccess.CLASS_REGISTRY, "SOUND_EVENT");
               if (soundEventField != null) {
                  entitySoundPacketConstructor = (MethodHandle)soundEventField.get(null);
               } else {
                  Object rootRegistry = null;

                  for (Field field : CraftBukkitAccess.CLASS_REGISTRY.getDeclaredFields()) {
                     int mask = 28;
                     if ((field.getModifiers() & 28) == 28 && field.getType().equals(CraftBukkitAccess.CLASS_WRITABLE_REGISTRY)) {
                        field.setAccessible(true);
                        rootRegistry = field.get(null);
                        break;
                     }
                  }

                  if (rootRegistry != null) {
                     entitySoundPacketConstructor = (MethodHandle)(Optional)REGISTRY_GET_OPTIONAL.invoke(
                           (Object)rootRegistry, (Object)(Object)NEW_RESOURCE_LOCATION.invoke((String)"minecraft", (String)"sound_event")
                        )
                        .orElse(null);
                  }
               }
            } catch (Throwable var8) {
               Knob.logError(var8, "Failed to initialize EntitySound CraftBukkit facet");
            }
         }

         REGISTRY_SOUND_EVENT = entitySoundPacketConstructor;
      }
   }

   static class EntitySound_1_19_3 extends CraftBukkitFacet.PacketFacet<Player> implements CraftBukkitFacet.PartialEntitySound {
      @Override
      public boolean isSupported() {
         return CraftBukkitAccess.EntitySound_1_19_3.isSupported() && super.isSupported();
      }

      @Override
      public Object createForEntity(final com.earth2me.essentials.libs.kyori.adventure.sound.Sound sound, final Entity entity) {
         try {
            Object resLoc = (Object)CraftBukkitAccess.NEW_RESOURCE_LOCATION.invoke((String)sound.name().namespace(), (String)sound.name().value());
            Optional<?> possibleSoundEvent = (Optional)CraftBukkitAccess.EntitySound_1_19_3.REGISTRY_GET_OPTIONAL
               .invoke((Object)CraftBukkitAccess.EntitySound_1_19_3.SOUND_EVENT_REGISTRY, (Object)resLoc);
            Object soundEvent;
            if (possibleSoundEvent.isPresent()) {
               soundEvent = possibleSoundEvent.get();
            } else {
               soundEvent = (Object)CraftBukkitAccess.EntitySound_1_19_3.SOUND_EVENT_CREATE_VARIABLE_RANGE.invoke((Object)resLoc);
            }

            Object soundEventHolder = (Object)CraftBukkitAccess.EntitySound_1_19_3.REGISTRY_WRAP_AS_HOLDER
               .invoke((Object)CraftBukkitAccess.EntitySound_1_19_3.SOUND_EVENT_REGISTRY, (Object)soundEvent);
            long seed = sound.seed().orElseGet(() -> ThreadLocalRandom.current().nextLong());
            return (Object)CraftBukkitAccess.EntitySound_1_19_3.NEW_CLIENTBOUND_ENTITY_SOUND
               .invoke(
                  (Object)soundEventHolder,
                  (Object)this.toVanilla(sound.source()),
                  (Object)this.toNativeEntity(entity),
                  (float)sound.volume(),
                  (float)sound.pitch(),
                  (long)seed
               );
         } catch (Throwable var9) {
            Knob.logError(var9, "Failed to send sound tracking an entity");
            return null;
         }
      }

      public void playSound(@NotNull final Player viewer, final Object packet) {
         this.sendPacket(viewer, packet);
      }
   }

   static class FakeEntity<E extends Entity> extends CraftBukkitFacet.PacketFacet<Player> implements Facet.FakeEntity<Player, Location>, Listener {
      private static final Class<? extends World> CLASS_CRAFT_WORLD = MinecraftReflection.findCraftClass("CraftWorld", World.class);
      private static final Class<?> CLASS_NMS_LIVING_ENTITY = MinecraftReflection.findNmsClass("EntityLiving");
      private static final Class<?> CLASS_DATA_WATCHER = MinecraftReflection.findNmsClass("DataWatcher");
      private static final MethodHandle CRAFT_WORLD_CREATE_ENTITY = MinecraftReflection.findMethod(
         CLASS_CRAFT_WORLD, "createEntity", CraftBukkitFacet.CLASS_NMS_ENTITY, Location.class, Class.class
      );
      private static final MethodHandle NMS_ENTITY_GET_BUKKIT_ENTITY = MinecraftReflection.findMethod(
         CraftBukkitFacet.CLASS_NMS_ENTITY, "getBukkitEntity", CraftBukkitFacet.CLASS_CRAFT_ENTITY
      );
      private static final MethodHandle NMS_ENTITY_GET_DATA_WATCHER = MinecraftReflection.findMethod(
         CraftBukkitFacet.CLASS_NMS_ENTITY, "getDataWatcher", CLASS_DATA_WATCHER
      );
      private static final MethodHandle NMS_ENTITY_SET_LOCATION = MinecraftReflection.findMethod(
         CraftBukkitFacet.CLASS_NMS_ENTITY, "setLocation", void.class, double.class, double.class, double.class, float.class, float.class
      );
      private static final MethodHandle NMS_ENTITY_SET_INVISIBLE = MinecraftReflection.findMethod(
         CraftBukkitFacet.CLASS_NMS_ENTITY, "setInvisible", void.class, boolean.class
      );
      private static final MethodHandle DATA_WATCHER_WATCH = MinecraftReflection.findMethod(CLASS_DATA_WATCHER, "watch", void.class, int.class, Object.class);
      private static final Class<?> CLASS_SPAWN_LIVING_PACKET = MinecraftReflection.findNmsClass("PacketPlayOutSpawnEntityLiving");
      private static final MethodHandle NEW_SPAWN_LIVING_PACKET = MinecraftReflection.findConstructor(CLASS_SPAWN_LIVING_PACKET, CLASS_NMS_LIVING_ENTITY);
      private static final Class<?> CLASS_ENTITY_DESTROY_PACKET = MinecraftReflection.findNmsClass("PacketPlayOutEntityDestroy");
      private static final MethodHandle NEW_ENTITY_DESTROY_PACKET = MinecraftReflection.findConstructor(CLASS_ENTITY_DESTROY_PACKET, int[].class);
      private static final Class<?> CLASS_ENTITY_METADATA_PACKET = MinecraftReflection.findNmsClass("PacketPlayOutEntityMetadata");
      private static final MethodHandle NEW_ENTITY_METADATA_PACKET = MinecraftReflection.findConstructor(
         CLASS_ENTITY_METADATA_PACKET, int.class, CLASS_DATA_WATCHER, boolean.class
      );
      private static final Class<?> CLASS_ENTITY_TELEPORT_PACKET = MinecraftReflection.findNmsClass("PacketPlayOutEntityTeleport");
      private static final MethodHandle NEW_ENTITY_TELEPORT_PACKET = MinecraftReflection.findConstructor(
         CLASS_ENTITY_TELEPORT_PACKET, CraftBukkitFacet.CLASS_NMS_ENTITY
      );
      private static final Class<?> CLASS_ENTITY_WITHER = MinecraftReflection.findNmsClass("EntityWither");
      private static final Class<?> CLASS_WORLD = MinecraftReflection.findNmsClass("World");
      private static final Class<?> CLASS_WORLD_SERVER = MinecraftReflection.findNmsClass("WorldServer");
      private static final MethodHandle CRAFT_WORLD_GET_HANDLE = MinecraftReflection.findMethod(CLASS_CRAFT_WORLD, "getHandle", CLASS_WORLD_SERVER);
      private static final MethodHandle NEW_ENTITY_WITHER = MinecraftReflection.findConstructor(CLASS_ENTITY_WITHER, CLASS_WORLD);
      private static final boolean SUPPORTED = (CRAFT_WORLD_CREATE_ENTITY != null || NEW_ENTITY_WITHER != null && CRAFT_WORLD_GET_HANDLE != null)
         && CraftBukkitFacet.CRAFT_ENTITY_GET_HANDLE != null
         && NMS_ENTITY_GET_BUKKIT_ENTITY != null
         && NMS_ENTITY_GET_DATA_WATCHER != null;
      private final E entity;
      private final Object entityHandle;
      protected final Set<Player> viewers;

      protected FakeEntity(@NotNull final Class<E> entityClass, @NotNull final Location location) {
         this(BukkitAudience.PLUGIN.get(), entityClass, location);
      }

      protected FakeEntity(@NotNull final Plugin plugin, @NotNull final Class<E> entityClass, @NotNull final Location location) {
         E entity = null;
         Object handle = null;
         if (SUPPORTED) {
            try {
               if (CRAFT_WORLD_CREATE_ENTITY != null) {
                  Object nmsEntity = (Object)CRAFT_WORLD_CREATE_ENTITY.invoke((World)location.getWorld(), (Location)location, (Class)entityClass);
                  entity = (E)(Entity)NMS_ENTITY_GET_BUKKIT_ENTITY.invoke((Object)nmsEntity);
               } else if (Wither.class.isAssignableFrom(entityClass) && NEW_ENTITY_WITHER != null) {
                  Object nmsEntity = (Object)NEW_ENTITY_WITHER.invoke((Object)(Object)CRAFT_WORLD_GET_HANDLE.invoke((World)location.getWorld()));
                  entity = (E)(Entity)NMS_ENTITY_GET_BUKKIT_ENTITY.invoke((Object)nmsEntity);
               }

               if (CraftBukkitFacet.CLASS_CRAFT_ENTITY.isInstance(entity)) {
                  handle = (Object)CraftBukkitFacet.CRAFT_ENTITY_GET_HANDLE.invoke((Entity)entity);
               }
            } catch (Throwable var7) {
               Knob.logError(var7, "Failed to create fake entity: %s", entityClass.getSimpleName());
            }
         }

         this.entity = entity;
         this.entityHandle = handle;
         this.viewers = new HashSet<>();
         if (this.isSupported()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
         }
      }

      @Override
      public boolean isSupported() {
         return super.isSupported() && this.entity != null && this.entityHandle != null;
      }

      @EventHandler(
         ignoreCancelled = false,
         priority = EventPriority.MONITOR
      )
      public void onPlayerMove(final PlayerMoveEvent event) {
         Player viewer = event.getPlayer();
         if (this.viewers.contains(viewer)) {
            this.teleport(viewer, this.createPosition(viewer));
         }
      }

      @Nullable
      public Object createSpawnPacket() {
         if (this.entity instanceof LivingEntity) {
            try {
               return (Object)NEW_SPAWN_LIVING_PACKET.invoke((Object)this.entityHandle);
            } catch (Throwable var2) {
               Knob.logError(var2, "Failed to create spawn packet: %s", this.entity);
            }
         }

         return null;
      }

      @Nullable
      public Object createDespawnPacket() {
         try {
            return (Object)NEW_ENTITY_DESTROY_PACKET.invoke((int)this.entity.getEntityId());
         } catch (Throwable var2) {
            Knob.logError(var2, "Failed to create despawn packet: %s", this.entity);
            return null;
         }
      }

      @Nullable
      public Object createMetadataPacket() {
         try {
            Object dataWatcher = (Object)NMS_ENTITY_GET_DATA_WATCHER.invoke((Object)this.entityHandle);
            return (Object)NEW_ENTITY_METADATA_PACKET.invoke((int)this.entity.getEntityId(), (Object)dataWatcher, (boolean)false);
         } catch (Throwable var2) {
            Knob.logError(var2, "Failed to create update metadata packet: %s", this.entity);
            return null;
         }
      }

      @Nullable
      public Object createLocationPacket() {
         try {
            return (Object)NEW_ENTITY_TELEPORT_PACKET.invoke((Object)this.entityHandle);
         } catch (Throwable var2) {
            Knob.logError(var2, "Failed to create teleport packet: %s", this.entity);
            return null;
         }
      }

      public void broadcastPacket(@Nullable final Object packet) {
         for (Player viewer : this.viewers) {
            this.sendPacket(viewer, packet);
         }
      }

      @NotNull
      public Location createPosition(@NotNull final Player viewer) {
         return viewer.getLocation();
      }

      @NotNull
      public Location createPosition(final double x, final double y, final double z) {
         return new Location(null, x, y, z);
      }

      public void teleport(@NotNull final Player viewer, @Nullable final Location position) {
         if (position == null) {
            this.viewers.remove(viewer);
            this.sendPacket(viewer, this.createDespawnPacket());
         } else {
            if (!this.viewers.contains(viewer)) {
               this.sendPacket(viewer, this.createSpawnPacket());
               this.viewers.add(viewer);
            }

            try {
               NMS_ENTITY_SET_LOCATION.invoke(
                  (Object)this.entityHandle,
                  (double)position.getX(),
                  (double)position.getY(),
                  (double)position.getZ(),
                  (float)position.getPitch(),
                  (float)position.getYaw()
               );
            } catch (Throwable var4) {
               Knob.logError(var4, "Failed to set entity location: %s %s", this.entity, position);
            }

            this.sendPacket(viewer, this.createLocationPacket());
         }
      }

      @Override
      public void metadata(final int position, @NotNull final Object data) {
         if (DATA_WATCHER_WATCH != null) {
            try {
               Object dataWatcher = (Object)NMS_ENTITY_GET_DATA_WATCHER.invoke((Object)this.entityHandle);
               DATA_WATCHER_WATCH.invoke((Object)dataWatcher, (int)position, (Object)data);
            } catch (Throwable var4) {
               Knob.logError(var4, "Failed to set entity metadata: %s %s=%s", this.entity, position, data);
            }

            this.broadcastPacket(this.createMetadataPacket());
         }
      }

      @Override
      public void invisible(final boolean invisible) {
         if (NMS_ENTITY_SET_INVISIBLE != null) {
            try {
               NMS_ENTITY_SET_INVISIBLE.invoke((Object)this.entityHandle, (boolean)invisible);
            } catch (Throwable var3) {
               Knob.logError(var3, "Failed to change entity visibility: %s", this.entity);
            }
         }
      }

      @Deprecated
      @Override
      public void health(final float health) {
         if (this.entity instanceof Damageable) {
            Damageable entity = (Damageable)this.entity;
            entity.setHealth(health * (entity.getMaxHealth() - 0.1F) + 0.1F);
            this.broadcastPacket(this.createMetadataPacket());
         }
      }

      @Override
      public void name(@NotNull final Component name) {
         this.entity.setCustomName(BukkitComponentSerializer.legacy().serialize(name));
         this.broadcastPacket(this.createMetadataPacket());
      }

      @Override
      public void close() {
         HandlerList.unregisterAll(this);

         for (Player viewer : new LinkedList<>(this.viewers)) {
            this.teleport(viewer, null);
         }
      }
   }

   static class PacketFacet<V extends CommandSender> extends CraftBukkitFacet<V> implements Facet.Message<V, Object> {
      protected PacketFacet() {
         super(CLASS_CRAFT_PLAYER);
      }

      public void sendPacket(@NotNull final Player player, @Nullable final Object packet) {
         if (packet != null) {
            try {
               CraftBukkitFacet.PLAYER_CONNECTION_SEND_PACKET
                  .invoke(
                     (Object)(Object)CraftBukkitFacet.ENTITY_PLAYER_GET_CONNECTION.invoke((Object)(Object)CRAFT_PLAYER_GET_HANDLE.invoke((Player)player)),
                     (Object)packet
                  );
            } catch (Throwable var4) {
               Knob.logError(var4, "Failed to invoke CraftBukkit sendPacket: %s", packet);
            }
         }
      }

      public void sendMessage(@NotNull final V player, @Nullable final Object packet) {
         this.sendPacket((Player)player, packet);
      }

      @Nullable
      public Object createMessage(@NotNull final V viewer, @NotNull final Component message) {
         try {
            return MinecraftComponentSerializer.get().serialize(message);
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to serialize net.minecraft.server IChatBaseComponent: %s", message);
            return null;
         }
      }
   }

   private interface PartialEntitySound extends Facet.EntitySound<Player, Object> {
      Map<String, Object> MC_SOUND_SOURCE_BY_NAME = new ConcurrentHashMap<>();

      default Object createForSelf(final Player viewer, @NotNull final com.earth2me.essentials.libs.kyori.adventure.sound.Sound sound) {
         return this.createForEntity(sound, viewer);
      }

      @Override
      default Object createForEmitter(
         @NotNull final com.earth2me.essentials.libs.kyori.adventure.sound.Sound sound,
         @NotNull final com.earth2me.essentials.libs.kyori.adventure.sound.Sound.Emitter emitter
      ) {
         Entity entity;
         if (emitter instanceof BukkitEmitter) {
            entity = ((BukkitEmitter)emitter).entity;
         } else {
            if (!(emitter instanceof Entity)) {
               return null;
            }

            entity = (Entity)emitter;
         }

         return this.createForEntity(sound, entity);
      }

      default Object toNativeEntity(final Entity entity) throws Throwable {
         return !CraftBukkitFacet.CLASS_CRAFT_ENTITY.isInstance(entity) ? null : (Object)CraftBukkitFacet.CRAFT_ENTITY_GET_HANDLE.invoke((Entity)entity);
      }

      default Object toVanilla(final com.earth2me.essentials.libs.kyori.adventure.sound.Sound.Source source) throws Throwable {
         if (MC_SOUND_SOURCE_BY_NAME.isEmpty()) {
            for (Object enumConstant : CraftBukkitAccess.EntitySound.CLASS_SOUND_SOURCE.getEnumConstants()) {
               MC_SOUND_SOURCE_BY_NAME.put((String)CraftBukkitAccess.EntitySound.SOUND_SOURCE_GET_NAME.invoke((Object)enumConstant), enumConstant);
            }
         }

         return MC_SOUND_SOURCE_BY_NAME.get(com.earth2me.essentials.libs.kyori.adventure.sound.Sound.Source.NAMES.key(source));
      }

      Object createForEntity(com.earth2me.essentials.libs.kyori.adventure.sound.Sound sound, Entity entity);
   }

   static class TabList extends CraftBukkitFacet.PacketFacet<Player> implements Facet.TabList<Player, Object> {
      private static final Class<?> CLIENTBOUND_TAB_LIST_PACKET = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("PacketPlayOutPlayerListHeaderFooter"),
         MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutPlayerListHeaderFooter"),
         MinecraftReflection.findMcClassName("network.protocol.game.ClientboundTabListPacket")
      );
      @Nullable
      private static final MethodHandle CLIENTBOUND_TAB_LIST_PACKET_CTOR_PRE_1_17 = MinecraftReflection.findConstructor(CLIENTBOUND_TAB_LIST_PACKET);
      @Nullable
      protected static final MethodHandle CLIENTBOUND_TAB_LIST_PACKET_CTOR = MinecraftReflection.findConstructor(
         CLIENTBOUND_TAB_LIST_PACKET, CraftBukkitFacet.CLASS_CHAT_COMPONENT, CraftBukkitFacet.CLASS_CHAT_COMPONENT
      );
      @Nullable
      private static final Field CRAFT_PLAYER_TAB_LIST_HEADER = MinecraftReflection.findField(CLASS_CRAFT_PLAYER, "playerListHeader");
      @Nullable
      private static final Field CRAFT_PLAYER_TAB_LIST_FOOTER = MinecraftReflection.findField(CLASS_CRAFT_PLAYER, "playerListFooter");
      @Nullable
      protected static final MethodHandle CLIENTBOUND_TAB_LIST_PACKET_SET_HEADER = first(
         MinecraftReflection.findSetterOf(MinecraftReflection.findField(CLIENTBOUND_TAB_LIST_PACKET, PaperFacet.NATIVE_COMPONENT_CLASS, "adventure$header")),
         MinecraftReflection.findSetterOf(MinecraftReflection.findField(CLIENTBOUND_TAB_LIST_PACKET, CraftBukkitFacet.CLASS_CHAT_COMPONENT, "header", "a"))
      );
      @Nullable
      protected static final MethodHandle CLIENTBOUND_TAB_LIST_PACKET_SET_FOOTER = first(
         MinecraftReflection.findSetterOf(MinecraftReflection.findField(CLIENTBOUND_TAB_LIST_PACKET, PaperFacet.NATIVE_COMPONENT_CLASS, "adventure$footer")),
         MinecraftReflection.findSetterOf(MinecraftReflection.findField(CLIENTBOUND_TAB_LIST_PACKET, CraftBukkitFacet.CLASS_CHAT_COMPONENT, "footer", "b"))
      );

      private static MethodHandle first(final MethodHandle... handles) {
         for (int i = 0; i < handles.length; i++) {
            MethodHandle handle = handles[i];
            if (handle != null) {
               return handle;
            }
         }

         return null;
      }

      @Override
      public boolean isSupported() {
         return (
               CLIENTBOUND_TAB_LIST_PACKET_CTOR != null
                  || CLIENTBOUND_TAB_LIST_PACKET_CTOR_PRE_1_17 != null
                     && CLIENTBOUND_TAB_LIST_PACKET_SET_HEADER != null
                     && CLIENTBOUND_TAB_LIST_PACKET_SET_FOOTER != null
            )
            && super.isSupported();
      }

      protected Object create117Packet(final Player viewer, @Nullable final Object header, @Nullable final Object footer) throws Throwable {
         return (Object)CLIENTBOUND_TAB_LIST_PACKET_CTOR.invoke(
            (Object)(header == null ? this.createMessage(viewer, Component.empty()) : header),
            (Object)(footer == null ? this.createMessage(viewer, Component.empty()) : footer)
         );
      }

      public void send(final Player viewer, @Nullable Object header, @Nullable Object footer) {
         try {
            if (CRAFT_PLAYER_TAB_LIST_HEADER != null && CRAFT_PLAYER_TAB_LIST_FOOTER != null) {
               if (header == null) {
                  header = CRAFT_PLAYER_TAB_LIST_HEADER.get(viewer);
               } else {
                  CRAFT_PLAYER_TAB_LIST_HEADER.set(viewer, header);
               }

               if (footer == null) {
                  footer = CRAFT_PLAYER_TAB_LIST_FOOTER.get(viewer);
               } else {
                  CRAFT_PLAYER_TAB_LIST_FOOTER.set(viewer, footer);
               }
            }

            Object packet;
            if (CLIENTBOUND_TAB_LIST_PACKET_CTOR != null) {
               packet = this.create117Packet(viewer, header, footer);
            } else {
               packet = (Object)CLIENTBOUND_TAB_LIST_PACKET_CTOR_PRE_1_17.invoke();
               CLIENTBOUND_TAB_LIST_PACKET_SET_HEADER.invoke((Object)packet, (Object)(header == null ? this.createMessage(viewer, Component.empty()) : header));
               CLIENTBOUND_TAB_LIST_PACKET_SET_FOOTER.invoke((Object)packet, (Object)(footer == null ? this.createMessage(viewer, Component.empty()) : footer));
            }

            this.sendPacket(viewer, packet);
         } catch (Throwable var5) {
            Knob.logError(var5, "Failed to send tab list header and footer to %s", viewer);
         }
      }
   }

   static class Title extends CraftBukkitFacet.PacketFacet<Player> implements Facet.Title<Player, Object, List<Object>, List<?>> {
      @Override
      public boolean isSupported() {
         return super.isSupported() && CraftBukkitFacet.CONSTRUCTOR_TITLE_MESSAGE != null && CraftBukkitFacet.CONSTRUCTOR_TITLE_TIMES != null;
      }

      @NotNull
      public List<Object> createTitleCollection() {
         return new ArrayList<>();
      }

      public void contributeTitle(@NotNull final List<Object> coll, @NotNull final Object title) {
         try {
            coll.add((Object)CraftBukkitFacet.CONSTRUCTOR_TITLE_MESSAGE.invoke((Object)CraftBukkitFacet.TITLE_ACTION_TITLE, (Object)title));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to invoke title packet constructor");
         }
      }

      public void contributeSubtitle(@NotNull final List<Object> coll, @NotNull final Object subtitle) {
         try {
            coll.add((Object)CraftBukkitFacet.CONSTRUCTOR_TITLE_MESSAGE.invoke((Object)CraftBukkitFacet.TITLE_ACTION_SUBTITLE, (Object)subtitle));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to invoke subtitle packet constructor");
         }
      }

      public void contributeTimes(@NotNull final List<Object> coll, final int inTicks, final int stayTicks, final int outTicks) {
         try {
            coll.add((Object)CraftBukkitFacet.CONSTRUCTOR_TITLE_TIMES.invoke((int)inTicks, (int)stayTicks, (int)outTicks));
         } catch (Throwable var6) {
            Knob.logError(var6, "Failed to invoke title animations packet constructor");
         }
      }

      @Nullable
      public List<?> completeTitle(@NotNull final List<Object> coll) {
         return coll;
      }

      public void showTitle(@NotNull final Player viewer, @NotNull final List<?> packets) {
         for (Object packet : packets) {
            this.sendMessage(viewer, packet);
         }
      }

      public void clearTitle(@NotNull final Player viewer) {
         try {
            if (CraftBukkitFacet.TITLE_ACTION_CLEAR != null) {
               this.sendPacket(viewer, (Object)CraftBukkitFacet.CONSTRUCTOR_TITLE_MESSAGE.invoke((Object)CraftBukkitFacet.TITLE_ACTION_CLEAR, (Void)null));
            } else {
               viewer.sendTitle("", "", -1, -1, -1);
            }
         } catch (Throwable var3) {
            Knob.logError(var3, "Failed to clear title");
         }
      }

      public void resetTitle(@NotNull final Player viewer) {
         try {
            if (CraftBukkitFacet.TITLE_ACTION_RESET != null) {
               this.sendPacket(viewer, (Object)CraftBukkitFacet.CONSTRUCTOR_TITLE_MESSAGE.invoke((Object)CraftBukkitFacet.TITLE_ACTION_RESET, (Void)null));
            } else {
               viewer.resetTitle();
            }
         } catch (Throwable var3) {
            Knob.logError(var3, "Failed to clear title");
         }
      }
   }

   static class Title_1_17 extends CraftBukkitFacet.PacketFacet<Player> implements Facet.Title<Player, Object, List<Object>, List<?>> {
      private static final Class<?> PACKET_SET_TITLE = MinecraftReflection.findMcClass("network.protocol.game.ClientboundSetTitleTextPacket");
      private static final Class<?> PACKET_SET_SUBTITLE = MinecraftReflection.findMcClass("network.protocol.game.ClientboundSetSubtitleTextPacket");
      private static final Class<?> PACKET_SET_TITLE_ANIMATION = MinecraftReflection.findMcClass("network.protocol.game.ClientboundSetTitlesAnimationPacket");
      private static final Class<?> PACKET_CLEAR_TITLES = MinecraftReflection.findMcClass("network.protocol.game.ClientboundClearTitlesPacket");
      private static final MethodHandle CONSTRUCTOR_SET_TITLE = MinecraftReflection.findConstructor(PACKET_SET_TITLE, CraftBukkitFacet.CLASS_CHAT_COMPONENT);
      private static final MethodHandle CONSTRUCTOR_SET_SUBTITLE = MinecraftReflection.findConstructor(
         PACKET_SET_SUBTITLE, CraftBukkitFacet.CLASS_CHAT_COMPONENT
      );
      private static final MethodHandle CONSTRUCTOR_SET_TITLE_ANIMATION = MinecraftReflection.findConstructor(
         PACKET_SET_TITLE_ANIMATION, int.class, int.class, int.class
      );
      private static final MethodHandle CONSTRUCTOR_CLEAR_TITLES = MinecraftReflection.findConstructor(PACKET_CLEAR_TITLES, boolean.class);

      @Override
      public boolean isSupported() {
         return super.isSupported()
            && CONSTRUCTOR_SET_TITLE != null
            && CONSTRUCTOR_SET_SUBTITLE != null
            && CONSTRUCTOR_SET_TITLE_ANIMATION != null
            && CONSTRUCTOR_CLEAR_TITLES != null;
      }

      @NotNull
      public List<Object> createTitleCollection() {
         return new ArrayList<>();
      }

      public void contributeTitle(@NotNull final List<Object> coll, @NotNull final Object title) {
         try {
            coll.add((Object)CONSTRUCTOR_SET_TITLE.invoke((Object)title));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to invoke title packet constructor");
         }
      }

      public void contributeSubtitle(@NotNull final List<Object> coll, @NotNull final Object subtitle) {
         try {
            coll.add((Object)CONSTRUCTOR_SET_SUBTITLE.invoke((Object)subtitle));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to invoke subtitle packet constructor");
         }
      }

      public void contributeTimes(@NotNull final List<Object> coll, final int inTicks, final int stayTicks, final int outTicks) {
         try {
            coll.add((Object)CONSTRUCTOR_SET_TITLE_ANIMATION.invoke((int)inTicks, (int)stayTicks, (int)outTicks));
         } catch (Throwable var6) {
            Knob.logError(var6, "Failed to invoke title animations packet constructor");
         }
      }

      @Nullable
      public List<?> completeTitle(@NotNull final List<Object> coll) {
         return coll;
      }

      public void showTitle(@NotNull final Player viewer, @NotNull final List<?> packets) {
         for (Object packet : packets) {
            this.sendMessage(viewer, packet);
         }
      }

      public void clearTitle(@NotNull final Player viewer) {
         try {
            if (CONSTRUCTOR_CLEAR_TITLES != null) {
               this.sendPacket(viewer, (Object)CONSTRUCTOR_CLEAR_TITLES.invoke((boolean)false));
            } else {
               viewer.sendTitle("", "", -1, -1, -1);
            }
         } catch (Throwable var3) {
            Knob.logError(var3, "Failed to clear title");
         }
      }

      public void resetTitle(@NotNull final Player viewer) {
         try {
            if (CONSTRUCTOR_CLEAR_TITLES != null) {
               this.sendPacket(viewer, (Object)CONSTRUCTOR_CLEAR_TITLES.invoke((boolean)true));
            } else {
               viewer.resetTitle();
            }
         } catch (Throwable var3) {
            Knob.logError(var3, "Failed to clear title");
         }
      }
   }

   static final class Translator extends FacetBase<Server> implements FacetComponentFlattener.Translator<Server> {
      private static final Class<?> CLASS_LANGUAGE = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("LocaleLanguage"),
         MinecraftReflection.findMcClassName("locale.LocaleLanguage"),
         MinecraftReflection.findMcClassName("locale.Language")
      );
      private static final MethodHandle LANGUAGE_GET_INSTANCE;
      private static final MethodHandle LANGUAGE_GET_OR_DEFAULT;

      private static MethodHandle unreflectUnchecked(final Method m) {
         try {
            m.setAccessible(true);
            return MinecraftReflection.lookup().unreflect(m);
         } catch (IllegalAccessException var2) {
            return null;
         }
      }

      Translator() {
         super(Server.class);
      }

      @Override
      public boolean isSupported() {
         return super.isSupported() && LANGUAGE_GET_INSTANCE != null && LANGUAGE_GET_OR_DEFAULT != null;
      }

      @NotNull
      public String valueOrDefault(@NotNull final Server game, @NotNull final String key) {
         try {
            return (String)LANGUAGE_GET_OR_DEFAULT.invoke((Object)(Object)LANGUAGE_GET_INSTANCE.invoke(), (String)key);
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to transate key '%s'", key);
            return key;
         }
      }

      static {
         if (CLASS_LANGUAGE == null) {
            LANGUAGE_GET_INSTANCE = null;
            LANGUAGE_GET_OR_DEFAULT = null;
         } else {
            LANGUAGE_GET_INSTANCE = Arrays.stream(CLASS_LANGUAGE.getDeclaredMethods())
               .filter(
                  m -> Modifier.isStatic(m.getModifiers())
                     && !Modifier.isPrivate(m.getModifiers())
                     && m.getReturnType().equals(CLASS_LANGUAGE)
                     && m.getParameterCount() == 0
               )
               .findFirst()
               .map(CraftBukkitFacet.Translator::unreflectUnchecked)
               .orElse(null);
            LANGUAGE_GET_OR_DEFAULT = Arrays.stream(CLASS_LANGUAGE.getDeclaredMethods())
               .filter(
                  m -> !Modifier.isStatic(m.getModifiers())
                     && Modifier.isPublic(m.getModifiers())
                     && m.getParameterCount() == 1
                     && m.getParameterTypes()[0] == String.class
                     && m.getReturnType().equals(String.class)
               )
               .findFirst()
               .map(CraftBukkitFacet.Translator::unreflectUnchecked)
               .orElse(null);
         }
      }
   }
}
