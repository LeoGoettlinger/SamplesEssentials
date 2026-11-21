package net.kyori.adventure.text.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.option.OptionState;

final class ShowEntitySerializer extends TypeAdapter<ShowEntity> {
   private final Gson gson;
   private final boolean emitKeyAsTypeAndUuidAsId;

   static TypeAdapter<ShowEntity> create(final Gson gson, final OptionState opt) {
      return new ShowEntitySerializer(gson, opt.value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_KEY_AS_TYPE_AND_UUID_AS_ID)).nullSafe();
   }

   private ShowEntitySerializer(final Gson gson, final boolean emitKeyAsTypeAndUuidAsId) {
      this.gson = gson;
      this.emitKeyAsTypeAndUuidAsId = emitKeyAsTypeAndUuidAsId;
   }

   public ShowEntity read(final JsonReader in) throws IOException {
      in.beginObject();
      Key type = null;
      UUID id = null;
      Component name = null;

      while (in.hasNext()) {
         String fieldName = in.nextName();
         switch (fieldName) {
            case "id":
               if (in.peek() == JsonToken.BEGIN_ARRAY) {
                  id = (UUID)this.gson.fromJson(in, UUID.class);
               } else {
                  String string = in.nextString();
                  if (string.contains(":")) {
                     type = Key.key(string);
                  }

                  try {
                     id = UUID.fromString(string);
                  } catch (IllegalArgumentException var12) {
                     try {
                        type = Key.key(string);
                     } catch (InvalidKeyException var11) {
                     }
                  }
               }
               break;
            case "type":
               type = (Key)this.gson.fromJson(in, Key.class);
               break;
            case "uuid":
               id = (UUID)this.gson.fromJson(in, UUID.class);
               break;
            case "name":
               name = (Component)this.gson.fromJson(in, SerializerFactory.COMPONENT_TYPE);
               break;
            default:
               in.skipValue();
         }
      }

      if (type != null && id != null) {
         in.endObject();
         return ShowEntity.showEntity(type, id, name);
      } else {
         throw new JsonParseException("A show entity hover event needs type and id fields to be deserialized");
      }
   }

   public void write(final JsonWriter out, final ShowEntity value) throws IOException {
      out.beginObject();
      out.name(this.emitKeyAsTypeAndUuidAsId ? "type" : "id");
      this.gson.toJson(value.type(), SerializerFactory.KEY_TYPE, out);
      out.name(this.emitKeyAsTypeAndUuidAsId ? "id" : "uuid");
      this.gson.toJson(value.id(), SerializerFactory.UUID_TYPE, out);
      Component name = value.name();
      if (name != null) {
         out.name("name");
         this.gson.toJson(name, SerializerFactory.COMPONENT_TYPE, out);
      }

      out.endObject();
   }
}
