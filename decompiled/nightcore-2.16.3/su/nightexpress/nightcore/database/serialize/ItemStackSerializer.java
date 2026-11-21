package su.nightexpress.nightcore.database.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.nightcore.util.ItemNbt;

@Deprecated
public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
   public JsonElement serialize(ItemStack item, Type type, JsonSerializationContext context) {
      JsonObject object = new JsonObject();
      object.addProperty("data64", ItemNbt.compress(item));
      return object;
   }

   public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      JsonObject object = json.getAsJsonObject();
      return ItemNbt.decompress(object.get("data64").getAsString());
   }
}
