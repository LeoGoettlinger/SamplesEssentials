package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.config.transport.TransportRoutesConfig;
import com.magmaguy.elitemobs.config.transport.TransportRoutesConfigFields;
import com.magmaguy.elitemobs.magmacore.config.CustomConfigFields;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

final class TransportFiles {
   final Path routes;
   private final Map<String, TransportRoute> loaded = new TreeMap<>();
   private TransportRoutesConfig configuration;

   TransportFiles(Path data, Logger logger) throws IOException {
      this.routes = data.resolve("transport_routes");
      Files.createDirectories(this.routes);
      this.reload();
   }

   void reload() throws IOException {
      Map<String, TransportRoute> next = new TreeMap<>();
      this.configuration = new TransportRoutesConfig();

      for (CustomConfigFields fields : this.configuration.getCustomConfigFieldsHashMap().values()) {
         if (fields.isEnabled()) {
            TransportRoute route = ((TransportRoutesConfigFields)fields).getRoute();
            if (route != null) {
               next.put(route.id(), route);
            }
         }
      }

      this.loaded.clear();
      this.loaded.putAll(next);
   }

   TransportRoute get(String id) {
      return this.loaded.get(id);
   }

   boolean hasDefinition(String id) {
      return this.configuration.getCustomConfigFieldsHashMap().containsKey(id + ".yml");
   }

   List<String> ids() {
      return List.copyOf(this.loaded.keySet());
   }

   void save(TransportRoute route) throws IOException {
      CustomConfigFields fields = this.configuration.getCustomConfigFieldsHashMap().get(route.id() + ".yml");
      Path target = fields == null ? this.routes.resolve(route.id() + ".yml") : fields.getFile().toPath();
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.options().parseComments(true);

      try {
         if (Files.exists(target)) {
            yaml.load(target.toFile());
         }
      } catch (InvalidConfigurationException var6) {
         throw new IOException("Invalid route YAML: " + target, var6);
      }

      route.write(yaml, !Files.exists(target));
      if (yaml.getComments("transportEntity").isEmpty()) {
         yaml.setComments("transportEntity", List.of("Enabled file in custombosses/. Configure its appearance and powers in that file."));
      }

      atomic(target, yaml);
      this.reload();
      TransportRoute published = this.get(route.id());
      if (published == null || !published.points().equals(route.points()) || Float.compare(published.arrivalYaw(), route.arrivalYaw()) != 0) {
         throw new IOException("Route file was written but could not be loaded as saved. Your draft is retained: " + target);
      }
   }

   static void atomic(Path path, YamlConfiguration yaml) throws IOException {
      Path temp = Files.createTempFile(path.getParent(), ".transport-", ".tmp");

      try {
         try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
            ByteBuffer bytes = ByteBuffer.wrap(yaml.saveToString().getBytes(StandardCharsets.UTF_8));

            while (bytes.hasRemaining()) {
               channel.write(bytes);
            }

            channel.force(true);
         }

         try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var12) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
         }
      } finally {
         Files.deleteIfExists(temp);
      }
   }
}
