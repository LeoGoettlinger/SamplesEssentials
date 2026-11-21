package com.viaversion.viaversion.protocols.v1_20_2to1_20_3;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_3;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.BlockItemPacketRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.EntityPacketRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.storage.ProtocolStorables1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.storage.ScoreboardStorage;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPacket1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;

public final class Protocol1_20_2To1_20_3
   extends AbstractProtocol<ClientboundPacket1_20_2, ClientboundPacket1_20_3, ServerboundPacket1_20_2, ServerboundPacket1_20_3> {
   public static final MappingData MAPPINGS = new MappingDataBase("1.20.2", "1.20.3");
   private final BlockItemPacketRewriter1_20_3 itemRewriter = new BlockItemPacketRewriter1_20_3(this);
   private final ParticleRewriter<ClientboundPacket1_20_2> particleRewriter = new ParticleRewriter<>(this);
   private final EntityPacketRewriter1_20_3 entityRewriter = new EntityPacketRewriter1_20_3(this);
   private final TagRewriter<ClientboundPacket1_20_2> tagRewriter = new TagRewriter<>(this);
   private final BlockRewriter<ClientboundPacket1_20_2> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_20_2::new);

   public Protocol1_20_2To1_20_3() {
      super(ClientboundPacket1_20_2.class, ClientboundPacket1_20_3.class, ServerboundPacket1_20_2.class, ServerboundPacket1_20_3.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.cancelServerbound(ServerboundPackets1_20_3.CONTAINER_SLOT_STATE_CHANGED);
      this.registerClientbound(ClientboundPackets1_20_2.SET_SCORE, wrapper -> {
         wrapper.passthrough(Types.STRING);
         int action = wrapper.read(Types.VAR_INT);
         String objectiveName = wrapper.read(Types.STRING);
         if (action == 1) {
            wrapper.write(Types.OPTIONAL_STRING, objectiveName.isEmpty() ? null : objectiveName);
            wrapper.setPacketType(ClientboundPackets1_20_3.RESET_SCORE);
         } else {
            wrapper.write(Types.STRING, objectiveName);
            wrapper.passthrough(Types.VAR_INT);
            wrapper.write(Types.TRUSTED_OPTIONAL_TAG, null);
            wrapper.write(Types.BOOLEAN, false);
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.SET_OBJECTIVE, wrapper -> {
         String objectiveName = wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action != 0 && action != 2) {
            if (action == 1 && Via.getConfig().hideScoreboardNumbers()) {
               ScoreboardStorage scoreboard = wrapper.user().<ProtocolStorables1_20_3>storables(this).scoreboard();
               scoreboard.removeObjective(objectiveName);
            }
         } else {
            if (!Via.getConfig().hideScoreboardNumbers()) {
               this.convertComponent(wrapper);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.write(Types.BOOLEAN, false);
               return;
            }

            Tag displayName = ComponentUtil.jsonToTag(wrapper.read(Types.COMPONENT));
            wrapper.write(Types.TRUSTED_TAG, displayName);
            ScoreboardStorage scoreboard = wrapper.user().<ProtocolStorables1_20_3>storables(this).scoreboard();
            int render = wrapper.passthrough(Types.VAR_INT);
            boolean hideNumberFormat = shouldHideNumberFormat(scoreboard, objectiveName, render);
            writeNumberFormat(wrapper, hideNumberFormat);
            scoreboard.putObjective(objectiveName, displayName.copy(), render, hideNumberFormat);
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.SET_DISPLAY_OBJECTIVE, wrapper -> {
         if (Via.getConfig().hideScoreboardNumbers()) {
            int slot = wrapper.passthrough(Types.VAR_INT);
            String objectiveName = wrapper.passthrough(Types.STRING);
            ScoreboardStorage scoreboard = wrapper.user().<ProtocolStorables1_20_3>storables(this).scoreboard();
            String previousObjective = scoreboard.setDisplaySlot(slot, objectiveName);
            updateNumberFormat(wrapper, scoreboard, previousObjective);
            if (!objectiveName.equals(previousObjective)) {
               updateNumberFormat(wrapper, scoreboard, objectiveName);
            }
         }
      });
      this.registerServerbound(ServerboundPackets1_20_3.SET_JIGSAW_BLOCK, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.STRING);
         wrapper.read(Types.VAR_INT);
         wrapper.read(Types.VAR_INT);
      });
      this.replaceClientbound(ClientboundPackets1_20_2.UPDATE_ADVANCEMENTS, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               this.convertComponent(wrapper);
               this.convertComponent(wrapper);
               this.itemRewriter.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_20_2));
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
               }

               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            int requirements = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.COMMAND_SUGGESTIONS, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         int suggestions = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < suggestions; i++) {
            wrapper.passthrough(Types.STRING);
            this.convertOptionalComponent(wrapper);
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.MAP_ITEM_DATA, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.BOOLEAN);
         if (wrapper.passthrough(Types.BOOLEAN)) {
            int icons = wrapper.passthrough(Types.VAR_INT);

            for (int i = 0; i < icons; i++) {
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               wrapper.passthrough(Types.BYTE);
               this.convertOptionalComponent(wrapper);
            }
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.BOSS_EVENT, wrapper -> {
         wrapper.passthrough(Types.UUID);
         int action = wrapper.passthrough(Types.VAR_INT);
         if (action == 0 || action == 3) {
            this.convertComponent(wrapper);
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.PLAYER_CHAT, wrapper -> {
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.LONG);
         int lastSeen = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < lastSeen; i++) {
            int index = wrapper.passthrough(Types.VAR_INT);
            if (index == 0) {
               wrapper.passthrough(Types.SIGNATURE_BYTES);
            }
         }

         this.convertOptionalComponent(wrapper);
         int filterMaskType = wrapper.passthrough(Types.VAR_INT);
         if (filterMaskType == 2) {
            wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
         }

         wrapper.passthrough(Types.VAR_INT);
         this.convertComponent(wrapper);
         this.convertOptionalComponent(wrapper);
      });
      this.registerClientbound(ClientboundPackets1_20_2.SET_PLAYER_TEAM, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.convertComponent(wrapper);
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.VAR_INT);
            this.convertComponent(wrapper);
            this.convertComponent(wrapper);
         }
      });
      this.registerClientbound(ClientboundConfigurationPackets1_20_2.DISCONNECT, this::convertComponent);
      this.registerClientbound(ClientboundPackets1_20_2.DISCONNECT, this::convertComponent);
      this.registerClientbound(
         ClientboundPackets1_20_2.RESOURCE_PACK,
         ClientboundPackets1_20_3.RESOURCE_PACK_PUSH,
         this.resourcePackHandler(ClientboundPackets1_20_3.RESOURCE_PACK_POP)
      );
      this.registerClientbound(ClientboundPackets1_20_2.SERVER_DATA, this::convertComponent);
      this.registerClientbound(ClientboundPackets1_20_2.SET_ACTION_BAR_TEXT, this::convertComponent);
      this.registerClientbound(ClientboundPackets1_20_2.SET_TITLE_TEXT, this::convertComponent);
      this.registerClientbound(ClientboundPackets1_20_2.SET_SUBTITLE_TEXT, this::convertComponent);
      this.registerClientbound(ClientboundPackets1_20_2.DISGUISED_CHAT, wrapper -> {
         this.convertComponent(wrapper);
         wrapper.passthrough(Types.VAR_INT);
         this.convertComponent(wrapper);
         this.convertOptionalComponent(wrapper);
      });
      this.registerClientbound(ClientboundPackets1_20_2.SYSTEM_CHAT, this::convertComponent);
      this.replaceClientbound(ClientboundPackets1_20_2.OPEN_SCREEN, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int containerTypeId = wrapper.read(Types.VAR_INT);
         wrapper.write(Types.VAR_INT, MAPPINGS.getMenuMappings().getNewId(containerTypeId));
         this.convertComponent(wrapper);
      });
      this.registerClientbound(ClientboundPackets1_20_2.TAB_LIST, wrapper -> {
         this.convertComponent(wrapper);
         this.convertComponent(wrapper);
      });
      this.registerClientbound(ClientboundPackets1_20_2.PLAYER_COMBAT_KILL, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(wrapper -> Protocol1_20_2To1_20_3.this.convertComponent(wrapper));
         }
      });
      this.registerClientbound(ClientboundPackets1_20_2.PLAYER_INFO_UPDATE, wrapper -> {
         BitSet actions = wrapper.passthrough(Types.PROFILE_ACTIONS_ENUM1_19_3);
         int entries = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < entries; i++) {
            wrapper.passthrough(Types.UUID);
            if (actions.get(0)) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
            }

            if (actions.get(1) && wrapper.passthrough(Types.BOOLEAN)) {
               wrapper.passthrough(Types.UUID);
               wrapper.passthrough(Types.PROFILE_KEY);
            }

            if (actions.get(2)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(3)) {
               wrapper.passthrough(Types.BOOLEAN);
            }

            if (actions.get(4)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(5)) {
               this.convertOptionalComponent(wrapper);
            }
         }
      });
      this.registerServerbound(ServerboundPackets1_20_3.RESOURCE_PACK, this.resourcePackStatusHandler());
      this.registerServerbound(ServerboundConfigurationPackets1_20_2.RESOURCE_PACK, this.resourcePackStatusHandler());
      this.registerClientbound(
         ClientboundConfigurationPackets1_20_2.RESOURCE_PACK,
         ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_PUSH,
         this.resourcePackHandler(ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_POP)
      );
   }

   private PacketHandler resourcePackStatusHandler() {
      return wrapper -> {
         wrapper.read(Types.UUID);
         int action = wrapper.read(Types.VAR_INT);
         if (action == 4) {
            wrapper.cancel();
         } else if (action > 4) {
            wrapper.write(Types.VAR_INT, 2);
         } else {
            wrapper.write(Types.VAR_INT, action);
         }
      };
   }

   private PacketHandler resourcePackHandler(ClientboundPacketType popType) {
      return wrapper -> {
         PacketWrapper dropPacksPacket = wrapper.create(popType);
         dropPacksPacket.write(Types.OPTIONAL_UUID, null);
         dropPacksPacket.send(Protocol1_20_2To1_20_3.class);
         String url = wrapper.read(Types.STRING);
         String hash = wrapper.read(Types.STRING);
         wrapper.write(Types.UUID, UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
         wrapper.write(Types.STRING, url);
         wrapper.write(Types.STRING, hash);
         wrapper.passthrough(Types.BOOLEAN);
         this.convertOptionalComponent(wrapper);
      };
   }

   private void convertComponent(PacketWrapper wrapper) {
      wrapper.write(Types.TRUSTED_TAG, ComponentUtil.jsonToTag(wrapper.read(Types.COMPONENT)));
   }

   private void convertOptionalComponent(PacketWrapper wrapper) {
      wrapper.write(Types.TRUSTED_OPTIONAL_TAG, ComponentUtil.jsonToTag(wrapper.read(Types.OPTIONAL_COMPONENT)));
   }

   private static boolean shouldHideNumberFormat(ScoreboardStorage scoreboard, String objectiveName, int renderType) {
      return renderType == 0 && scoreboard.isSidebar(objectiveName);
   }

   private static void writeNumberFormat(PacketWrapper wrapper, boolean hideNumberFormat) {
      wrapper.write(Types.BOOLEAN, hideNumberFormat);
      if (hideNumberFormat) {
         wrapper.write(Types.VAR_INT, 0);
      }
   }

   private static void updateNumberFormat(PacketWrapper wrapper, ScoreboardStorage scoreboard, String objectiveName) {
      if (objectiveName != null && !objectiveName.isEmpty()) {
         ScoreboardStorage.Objective objective = scoreboard.objective(objectiveName);
         if (objective != null) {
            boolean hideNumberFormat = shouldHideNumberFormat(scoreboard, objectiveName, objective.renderType());
            if (hideNumberFormat != objective.numberFormatHidden()) {
               PacketWrapper objectivePacket = wrapper.create(ClientboundPackets1_20_3.SET_OBJECTIVE);
               objectivePacket.write(Types.STRING, objectiveName);
               objectivePacket.write(Types.BYTE, (byte)2);
               objectivePacket.write(Types.TRUSTED_TAG, objective.displayName().copy());
               objectivePacket.write(Types.VAR_INT, objective.renderType());
               writeNumberFormat(objectivePacket, hideNumberFormat);
               objectivePacket.send(Protocol1_20_2To1_20_3.class);
               scoreboard.setNumberFormatHidden(objectiveName, hideNumberFormat);
            }
         }
      }
   }

   @Override
   protected void onMappingDataLoaded() {
      EntityTypes1_20_3.initialize(this);
      ParticleType.Fillers.fill1_20_3(this, Types1_20_3.PARTICLE);
      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_3.PLAYER));
   }

   @Override
   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables1_20_3();
   }

   @Override
   public MappingData getMappingData() {
      return MAPPINGS;
   }

   public BlockItemPacketRewriter1_20_3 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPacket1_20_2> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_20_2> getParticleRewriter() {
      return this.particleRewriter;
   }

   public EntityPacketRewriter1_20_3 getEntityRewriter() {
      return this.entityRewriter;
   }

   public TagRewriter<ClientboundPacket1_20_2> getTagRewriter() {
      return this.tagRewriter;
   }

   @Override
   protected PacketTypesProvider<ClientboundPacket1_20_2, ClientboundPacket1_20_3, ServerboundPacket1_20_2, ServerboundPacket1_20_3> createPacketTypesProvider() {
      return new SimplePacketTypesProvider<>(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, ClientboundPackets1_20_2.class, ClientboundConfigurationPackets1_20_2.class),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, ClientboundPackets1_20_3.class, ClientboundConfigurationPackets1_20_3.class),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, ServerboundPackets1_20_2.class, ServerboundConfigurationPackets1_20_2.class),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, ServerboundPackets1_20_3.class, ServerboundConfigurationPackets1_20_2.class)
      );
   }
}
