package com.viaversion.viaversion.protocols.v1_19to1_19_1;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.SignableCommandArgumentsProvider;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_0;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.data.ChatDecorationResult;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.data.ChatRegistry1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ServerboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.storage.ChatTypeStorage;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.storage.NonceStorage1_19_1;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.ProtocolLogger;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_19To1_19_1
   extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19_1, ServerboundPackets1_19, ServerboundPackets1_19_1> {
   public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_19To1_19_1.class);

   public Protocol1_19To1_19_1() {
      super(ClientboundPackets1_19.class, ClientboundPackets1_19_1.class, ServerboundPackets1_19.class, ServerboundPackets1_19_1.class);
   }

   @Override
   protected void registerPackets() {
      this.registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.COMPONENT);
            this.handler(wrapper -> {
               int type = wrapper.read(Types.VAR_INT);
               boolean overlay = type == 2;
               wrapper.write(Types.BOOLEAN, overlay);
            });
         }
      });
      this.registerClientbound(
         ClientboundPackets1_19.PLAYER_CHAT,
         ClientboundPackets1_19_1.SYSTEM_CHAT,
         new PacketHandlers() {
            @Override
            public void register() {
               this.handler(
                  wrapper -> {
                     JsonElement signedContent = wrapper.read(Types.COMPONENT);
                     JsonElement unsignedContent = wrapper.read(Types.OPTIONAL_COMPONENT);
                     int chatTypeId = wrapper.read(Types.VAR_INT);
                     wrapper.read(Types.UUID);
                     JsonElement senderName = wrapper.read(Types.COMPONENT);
                     JsonElement teamName = wrapper.read(Types.OPTIONAL_COMPONENT);
                     CompoundTag chatType = wrapper.user().get(ChatTypeStorage.class).chatType(chatTypeId);
                     ChatDecorationResult decorationResult = Protocol1_19To1_19_1.decorateChatMessage(
                        chatType, chatTypeId, senderName, teamName, unsignedContent != null ? unsignedContent : signedContent
                     );
                     if (decorationResult == null) {
                        wrapper.cancel();
                     } else {
                        wrapper.write(Types.COMPONENT, decorationResult.content());
                        wrapper.write(Types.BOOLEAN, decorationResult.overlay());
                     }
                  }
               );
               this.read(Types.LONG);
               this.read(Types.LONG);
               this.read(Types.BYTE_ARRAY_PRIMITIVE);
            }
         }
      );
      this.registerServerbound(ServerboundPackets1_19_1.CHAT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.LONG);
            this.map(Types.BYTE_ARRAY_PRIMITIVE);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
               if (chatSession != null) {
                  UUID sender = wrapper.user().getProtocolInfo().getUuid();
                  String message = wrapper.get(Types.STRING, 0);
                  long timestamp = wrapper.get(Types.LONG, 0);
                  long salt = wrapper.get(Types.LONG, 1);
                  MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                  DecoratableMessage decoratableMessage = new DecoratableMessage(message);

                  byte[] signature;
                  try {
                     signature = chatSession.signChatMessage(metadata, decoratableMessage);
                  } catch (SignatureException var12) {
                     throw new RuntimeException(var12);
                  }

                  wrapper.set(Types.BYTE_ARRAY_PRIMITIVE, 0, signature);
                  wrapper.set(Types.BOOLEAN, 0, decoratableMessage.isDecorated());
               }
            });
            this.read(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY);
            this.read(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE);
         }
      });
      this.registerServerbound(ServerboundPackets1_19_1.CHAT_COMMAND, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.LONG);
            this.handler(wrapper -> {
               ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
               SignableCommandArgumentsProvider argumentsProvider = Via.getManager().getProviders().get(SignableCommandArgumentsProvider.class);
               int signatures = wrapper.read(Types.VAR_INT);

               for (int i = 0; i < signatures; i++) {
                  wrapper.read(Types.STRING);
                  wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
               }

               if (chatSession != null && argumentsProvider != null) {
                  UUID sender = wrapper.user().getProtocolInfo().getUuid();
                  String message = wrapper.get(Types.STRING, 0);
                  long timestamp = wrapper.get(Types.LONG, 0);
                  long salt = wrapper.get(Types.LONG, 1);
                  List<Pair<String, String>> arguments = argumentsProvider.getSignableArguments(message);
                  wrapper.write(Types.VAR_INT, arguments.size());

                  for (Pair<String, String> argument : arguments) {
                     MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                     DecoratableMessage decoratableMessage = new DecoratableMessage(argument.value());

                     byte[] signature;
                     try {
                        signature = chatSession.signChatMessage(metadata, decoratableMessage);
                     } catch (SignatureException var17) {
                        throw new RuntimeException(var17);
                     }

                     wrapper.write(Types.STRING, argument.key());
                     wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature);
                  }
               } else {
                  wrapper.write(Types.VAR_INT, 0);
               }
            });
            this.map(Types.BOOLEAN);
            this.read(Types.PLAYER_MESSAGE_SIGNATURE_ARRAY);
            this.read(Types.OPTIONAL_PLAYER_MESSAGE_SIGNATURE);
         }
      });
      this.cancelServerbound(ServerboundPackets1_19_1.CHAT_ACK);
      this.registerClientbound(ClientboundPackets1_19.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.STRING_ARRAY);
            this.handler(wrapper -> {
               ChatTypeStorage chatTypeStorage = wrapper.user().get(ChatTypeStorage.class);
               chatTypeStorage.clear();
               CompoundTag registry = wrapper.passthrough(Types.NAMED_COMPOUND_TAG);

               for (CompoundTag chatType : TagUtil.removeRegistryEntries(registry, "chat_type")) {
                  NumberTag idTag = chatType.getNumberTag("id");
                  chatTypeStorage.addChatType(idTag.asInt(), chatType);
               }

               registry.put("minecraft:chat_type", ChatRegistry1_19_1.chatRegistry());
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_19.SERVER_DATA, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.OPTIONAL_COMPONENT);
            this.map(Types.OPTIONAL_STRING);
            this.map(Types.BOOLEAN);
            this.create(Types.BOOLEAN, Via.getConfig().enforceSecureChat());
         }
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.handler(wrapper -> {
               ProfileKey profileKey = wrapper.read(Types.OPTIONAL_PROFILE_KEY);
               ChatSession1_19_0 chatSession = wrapper.user().get(ChatSession1_19_0.class);
               wrapper.write(Types.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey());
               if (profileKey == null || chatSession != null) {
                  wrapper.user().put(new NonceStorage1_19_1(null));
               }
            });
            this.read(Types.OPTIONAL_UUID);
         }
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.handler(wrapper -> {
               if (!wrapper.user().has(NonceStorage1_19_1.class)) {
                  byte[] publicKey = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                  byte[] nonce = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
                  wrapper.user().put(new NonceStorage1_19_1(CipherUtil.encryptNonce(publicKey, nonce)));
               }
            });
         }
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.BYTE_ARRAY_PRIMITIVE);
            this.handler(wrapper -> {
               NonceStorage1_19_1 nonceStorage = wrapper.user().remove(NonceStorage1_19_1.class);
               if (nonceStorage.nonce() != null) {
                  boolean isNonce = wrapper.read(Types.BOOLEAN);
                  wrapper.write(Types.BOOLEAN, true);
                  if (!isNonce) {
                     wrapper.read(Types.LONG);
                     wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
                     wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
                  }
               }
            });
         }
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.CUSTOM_QUERY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.STRING);
            this.handler(wrapper -> {
               String identifier = wrapper.get(Types.STRING, 0);
               if (identifier.equals("velocity:player_info")) {
                  byte[] data = wrapper.passthrough(Types.REMAINING_BYTES);
                  if (data.length == 1 && data[0] > 1) {
                     data[0] = 1;
                  } else if (data.length == 0) {
                     data = new byte[]{1};
                     wrapper.set(Types.REMAINING_BYTES, 0, data);
                  } else {
                     Protocol1_19To1_19_1.LOGGER.warning("Received unexpected data in velocity:player_info (length=" + data.length + ")");
                  }
               }
            });
         }
      });
   }

   @Override
   public void init(UserConnection connection) {
      connection.put(new ChatTypeStorage());
   }

   @Override
   public ProtocolLogger getLogger() {
      return LOGGER;
   }

   @Nullable
   public static ChatDecorationResult decorateChatMessage(
      CompoundTag chatType, int chatTypeId, JsonElement senderName, @Nullable JsonElement teamName, JsonElement message
   ) {
      if (chatType == null) {
         LOGGER.warning("Chat message has unknown chat type id " + chatTypeId + ". Message: " + message);
         return null;
      } else {
         CompoundTag chatData = chatType.getCompoundTag("element").getCompoundTag("chat");
         boolean overlay = false;
         if (chatData == null) {
            chatData = chatType.getCompoundTag("element").getCompoundTag("overlay");
            if (chatData == null) {
               return null;
            }

            overlay = true;
         }

         CompoundTag decoration = chatData.getCompoundTag("decoration");
         return decoration == null
            ? new ChatDecorationResult(message, overlay)
            : new ChatDecorationResult(translatabaleComponentFromTag(decoration, senderName, teamName, message), overlay);
      }
   }

   public static JsonElement translatabaleComponentFromTag(CompoundTag tag, JsonElement senderName, @Nullable JsonElement targetName, JsonElement message) {
      String translationKey = tag.getStringTag("translation_key").getValue();
      Style style = new Style();
      CompoundTag styleTag = tag.getCompoundTag("style");
      if (styleTag != null) {
         StringTag color = styleTag.getStringTag("color");
         if (color != null) {
            TextFormatting textColor = TextFormatting.getByName(color.getValue());
            if (textColor != null) {
               style.setFormatting(textColor);
            }
         }

         for (Entry<String, TextFormatting> entry : TextFormatting.FORMATTINGS.entrySet()) {
            NumberTag formattingTag = styleTag.getNumberTag(entry.getKey());
            if (formattingTag instanceof ByteTag) {
               boolean value = formattingTag.asBoolean();
               TextFormatting formatting = entry.getValue();
               if (formatting == TextFormatting.OBFUSCATED) {
                  style.setObfuscated(value);
               } else if (formatting == TextFormatting.BOLD) {
                  style.setBold(value);
               } else if (formatting == TextFormatting.STRIKETHROUGH) {
                  style.setStrikethrough(value);
               } else if (formatting == TextFormatting.UNDERLINE) {
                  style.setUnderlined(value);
               } else if (formatting == TextFormatting.ITALIC) {
                  style.setItalic(value);
               }
            }
         }
      }

      ListTag<StringTag> parameters = tag.getListTag("parameters", StringTag.class);
      List<TextComponent> arguments = new ArrayList<>();
      if (parameters != null) {
         for (StringTag element : parameters) {
            JsonElement argument = null;
            String var20 = element.getValue();
            switch (var20) {
               case "sender":
                  argument = senderName;
                  break;
               case "content":
                  argument = message;
                  break;
               case "team_name":
               case "target":
                  Preconditions.checkNotNull(targetName, "Team name is null");
                  argument = targetName;
                  break;
               default:
                  LOGGER.warning("Unknown parameter for chat decoration: " + element.getValue());
            }

            if (argument != null) {
               arguments.add(SerializerVersion.V1_18.toComponent(argument));
            }
         }
      }

      return SerializerVersion.V1_18.toJson(new TranslationComponent(translationKey, arguments));
   }
}
