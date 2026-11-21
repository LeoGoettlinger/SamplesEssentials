package net.kyori.adventure.text.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.BlockNBTComponent.Pos;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.PlayerHeadObjectContents.ProfileProperty;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.Nullable;

final class SerializerFactory implements TypeAdapterFactory {
   static final Class<Key> KEY_TYPE = Key.class;
   static final Class<Component> COMPONENT_TYPE = Component.class;
   static final Class<Style> STYLE_TYPE = Style.class;
   static final Class<Action> CLICK_ACTION_TYPE = Action.class;
   static final Class<net.kyori.adventure.text.event.HoverEvent.Action> HOVER_ACTION_TYPE = net.kyori.adventure.text.event.HoverEvent.Action.class;
   static final Class<ShowItem> SHOW_ITEM_TYPE = ShowItem.class;
   static final Class<ShowEntity> SHOW_ENTITY_TYPE = ShowEntity.class;
   static final Class<String> STRING_TYPE = String.class;
   static final Class<TextColorWrapper> COLOR_WRAPPER_TYPE = TextColorWrapper.class;
   static final Class<TextColor> COLOR_TYPE = TextColor.class;
   static final Class<ShadowColor> SHADOW_COLOR_TYPE = ShadowColor.class;
   static final Class<TextDecoration> TEXT_DECORATION_TYPE = TextDecoration.class;
   static final Class<Pos> BLOCK_NBT_POS_TYPE = Pos.class;
   static final Class<UUID> UUID_TYPE = UUID.class;
   static final Class<TranslationArgument> TRANSLATION_ARGUMENT_TYPE = TranslationArgument.class;
   static final Class<ProfileProperty> PROFILE_PROPERTY_TYPE = ProfileProperty.class;
   private final OptionState features;
   private final net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer;

   SerializerFactory(final OptionState features, @Nullable final net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer legacyHoverSerializer) {
      this.features = features;
      this.legacyHoverSerializer = legacyHoverSerializer;
   }

   public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
      Class<? super T> rawType = type.getRawType();
      if (COMPONENT_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)ComponentSerializerImpl.create(this.features, gson);
      } else if (KEY_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)KeySerializer.INSTANCE;
      } else if (STYLE_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)StyleSerializer.create(this.legacyHoverSerializer, this.features, gson);
      } else if (CLICK_ACTION_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)ClickEventActionSerializer.INSTANCE;
      } else if (HOVER_ACTION_TYPE.isAssignableFrom(rawType)) {
         return HoverEventActionSerializer.INSTANCE;
      } else if (SHOW_ITEM_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)ShowItemSerializer.create(gson, this.features);
      } else if (SHOW_ENTITY_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)ShowEntitySerializer.create(gson, this.features);
      } else if (COLOR_WRAPPER_TYPE.isAssignableFrom(rawType)) {
         return TextColorWrapper.Serializer.INSTANCE;
      } else if (COLOR_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)(this.features.value(JSONOptions.EMIT_RGB) ? TextColorSerializer.INSTANCE : TextColorSerializer.DOWNSAMPLE_COLOR);
      } else if (SHADOW_COLOR_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)ShadowColorSerializer.create(this.features);
      } else if (TEXT_DECORATION_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)TextDecorationSerializer.INSTANCE;
      } else if (BLOCK_NBT_POS_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)BlockNBTComponentPosSerializer.INSTANCE;
      } else if (UUID_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)UUIDSerializer.uuidSerializer(this.features);
      } else if (TRANSLATION_ARGUMENT_TYPE.isAssignableFrom(rawType)) {
         return (TypeAdapter<T>)TranslationArgumentSerializer.create(gson);
      } else {
         return (TypeAdapter<T>)(PROFILE_PROPERTY_TYPE.isAssignableFrom(rawType) ? ProfilePropertySerializer.INSTANCE : null);
      }
   }
}
