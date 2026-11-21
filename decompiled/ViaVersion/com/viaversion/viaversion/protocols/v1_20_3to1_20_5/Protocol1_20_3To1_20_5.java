package com.viaversion.viaversion.protocols.v1_20_3to1_20_5;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MappingData1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.BlockItemPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.ComponentRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.EntityPacketRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter.ParticleRewriter1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.AcknowledgedMessagesStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ProtocolStorables1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ScoreboardTeamStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.TagKeys;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.ProtocolLogger;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Protocol1_20_3To1_20_5
   extends AbstractProtocol<ClientboundPacket1_20_3, ClientboundPacket1_20_5, ServerboundPacket1_20_3, ServerboundPacket1_20_5> {
   public static final MappingData1_20_5 MAPPINGS = new MappingData1_20_5();
   public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_20_3To1_20_5.class);
   public static boolean strictErrorHandling = System.getProperty("viaversion.strict-error-handling1_20_5", "true").equalsIgnoreCase("true");
   private final EntityPacketRewriter1_20_5 entityRewriter = new EntityPacketRewriter1_20_5(this);
   private final BlockItemPacketRewriter1_20_5 itemRewriter = new BlockItemPacketRewriter1_20_5(this);
   private final ParticleRewriter1_20_5 particleRewriter = new ParticleRewriter1_20_5(this);
   private final TagRewriter<ClientboundPacket1_20_3> tagRewriter = new TagRewriter<ClientboundPacket1_20_3>(this) {
      @Override
      public void handleGeneric(PacketWrapper wrapper) {
         super.handleGeneric(wrapper);
         wrapper.resetReader();
         ProtocolStorables1_20_5 storables = wrapper.user().storables(Protocol1_20_3To1_20_5.this);
         storables.setTagKeys(new TagKeys(wrapper));
      }
   };
   private final ComponentRewriter1_20_5<ClientboundPacket1_20_3> componentRewriter = new ComponentRewriter1_20_5<>(this, VersionedTypes.V1_20_5.structuredData);
   private final BlockRewriter<ClientboundPacket1_20_3> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_20_2::new);

   public Protocol1_20_3To1_20_5() {
      super(ClientboundPacket1_20_3.class, ClientboundPacket1_20_5.class, ServerboundPacket1_20_3.class, ServerboundPacket1_20_5.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
         wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
         wrapper.write(Types.BOOLEAN, true);
      });
      this.replaceClientbound(ClientboundPackets1_20_3.SERVER_DATA, wrapper -> {
         this.componentRewriter.passthroughAndProcess(wrapper);
         wrapper.passthrough(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE);
         boolean enforcesSecureChat = wrapper.read(Types.BOOLEAN);
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         AcknowledgedMessagesStorage storage = storables.acknowledgedMessages();
         storage.setSecureChatEnforced(enforcesSecureChat);
         if (enforcesSecureChat) {
            storage.sendQueuedChatSession(wrapper);
         }
      });
      this.registerServerbound(ServerboundPackets1_20_5.CHAT, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         AcknowledgedMessagesStorage storage = storables.acknowledgedMessages();
         long salt = wrapper.read(Types.LONG);
         byte[] signature = wrapper.read(Types.OPTIONAL_SIGNATURE_BYTES);
         if (storage.isSecureChatEnforced()) {
            wrapper.write(Types.LONG, salt);
            wrapper.write(Types.OPTIONAL_SIGNATURE_BYTES, signature);
         } else {
            wrapper.write(Types.LONG, 0L);
            wrapper.write(Types.OPTIONAL_SIGNATURE_BYTES, null);
         }

         this.fixChatAck(wrapper, storage);
      });
      this.registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND_SIGNED, ServerboundPackets1_20_3.CHAT_COMMAND, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         AcknowledgedMessagesStorage storage = storables.acknowledgedMessages();
         long salt = wrapper.read(Types.LONG);
         int signatures = wrapper.read(Types.VAR_INT);
         if (storage.isSecureChatEnforced()) {
            wrapper.write(Types.LONG, salt);
            wrapper.write(Types.VAR_INT, signatures);

            for (int i = 0; i < signatures; i++) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.SIGNATURE_BYTES);
            }
         } else {
            wrapper.write(Types.LONG, 0L);
            wrapper.write(Types.VAR_INT, 0);

            for (int i = 0; i < signatures; i++) {
               wrapper.read(Types.STRING);
               wrapper.read(Types.SIGNATURE_BYTES);
            }
         }

         this.fixChatAck(wrapper, storage);
      });
      this.registerServerbound(ServerboundPackets1_20_5.CHAT_ACK, wrapper -> {
         int offset = wrapper.read(Types.VAR_INT);
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         int fixedOffset = storables.acknowledgedMessages().accumulateAckCount(offset);
         if (fixedOffset > 0) {
            wrapper.write(Types.VAR_INT, fixedOffset);
         } else {
            wrapper.cancel();
         }
      });
      this.registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.write(Types.LONG, System.currentTimeMillis());
         wrapper.write(Types.LONG, 0L);
         wrapper.write(Types.VAR_INT, 0);
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         this.writeSpoofedChatAck(wrapper, storables.acknowledgedMessages());
      });
      this.registerServerbound(ServerboundPackets1_20_5.CHAT_SESSION_UPDATE, wrapper -> {
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         AcknowledgedMessagesStorage storage = storables.acknowledgedMessages();
         if (storage.secureChatEnforced() == null || !storage.secureChatEnforced()) {
            UUID sessionId = wrapper.read(Types.UUID);
            ProfileKey profileKey = wrapper.read(Types.PROFILE_KEY);
            storage.queueChatSession(sessionId, profileKey);
            wrapper.cancel();
         }
      });
      this.appendClientbound(ClientboundPackets1_20_3.START_CONFIGURATION, wrapper -> {
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         storables.setAcknowledgedMessages(new AcknowledgedMessagesStorage());
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
         wrapper.write(Types.BOOLEAN, strictErrorHandling);
      });
      this.replaceClientbound(ClientboundPackets1_20_3.SET_PLAYER_TEAM, wrapper -> {
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this);
         ScoreboardTeamStorage storage = storables.scoreboardTeams();
         String teamName = wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0) {
            this.componentRewriter.passthroughAndProcess(wrapper);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.VAR_INT);
            this.componentRewriter.passthroughAndProcess(wrapper);
            this.componentRewriter.passthroughAndProcess(wrapper);
            storage.createTeam(teamName);
            String[] players = wrapper.passthrough(Types.STRING_ARRAY);
            storage.addPlayerToTeam(teamName, players);
         } else if (action == 1) {
            storage.removeTeam(teamName);
         } else if (action == 3) {
            String[] players = wrapper.passthrough(Types.STRING_ARRAY);
            storage.addPlayerToTeam(teamName, players);
         }

         if (action == 4) {
            String[] players = wrapper.read(Types.STRING_ARRAY);
            Set<String> filteredPlayers = new HashSet<>();

            for (String player : players) {
               String team = storage.getPlayerTeam(player);
               if (!Objects.equals(team, teamName)) {
                  break;
               }

               storage.removeFromTeam(teamName, player);
               filteredPlayers.add(player);
            }

            if (!filteredPlayers.isEmpty()) {
               wrapper.write(Types.STRING_ARRAY, filteredPlayers.toArray(new String[0]));
            } else {
               wrapper.cancel();
            }
         }
      });
      this.cancelServerbound(State.LOGIN, ServerboundLoginPackets.COOKIE_RESPONSE.getId());
      this.cancelServerbound(ServerboundConfigurationPackets1_20_5.COOKIE_RESPONSE);
      this.cancelServerbound(ServerboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
      this.cancelServerbound(ServerboundPackets1_20_5.COOKIE_RESPONSE);
      this.cancelServerbound(ServerboundPackets1_20_5.DEBUG_SUBSCRIPTION_REQUEST);
   }

   private void fixChatAck(PacketWrapper wrapper, AcknowledgedMessagesStorage storage) {
      int offset = wrapper.read(Types.VAR_INT);
      BitSet acknowledged = wrapper.read(Types.ACKNOWLEDGED_BIT_SET);
      int fixedOffset = storage.updateFromMessage(offset, acknowledged);
      wrapper.write(Types.VAR_INT, fixedOffset);
      wrapper.write(Types.ACKNOWLEDGED_BIT_SET, acknowledged);
   }

   private void writeSpoofedChatAck(PacketWrapper wrapper, AcknowledgedMessagesStorage storage) {
      wrapper.write(Types.VAR_INT, 0);
      wrapper.write(Types.ACKNOWLEDGED_BIT_SET, storage.createSpoofedAck());
   }

   @Override
   protected void onMappingDataLoaded() {
      EntityTypes1_20_5.initialize(this);
      ParticleType.Fillers.fill1_20_5(this, VersionedTypes.V1_20_5.particle);
      VersionedTypes.V1_20_5
         .structuredData
         .filler(this)
         .add(StructuredDataKey.CUSTOM_DATA)
         .add(StructuredDataKey.MAX_STACK_SIZE)
         .add(StructuredDataKey.MAX_DAMAGE)
         .add(StructuredDataKey.DAMAGE)
         .add(StructuredDataKey.UNBREAKABLE1_20_5)
         .add(StructuredDataKey.RARITY)
         .add(StructuredDataKey.HIDE_TOOLTIP)
         .add(StructuredDataKey.FOOD1_20_5)
         .add(StructuredDataKey.FIRE_RESISTANT)
         .add(StructuredDataKey.CUSTOM_NAME)
         .add(StructuredDataKey.LORE)
         .add(StructuredDataKey.ENCHANTMENTS1_20_5)
         .add(StructuredDataKey.CAN_PLACE_ON1_20_5)
         .add(StructuredDataKey.CAN_BREAK1_20_5)
         .add(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5)
         .add(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5)
         .add(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP)
         .add(StructuredDataKey.REPAIR_COST)
         .add(StructuredDataKey.CREATIVE_SLOT_LOCK)
         .add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE)
         .add(StructuredDataKey.INTANGIBLE_PROJECTILE)
         .add(StructuredDataKey.STORED_ENCHANTMENTS1_20_5)
         .add(StructuredDataKey.DYED_COLOR1_20_5)
         .add(StructuredDataKey.MAP_COLOR)
         .add(StructuredDataKey.MAP_ID)
         .add(StructuredDataKey.MAP_DECORATIONS)
         .add(StructuredDataKey.MAP_POST_PROCESSING)
         .add(StructuredDataKey.POTION_CONTENTS1_20_5)
         .add(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS)
         .add(StructuredDataKey.WRITABLE_BOOK_CONTENT)
         .add(StructuredDataKey.WRITTEN_BOOK_CONTENT)
         .add(StructuredDataKey.TRIM1_20_5)
         .add(StructuredDataKey.DEBUG_STICK_STATE)
         .add(StructuredDataKey.ENTITY_DATA1_20_5)
         .add(StructuredDataKey.BUCKET_ENTITY_DATA)
         .add(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5)
         .add(StructuredDataKey.INSTRUMENT1_20_5)
         .add(StructuredDataKey.RECIPES)
         .add(StructuredDataKey.LODESTONE_TRACKER)
         .add(StructuredDataKey.FIREWORK_EXPLOSION)
         .add(StructuredDataKey.FIREWORKS)
         .add(StructuredDataKey.PROFILE1_20_5)
         .add(StructuredDataKey.NOTE_BLOCK_SOUND)
         .add(StructuredDataKey.BANNER_PATTERNS)
         .add(StructuredDataKey.BASE_COLOR)
         .add(StructuredDataKey.POT_DECORATIONS1_20_5)
         .add(StructuredDataKey.BLOCK_STATE)
         .add(StructuredDataKey.BEES1_20_5)
         .add(StructuredDataKey.LOCK1_20_5)
         .add(StructuredDataKey.CONTAINER_LOOT)
         .add(StructuredDataKey.TOOL1_20_5)
         .add(StructuredDataKey.ITEM_NAME)
         .add(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER)
         .add(VersionedTypes.V1_20_5.structuredDataKeys().keys());
      this.tagRewriter.renameTag(RegistryType.ITEM, "minecraft:axolotl_tempt_items", "minecraft:axolotl_food");
      this.tagRewriter.removeTag(RegistryType.ITEM, "minecraft:tools");
      this.tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:badlands_terracotta");
      this.tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:enchantable/mace");
      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
      ProtocolStorables1_20_5 storables = connection.storables(this);
      connection.put(storables.armorTrims());
   }

   @Override
   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables1_20_5();
   }

   public MappingData1_20_5 getMappingData() {
      return MAPPINGS;
   }

   @Override
   public ProtocolLogger getLogger() {
      return LOGGER;
   }

   public EntityPacketRewriter1_20_5 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_20_5 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPacket1_20_3> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter1_20_5 getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPacket1_20_3> getTagRewriter() {
      return this.tagRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPacket1_20_3> getComponentRewriter() {
      return this.componentRewriter;
   }

   @Override
   protected PacketTypesProvider<ClientboundPacket1_20_3, ClientboundPacket1_20_5, ServerboundPacket1_20_3, ServerboundPacket1_20_5> createPacketTypesProvider() {
      return new SimplePacketTypesProvider<>(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, ClientboundPackets1_20_3.class, ClientboundConfigurationPackets1_20_3.class),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, ClientboundPackets1_20_5.class, ClientboundConfigurationPackets1_20_5.class),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, ServerboundPackets1_20_3.class, ServerboundConfigurationPackets1_20_2.class),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class)
      );
   }
}
