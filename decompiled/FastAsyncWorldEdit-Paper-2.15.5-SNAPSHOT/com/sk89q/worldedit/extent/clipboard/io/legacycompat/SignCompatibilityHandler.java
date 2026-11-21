package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;

public class SignCompatibilityHandler implements NBTCompatibilityHandler {
   @Override
   public BaseBlock updateNbt(BaseBlock block) {
      if (!DeprecationUtil.isSign(block.getBlockType())) {
         return block;
      } else {
         LinCompoundTag tag = block.getNbt();
         if (tag == null) {
            return block;
         } else {
            LinCompoundTag.Builder newTag = tag.toBuilder();

            for (int i = 0; i < 4; i++) {
               String key = "Text" + (i + 1);
               LinStringTag value = tag.findTag(key, LinTagType.stringTag());
               if (value != null) {
                  String storedString = value.value();
                  JsonElement jsonElement = null;
                  if (storedString.startsWith("{")) {
                     try {
                        jsonElement = JsonParser.parseString(storedString);
                     } catch (JsonSyntaxException var10) {
                     }
                  }

                  if (jsonElement == null) {
                     jsonElement = new JsonPrimitive(storedString);
                  }

                  if (!jsonElement.isJsonObject()) {
                     if (jsonElement.isJsonNull()) {
                        jsonElement = new JsonPrimitive("");
                     }

                     JsonObject jsonTextObject = new JsonObject();
                     jsonTextObject.add("text", jsonElement);
                     newTag.put("Text" + (i + 1), LinStringTag.of(jsonTextObject.toString()));
                  }
               }
            }

            return block;
         }
      }
   }
}
