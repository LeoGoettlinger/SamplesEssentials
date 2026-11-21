package com.viaversion.viaversion.libs.mcstructs.text.serializer;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.GsonBuilder;
import com.viaversion.viaversion.libs.gson.JsonDeserializer;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.gson.JsonSerializer;
import com.viaversion.viaversion.libs.gson.internal.Streams;
import com.viaversion.viaversion.libs.gson.stream.JsonReader;
import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.codec.Codec;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3.JsonConverter_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3.NbtConverter_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_5.JsonConverter_v1_20_5;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_21_5.NbtConverter_v1_21_5;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v26_2.NbtConverter_v26_2;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbt;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtDeserializeException;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtSerializeException;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.StyleCodecs_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.TextCodecs_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_5.StyleCodecs_v1_20_5;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_5.TextCodecs_v1_20_5;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_2.StyleCodecs_v1_21_2;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_2.TextCodecs_v1_21_2;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_4.StyleCodecs_v1_21_4;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_4.TextCodecs_v1_21_4;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_5.StyleCodecs_v1_21_5;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_5.TextCodecs_v1_21_5;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_6.StyleCodecs_v1_21_6;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_6.TextCodecs_v1_21_6;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_9.StyleCodecs_v1_21_9;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_21_9.TextCodecs_v1_21_9;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v26_1.StyleCodecs_v26_1;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v26_1.TextCodecs_v26_1;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v26_2.StyleCodecs_v26_2;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v26_2.TextCodecs_v26_2;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.verify.TextVerifier;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.verify.VerifyingConverter;
import java.io.StringReader;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TextComponentCodec {
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
   public static final TextComponentCodec V1_20_3 = new TextComponentCodec(
      () -> SNbt.V1_14, () -> TextCodecs_v1_20_3.TEXT, () -> StyleCodecs_v1_20_3.CODEC, JsonConverter_v1_20_3.INSTANCE, NbtConverter_v1_20_3.INSTANCE
   );
   public static final TextComponentCodec V1_20_5 = new TextComponentCodec(
      () -> SNbt.V1_14, () -> TextCodecs_v1_20_5.TEXT, () -> StyleCodecs_v1_20_5.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_20_3.INSTANCE
   );
   public static final TextComponentCodec V1_21_2 = new TextComponentCodec(
      () -> SNbt.V1_14, () -> TextCodecs_v1_21_2.TEXT, () -> StyleCodecs_v1_21_2.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_20_3.INSTANCE
   );
   public static final TextComponentCodec V1_21_4 = new TextComponentCodec(
      () -> SNbt.V1_14, () -> TextCodecs_v1_21_4.TEXT, () -> StyleCodecs_v1_21_4.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_20_3.INSTANCE
   );
   public static final TextComponentCodec V1_21_5 = new TextComponentCodec(
      () -> SNbt.V1_21_5, () -> TextCodecs_v1_21_5.TEXT, () -> StyleCodecs_v1_21_5.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_21_5.INSTANCE
   );
   public static final TextComponentCodec V1_21_6 = new TextComponentCodec(
      () -> SNbt.V1_21_5, () -> TextCodecs_v1_21_6.TEXT, () -> StyleCodecs_v1_21_6.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_21_5.INSTANCE
   );
   public static final TextComponentCodec V1_21_9 = new TextComponentCodec(
      () -> SNbt.V1_21_5, () -> TextCodecs_v1_21_9.TEXT, () -> StyleCodecs_v1_21_9.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_21_5.INSTANCE
   );
   public static final TextComponentCodec V26_1 = new TextComponentCodec(
      () -> SNbt.V1_21_5, () -> TextCodecs_v26_1.TEXT, () -> StyleCodecs_v26_1.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v1_21_5.INSTANCE
   );
   public static final TextComponentCodec V26_2 = new TextComponentCodec(
      () -> SNbt.V1_21_5, () -> TextCodecs_v26_2.TEXT, () -> StyleCodecs_v26_2.CODEC, JsonConverter_v1_20_5.INSTANCE, NbtConverter_v26_2.INSTANCE
   );
   public static final TextComponentCodec LATEST = V26_2;
   private final Supplier<SNbt<CompoundTag>> sNbtSupplier;
   private final Supplier<Codec<TextComponent>> textCodecSupplier;
   private final Supplier<Codec<Style>> styleCodecSupplier;
   private final DataConverter<JsonElement> jsonConverter;
   private final DataConverter<Tag> nbtConverter;
   private SNbt<CompoundTag> sNbt;
   private Codec<TextComponent> textCodec;
   private Codec<Style> styleCodec;

   public TextComponentCodec(
      Supplier<SNbt<CompoundTag>> sNbtSupplier,
      Supplier<Codec<TextComponent>> textCodec,
      Supplier<Codec<Style>> styleCodec,
      DataConverter<JsonElement> jsonConverter,
      DataConverter<Tag> nbtConverter
   ) {
      this.sNbtSupplier = sNbtSupplier;
      this.textCodecSupplier = textCodec;
      this.styleCodecSupplier = styleCodec;
      this.jsonConverter = jsonConverter;
      this.nbtConverter = nbtConverter;
   }

   private SNbt<CompoundTag> getSNbtSerializer() {
      if (this.sNbt == null) {
         this.sNbt = this.sNbtSupplier.get();
      }

      return this.sNbtSupplier.get();
   }

   public Codec<TextComponent> getTextCodec() {
      if (this.textCodec == null) {
         this.textCodec = this.textCodecSupplier.get();
      }

      return this.textCodec;
   }

   public Codec<Style> getStyleCodec() {
      if (this.styleCodec == null) {
         this.styleCodec = this.styleCodecSupplier.get();
      }

      return this.styleCodec;
   }

   public DataConverter<JsonElement> getJsonConverter() {
      return this.jsonConverter;
   }

   public DataConverter<Tag> getNbtConverter() {
      return this.nbtConverter;
   }

   public TextComponentCodec withVerifier(TextVerifier verifier) {
      return new TextComponentCodec(
         this.sNbtSupplier,
         this.textCodecSupplier,
         this.styleCodecSupplier,
         new VerifyingConverter<>(this.jsonConverter, verifier),
         new VerifyingConverter<>(this.nbtConverter, verifier)
      );
   }

   public TextComponent deserializeJson(String json) {
      return this.deserializeJsonTree(JsonParser.parseString(json));
   }

   public TextComponent deserializeJsonReader(String json) {
      JsonReader reader = new JsonReader(new StringReader(json));
      reader.setLenient(false);

      try {
         return this.deserialize(Streams.parse(reader));
      } catch (StackOverflowError var4) {
         throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", var4);
      }
   }

   public TextComponent deserializeLenientJson(String json) {
      JsonReader reader = new JsonReader(new StringReader(json));
      reader.setLenient(true);
      return this.deserializeJsonTree(JsonParser.parseReader(reader));
   }

   public TextComponent deserializeNbt(String nbt) {
      try {
         return this.deserialize(this.getSNbtSerializer().getDeserializer().deserializeValue(nbt));
      } catch (SNbtDeserializeException var3) {
         throw new RuntimeException("Failed to deserialize SNbt", var3);
      }
   }

   public TextComponent deserializeJsonTree(@Nullable JsonElement element) {
      return element == null ? null : this.deserialize(element);
   }

   public TextComponent deserializeNbtTree(@Nullable Tag nbt) {
      return nbt == null ? null : this.deserialize(nbt);
   }

   public TextComponent deserialize(JsonElement json) {
      return this.getTextCodec().deserialize(this.jsonConverter, json).getOrThrow(JsonParseException::new);
   }

   public TextComponent deserialize(Tag nbt) {
      return this.getTextCodec().deserialize(this.nbtConverter, nbt).get();
   }

   public JsonElement serializeJsonTree(TextComponent component) {
      return this.getTextCodec().serialize(this.jsonConverter, component).getOrThrow(JsonParseException::new);
   }

   public Tag serializeNbtTree(TextComponent component) {
      return this.getTextCodec().serialize(this.nbtConverter, component).get();
   }

   public String serializeJsonString(TextComponent component) {
      return GSON.toJson(this.serializeJsonTree(component));
   }

   public String serializeNbtString(TextComponent component) {
      try {
         return this.getSNbtSerializer().serialize(this.serializeNbtTree(component));
      } catch (SNbtSerializeException var3) {
         throw new RuntimeException("Failed to serialize SNbt", var3);
      }
   }

   public TextComponentSerializer asSerializer() {
      return new TextComponentSerializer(
         this,
         () -> new GsonBuilder()
            .registerTypeHierarchyAdapter(TextComponent.class, (JsonSerializer<TextComponent>)(src, typeOfSrc, context) -> this.serializeJsonTree(src))
            .registerTypeHierarchyAdapter(TextComponent.class, (JsonDeserializer<TextComponent>)(src, typeOfSrc, context) -> this.deserializeJsonTree(src))
            .disableHtmlEscaping()
            .create()
      );
   }
}
