package com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant26_1;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.ProvidesBannerPatterns;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ProvidesTrimMaterial1_20_5;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter26_1 extends StructuredItemRewriter<ClientboundPacket1_21_11, ServerboundPacket26_1, Protocol1_21_11To26_1> {
   public BlockItemPacketRewriter26_1(Protocol1_21_11To26_1 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol
         .replaceClientbound(
            ClientboundPackets1_21_11.LEVEL_CHUNK_WITH_LIGHT,
            wrapper -> {
               Chunk chunk = this.protocol.getBlockRewriter().handleChunk1_18(wrapper);

               for (ChunkSection section : chunk.getSections()) {
                  if (section.getNonAirBlocksCount() != 0) {
                     DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
                     blockPalette.forEachMatchingCoordinate(
                        id -> Protocol1_21_11To26_1.MAPPINGS.fluidBlockStates().contains(id), $ -> section.setFluidCount(section.getFluidCount() + 1)
                     );
                  }
               }

               this.protocol.getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
            }
         );
   }

   @Override
   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      upgradeData(this.protocol, connection, this.protocol.types().structuredDataKeys(), container);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   public static void upgradeData(
      Protocol<?, ?, ?, ?> protocol, UserConnection connection, StructuredDataKeys1_21_11 dataKeys, StructuredDataContainer container
   ) {
      container.replace(
         StructuredDataKey.JUKEBOX_PLAYABLE1_21_5,
         StructuredDataKey.JUKEBOX_PLAYABLE26_1,
         jukeboxPlayable -> upgradeHolder(protocol, connection, jukeboxPlayable.song(), "jukebox_song")
      );
      container.replace(
         StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.INSTRUMENT26_1, instrument -> upgradeHolder(protocol, connection, instrument, "instrument")
      );
      container.replace(
         StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5,
         StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1,
         providesTrimMaterial -> upgradeHolder(protocol, connection, providesTrimMaterial.material(), "trim_material")
      );
      container.replace(
         StructuredDataKey.CHICKEN_VARIANT1_21_5,
         StructuredDataKey.CHICKEN_VARIANT26_1,
         chickenVariant -> upgradeEitherVariant(protocol, connection, chickenVariant, "chicken_variant")
      );
      container.replace(
         StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11,
         StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT26_1,
         nautilusVariant -> upgradeEitherVariant(protocol, connection, nautilusVariant, "zombie_nautilus_variant")
      );
      container.replace(
         StructuredDataKey.DAMAGE_TYPE1_21_11,
         StructuredDataKey.DAMAGE_TYPE26_1,
         damageType -> upgradeEitherVariant(protocol, connection, damageType, "damage_type")
      );
      container.replace(
         StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5,
         StructuredDataKey.PROVIDES_BANNER_PATTERNS26_1,
         key -> new ProvidesBannerPatterns(tagKeyToHolderSet(protocol, connection, "banner_pattern", key.original()))
      );
      container.replace(
         StructuredDataKey.DAMAGE_RESISTANT1_21_2,
         StructuredDataKey.DAMAGE_RESISTANT26_1,
         damageResistant -> new DamageResistant26_1(tagKeyToHolderSet(protocol, connection, "damage_type", damageResistant.typesTagKey().original()))
      );
      container.replaceKey(StructuredDataKey.BLOCKS_ATTACKS1_21_5, StructuredDataKey.BLOCKS_ATTACKS26_1);
      Item[] containerData = container.get(dataKeys.container);
      if (containerData != null) {
         for (int i = 0; i < containerData.length; i++) {
            if (containerData[i].isEmpty()) {
               containerData[i] = null;
            }
         }
      }
   }

   @Nullable
   private static Integer upgradeEitherVariant(Protocol<?, ?, ?, ?> protocol, UserConnection connection, Either<Integer, String> eitherHolder, String registry) {
      return eitherHolder.isLeft() ? eitherHolder.left() : registryIdOrNull(protocol, connection, registry, eitherHolder.right());
   }

   @Nullable
   private static <T> Holder<T> upgradeHolder(Protocol<?, ?, ?, ?> protocol, UserConnection connection, EitherHolder<T> eitherHolder, String registry) {
      if (eitherHolder.hasHolder()) {
         return eitherHolder.holder();
      } else {
         Integer id = registryIdOrNull(protocol, connection, registry, eitherHolder.key());
         return id != null ? Holder.of(id.intValue()) : null;
      }
   }

   @Nullable
   private static Integer registryIdOrNull(Protocol<?, ?, ?, ?> protocol, UserConnection connection, String registry, String key) {
      int id = protocol.getEntityRewriter().<EntityTracker>tracker(connection).registryKeys(registry).keyToId(key);
      return id != -1 ? id : null;
   }

   private static HolderSet tagKeyToHolderSet(Protocol<?, ?, ?, ?> protocol, UserConnection connection, String registry, String key) {
      KeyMappings damageTypes = protocol.getEntityRewriter().<EntityTracker>tracker(connection).registryKeys(registry);
      int damageType = damageTypes.keyToId(key);
      return damageType != -1 ? HolderSet.of(new int[]{damageType}) : HolderSet.of(key);
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeData(this.protocol.mappedTypes().structuredDataKeys(), container);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   public static void downgradeData(StructuredDataKeys1_21_11 dataKeys, StructuredDataContainer container) {
      container.replace(
         StructuredDataKey.JUKEBOX_PLAYABLE26_1, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, jukeboxPlayable -> new JukeboxPlayable(jukeboxPlayable, true)
      );
      container.replace(StructuredDataKey.INSTRUMENT26_1, StructuredDataKey.INSTRUMENT1_21_5, EitherHolder::of);
      container.replace(
         StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1,
         StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5,
         providesTrimMaterial -> new ProvidesTrimMaterial1_20_5(EitherHolder.of(providesTrimMaterial))
      );
      container.replace(StructuredDataKey.CHICKEN_VARIANT26_1, StructuredDataKey.CHICKEN_VARIANT1_21_5, Either::left);
      container.replace(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT26_1, StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11, Either::left);
      container.replace(StructuredDataKey.DAMAGE_TYPE26_1, StructuredDataKey.DAMAGE_TYPE1_21_11, Either::left);
      container.replace(
         StructuredDataKey.PROVIDES_BANNER_PATTERNS26_1, StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5, patterns -> tagOrNull(patterns.patterns())
      );
      container.replace(StructuredDataKey.DAMAGE_RESISTANT26_1, StructuredDataKey.DAMAGE_RESISTANT1_21_2, damageResistant -> {
         Key key = tagOrNull(damageResistant.types());
         return key != null ? new DamageResistant1_21_2(key) : null;
      });
      container.replace(
         StructuredDataKey.BLOCKS_ATTACKS26_1,
         StructuredDataKey.BLOCKS_ATTACKS1_21_5,
         blocksAttacks -> blocksAttacks.bypassedBy() != null && !blocksAttacks.bypassedBy().hasTagKey()
            ? new BlocksAttacks(
               blocksAttacks.blockDelaySeconds(),
               blocksAttacks.disableCooldownScale(),
               blocksAttacks.damageReductions(),
               blocksAttacks.itemDamage(),
               null,
               blocksAttacks.blockSound(),
               blocksAttacks.disableSound()
            )
            : blocksAttacks
      );
      container.remove(StructuredDataKey.ADDITIONAL_TRADE_COST);
      container.remove(StructuredDataKey.DYE);
      container.remove(StructuredDataKey.CAT_SOUND_VARIANT);
      container.remove(StructuredDataKey.CHICKEN_SOUND_VARIANT);
      container.remove(StructuredDataKey.COW_SOUND_VARIANT);
      container.remove(StructuredDataKey.PIG_SOUND_VARIANT);
      Item[] containerData = container.get(dataKeys.container);
      if (containerData != null) {
         for (int i = 0; i < containerData.length; i++) {
            if (containerData[i] == null) {
               containerData[i] = StructuredItem.empty();
            }
         }
      }
   }

   @Nullable
   private static Key tagOrNull(HolderSet holderSet) {
      return holderSet.hasTagKey() ? Key.of(holderSet.tagKey()) : null;
   }
}
