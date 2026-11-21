package com.viaversion.viabackwards.protocol.v1_14to1_13_2;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.data.BackwardsMappingData1_14;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter.BlockItemPacketRewriter1_14;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter.CommandRewriter1_14;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter.EntityPacketRewriter1_14;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter.PlayerPacketRewriter1_14;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.rewriter.SoundPacketRewriter1_14;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage.ChunkLightStorage;
import com.viaversion.viabackwards.protocol.v1_14to1_13_2.storage.ProtocolStorables1_14;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase.ReadType;

public class Protocol1_14To1_13_2 extends BackwardsProtocol<ClientboundPackets1_14, ClientboundPackets1_13, ServerboundPackets1_14, ServerboundPackets1_13> {
   public static final BackwardsMappingData1_14 MAPPINGS = new BackwardsMappingData1_14();
   private final EntityPacketRewriter1_14 entityRewriter = new EntityPacketRewriter1_14(this);
   private final BlockItemPacketRewriter1_14 itemRewriter = new BlockItemPacketRewriter1_14(this);
   private final BlockRewriter<ClientboundPackets1_14> blockRewriter = BlockRewriter.legacy(this);
   private final ParticleRewriter<ClientboundPackets1_14> particleRewriter = new ParticleRewriter(this);
   private final JsonNBTComponentRewriter<ClientboundPackets1_14> translatableRewriter = new JsonNBTComponentRewriter(this, ReadType.JSON);
   private final TagRewriter<ClientboundPackets1_14> tagRewriter = new TagRewriter(this);

   public Protocol1_14To1_13_2() {
      super(ClientboundPackets1_14.class, ClientboundPackets1_13.class, ServerboundPackets1_14.class, ServerboundPackets1_13.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      new CommandRewriter1_14(this).registerDeclareCommands(ClientboundPackets1_14.COMMANDS);
      new PlayerPacketRewriter1_14(this).register();
      new SoundPacketRewriter1_14(this).register();
      this.cancelClientbound(ClientboundPackets1_14.SET_CHUNK_CACHE_CENTER);
      this.cancelClientbound(ClientboundPackets1_14.SET_CHUNK_CACHE_RADIUS);
      this.registerClientbound(ClientboundPackets1_14.UPDATE_TAGS, wrapper -> {
         this.tagRewriter.handle(wrapper, RegistryType.BLOCK);
         this.tagRewriter.handle(wrapper, RegistryType.ITEM);
         this.tagRewriter.handle(wrapper, RegistryType.FLUID);
         int entityTagsSize = (Integer)wrapper.read(Types.VAR_INT);

         for (int i = 0; i < entityTagsSize; i++) {
            wrapper.read(Types.STRING);
            wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
         }
      });
      this.registerClientbound(ClientboundPackets1_14.LIGHT_UPDATE, null, wrapper -> {
         int x = (Integer)wrapper.read(Types.VAR_INT);
         int z = (Integer)wrapper.read(Types.VAR_INT);
         int skyLightMask = (Integer)wrapper.read(Types.VAR_INT);
         int blockLightMask = (Integer)wrapper.read(Types.VAR_INT);
         int emptySkyLightMask = (Integer)wrapper.read(Types.VAR_INT);
         int emptyBlockLightMask = (Integer)wrapper.read(Types.VAR_INT);
         byte[][] skyLight = new byte[16][];
         if (isSet(skyLightMask, 0)) {
            wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
         }

         for (int i = 0; i < 16; i++) {
            if (isSet(skyLightMask, i + 1)) {
               skyLight[i] = (byte[])wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
            } else if (isSet(emptySkyLightMask, i + 1)) {
               skyLight[i] = ChunkLightStorage.EMPTY_LIGHT;
            }
         }

         if (isSet(skyLightMask, 17)) {
            wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
         }

         byte[][] blockLight = new byte[16][];
         if (isSet(blockLightMask, 0)) {
            wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
         }

         for (int ix = 0; ix < 16; ix++) {
            if (isSet(blockLightMask, ix + 1)) {
               blockLight[ix] = (byte[])wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
            } else if (isSet(emptyBlockLightMask, ix + 1)) {
               blockLight[ix] = ChunkLightStorage.EMPTY_LIGHT;
            }
         }

         if (isSet(blockLightMask, 17)) {
            wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
         }

         ProtocolStorables1_14 storables = (ProtocolStorables1_14)wrapper.user().storables(this);
         storables.chunkLightStorage().setStoredLight(skyLight, blockLight, x, z);
         wrapper.cancel();
      });
   }

   private static boolean isSet(int mask, int i) {
      return (mask & 1 << i) != 0;
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_14.PLAYER));
      ProtocolStorables1_14 storables = (ProtocolStorables1_14)user.storables(this);
      storables.setClientWorld(new ClientWorld());
   }

   public ProtocolStorables1_14 createStorables() {
      return new ProtocolStorables1_14();
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_14 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_14 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPackets1_14> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_14> getParticleRewriter() {
      return this.particleRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPackets1_14> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPackets1_14> getTagRewriter() {
      return this.tagRewriter;
   }
}
