package com.github.retrooper.packetevents.protocol.chat;

import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.adventure.AdventureIndexUtil;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Index;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ChatType extends MappedEntity, CopyableEntity<ChatType>, DeepComparableEntity {
   @UnknownNullability("only nullable for 1.19")
   ChatTypeDecoration getChatDecoration();

   @Obsolete(
      since = "1.19.1"
   )
   @Nullable
   ChatTypeDecoration getOverlayDecoration();

   @UnknownNullability("only nullable for 1.19")
   ChatTypeDecoration getNarrationDecoration();

   @Obsolete(
      since = "1.19.1"
   )
   @Nullable
   ChatType.NarrationPriority getNarrationPriority();

   static ChatType readDirect(PacketWrapper<?> wrapper) {
      ChatTypeDecoration chatDecoration = ChatTypeDecoration.read(wrapper);
      ChatTypeDecoration narrationDecoration = ChatTypeDecoration.read(wrapper);
      return new StaticChatType(chatDecoration, narrationDecoration);
   }

   static void writeDirect(PacketWrapper<?> wrapper, ChatType chatType) {
      ChatTypeDecoration.write(wrapper, chatType.getChatDecoration());
      ChatTypeDecoration.write(wrapper, chatType.getNarrationDecoration());
   }

   static ChatType decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      NBTCompound chatTag = compound.getCompoundTagOrNull("chat");
      NBTCompound narrationTag = compound.getCompoundTagOrNull("narration");
      ChatTypeDecoration overlay = null;
      ChatType.NarrationPriority narrationPriority = null;
      if (version.isOlderThan(ClientVersion.V_1_19_1)) {
         NBTCompound overlayTag = compound.getCompoundTagOrNull("overlay");
         if (overlayTag != null) {
            overlayTag = overlayTag.getCompoundTagOrNull("description");
            if (overlayTag != null) {
               overlay = ChatTypeDecoration.decode(overlayTag, version);
            }
         }

         if (chatTag != null) {
            chatTag = chatTag.getCompoundTagOrNull("description");
         }

         if (narrationTag != null) {
            narrationPriority = AdventureIndexUtil.indexValueOrThrow(ChatType.NarrationPriority.ID_INDEX, narrationTag.getStringTagValueOrThrow("priority"));
            narrationTag = narrationTag.getCompoundTagOrNull("description");
         }
      } else {
         Objects.requireNonNull(chatTag, "NBT chat does not exist");
         Objects.requireNonNull(narrationTag, "NBT narration does not exist");
      }

      ChatTypeDecoration chat = chatTag == null ? null : ChatTypeDecoration.decode(chatTag, version);
      ChatTypeDecoration narration = narrationTag == null ? null : ChatTypeDecoration.decode(narrationTag, version);
      return new StaticChatType(data, chat, overlay, narration, narrationPriority);
   }

   static NBT encode(ChatType chatType, ClientVersion version) {
      NBTCompound compound = new NBTCompound();
      NBT chatTag = chatType.getChatDecoration() == null ? null : ChatTypeDecoration.encode(chatType.getChatDecoration(), version);
      NBT narrationTag = chatType.getNarrationDecoration() == null ? null : ChatTypeDecoration.encode(chatType.getNarrationDecoration(), version);
      if (version.isOlderThan(ClientVersion.V_1_19_1)) {
         ChatTypeDecoration overlayDeco = chatType.getOverlayDecoration();
         if (overlayDeco != null) {
            NBTCompound overlayCompound = new NBTCompound();
            overlayCompound.setTag("description", ChatTypeDecoration.encode(overlayDeco, version));
            compound.setTag("overlay", overlayCompound);
         }

         if (narrationTag != null) {
            NBTCompound narrationCompound = new NBTCompound();
            narrationCompound.setTag("description", narrationTag);
            if (chatType.getNarrationPriority() != null) {
               narrationCompound.setTag("priority", new NBTString(chatType.getNarrationPriority().getId()));
            }

            narrationTag = narrationCompound;
         }

         if (chatTag != null) {
            NBTCompound chatCompound = new NBTCompound();
            chatCompound.setTag("description", chatTag);
            chatTag = chatCompound;
         }
      }

      if (chatTag != null) {
         compound.setTag("chat", chatTag);
      }

      if (narrationTag != null) {
         compound.setTag("narration", narrationTag);
      }

      return compound;
   }

   public static class Bound extends ChatMessage_v1_19_1.ChatTypeBoundNetwork {
      public Bound(ChatType type, Component name, @Nullable Component targetName) {
         super(type, name, targetName);
      }
   }

   @Obsolete(
      since = "1.19.1"
   )
   public static enum NarrationPriority {
      CHAT("chat"),
      SYSTEM("system");

      public static final Index<String, ChatType.NarrationPriority> ID_INDEX = Index.create(ChatType.NarrationPriority.class, ChatType.NarrationPriority::getId);
      private final String id;

      private NarrationPriority(String id) {
         this.id = id;
      }

      public String getId() {
         return this.id;
      }
   }
}
