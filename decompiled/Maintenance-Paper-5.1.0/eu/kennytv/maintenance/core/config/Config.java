package eu.kennytv.maintenance.core.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

public final class Config extends ConfigSection {
   private static final String AWESOME_HEADER = "###################################################################################################################\n#   __  __       _       _                                    _             _                          _          #\n#  |  \\/  | __ _(_)_ __ | |_ ___ _ __   __ _ _ __   ___ ___  | |__  _   _  | | _____ _ __  _ __  _   _| |___   __ #\n#  | |\\/| |/ _` | | '_ \\| __/ _ \\ '_ \\ / _` | '_ \\ / __/ _ \\ | '_ \\| | | | | |/ / _ \\ '_ \\| '_ \\| | | | __\\ \\ / / #\n#  | |  | | (_| | | | | | ||  __/ | | | (_| | | | | (_|  __/ | |_) | |_| | |   <  __/ | | | | | | |_| | |_ \\ V /  #\n#  |_|  |_|\\__,_|_|_| |_|\\__\\___|_| |_|\\__,_|_| |_|\\___\\___| |_.__/ \\__, | |_|\\_\\___|_| |_|_| |_|\\__, |\\__| \\_/   #\n#                                                                  |___/                        |___/             #\n###################################################################################################################\n# You can report bugs here: https://github.com/kennytv/Maintenance/issues\n# If you need any other help/support, you can also join my Discord server: https://discord.gg/vGCUzHq\n# The config and language files use MiniMessage, NOT legacy text for input. Use https://webui.advntr.dev/ to edit and preview the formatted text.\n# For a full list of formats and fancy examples of MiniMessage, see https://docs.papermc.io/adventure/minimessage/format/\n";
   private final Yaml yaml = createYaml();
   private final File file;
   private final Set<String> unsupportedFields;
   private Map<String, String[]> comments = new HashMap<>();
   private String header;

   public Config(File file, String... unsupportedFields) {
      super(null, "");
      this.file = file;
      this.unsupportedFields = unsupportedFields.length == 0 ? Collections.emptySet() : Set.of(unsupportedFields);
   }

   public void load() throws IOException {
      String data = Files.readString(this.file.toPath());
      Map<String, Object> map = (Map<String, Object>)this.yaml.load(data);
      this.values = (Map<String, Object>)(map != null ? map : new LinkedHashMap<>());
      this.comments = ConfigSerializer.deserializeComments(data);
      String[] header = this.comments.remove(".header");
      if (header != null) {
         this.header = String.join("\n", header);
      }

      boolean removedFields = this.values.keySet().removeIf(key -> {
         String[] split = key.split("\\.");
         String splitKey = "";

         for (String s : split) {
            splitKey = splitKey + s;
            if (this.unsupportedFields.contains(splitKey)) {
               this.comments.remove(key);
               return true;
            }

            splitKey = splitKey + ".";
         }

         return false;
      });
      if (removedFields) {
         this.save();
      }
   }

   public void save() throws IOException {
      this.saveTo(this.file);
   }

   public void saveTo(File file) throws IOException {
      if (file.getParentFile() != null) {
         file.getParentFile().mkdirs();
      }

      file.createNewFile();
      Files.writeString(file.toPath(), this.toString());
   }

   public void replaceComments(Config fromConfig) {
      this.comments = new HashMap<>(fromConfig.comments);
   }

   private static Yaml createYaml() {
      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(FlowStyle.BLOCK);
      options.setPrettyFlow(false);
      options.setIndent(2);
      options.setWidth(10000);
      return new Yaml(options);
   }

   public void clear() {
      this.values.clear();
      this.comments.clear();
      this.header = null;
   }

   public Map<String, String[]> getComments() {
      return this.comments;
   }

   public Set<String> getUnsupportedFields() {
      return this.unsupportedFields;
   }

   @Override
   public Config getRoot() {
      return this;
   }

   @Nullable
   public String getHeader() {
      return this.header;
   }

   public void resetAwesomeHeader() {
      this.header = "###################################################################################################################\n#   __  __       _       _                                    _             _                          _          #\n#  |  \\/  | __ _(_)_ __ | |_ ___ _ __   __ _ _ __   ___ ___  | |__  _   _  | | _____ _ __  _ __  _   _| |___   __ #\n#  | |\\/| |/ _` | | '_ \\| __/ _ \\ '_ \\ / _` | '_ \\ / __/ _ \\ | '_ \\| | | | | |/ / _ \\ '_ \\| '_ \\| | | | __\\ \\ / / #\n#  | |  | | (_| | | | | | ||  __/ | | | (_| | | | | (_|  __/ | |_) | |_| | |   <  __/ | | | | | | |_| | |_ \\ V /  #\n#  |_|  |_|\\__,_|_|_| |_|\\__\\___|_| |_|\\__,_|_| |_|\\___\\___| |_.__/ \\__, | |_|\\_\\___|_| |_|_| |_|\\__, |\\__| \\_/   #\n#                                                                  |___/                        |___/             #\n###################################################################################################################\n# You can report bugs here: https://github.com/kennytv/Maintenance/issues\n# If you need any other help/support, you can also join my Discord server: https://discord.gg/vGCUzHq\n# The config and language files use MiniMessage, NOT legacy text for input. Use https://webui.advntr.dev/ to edit and preview the formatted text.\n# For a full list of formats and fancy examples of MiniMessage, see https://docs.papermc.io/adventure/minimessage/format/\n";
   }

   @Override
   public String toString() {
      return ConfigSerializer.serialize(this.header, this.values, this.comments, this.yaml);
   }
}
