package com.viaversion.viaversion.libs.mcstructs.text.serializer.v26_1;

import com.viaversion.viaversion.libs.mcstructs.converter.codec.Codec;
import com.viaversion.viaversion.libs.mcstructs.converter.codec.impl.LazyInitCodec;
import com.viaversion.viaversion.libs.mcstructs.converter.codec.map.MapCodecMerger;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3.JavaConverter_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.converter.mapcodec.MapCodec;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Either;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import com.viaversion.viaversion.libs.mcstructs.converter.types.NamedType;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.KeybindComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.NbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.ObjectComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.ScoreComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.SelectorComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.BlockNbtSource;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.EntityNbtSource;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.NbtDataSource;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.StorageNbtSource;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.TextCodecs_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.verify.VerifyingConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;

public class TextCodecs_v26_1 {
   public static final Codec<TextComponent> TEXT = new LazyInitCodec<>(
      () -> Codec.recursive(
         thiz -> TextCodecs_v1_20_3.createCodec(
            thiz, StyleCodecs_v26_1.MAP_CODEC, TextCodecs_v26_1.ComponentType.values(), TextCodecs_v26_1.ComponentType::forComponent
         )
      )
   );
   public static final MapCodec<StringComponent> STRING_COMPONENT = MapCodecMerger.mapCodec(
      Codec.STRING.mapCodec("text").required(), StringComponent::getText, StringComponent::new
   );
   public static final MapCodec<TranslationComponent> TRANSLATION_COMPONENT = MapCodecMerger.mapCodec(
      Codec.STRING.mapCodec("translate").required(),
      TranslationComponent::getKey,
      Codec.STRING.mapCodec("fallback").optional().lenient().defaulted(null),
      TranslationComponent::getFallback,
      Codec.either(
            JavaConverter_v1_20_3.INSTANCE
               .toCodec()
               .verified(
                  o -> !(o instanceof Boolean) && !(o instanceof Number) && !(o instanceof String)
                     ? Result.error("Invalid value type: " + o.getClass().getName())
                     : null
               ),
            TEXT
         )
         .map(o -> o instanceof TextComponent ? Either.right((TextComponent)o) : Either.left(o), Either::unwrap)
         .listOf()
         .mapCodec("with")
         .optional()
         .defaulted(List::isEmpty, ArrayList::new),
      comp -> Arrays.asList(comp.getArgs()),
      (key, fallback, arguments) -> new TranslationComponent(key, arguments).setFallback(fallback)
   );
   public static final MapCodec<KeybindComponent> KEYBIND_COMPONENT = MapCodecMerger.mapCodec(
      Codec.STRING.mapCodec("keybind").required(), KeybindComponent::getKeybind, KeybindComponent::new
   );
   public static final MapCodec<ScoreComponent> SCORE_COMPONENT = MapCodecMerger.codec(
         Codec.STRING
            .converterVerified(VerifyingConverter.verify(TextVerifier_v26_1.class, TextVerifier_v26_1::verifySelector, "Invalid selector"))
            .mapCodec("name")
            .required(),
         ScoreComponent::getName,
         Codec.STRING.mapCodec("objective").required(),
         ScoreComponent::getObjective,
         ScoreComponent::new
      )
      .mapCodec("score")
      .required();
   public static final MapCodec<SelectorComponent> SELECTOR_COMPONENT = MapCodecMerger.mapCodec(
      Codec.STRING
         .converterVerified(VerifyingConverter.verify(TextVerifier_v26_1.class, TextVerifier_v26_1::verifySelector, "Invalid selector"))
         .mapCodec("selector")
         .required(),
      SelectorComponent::getSelector,
      TEXT.mapCodec("separator").optional().defaulted(null),
      SelectorComponent::getSeparator,
      SelectorComponent::new
   );
   public static final MapCodec<NbtComponent> NBT_COMPONENT = MapCodecMerger.<String, Boolean, Boolean, TextComponent, NbtDataSource, NbtComponent>mapCodec(
         Codec.STRING
            .converterVerified(VerifyingConverter.verify(TextVerifier_v26_1.class, TextVerifier_v26_1::verifyNbtPath, "Invalid nbt path"))
            .mapCodec("nbt")
            .required(),
         NbtComponent::getComponent,
         Codec.BOOLEAN.mapCodec("interpret").optional().lenient().defaulted(false),
         NbtComponent::isResolve,
         Codec.BOOLEAN.mapCodec("plain").optional().lenient().defaulted(false),
         NbtComponent::isPlain,
         TEXT.mapCodec("separator").optional().lenient().defaulted(null),
         NbtComponent::getSeparator,
         TextCodecs_v1_20_3.createLegacyComponentMatcher(
            TextCodecs_v26_1.NbtDataSourceType.values(),
            TextCodecs_v26_1.NbtDataSourceType::getCodec,
            TextCodecs_v26_1.NbtDataSourceType::forDataSource,
            "source"
         ),
         NbtComponent::getDataSource,
         NbtComponent::new
      )
      .verified(comp -> comp.isResolve() && comp.isPlain() ? Result.error("'interpret' and 'plain' flags can't be both on") : null);
   public static final MapCodec<ObjectComponent> OBJECT_COMPONENT = MapCodecMerger.mapCodec(
      TextCodecs_v1_20_3.createLegacyComponentMatcher(
         TextCodecs_v26_1.ObjectInfos.values(), TextCodecs_v26_1.ObjectInfos::getCodec, TextCodecs_v26_1.ObjectInfos::forObjectInfo, "object"
      ),
      ObjectComponent::getObjectInfo,
      TEXT.mapCodec("fallback").optional().defaulted(null),
      ObjectComponent::getFallback,
      ObjectComponent::new
   );

