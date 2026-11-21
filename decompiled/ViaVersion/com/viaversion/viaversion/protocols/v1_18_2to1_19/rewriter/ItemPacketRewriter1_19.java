package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.provider.AckSequenceProvider;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.LoomStorage;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public final class ItemPacketRewriter1_19 extends ItemRewriter<ClientboundPackets1_18, ServerboundPackets1_19, Protocol1_18_2To1_19> {
   private static final int LOOM_MENU_TYPE_ID = 17;
   private static final int LOOM_SELECTABLE_PATTERNS = 34;

   public ItemPacketRewriter1_19(Protocol1_18_2To1_19 protocol) {
      super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
   }

   @Override
   public void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets1_18.LEVEL_PARTICLES, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT, Types.VAR_INT);
            this.map(Types.BOOLEAN);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.DOUBLE);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int id = wrapper.get(Types.VAR_INT, 0);
               ParticleMappings particleMappings = ItemPacketRewriter1_19.this.protocol.getMappingData().getParticleMappings();
               if (id == particleMappings.id("vibration")) {
                  wrapper.read(Types.BLOCK_POSITION1_14);
                  String resourceLocation = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
                  if (resourceLocation.equals("entity")) {
                     wrapper.passthrough(Types.VAR_INT);
                     wrapper.write(Types.FLOAT, 0.0F);
                  }
               }
            });
            this.handler(ItemPacketRewriter1_19.this.protocol.getParticleRewriter().levelParticlesHandler1_13(Types.VAR_INT));
         }
      });
      this.protocol.replaceClientbound(ClientboundPackets1_18.MERCHANT_OFFERS, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int size = wrapper.read(Types.UNSIGNED_BYTE);
               wrapper.write(Types.VAR_INT, size);

               for (int i = 0; i < size; i++) {
                  ItemPacketRewriter1_19.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                  ItemPacketRewriter1_19.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                  if (wrapper.read(Types.BOOLEAN)) {
                     ItemPacketRewriter1_19.this.handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2));
                  } else {
                     wrapper.write(Types.ITEM1_13_2, null);
                  }

                  wrapper.passthrough(Types.BOOLEAN);
                  wrapper.passthrough(Types.INT);
                  wrapper.passthrough(Types.INT);
                  wrapper.passthrough(Types.INT);
                  wrapper.passthrough(Types.INT);
                  wrapper.passthrough(Types.FLOAT);
                  wrapper.passthrough(Types.INT);
               }
            });
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_19.PLAYER_ACTION, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BLOCK_POSITION1_14);
            this.map(Types.UNSIGNED_BYTE);
            this.handler(ItemPacketRewriter1_19.this.sequenceHandler());
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_19.USE_ITEM_ON, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BLOCK_POSITION1_14);
            this.map(Types.VAR_INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.map(Types.BOOLEAN);
            this.handler(ItemPacketRewriter1_19.this.sequenceHandler());
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_19.USE_ITEM, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(ItemPacketRewriter1_19.this.sequenceHandler());
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_18.CONTAINER_CLOSE, wrapper -> wrapper.user().get(LoomStorage.class).reset());
      this.protocol.registerServerbound(ServerboundPackets1_19.CONTAINER_CLOSE, wrapper -> wrapper.user().get(LoomStorage.class).reset());
      this.protocol.registerServerbound(ServerboundPackets1_19.CONTAINER_BUTTON_CLICK, wrapper -> {
         byte containerId = wrapper.passthrough(Types.BYTE);
         if (containerId != -1 && containerId == wrapper.user().get(LoomStorage.class).containerId()) {
            byte patternIndex = wrapper.read(Types.BYTE);
            wrapper.write(Types.BYTE, (byte)(patternIndex + 1));
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_18.OPEN_SCREEN, wrapper -> {
         int containerId = wrapper.passthrough(Types.VAR_INT);
         int menuTypeId = wrapper.passthrough(Types.VAR_INT);
         wrapper.user().get(LoomStorage.class).setContainerId(menuTypeId == 17 ? containerId : -1);
      });
      this.protocol.appendClientbound(ClientboundPackets1_18.CONTAINER_SET_DATA, wrapper -> {
         wrapper.resetReader();
         short containerId = wrapper.passthrough(Types.UNSIGNED_BYTE);
         if (containerId != -1 && containerId == wrapper.user().get(LoomStorage.class).containerId()) {
            short property = wrapper.passthrough(Types.SHORT);
            if (property == 0) {
               short patternId = wrapper.read(Types.SHORT);
               short patternIndex;
               if (patternId == 0) {
                  patternIndex = -1;
               } else if (patternId <= 34) {
                  patternIndex = (short)(patternId - 1);
               } else {
                  patternIndex = 0;
               }

               wrapper.write(Types.SHORT, patternIndex);
            }
         }
      });
      new RecipeRewriter<>(this.protocol).register(ClientboundPackets1_18.UPDATE_RECIPES);
   }

   private PacketHandler sequenceHandler() {
      return wrapper -> {
         int sequence = wrapper.read(Types.VAR_INT);
         AckSequenceProvider provider = Via.getManager().getProviders().get(AckSequenceProvider.class);
         provider.handleSequence(wrapper.user(), sequence);
      };
   }
}
