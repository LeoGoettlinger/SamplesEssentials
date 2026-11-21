package org.sethomegui.Storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;

public class YamlStorageProvider implements StorageProvider {
   private final SetHomeGUI plugin;
   private final File dataFolder;

   public YamlStorageProvider(SetHomeGUI plugin) {
      this.plugin = plugin;
      this.dataFolder = new File(plugin.getDataFolder(), "data");
   }

   @Override
   public String getName() {
      return "YAML (archivos locales)";
   }

   @Override
   public void initialize() {
      if (!this.dataFolder.exists()) {
         this.dataFolder.mkdirs();
      }
   }

   @Override
   public void shutdown() {
   }

   public File getPlayerFile(UUID uuid) {
      return new File(this.dataFolder, uuid.toString() + ".yml");
   }

   @Override
   public PlayerRecord load(UUID uuid) throws IOException {
      File file = this.getPlayerFile(uuid);
      if (!file.exists()) {
         return null;
      } else {
         YamlDocument document = YamlDocument.create(file, GeneralSettings.DEFAULT);
         return PlayerDataCodec.toRecord(uuid, document);
      }
   }

   @Override
   public void save(UUID uuid, PlayerRecord record) throws IOException {
      this.saveRaw(uuid, PlayerDataCodec.toYaml(record));
   }

   public void saveRaw(UUID uuid, String yaml) throws IOException {
      if (!this.dataFolder.exists()) {
         this.dataFolder.mkdirs();
      }

      Files.write(this.getPlayerFile(uuid).toPath(), yaml.getBytes(StandardCharsets.UTF_8));
   }

   @Override
   public void delete(UUID uuid) {
      File file = this.getPlayerFile(uuid);
      if (file.exists() && !file.delete()) {
         this.plugin.getLogger().warning("No se pudo eliminar el archivo de datos de " + uuid);
      }
   }

   @Override
   public Map<UUID, Integer> countHomesByPlayer() {
      Map<UUID, Integer> counts = new HashMap<>();

      for (UUID uuid : this.listPlayers()) {
         try {
            PlayerRecord record = this.load(uuid);
            counts.put(uuid, record == null ? 0 : record.getHomes().size());
         } catch (IOException var5) {
            this.plugin.getLogger().warning("No se pudo leer el archivo de datos de " + uuid + ": " + var5.getMessage());
         }
      }

      return counts;
   }

   @Override
   public Set<UUID> listPlayers() {
      Set<UUID> players = new LinkedHashSet<>();
      File[] files = this.dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
      if (files == null) {
         return players;
      } else {
         for (File file : files) {
            try {
               players.add(UUID.fromString(file.getName().replace(".yml", "")));
            } catch (IllegalArgumentException var8) {
            }
         }

         return players;
      }
   }
}
