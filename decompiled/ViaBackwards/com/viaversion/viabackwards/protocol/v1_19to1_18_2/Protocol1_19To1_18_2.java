package com.viaversion.viabackwards.protocol.v1_19to1_18_2;

import com.google.common.primitives.Longs;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.SoundRewriter;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.data.BackwardsMappingData1_19;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.rewriter.BlockItemPacketRewriter1_19;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.rewriter.CommandRewriter1_19;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.rewriter.EntityPacketRewriter1_19;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.DimensionRegistryStorage;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.EntityTracker1_19;
import com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage.NonceStorage;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.signature.SignableCommandArgumentsProvider;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_0;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.Protocol1_19To1_19_1;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.data.ChatDecorationResult;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase.ReadType;
import com.viaversion.viaversion.util.Pair;
import java.security.SignatureException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class Protocol1_19To1_18_2
   extends BackwardsProtocol<ClientboundPackets1_19, ClientboundPackets1_18, ServerboundPackets1_19, ServerboundPackets1_17> {
   public static final BackwardsMappingData1_19 MAPPINGS = new BackwardsMappingData1_19();
   private static final UUID ZERO_UUID = new UUID(0L, 0L);
   private static final byte[] EMPTY_BYTES = new byte[0];
   private final EntityPacketRewriter1_19 entityRewriter = new EntityPacketRewriter1_19(this);
   private final BlockItemPacketRewriter1_19 blockItemPackets = new BlockItemPacketRewriter1_19(this);
   private final ParticleRewriter<ClientboundPackets1_19> particleRewriter = new ParticleRewriter(this);
   private final JsonNBTComponentRewriter<ClientboundPackets1_19> translatableRewriter = new JsonNBTComponentRewriter(this, ReadType.JSON);
   private final TagRewriter<ClientboundPackets1_19> tagRewriter = new TagRewriter(this);
   private final BlockRewriter<ClientboundPackets1_19> blockRewriter = BlockRewriter.for1_18(this, ChunkType1_18::new);

   public Protocol1_19To1_18_2() {
      super(ClientboundPackets1_19.class, ClientboundPackets1_18.class, ServerboundPackets1_19.class, ServerboundPackets1_17.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      final SoundRewriter<ClientboundPackets1_19> soundRewriter = new SoundRewriter(this);
      this.replaceClientbound(ClientboundPackets1_19.SOUND, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.read(Types.LONG);
            this.handler(soundRewriter.getSoundHandler());
         }
      });
      this.replaceClientbound(ClientboundPackets1_19.SOUND_ENTITY, new PacketHandlers() {
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.read(Types.LONG);
            this.handler(soundRewriter.getSoundHandler());
         }
      });
      this.replaceClientbound(ClientboundPackets1_19.CUSTOM_SOUND, new PacketHandlers() {
         public void register() {
            this.map(Types.STRING);
            this.map(Types.VAR_INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.read(Types.LONG);
            this.handler(soundRewriter.getNamedSoundHandler());
         }
      });
      this.tagRewriter.removeTags("minecraft:banner_pattern");
      this.tagRewriter.removeTags("minecraft:instrument");
      this.tagRewriter.removeTags("minecraft:cat_variant");
      this.tagRewriter.removeTags("minecraft:painting_variant");
      this.tagRewriter.addEmptyTag(RegistryType.BLOCK, "minecraft:polar_bears_spawnable_on_in_frozen_ocean");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "minecraft:wool_carpets", "minecraft:carpets");
      this.tagRewriter.renameTag(RegistryType.ITEM, "minecraft:wool_carpets", "minecraft:carpets");
      this.tagRewriter.addEmptyTag(RegistryType.ITEM, "minecraft:occludes_vibration_signals");
      CommandRewriter<ClientboundPackets1_19> commandRewriter = new CommandRewriter1_19(this);
      this.replaceClientbound(ClientboundPackets1_19.COMMANDS, wrapper -> {
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            byte flags = (Byte)wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            if ((flags & 8) != 0) {
               wrapper.passthrough(Types.VAR_INT);
            }

            int nodeType = flags & 3;
            if (nodeType == 1 || nodeType == 2) {
               wrapper.passthrough(Types.STRING);
            }

            if (nodeType == 2) {
               int argumentTypeId = (Integer)wrapper.read(Types.VAR_INT);
               String argumentType = MAPPINGS.getArgumentTypeMappings().identifier(argumentTypeId);
               if (argumentType == null) {
                  this.getLogger().warning("Unknown command argument type id: " + argumentTypeId);
                  argumentType = "minecraft:no";
               }

               wrapper.write(Types.STRING, commandRewriter.handleArgumentType(argumentType));
               commandRewriter.handleArgument(wrapper, argumentType);
               if ((flags & 16) != 0) {
                  wrapper.passthrough(Types.STRING);
               }
            }
         }

         wrapper.passthrough(Types.VAR_INT);
      });
      this.cancelClientbound(ClientboundPackets1_19.SERVER_DATA);
      this.cancelClientbound(ClientboundPackets1_19.CHAT_PREVIEW);
      this.cancelClientbound(ClientboundPackets1_19.SET_DISPLAY_CHAT_PREVIEW);
      this.registerClientbound(
         ClientboundPackets1_19.PLAYER_CHAT,
         ClientboundPackets1_18.CHAT,
         new PacketHandlers() {
            public void register() {
               this.handler(
                  wrapper -> {
                     JsonElement signedContent = (JsonElement)wrapper.read(Types.COMPONENT);
                     JsonElement unsignedContent = (JsonElement)wrapper.read(Types.OPTIONAL_COMPONENT);
                     int chatTypeId = (Integer)wrapper.read(Types.VAR_INT);
                     UUID sender = (UUID)wrapper.read(Types.UUID);
                     JsonElement senderName = (JsonElement)wrapper.read(Types.COMPONENT);
                     JsonElement teamName = (JsonElement)wrapper.read(Types.OPTIONAL_COMPONENT);
                     CompoundTag chatType = ((DimensionRegistryStorage)wrapper.user().get(DimensionRegistryStorage.class)).chatType(chatTypeId);
                     ChatDecorationResult decorationResult = Protocol1_19To1_19_1.decorateChatMessage(
                        chatType, chatTypeId, senderName, teamName, unsignedContent != null ? unsignedContent : signedContent
                     );
                     if (decorationResult == null) {
                        wrapper.cancel();
                     } else {
                        Protocol1_19To1_18_2.this.translatableRewriter.processText(wrapper.user(), decorationResult.content());
                        wrapper.write(Types.COMPONENT, decorationResult.content());
                        wrapper.write(Types.BYTE, Byte.valueOf((byte)(decorationResult.overlay() ? 2 : 1)));
                        wrapper.write(Types.UUID, sender);
                     }
                  }
               );
               this.read(Types.LONG);
               this.read(Types.LONG);
               this.read(Types.BYTE_ARRAY_PRIMITIVE);
            }
         }
      );
      this.registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, ClientboundPackets1_18.CHAT, new PacketHandlers() {
         public void register() {
            this.handler(wrapper -> {
               JsonElement content = (JsonElement)wrapper.passthrough(Types.COMPONENT);
               Protocol1_19To1_18_2.this.translatableRewriter.processText(wrapper.user(), content);
               int typeId = (Integer)wrapper.read(Types.VAR_INT);
               wrapper.write(Types.BYTE, Byte.valueOf((byte)(typeId == 2 ? 2 : 0)));
            });
            this.create(Types.UUID, Protocol1_19To1_18_2.ZERO_UUID);
         }
      }, true);
      this.registerServerbound(
         ServerboundPackets1_17.CHAT,
         new PacketHandlers() {
            public void register() {
               this.map(Types.STRING);
               this.handler(
                  wrapper -> {
                     ChatSession1_19_0 chatSession = (ChatSession1_19_0)wrapper.user().get(ChatSession1_19_0.class);
                     UUID sender = wrapper.user().getProtocolInfo().getUuid();
                     Instant timestamp = Instant.now();
                     long salt = ThreadLocalRandom.current().nextLong();
                     wrapper.write(Types.LONG, timestamp.toEpochMilli());
                     wrapper.write(Types.LONG, chatSession != null ? salt : 0L);
                     String message = (String)wrapper.get(Types.STRING, 0);
                     if (!message.isEmpty() && message.charAt(0) == '/') {
                        String command = message.substring(1);
                        wrapper.setPacketType(ServerboundPackets1_19.CHAT_COMMAND);
                        wrapper.set(Types.STRING, 0, command);
                        SignableCommandArgumentsProvider argumentsProvider = (SignableCommandArgumentsProvider)Via.getManager()
                           .getProviders()
                           .get(SignableCommandArgumentsProvider.class);
                        if (chatSession != null && argumentsProvider != null) {
                           MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                           List<Pair<String, String>> arguments = argumentsProvider.getSignableArguments(command);
                           wrapper.write(Types.VAR_INT, arguments.size());

                           for (Pair<String, String> argument : arguments) {
                              byte[] signature;
                              try {
                                 signature = chatSession.signChatMessage(metadata, new DecoratableMessage((String)argument.value()));
                              } catch (SignatureException var16) {
                                 throw new RuntimeException(var16);
                              }

                              wrapper.write(Types.STRING, (String)argument.key());
                              wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature);
                           }
                        } else {
                           wrapper.write(Types.VAR_INT, 0);
                        }
                     } else if (chatSession != null) {
                        MessageMetadata metadata = new MessageMetadata(sender, timestamp, salt);
                        DecoratableMessage decoratableMessage = new DecoratableMessage(message);

                        byte[] signature;
                        try {
                           signature = chatSession.signChatMessage(metadata, decoratableMessage);
                        } catch (SignatureException var15) {
                           throw new RuntimeException(var15);
                        }

                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature);
                     } else {
                        wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, Protocol1_19To1_18_2.EMPTY_BYTES);
                     }

                     wrapper.write(Types.BOOLEAN, false);
                  }
               );
            }
         }
      );
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, new PacketHandlers() {
         public void register() {
            this.map(Types.UUID);
            this.map(Types.STRING);
            this.read(Types.PROFILE_PROPERTY_ARRAY);
         }
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, new PacketHandlers() {
         public void register() {
            this.map(Types.STRING);
            this.map(Types.BYTE_ARRAY_PRIMITIVE);
            this.handler(wrapper -> {
               if (wrapper.user().has(ChatSession1_19_0.class)) {
                  wrapper.user().put(new NonceStorage((byte[])wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE)));
               }
            });
         }
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, new PacketHandlers() {
         public void register() {
            this.map(Types.STRING);
            this.handler(wrapper -> {
               ChatSession1_19_0 chatSession = (ChatSession1_19_0)wrapper.user().get(ChatSession1_19_0.class);
               wrapper.write(Types.OPTIONAL_PROFILE_KEY, chatSession == null ? null : chatSession.getProfileKey());
            });
         }
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY, new PacketHandlers() {
         public void register() {
            this.map(Types.BYTE_ARRAY_PRIMITIVE);
            this.handler(wrapper -> {
               ChatSession1_19_0 chatSession = (ChatSession1_19_0)wrapper.user().get(ChatSession1_19_0.class);
               byte[] verifyToken = (byte[])wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
               wrapper.write(Types.BOOLEAN, chatSession == null);
               if (chatSession != null) {
                  long salt = ThreadLocalRandom.current().nextLong();

                  byte[] signature;
                  try {
                     signature = chatSession.sign(signer -> {
                        signer.accept(((NonceStorage)wrapper.user().remove(NonceStorage.class)).nonce());
                        signer.accept(Longs.toByteArray(salt));
                     });
                  } catch (SignatureException var7) {
                     throw new RuntimeException(var7);
                  }

                  wrapper.write(Types.LONG, salt);
                  wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, signature);
               } else {
                  wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, verifyToken);
               }
            });
         }
      });
   }

   public void init(UserConnection user) {
      user.put(new DimensionRegistryStorage());
      this.addEntityTracker(user, new EntityTracker1_19(user));
   }

   public BackwardsMappingData1_19 getMappingData() {
      return MAPPINGS;
   }

   public JsonNBTComponentRewriter<ClientboundPackets1_19> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public EntityPacketRewriter1_19 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_19 getItemRewriter() {
      return this.blockItemPackets;
   }

   public BlockRewriter<ClientboundPackets1_19> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_19> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPackets1_19> getTagRewriter() {
      return this.tagRewriter;
   }
}
