package com.viaversion.viaversion.protocols.v1_18_2to1_19;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.data.MappingData1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.provider.AckSequenceProvider;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.ComponentRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.EntityPacketRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.ItemPacketRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter.WorldPacketRewriter1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.DimensionRegistryStorage;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.LoomStorage;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.NonceStorage1_19;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.storage.SequenceStorage;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.CipherUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.concurrent.ThreadLocalRandom;

public final class Protocol1_18_2To1_19 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_19, ServerboundPackets1_17, ServerboundPackets1_19> {
   public static final MappingData1_19 MAPPINGS = new MappingData1_19();
   private final EntityPacketRewriter1_19 entityRewriter = new EntityPacketRewriter1_19(this);
   private final ItemPacketRewriter1_19 itemRewriter = new ItemPacketRewriter1_19(this);
   private final ParticleRewriter<ClientboundPackets1_18> particleRewriter = new ParticleRewriter<>(this);
   private final ComponentRewriter1_19 componentRewriter = new ComponentRewriter1_19(this);
   private final TagRewriter<ClientboundPackets1_18> tagRewriter = new TagRewriter<>(this);
   private final BlockRewriter<ClientboundPackets1_18> blockRewriter = BlockRewriter.for1_18(this, ChunkType1_18::new);

   public Protocol1_18_2To1_19() {
      super(ClientboundPackets1_18.class, ClientboundPackets1_19.class, ServerboundPackets1_17.class, ServerboundPackets1_19.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      WorldPacketRewriter1_19.register(this);
      this.cancelClientbound(ClientboundPackets1_18.ADD_VIBRATION_SIGNAL);
      final SoundRewriter<ClientboundPackets1_18> soundRewriter = new SoundRewriter<>(this);
      this.replaceClientbound(ClientboundPackets1_18.SOUND, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.handler(wrapper -> wrapper.write(Types.LONG, Protocol1_18_2To1_19.this.randomLong()));
            this.handler(soundRewriter.getSoundHandler());
         }
      });
      this.replaceClientbound(ClientboundPackets1_18.SOUND_ENTITY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.handler(wrapper -> wrapper.write(Types.LONG, Protocol1_18_2To1_19.this.randomLong()));
            this.handler(soundRewriter.getSoundHandler());
         }
      });
      this.registerClientbound(ClientboundPackets1_18.CUSTOM_SOUND, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.map(Types.VAR_INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.INT);
            this.map(Types.FLOAT);
            this.map(Types.FLOAT);
            this.handler(wrapper -> wrapper.write(Types.LONG, Protocol1_18_2To1_19.this.randomLong()));
         }
      });
      PacketHandler singleNullTextComponentMapper = wrapper -> wrapper.write(
         Types.COMPONENT, this.mapTextComponentIfNull(wrapper.user(), wrapper.read(Types.COMPONENT))
      );
      this.registerClientbound(ClientboundPackets1_18.SET_TITLE_TEXT, singleNullTextComponentMapper);
      this.registerClientbound(ClientboundPackets1_18.SET_SUBTITLE_TEXT, singleNullTextComponentMapper);
      this.registerClientbound(ClientboundPackets1_18.SET_ACTION_BAR_TEXT, singleNullTextComponentMapper);
      this.registerClientbound(ClientboundPackets1_18.SET_OBJECTIVE, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            wrapper.write(Types.COMPONENT, this.mapTextComponentIfNull(wrapper.user(), wrapper.read(Types.COMPONENT)));
         }
      });
      this.registerClientbound(ClientboundPackets1_18.SET_PLAYER_TEAM, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            wrapper.write(Types.COMPONENT, this.mapTextComponentIfNull(wrapper.user(), wrapper.read(Types.COMPONENT)));
            wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.VAR_INT);
            wrapper.write(Types.COMPONENT, this.mapTextComponentIfNull(wrapper.user(), wrapper.read(Types.COMPONENT)));
            wrapper.write(Types.COMPONENT, this.mapTextComponentIfNull(wrapper.user(), wrapper.read(Types.COMPONENT)));
         }
      });
      this.registerClientbound(ClientboundPackets1_18.PLAYER_INFO, wrapper -> {
         int action = wrapper.passthrough(Types.VAR_INT);
         int entries = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < entries; i++) {
            wrapper.passthrough(Types.UUID);
            if (action == 0) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.VAR_INT);
               JsonElement displayName = wrapper.read(Types.OPTIONAL_COMPONENT);
               if (this.isJsonNotNull(displayName)) {
                  wrapper.write(Types.OPTIONAL_COMPONENT, displayName);
               } else {
                  wrapper.write(Types.OPTIONAL_COMPONENT, null);
               }

               wrapper.write(Types.OPTIONAL_PROFILE_KEY, null);
            } else if (action == 1 || action == 2) {
               wrapper.passthrough(Types.VAR_INT);
            } else if (action == 3) {
               JsonElement displayName = wrapper.read(Types.OPTIONAL_COMPONENT);
               if (this.isJsonNotNull(displayName)) {
                  wrapper.write(Types.OPTIONAL_COMPONENT, displayName);
               } else {
                  wrapper.write(Types.OPTIONAL_COMPONENT, null);
               }
            }
         }
      });
      CommandRewriter<ClientboundPackets1_18> commandRewriter = new CommandRewriter<>(this);
      this.registerClientbound(ClientboundPackets1_18.COMMANDS, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            byte flags = wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            if ((flags & 8) != 0) {
               wrapper.passthrough(Types.VAR_INT);
            }

            int nodeType = flags & 3;
            if (nodeType == 1 || nodeType == 2) {
               wrapper.passthrough(Types.STRING);
            }

            if (nodeType == 2) {
               String argumentType = wrapper.read(Types.STRING);
               int argumentTypeId = MAPPINGS.getArgumentTypeMappings().mappedId(argumentType);
               if (argumentTypeId == -1) {
                  this.getLogger().warning("Unknown command argument type: " + argumentType);
               }

               wrapper.write(Types.VAR_INT, argumentTypeId);
               commandRewriter.handleArgument(wrapper, argumentType);
               if ((flags & 16) != 0) {
                  wrapper.passthrough(Types.STRING);
               }
            }
         }

         wrapper.passthrough(Types.VAR_INT);
      });
      this.registerClientbound(ClientboundPackets1_18.CHAT, ClientboundPackets1_19.SYSTEM_CHAT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.COMPONENT);
            this.handler(wrapper -> {
               int type = wrapper.read(Types.BYTE);
               wrapper.write(Types.VAR_INT, type == 0 ? 1 : type);
            });
            this.read(Types.UUID);
         }
      });
      this.registerServerbound(ServerboundPackets1_19.CHAT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.read(Types.LONG);
            this.read(Types.LONG);
            this.read(Types.BYTE_ARRAY_PRIMITIVE);
            this.read(Types.BOOLEAN);
         }
      });
      this.registerServerbound(ServerboundPackets1_19.CHAT_COMMAND, ServerboundPackets1_17.CHAT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.read(Types.LONG);
            this.read(Types.LONG);
            this.handler(wrapper -> {
               String command = wrapper.get(Types.STRING, 0);
               wrapper.set(Types.STRING, 0, "/" + command);
               int signatures = wrapper.read(Types.VAR_INT);

               for (int i = 0; i < signatures; i++) {
                  wrapper.read(Types.STRING);
                  wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
               }
            });
            this.read(Types.BOOLEAN);
         }
      });
      this.cancelServerbound(ServerboundPackets1_19.CHAT_PREVIEW);
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.UUID);
            this.map(Types.STRING);
            this.create(Types.PROFILE_PROPERTY_ARRAY, new GameProfile.Property[0]);
         }
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.handler(wrapper -> {
               byte[] publicKey = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
               byte[] nonce = wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
               wrapper.user().put(new NonceStorage1_19(CipherUtil.encryptNonce(publicKey, nonce)));
            });
         }
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.read(Types.OPTIONAL_PROFILE_KEY);
         }
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.BYTE_ARRAY_PRIMITIVE);
            this.handler(wrapper -> {
               if (wrapper.read(Types.BOOLEAN)) {
                  wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
               } else {
                  NonceStorage1_19 nonceStorage = wrapper.user().remove(NonceStorage1_19.class);
                  if (nonceStorage == null) {
                     throw new IllegalArgumentException("Server sent nonce is missing");
                  }

                  wrapper.read(Types.LONG);
                  wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
                  wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, nonceStorage.nonce());
               }
            });
         }
      });
   }

   private long randomLong() {
      return ThreadLocalRandom.current().nextLong();
   }

   public boolean isJsonNotNull(JsonElement element) {
      return element != null && !element.isJsonNull() && (!element.isJsonArray() || !element.getAsJsonArray().isEmpty());
   }

   public JsonElement mapTextComponentIfNull(UserConnection connection, JsonElement component) {
      if (this.isJsonNotNull(component)) {
         this.componentRewriter.processText(connection, component);
         return component;
      } else {
         return ComponentUtil.emptyJsonComponent();
      }
   }

   @Override
   protected void onMappingDataLoaded() {
      EntityTypes1_19.initialize(this);
      ParticleType.Fillers.fill1_19(this, Types1_19.PARTICLE);
      this.tagRewriter.removeTag(RegistryType.ITEM, "minecraft:occludes_vibration_signals");
      this.tagRewriter.renameTag(RegistryType.ITEM, "minecraft:carpets", "minecraft:wool_carpets");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "minecraft:carpets", "minecraft:wool_carpets");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "minecraft:polar_bears_spawnable_on_in_frozen_ocean", "minecraft:polar_bears_spawnable_on_alternate");
      this.tagRewriter
         .addEmptyTags(
            RegistryType.ITEM, "minecraft:chest_boats", "minecraft:dampens_vibrations", "minecraft:mangrove_logs", "minecraft:overworld_natural_logs"
         );
      this.tagRewriter
         .addEmptyTags(
            RegistryType.BLOCK,
            "minecraft:ancient_city_replaceable",
            "minecraft:convertable_to_mud",
            "minecraft:dampens_vibrations",
            "minecraft:frog_prefer_jump_to",
            "minecraft:frogs_spawnable_on",
            "minecraft:mangrove_logs",
            "minecraft:mangrove_logs_can_grow_through",
            "minecraft:mangrove_roots_can_grow_through",
            "minecraft:nether_carver_replaceables",
            "minecraft:overworld_carver_replaceables",
            "minecraft:overworld_natural_logs",
            "minecraft:sculk_replaceable",
            "minecraft:sculk_replaceable_world_gen",
            "minecraft:snaps_goat_horn"
         );
      this.tagRewriter.addEmptyTag(RegistryType.ENTITY, "minecraft:frog_food");
      this.tagRewriter.addEmptyTags(RegistryType.GAME_EVENT, "minecraft:allay_can_listen", "minecraft:shrieker_can_listen", "minecraft:warden_can_listen");
      int[] noItemRequiredPatterns = new int[34];

      for (int i = 0; i < noItemRequiredPatterns.length; i++) {
         noItemRequiredPatterns[i] = i + 1;
      }

      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:no_item_required", noItemRequiredPatterns);
      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:pattern_item/globe", 35);
      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:pattern_item/creeper", 36);
      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:pattern_item/skull", 37);
      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:pattern_item/flower", 38);
      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:pattern_item/mojang", 39);
      this.tagRewriter.addTagRaw(RegistryType.BANNER_PATTERN, "minecraft:pattern_item/piglin", 40);
      super.onMappingDataLoaded();
   }

   @Override
   public void register(ViaProviders providers) {
      providers.register(AckSequenceProvider.class, new AckSequenceProvider());
   }

   @Override
   public void init(UserConnection user) {
      if (!user.has(DimensionRegistryStorage.class)) {
         user.put(new DimensionRegistryStorage());
      }

      user.put(new SequenceStorage());
      user.put(new LoomStorage());
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19.PLAYER));
   }

   public MappingData1_19 getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_19 getEntityRewriter() {
      return this.entityRewriter;
   }

   public ItemPacketRewriter1_19 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPackets1_18> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_18> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPackets1_18> getTagRewriter() {
      return this.tagRewriter;
   }
}
