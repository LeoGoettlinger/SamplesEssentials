package com.viaversion.viaversion.protocols.v1_19_3to1_19_4;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19_4;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.data.MappingData1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.EntityPacketRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.ItemPacketRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.storage.ProtocolStorables1_19_4;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Protocol1_19_3To1_19_4
   extends AbstractProtocol<ClientboundPackets1_19_3, ClientboundPackets1_19_4, ServerboundPackets1_19_3, ServerboundPackets1_19_4> {
   public static final MappingData1_19_4 MAPPINGS = new MappingData1_19_4();
   private final EntityPacketRewriter1_19_4 entityRewriter = new EntityPacketRewriter1_19_4(this);
   private final ItemPacketRewriter1_19_4 itemRewriter = new ItemPacketRewriter1_19_4(this);
   private final ParticleRewriter<ClientboundPackets1_19_3> particleRewriter = new ParticleRewriter<>(this);
   private final TagRewriter<ClientboundPackets1_19_3> tagRewriter = new TagRewriter<>(this);
   private final BlockRewriter<ClientboundPackets1_19_3> blockRewriter = BlockRewriter.for1_18(this, ChunkType1_18::new);

   public Protocol1_19_3To1_19_4() {
      super(ClientboundPackets1_19_3.class, ClientboundPackets1_19_4.class, ServerboundPackets1_19_3.class, ServerboundPackets1_19_4.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      CommandRewriter<ClientboundPackets1_19_3> commandRewriter = new CommandRewriter<ClientboundPackets1_19_3>(this) {
         @Override
         public void handleArgument(PacketWrapper wrapper, String argumentType) {
            if (argumentType.equals("minecraft:time")) {
               wrapper.write(Types.INT, 0);
            } else {
               super.handleArgument(wrapper, argumentType);
            }
         }
      };
      this.replaceClientbound(ClientboundPackets1_19_3.COMMANDS, commandRewriter::handle1_19);
      this.registerClientbound(ClientboundPackets1_19_3.UPDATE_MOB_EFFECT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BYTE);
         int duration = wrapper.read(Types.VAR_INT);
         if (duration >= 32767) {
            wrapper.write(Types.VAR_INT, -1);
         } else {
            wrapper.write(Types.VAR_INT, duration);
         }
      });
      this.registerClientbound(ClientboundPackets1_19_3.SERVER_DATA, wrapper -> {
         JsonElement element = wrapper.read(Types.OPTIONAL_COMPONENT);
         if (element != null) {
            wrapper.write(Types.COMPONENT, element);
         } else {
            wrapper.write(Types.COMPONENT, ComponentUtil.emptyJsonComponent());
         }

         String iconBase64 = wrapper.read(Types.OPTIONAL_STRING);
         byte[] iconBytes = null;
         if (iconBase64 != null && iconBase64.startsWith("data:image/png;base64,")) {
            iconBytes = Base64.getDecoder().decode(iconBase64.substring("data:image/png;base64,".length()).getBytes(StandardCharsets.UTF_8));
         }

         wrapper.write(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE, iconBytes);
      });
   }

   @Override
   protected void onMappingDataLoaded() {
      EntityTypes1_19_4.initialize(this);
      ParticleType.Fillers.fill1_19(this, Types1_19_4.PARTICLE);
      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19_4.PLAYER));
   }

   public ProtocolStorables1_19_4 createStorables() {
      return new ProtocolStorables1_19_4();
   }

   public MappingData1_19_4 getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_19_4 getEntityRewriter() {
      return this.entityRewriter;
   }

   public ItemPacketRewriter1_19_4 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPackets1_19_3> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_19_3> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPackets1_19_3> getTagRewriter() {
      return this.tagRewriter;
   }
}
