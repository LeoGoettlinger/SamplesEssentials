package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.Dialog;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.provider.ChestDialogViewProvider;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.provider.DialogViewProvider;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter.BlockItemPacketRewriter1_21_6;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter.ComponentRewriter1_21_6;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter.EntityPacketRewriter1_21_6;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.rewriter.RegistryDataRewriter1_21_6;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.ChestDialogStorage;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.ClickEvents;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.RegistryAndTags;
import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.storage.ServerLinks;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.Protocol1_21_5To1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21_6To1_21_5
   extends BackwardsProtocol<ClientboundPacket1_21_6, ClientboundPacket1_21_5, ServerboundPacket1_21_6, ServerboundPacket1_21_5> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.6", "1.21.5", Protocol1_21_5To1_21_6.class);
   private final EntityPacketRewriter1_21_6 entityRewriter = new EntityPacketRewriter1_21_6(this);
   private final BlockItemPacketRewriter1_21_6 itemRewriter = new BlockItemPacketRewriter1_21_6(this);
   private final ParticleRewriter<ClientboundPacket1_21_6> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket1_21_6> translatableRewriter = new ComponentRewriter1_21_6(this);
   private final TagRewriter<ClientboundPacket1_21_6> tagRewriter = new TagRewriter(this);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_6> recipeRewriter = new RecipeDisplayRewriter1_21_5(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new RegistryDataRewriter1_21_6(this);
   private final BlockRewriter<ClientboundPacket1_21_6> blockRewriter = new BlockRewriter1_21_5(this, ChunkType1_21_5::new);

   public Protocol1_21_6To1_21_5() {
      super(ClientboundPacket1_21_6.class, ClientboundPacket1_21_5.class, ServerboundPacket1_21_6.class, ServerboundPacket1_21_5.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.tagRewriter.renameTag(RegistryType.BLOCK, "triggers_ambient_desert_dry_vegetation_block_sounds", "plays_ambient_desert_block_sounds");
      this.replaceClientbound(ClientboundPackets1_21_6.UPDATE_TAGS, this::updateTags);
      this.replaceClientbound(ClientboundConfigurationPackets1_21_6.UPDATE_TAGS, this::updateTags);
      this.appendClientbound(ClientboundPackets1_21_6.SOUND, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         this.fixSoundSource(wrapper);
      });
      this.appendClientbound(ClientboundPackets1_21_6.SOUND_ENTITY, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         this.fixSoundSource(wrapper);
      });
      this.appendClientbound(ClientboundPackets1_21_6.STOP_SOUND, wrapper -> {
         byte flags = (Byte)wrapper.get(Types.BYTE, 0);
         if ((flags & 1) != 0) {
            this.fixSoundSource(wrapper);
         }
      });
      CommandRewriter1_19_4<ClientboundPacket1_21_6> commandRewriter = new CommandRewriter1_19_4<ClientboundPacket1_21_6>(this) {
         public void handleArgument(PacketWrapper wrapper, String argumentType) {
            if (!argumentType.equals("minecraft:hex_color") && !argumentType.equals("minecraft:dialog")) {
               super.handleArgument(wrapper, argumentType);
            } else {
               wrapper.write(Types.VAR_INT, 0);
            }
         }
      };
      this.replaceClientbound(ClientboundPackets1_21_6.COMMANDS, commandRewriter::handle1_19);
      this.registerClientbound(ClientboundPackets1_21_6.CHANGE_DIFFICULTY, wrapper -> {
         int difficulty = (Integer)wrapper.read(Types.VAR_INT);
         wrapper.write(Types.UNSIGNED_BYTE, (short)difficulty);
      });
      this.registerServerbound(ServerboundPackets1_21_5.CHANGE_DIFFICULTY, wrapper -> {
         short difficulty = (Short)wrapper.read(Types.UNSIGNED_BYTE);
         wrapper.write(Types.VAR_INT, Integer.valueOf(difficulty));
      });
      this.registerServerbound(ServerboundPackets1_21_5.CHAT_COMMAND, this::handleClickEvents);
      this.registerServerbound(ServerboundPackets1_21_5.CHAT_COMMAND_SIGNED, this::handleClickEvents);
      this.registerClientbound(ClientboundPackets1_21_6.SHOW_DIALOG, null, wrapper -> {
         wrapper.cancel();
         if (ViaBackwards.getConfig().dialogsViaChests()) {
            RegistryAndTags registryAndTags = (RegistryAndTags)wrapper.user().get(RegistryAndTags.class);
            ServerLinks serverLinks = (ServerLinks)wrapper.user().get(ServerLinks.class);
            Holder<CompoundTag> holder = (Holder<CompoundTag>)wrapper.passthrough(Types.TRUSTED_COMPOUND_TAG_HOLDER);
            CompoundTag tag = holder.isDirect() ? (CompoundTag)holder.value() : registryAndTags.fromRegistry(holder.id());
            DialogViewProvider provider = (DialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
            provider.openDialog(wrapper.user(), new Dialog(registryAndTags, serverLinks, tag));
         }
      });
      this.registerClientbound(ClientboundConfigurationPackets1_21_6.SHOW_DIALOG, null, wrapper -> {
         wrapper.cancel();
         if (ViaBackwards.getConfig().dialogsViaChests()) {
            RegistryAndTags registryAndTags = (RegistryAndTags)wrapper.user().get(RegistryAndTags.class);
            ServerLinks serverLinks = (ServerLinks)wrapper.user().get(ServerLinks.class);
            CompoundTag tag = (CompoundTag)wrapper.read(Types.TRUSTED_COMPOUND_TAG);
            DialogViewProvider provider = (DialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
            provider.openDialog(wrapper.user(), new Dialog(registryAndTags, serverLinks, tag));
         }
      });
      this.registerClientbound(ClientboundPackets1_21_6.CLEAR_DIALOG, null, this::clearDialog);
      this.registerClientbound(ClientboundConfigurationPackets1_21_6.CLEAR_DIALOG, null, this::clearDialog);
      this.registerClientbound(ClientboundPackets1_21_6.SERVER_LINKS, this::storeServerLinks);
      this.registerClientbound(ClientboundConfigurationPackets1_21_6.SERVER_LINKS, this::storeServerLinks);
      this.registerServerbound(ServerboundPackets1_21_5.CONTAINER_CLOSE, wrapper -> {
         ChestDialogStorage storage = (ChestDialogStorage)wrapper.user().get(ChestDialogStorage.class);
         if (storage != null) {
            ChestDialogViewProvider provider = (ChestDialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
            if (storage.phase() == ChestDialogStorage.Phase.ANVIL_VIEW) {
               wrapper.cancel();
               provider.openChestView(wrapper.user(), storage, ChestDialogStorage.Phase.DIALOG_VIEW);
            } else if (storage.phase() == ChestDialogStorage.Phase.WAITING_FOR_RESPONSE) {
               wrapper.cancel();
               if (storage.closeButtonEnabled()) {
                  provider.openChestView(wrapper.user(), storage, ChestDialogStorage.Phase.DIALOG_VIEW);
               } else {
                  provider.openChestView(wrapper.user(), storage, ChestDialogStorage.Phase.WAITING_FOR_RESPONSE);
               }
            } else {
               boolean allowClosing = storage.allowClosing();
               if (!allowClosing) {
                  wrapper.cancel();
                  if (storage.dialog().canCloseWithEscape()) {
                     provider.clickButton(wrapper.user(), Dialog.AfterAction.CLOSE, storage.dialog().actionButton());
                  } else {
                     provider.openChestView(wrapper.user(), storage, ChestDialogStorage.Phase.DIALOG_VIEW);
                  }
               }

               storage.setAllowClosing(false);
            }
         }
      });
      this.registerServerbound(ServerboundPackets1_21_5.RENAME_ITEM, wrapper -> {
         ChestDialogStorage storage = (ChestDialogStorage)wrapper.user().get(ChestDialogStorage.class);
         if (storage != null && storage.phase() == ChestDialogStorage.Phase.ANVIL_VIEW) {
            wrapper.cancel();
            String name = (String)wrapper.read(Types.STRING);
            ChestDialogViewProvider provider = (ChestDialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
            provider.updateAnvilText(wrapper.user(), name);
         }
      });
      this.appendServerbound(ServerboundPackets1_21_5.CONTAINER_CLICK, wrapper -> {
         ChestDialogViewProvider provider = (ChestDialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
         if (provider != null) {
            int containerId = (Integer)wrapper.get(Types.VAR_INT, 0);
            int slot = (Short)wrapper.get(Types.SHORT, 0);
            byte button = (Byte)wrapper.get(Types.BYTE, 0);
            int mode = (Integer)wrapper.get(Types.VAR_INT, 2);
            if (provider.clickDialog(wrapper.user(), containerId, slot, button, mode)) {
               wrapper.cancel();
            }
         }
      });
      this.cancelClientbound(ClientboundPackets1_21_6.TRACKED_WAYPOINT);
   }

   private void fixSoundSource(PacketWrapper wrapper) {
      int source = (Integer)wrapper.get(Types.VAR_INT, 0);
      if (source == 10) {
         wrapper.set(Types.VAR_INT, 0, 0);
      }
   }

   private void updateTags(PacketWrapper wrapper) {
      RegistryAndTags registryAndTags = (RegistryAndTags)wrapper.user().get(RegistryAndTags.class);
      int length = (Integer)wrapper.passthrough(Types.VAR_INT);

      for (int i = 0; i < length; i++) {
         String registryKey = (String)wrapper.passthrough(Types.STRING);
         boolean dialog = "dialog".equals(Key.stripMinecraftNamespace(registryKey));
         int tagsSize = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int j = 0; j < tagsSize; j++) {
            String key = (String)wrapper.passthrough(Types.STRING);
            int[] ids = (int[])wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
            if (dialog) {
               registryAndTags.storeTags(key, ids);
            }
         }
      }

      wrapper.resetReader();
      this.tagRewriter.handleGeneric(wrapper);
   }

   private void clearDialog(PacketWrapper wrapper) {
      wrapper.cancel();
      if (ViaBackwards.getConfig().dialogsViaChests()) {
         DialogViewProvider provider = (DialogViewProvider)Via.getManager().getProviders().get(DialogViewProvider.class);
         provider.closeDialog(wrapper.user());
      }
   }

   private void storeServerLinks(PacketWrapper wrapper) {
      ServerLinks serverLinks = new ServerLinks();
      int length = (Integer)wrapper.passthrough(Types.VAR_INT);

      for (int i = 0; i < length; i++) {
         if ((Boolean)wrapper.passthrough(Types.BOOLEAN)) {
            int id = (Integer)wrapper.passthrough(Types.VAR_INT);
            String url = (String)wrapper.passthrough(Types.STRING);
            serverLinks.storeLink(id, url);
         } else {
            Tag tag = (Tag)wrapper.passthrough(Types.TRUSTED_TAG);
            String url = (String)wrapper.passthrough(Types.STRING);
            serverLinks.storeLink(tag, url);
         }
      }

      wrapper.user().put(serverLinks);
   }

   private void handleClickEvents(PacketWrapper wrapper) {
      String command = (String)wrapper.passthrough(Types.STRING);
      ClickEvents clickEvents = (ClickEvents)wrapper.user().get(ClickEvents.class);
      if (clickEvents.handleChatCommand(wrapper.user(), command)) {
         wrapper.cancel();
      }
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_21_6.PLAYER));
      user.storables(this).setItemHasher(new ItemHasherBase(this, user));
      user.put(new RegistryAndTags());
      user.put(new ClickEvents());
   }

   public void register(ViaProviders providers) {
      providers.register(DialogViewProvider.class, new ChestDialogViewProvider(this));
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_6 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_6 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_6> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_6> getParticleRewriter() {
      return this.particleRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket1_21_6> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_6> getTagRewriter() {
      return this.tagRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket1_21_6> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V1_21_6;
   }

   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V1_21_5;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_6, ClientboundPacket1_21_5, ServerboundPacket1_21_6, ServerboundPacket1_21_5> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(
            this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class}
         ),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_5.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_5.class, ServerboundConfigurationPackets1_20_5.class}
         )
      );
   }
}
