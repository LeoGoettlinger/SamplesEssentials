package org.lushplugins.lushrewards.storage.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushrewards.storage.Storage;

public class JsonStorage extends Storage {
   private final File storageDir = new File(LushRewards.getInstance().getDataFolder(), "data");

   @Override
   public JsonObject loadModuleUserDataJson(UUID uuid, String moduleId) {
      String path = moduleId != null ? moduleId : "main";
      JsonObject json = this.loadFile(uuid);
      return json.has(path) ? json.get(path).getAsJsonObject() : null;
   }

   @Override
   public void saveModuleUserData(UserDataModule.UserData userData) {
      if (!this.storageDir.exists()) {
         this.storageDir.mkdir();
      }

      UUID uuid = userData.getUniqueId();
      String moduleId = userData.getModuleId();
      JsonObject moduleJson = userData.asJson();
      if (moduleJson == null) {
         throw new NullPointerException("JsonObject cannot be null when saving");
      } else {
         JsonObject json = this.loadFile(uuid);
         json.add(moduleId != null ? moduleId : "main", moduleJson);

         try {
            FileWriter writer = new FileWriter(this.getUserFile(uuid));
            LushRewards.getInstance().getGson().toJson(json, writer);
            writer.flush();
         } catch (IOException var7) {
            throw new RuntimeException(var7);
         }
      }
   }

   private JsonObject loadFile(UUID uuid) {
      if (!this.storageDir.exists()) {
         this.storageDir.mkdir();
      }

      try {
         JsonElement json = JsonParser.parseReader(new FileReader(this.getUserFile(uuid)));
         return json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();
      } catch (FileNotFoundException var3) {
         return new JsonObject();
      }
   }

   private File getUserFile(UUID uuid) {
      return new File(this.storageDir, uuid + ".json");
   }
}
