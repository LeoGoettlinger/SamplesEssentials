package ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.json.legacyimpl;

import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.gson.BackwardCompatUtil;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.json.LegacyHoverEventSerializer;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.key.Key;
import ac.grim.grimac.shaded.kyori.adventure.nbt.CompoundBinaryTag;
import ac.grim.grimac.shaded.kyori.adventure.nbt.TagStringIO;
import ac.grim.grimac.shaded.kyori.adventure.nbt.api.BinaryTagHolder;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.TextComponent;
import ac.grim.grimac.shaded.kyori.adventure.text.event.HoverEvent;
import ac.grim.grimac.shaded.kyori.adventure.util.Codec;
import java.io.IOException;
import java.util.UUID;

final class NBTLegacyHoverEventSerializerImpl implements LegacyHoverEventSerializer {
   static final NBTLegacyHoverEventSerializerImpl INSTANCE = new NBTLegacyHoverEventSerializerImpl();
   private static final TagStringIO SNBT_IO;
   private static final Codec<CompoundBinaryTag, String, IOException, IOException> SNBT_CODEC;
   static final String ITEM_TYPE = "id";
   static final String ITEM_COUNT = "Count";
   static final String ITEM_TAG = "tag";
   static final String ENTITY_NAME = "name";
   static final String ENTITY_TYPE = "type";
   static final String ENTITY_ID = "id";

   private NBTLegacyHoverEventSerializerImpl() {
   }

   @NotNull
   @Override
   public HoverEvent.ShowItem deserializeShowItem(@NotNull Component input) throws IOException {
      assertTextComponent(input);
      CompoundBinaryTag contents = SNBT_CODEC.decode(((TextComponent)input).content());
      CompoundBinaryTag tag = contents.getCompound("tag");
      return BackwardCompatUtil.createShowItem(
         Key.key(contents.getString("id")),
         contents.getByte("Count", (byte)1),
         tag == CompoundBinaryTag.empty() ? null : BinaryTagHolder.encode(tag, SNBT_CODEC)
      );
   }

   @NotNull
   @Override
   public Component serializeShowItem(HoverEvent.ShowItem input) throws IOException {
      CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().putString("id", input.item().asString()).putByte("Count", (byte)input.count());
      BinaryTagHolder nbt = input.nbt();
      if (nbt != null) {
         builder.put("tag", nbt.get(SNBT_CODEC));
      }

      return Component.text(SNBT_CODEC.encode(builder.build()));
   }

   @NotNull
   @Override
   public HoverEvent.ShowEntity deserializeShowEntity(@NotNull Component input, Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
      assertTextComponent(input);
      CompoundBinaryTag contents = SNBT_CODEC.decode(((TextComponent)input).content());
      return BackwardCompatUtil.createShowEntity(
         Key.key(contents.getString("type")), UUID.fromString(contents.getString("id")), componentCodec.decode(contents.getString("name"))
      );
   }

   @NotNull
   @Override
   public Component serializeShowEntity(HoverEvent.ShowEntity input, Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
      CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().putString("id", input.id().toString()).putString("type", input.type().asString());
      Component name = input.name();
      if (name != null) {
         builder.putString("name", componentCodec.encode(name));
      }

      return Component.text(SNBT_CODEC.encode(builder.build()));
   }

   private static void assertTextComponent(Component component) {
      if (!(component instanceof TextComponent) || !component.children().isEmpty()) {
         throw new IllegalArgumentException("Legacy events must be single Component instances");
      }
   }

   static {
      TagStringIO io;
      try {
         io = TagStringIO.tagStringIO();
      } catch (Throwable var2) {
         io = TagStringIO.get();
      }

      SNBT_IO = io;
      SNBT_CODEC = BackwardCompatUtil.createCodec(SNBT_IO::asCompound, SNBT_IO::asString);
   }
}