   private static enum ComponentType implements TextCodecs_v1_20_3.TextComponentType {
      STRING("text", TextCodecs_v26_1.STRING_COMPONENT),
      TRANSLATION("translatable", TextCodecs_v26_1.TRANSLATION_COMPONENT),
      KEYBIND("keybind", TextCodecs_v26_1.KEYBIND_COMPONENT),
      SCORE("score", TextCodecs_v26_1.SCORE_COMPONENT),
      SELECTOR("selector", TextCodecs_v26_1.SELECTOR_COMPONENT),
      NBT("nbt", TextCodecs_v26_1.NBT_COMPONENT),
      OBJECT("object", TextCodecs_v26_1.OBJECT_COMPONENT);

      private final String name;
      private final MapCodec<? extends TextComponent> codec;

      public static TextCodecs_v26_1.ComponentType forComponent(TextComponent component) {
         if (component instanceof StringComponent) {
            return STRING;
         } else if (component instanceof TranslationComponent) {
            return TRANSLATION;
         } else if (component instanceof KeybindComponent) {
            return KEYBIND;
         } else if (component instanceof ScoreComponent) {
            return SCORE;
         } else if (component instanceof SelectorComponent) {
            return SELECTOR;
         } else if (component instanceof NbtComponent) {
            return NBT;
         } else if (component instanceof ObjectComponent) {
            return OBJECT;
         } else {
            throw new IllegalArgumentException("Unknown component type: " + component.getClass().getName());
         }
      }

      @Generated
      @Override
      public String getName() {
         return this.name;
      }

      @Generated
      @Override
      public MapCodec<? extends TextComponent> getCodec() {
         return this.codec;
      }

      @Generated
      private ComponentType(String name, MapCodec<? extends TextComponent> codec) {
         this.name = name;
         this.codec = codec;
      }
   }

   private static enum NbtDataSourceType implements NamedType {
      ENTITY(
         "entity",
         MapCodecMerger.mapCodec(
            Codec.STRING
               .converterVerified(VerifyingConverter.verify(TextVerifier_v26_1.class, TextVerifier_v26_1::verifySelector, "Invalid selector"))
               .mapCodec("entity")
               .required(),
            EntityNbtSource::getSelector,
            EntityNbtSource::new
         )
      ),
      BLOCK(
         "block",
         MapCodecMerger.mapCodec(
            Codec.STRING
               .converterVerified(VerifyingConverter.verify(TextVerifier_v26_1.class, TextVerifier_v26_1::verifyCoordinates, "Invalid coordinates"))
               .mapCodec("block")
               .required(),
            BlockNbtSource::getPos,
            BlockNbtSource::new
         )
      ),
      STORAGE("storage", MapCodecMerger.mapCodec(Codec.STRING_IDENTIFIER.mapCodec("storage").required(), StorageNbtSource::getId, StorageNbtSource::new));

      private final String name;
      private final MapCodec<? extends NbtDataSource> codec;

      public static TextCodecs_v26_1.NbtDataSourceType forDataSource(NbtDataSource dataSource) {
         if (dataSource instanceof EntityNbtSource) {
            return ENTITY;
         } else if (dataSource instanceof BlockNbtSource) {
            return BLOCK;
         } else if (dataSource instanceof StorageNbtSource) {
            return STORAGE;
         } else {
            throw new IllegalArgumentException("Unknown data source type: " + dataSource.getClass().getName());
         }
      }

      @Generated
      @Override
      public String getName() {
         return this.name;
      }

      @Generated
      public MapCodec<? extends NbtDataSource> getCodec() {
         return this.codec;
      }

      @Generated
      private NbtDataSourceType(String name, MapCodec<? extends NbtDataSource> codec) {
         this.name = name;
         this.codec = codec;
      }
   }

   private static enum ObjectInfos implements NamedType {
      ATLAS_SPRITE(
         "atlas",
         MapCodecMerger.mapCodec(
            Codec.STRING_IDENTIFIER.mapCodec("atlas").optional().defaulted(ObjectComponent.AtlasSprite.DEFAULT_ATLAS),
            ObjectComponent.AtlasSprite::getAtlas,
            Codec.STRING_IDENTIFIER.mapCodec("sprite").required(),
            ObjectComponent.AtlasSprite::getSprite,
            ObjectComponent.AtlasSprite::new
         )
      ),
      PLAYER_SPRITE(
         "player",
         MapCodecMerger.mapCodec(
            Codec.RAW.mapCodec("player").required(),
            ObjectComponent.PlayerSprite::getProfile,
            Codec.BOOLEAN.mapCodec("hat").optional().defaulted(true),
            ObjectComponent.PlayerSprite::isHat,
            ObjectComponent.PlayerSprite::new
         )
      );

      private final String name;
      private final MapCodec<? extends ObjectComponent.ObjectInfo> codec;

      public static TextCodecs_v26_1.ObjectInfos forObjectInfo(ObjectComponent.ObjectInfo objectInfo) {
         if (objectInfo instanceof ObjectComponent.AtlasSprite) {
            return ATLAS_SPRITE;
         } else if (objectInfo instanceof ObjectComponent.PlayerSprite) {
            return PLAYER_SPRITE;
         } else {
            throw new IllegalArgumentException("Unknown object info type: " + objectInfo.getClass().getName());
         }
      }

      @Generated
      @Override
      public String getName() {
         return this.name;
      }

      @Generated
      public MapCodec<? extends ObjectComponent.ObjectInfo> getCodec() {
         return this.codec;
      }

      @Generated
      private ObjectInfos(String name, MapCodec<? extends ObjectComponent.ObjectInfo> codec) {
         this.name = name;
         this.codec = codec;
      }
   }
}
