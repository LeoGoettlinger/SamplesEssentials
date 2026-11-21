package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.painting;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTString;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface PaintingVariant extends MappedEntity, CopyableEntity<PaintingVariant>, DeepComparableEntity {
   NbtCodec<PaintingVariant> CODEC = (new NbtMapCodec<PaintingVariant>() {
      public PaintingVariant decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         int width = tag.getNumberTagOrThrow("width").getAsInt();
         int height = tag.getNumberTagOrThrow("height").getAsInt();
         ResourceLocation assetId = new ResourceLocation(tag.getStringTagValueOrThrow("asset_id"));
         Component title = tag.getOrNull("title", wrapper.getSerializers(), wrapper);
         Component author = tag.getOrNull("author", wrapper.getSerializers(), wrapper);
         return new StaticPaintingVariant(width, height, assetId, title, author);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, PaintingVariant value) throws NbtCodecException {
         tag.setTag("width", new NBTInt(value.getWidth()));
         tag.setTag("height", new NBTInt(value.getHeight()));
         tag.setTag("asset_id", new NBTString(value.getAssetId().toString()));
         if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
            Component title = value.getTitle();
            if (title != null) {
               tag.set("title", title, wrapper.getSerializers(), wrapper);
            }

            Component author = value.getAuthor();
            if (author != null) {
               tag.set("author", author, wrapper.getSerializers(), wrapper);
            }
         }
      }
   }).codec();

   @Contract(
      pure = true
   )
   int getWidth();

   @Contract(
      pure = true
   )
   int getHeight();

   @Contract(
      pure = true
   )
   ResourceLocation getAssetId();

   @Contract(
      pure = true
   )
   @Nullable
   Component getTitle();

   @Contract(
      pure = true
   )
   @Nullable
   Component getAuthor();

   static PaintingVariant read(PacketWrapper<?> wrapper) {
      return wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21)
         ? wrapper.readMappedEntityOrDirect(PaintingVariants.getRegistry(), PaintingVariant::readDirect)
         : wrapper.readMappedEntity(PaintingVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, PaintingVariant variant) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
         wrapper.writeMappedEntityOrDirect(variant, PaintingVariant::writeDirect);
      } else {
         wrapper.writeMappedEntity(variant);
      }
   }

   static PaintingVariant readDirect(PacketWrapper<?> wrapper) {
      int width = wrapper.readVarInt();
      int height = wrapper.readVarInt();
      ResourceLocation assetId = wrapper.readIdentifier();
      Component title = null;
      Component author = null;
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
         title = wrapper.readOptional(PacketWrapper::readComponent);
         author = wrapper.readOptional(PacketWrapper::readComponent);
      }

      return new StaticPaintingVariant(width, height, assetId, title, author);
   }

   static void writeDirect(PacketWrapper<?> wrapper, PaintingVariant variant) {
      wrapper.writeVarInt(variant.getWidth());
      wrapper.writeVarInt(variant.getHeight());
      wrapper.writeIdentifier(variant.getAssetId());
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
         wrapper.writeOptional(variant.getTitle(), PacketWrapper::writeComponent);
         wrapper.writeOptional(variant.getAuthor(), PacketWrapper::writeComponent);
      }
   }

   @Deprecated
   static PaintingVariant decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      return CODEC.decode(nbt, PacketWrapper.createDummyWrapper(version)).copy(data);
   }

   @Deprecated
   static NBT encode(PaintingVariant variant, ClientVersion version) {
      return CODEC.encode(PacketWrapper.createDummyWrapper(version), variant);
   }
}
