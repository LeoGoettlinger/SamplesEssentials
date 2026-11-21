package net.kyori.adventure.text.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.option.OptionState;

final class ShowItemSerializer extends TypeAdapter<ShowItem> {
   private static final String LEGACY_SHOW_ITEM_TAG = "tag";
   private static final String DATA_COMPONENT_REMOVAL_PREFIX = "!";
   private final Gson gson;
   private final boolean emitDefaultQuantity;
   private final JSONOptions.ShowItemHoverDataMode itemDataMode;

   static TypeAdapter<ShowItem> create(final Gson gson, final OptionState opt) {
      return new ShowItemSerializer(gson, opt.value(JSONOptions.EMIT_DEFAULT_ITEM_HOVER_QUANTITY), opt.value(JSONOptions.SHOW_ITEM_HOVER_DATA_MODE)).nullSafe();
   }

   private ShowItemSerializer(final Gson gson, final boolean emitDefaultQuantity, final JSONOptions.ShowItemHoverDataMode itemDataMode) {
      this.gson = gson;
      this.emitDefaultQuantity = emitDefaultQuantity;
      this.itemDataMode = itemDataMode;
   }

   public ShowItem read(final JsonReader in) throws IOException {
      in.beginObject();
      Key key = null;
      int count = 1;
      BinaryTagHolder nbt = null;
      Map<Key, DataComponentValue> dataComponents = null;

      while (in.hasNext()) {
         String fieldName = in.nextName();
         if (fieldName.equals("id")) {
            key = (Key)this.gson.fromJson(in, SerializerFactory.KEY_TYPE);
         } else if (fieldName.equals("count")) {
            count = in.nextInt();
         } else if (fieldName.equals("tag")) {
            JsonToken token = in.peek();
            if (token == JsonToken.STRING || token == JsonToken.NUMBER) {
               nbt = BinaryTagHolder.binaryTagHolder(in.nextString());
            } else if (token == JsonToken.BOOLEAN) {
               nbt = BinaryTagHolder.binaryTagHolder(String.valueOf(in.nextBoolean()));
            } else {
               if (token != JsonToken.NULL) {
                  throw new JsonParseException("Expected tag to be a string");
               }

               in.nextNull();
            }
         } else if (!fieldName.equals("components")) {
            in.skipValue();
         } else {
            in.beginObject();

            while (in.peek() != JsonToken.END_OBJECT) {
               String name = in.nextName();
               Key id;
               boolean removed;
               if (name.startsWith("!")) {
                  id = Key.key(name.substring(1));
                  removed = true;
               } else {
                  id = Key.key(name);
                  removed = false;
               }

               JsonElement tree = (JsonElement)this.gson.fromJson(in, JsonElement.class);
               if (dataComponents == null) {
                  dataComponents = new HashMap<>();
               }

               dataComponents.put(id, (DataComponentValue)(removed ? DataComponentValue.removed() : GsonDataComponentValue.gsonDataComponentValue(tree)));
            }

            in.endObject();
         }
      }

      if (key == null) {
         throw new JsonParseException("Not sure how to deserialize show_item hover event");
      } else {
         in.endObject();
         return dataComponents != null ? ShowItem.showItem(key, count, dataComponents) : ShowItem.showItem(key, count, nbt);
      }
   }

   public void write(final JsonWriter out, final ShowItem value) throws IOException {
      out.beginObject();
      out.name("id");
      this.gson.toJson(value.item(), SerializerFactory.KEY_TYPE, out);
      int count = value.count();
      if (count != 1 || this.emitDefaultQuantity) {
         out.name("count");
         out.value(count);
      }

      Map<Key, DataComponentValue> dataComponents = value.dataComponents();
      if (!dataComponents.isEmpty() && this.itemDataMode != JSONOptions.ShowItemHoverDataMode.EMIT_LEGACY_NBT) {
         out.name("components");
         out.beginObject();

         for (Entry<Key, GsonDataComponentValue> entry : value.dataComponentsAs(GsonDataComponentValue.class).entrySet()) {
            JsonElement el = entry.getValue().element();
            if (el instanceof JsonNull) {
               out.name("!" + entry.getKey().asString());
               out.beginObject().endObject();
            } else {
               out.name(entry.getKey().asString());
               this.gson.toJson(el, out);
            }
         }

         out.endObject();
      } else if (this.itemDataMode != JSONOptions.ShowItemHoverDataMode.EMIT_DATA_COMPONENTS) {
         maybeWriteLegacy(out, value);
      }

      out.endObject();
   }

   private static void maybeWriteLegacy(final JsonWriter out, final ShowItem value) throws IOException {
      BinaryTagHolder nbt = value.nbt();
      if (nbt != null) {
         out.name("tag");
         out.value(nbt.string());
      }
   }
}
