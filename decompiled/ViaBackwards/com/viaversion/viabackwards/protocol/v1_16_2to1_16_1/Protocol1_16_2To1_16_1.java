package com.viaversion.viabackwards.protocol.v1_16_2to1_16_1;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.rewriter.BlockItemPacketRewriter1_16_2;
import com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.rewriter.CommandRewriter1_16_2;
import com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.rewriter.EntityPacketRewriter1_16_2;
import com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.storage.ProtocolStorables1_16_2;
import com.viaversion.viabackwards.utils.BackwardsProtocolLogger;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase.ReadType;
import com.viaversion.viaversion.util.ProtocolLogger;

public class Protocol1_16_2To1_16_1
   extends BackwardsProtocol<ClientboundPackets1_16_2, ClientboundPackets1_16, ServerboundPackets1_16_2, ServerboundPackets1_16> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.16.2", "1.16", Protocol1_16_1To1_16_2.class);
   public static final ProtocolLogger LOGGER = new BackwardsProtocolLogger(Protocol1_16_2To1_16_1.class);
   private final EntityPacketRewriter1_16_2 entityRewriter = new EntityPacketRewriter1_16_2(this);
   private final BlockItemPacketRewriter1_16_2 blockItemPackets = new BlockItemPacketRewriter1_16_2(this);
   private final BlockRewriter<ClientboundPackets1_16_2> blockRewriter = BlockRewriter.for1_14(this);
   private final ParticleRewriter<ClientboundPackets1_16_2> particleRewriter = new ParticleRewriter(this);
   private final JsonNBTComponentRewriter<ClientboundPackets1_16_2> translatableRewriter = new JsonNBTComponentRewriter(this, ReadType.JSON);
   private final TagRewriter<ClientboundPackets1_16_2> tagRewriter = new TagRewriter(this);

   public Protocol1_16_2To1_16_1() {
      super(ClientboundPackets1_16_2.class, ClientboundPackets1_16.class, ServerboundPackets1_16_2.class, ServerboundPackets1_16.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      new CommandRewriter1_16_2(this).registerDeclareCommands(ClientboundPackets1_16_2.COMMANDS);
      this.replaceClientbound(ClientboundPackets1_16_2.CHAT, wrapper -> {
         JsonElement message = (JsonElement)wrapper.passthrough(Types.COMPONENT);
         this.translatableRewriter.processText(wrapper.user(), message);
         byte position = (Byte)wrapper.passthrough(Types.BYTE);
         if (position == 2) {
            wrapper.clearPacket();
            wrapper.setPacketType(ClientboundPackets1_16.SET_TITLES);
            wrapper.write(Types.VAR_INT, 2);
            wrapper.write(Types.COMPONENT, message);
         }
      });
      this.registerServerbound(ServerboundPackets1_16.RECIPE_BOOK_UPDATE, ServerboundPackets1_16_2.RECIPE_BOOK_CHANGE_SETTINGS, wrapper -> {
         int type = (Integer)wrapper.read(Types.VAR_INT);
         if (type == 0) {
            wrapper.passthrough(Types.STRING);
            wrapper.setPacketType(ServerboundPackets1_16_2.RECIPE_BOOK_SEEN_RECIPE);
         } else {
            wrapper.cancel();

            for (int i = 0; i < 3; i++) {
               sendSeenRecipePacket(i, wrapper);
            }
         }
      });
      this.tagRewriter.register(ClientboundPackets1_16_2.UPDATE_TAGS, RegistryType.ENTITY);
   }

   private static void sendSeenRecipePacket(int recipeType, PacketWrapper wrapper) {
      boolean open = (Boolean)wrapper.read(Types.BOOLEAN);
      boolean filter = (Boolean)wrapper.read(Types.BOOLEAN);
      PacketWrapper newPacket = wrapper.create(ServerboundPackets1_16_2.RECIPE_BOOK_CHANGE_SETTINGS);
      newPacket.write(Types.VAR_INT, recipeType);
      newPacket.write(Types.BOOLEAN, open);
      newPacket.write(Types.BOOLEAN, filter);
      newPacket.sendToServer(Protocol1_16_2To1_16_1.class);
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_16_2.PLAYER));
      ProtocolStorables1_16_2 storables = (ProtocolStorables1_16_2)user.storables(this);
      user.put(storables.biomeStorage());
   }

   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables1_16_2();
   }

   public JsonNBTComponentRewriter<ClientboundPackets1_16_2> getComponentRewriter() {
      return this.translatableRewriter;
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public ProtocolLogger getLogger() {
      return LOGGER;
   }

   public EntityPacketRewriter1_16_2 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_16_2 getItemRewriter() {
      return this.blockItemPackets;
   }

   public BlockRewriter<ClientboundPackets1_16_2> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_16_2> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPackets1_16_2> getTagRewriter() {
      return this.tagRewriter;
   }
}
