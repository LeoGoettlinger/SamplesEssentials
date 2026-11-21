package com.viaversion.viabackwards.protocol.v1_19_4to1_19_3;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.rewriter.BlockItemPacketRewriter1_19_4;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.rewriter.ComponentRewriter1_19_4;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.rewriter.EntityPacketRewriter1_19_4;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.storage.EntityTracker1_19_4;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import java.util.Base64;

public final class Protocol1_19_4To1_19_3
   extends BackwardsProtocol<ClientboundPackets1_19_4, ClientboundPackets1_19_3, ServerboundPackets1_19_4, ServerboundPackets1_19_3> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.19.4", "1.19.3", Protocol1_19_3To1_19_4.class);
   private final EntityPacketRewriter1_19_4 entityRewriter = new EntityPacketRewriter1_19_4(this);
   private final BlockItemPacketRewriter1_19_4 itemRewriter = new BlockItemPacketRewriter1_19_4(this);
   private final ParticleRewriter<ClientboundPackets1_19_4> particleRewriter = new ParticleRewriter(this);
   private final JsonNBTComponentRewriter<ClientboundPackets1_19_4> translatableRewriter = new ComponentRewriter1_19_4(this);
   private final TagRewriter<ClientboundPackets1_19_4> tagRewriter = new TagRewriter(this);
   private final BlockRewriter<ClientboundPackets1_19_4> blockRewriter = BlockRewriter.for1_18(this, ChunkType1_18::new);

   public Protocol1_19_4To1_19_3() {
      super(ClientboundPackets1_19_4.class, ClientboundPackets1_19_3.class, ServerboundPackets1_19_4.class, ServerboundPackets1_19_3.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      CommandRewriter<ClientboundPackets1_19_4> commandRewriter = new CommandRewriter<ClientboundPackets1_19_4>(this) {
         public void handleArgument(PacketWrapper wrapper, String argumentType) {
            switch (argumentType) {
               case "minecraft:heightmap":
                  wrapper.write(Types.VAR_INT, 0);
                  break;
               case "minecraft:time":
                  wrapper.read(Types.INT);
                  break;
               case "minecraft:resource":
               case "minecraft:resource_or_tag":
                  String resource = (String)wrapper.read(Types.STRING);
                  wrapper.write(Types.STRING, resource.equals("minecraft:damage_type") ? "minecraft:mob_effect" : resource);
                  break;
               default:
                  super.handleArgument(wrapper, argumentType);
            }
         }
      };
      this.replaceClientbound(ClientboundPackets1_19_4.COMMANDS, commandRewriter::handle1_19);
      this.tagRewriter.removeTags("minecraft:damage_type");
      this.replaceClientbound(ClientboundPackets1_19_4.SERVER_DATA, wrapper -> {
         JsonElement element = (JsonElement)wrapper.read(Types.COMPONENT);
         this.translatableRewriter.processText(wrapper.user(), element);
         wrapper.write(Types.OPTIONAL_COMPONENT, element);
         byte[] iconBytes = (byte[])wrapper.read(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE);
         String iconBase64 = iconBytes != null ? "data:image/png;base64," + Base64.getEncoder().encodeToString(iconBytes) : null;
         wrapper.write(Types.OPTIONAL_STRING, iconBase64);
      });
      this.cancelClientbound(ClientboundPackets1_19_4.BUNDLE_DELIMITER);
      this.cancelClientbound(ClientboundPackets1_19_4.CHUNKS_BIOMES);
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTracker1_19_4(user));
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public BlockItemPacketRewriter1_19_4 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPackets1_19_4> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_19_4> getParticleRewriter() {
      return this.particleRewriter;
   }

   public EntityPacketRewriter1_19_4 getEntityRewriter() {
      return this.entityRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPackets1_19_4> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPackets1_19_4> getTagRewriter() {
      return this.tagRewriter;
   }
}
