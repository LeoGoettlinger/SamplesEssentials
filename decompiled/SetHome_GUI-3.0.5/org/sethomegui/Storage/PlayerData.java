package org.sethomegui.Storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.settings.dumper.DumperSettings;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;

public class PlayerData extends YamlDocument {
   private final UUID uuid;
   private final StorageManager storage;

   private PlayerData(UUID uuid, StorageManager storage, InputStream content) throws IOException {
      super(content, null, GeneralSettings.DEFAULT);
      this.uuid = uuid;
      this.storage = storage;
   }

   public static PlayerData of(UUID uuid, StorageManager storage, String yaml) throws IOException {
      String content = yaml != null && !yaml.trim().isEmpty() ? yaml : "{}";
      return new PlayerData(uuid, storage, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
   }

   public UUID getPlayerUuid() {
      return this.uuid;
   }

   @Override
   public boolean save() throws IOException {
      this.storage.persist(this.uuid, this);
      return true;
   }

   @Override
   public String dump() {
      try {
         return PlayerDataCodec.plainCopy(this).dump();
      } catch (IOException var2) {
         throw new IllegalStateException("No se pudo serializar los datos de " + this.uuid, var2);
      }
   }

   @Override
   public String dump(DumperSettings dumperSettings) {
      try {
         return PlayerDataCodec.plainCopy(this).dump(dumperSettings);
      } catch (IOException var3) {
         throw new IllegalStateException("No se pudo serializar los datos de " + this.uuid, var3);
      }
   }

   @Override
   public boolean reload() throws IOException {
      this.storage.invalidate(this.uuid);
      return true;
   }
}
