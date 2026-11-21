package com.fastasyncworldedit.core.configuration.file;

import com.fastasyncworldedit.core.configuration.Configuration;
import com.fastasyncworldedit.core.configuration.ConfigurationSection;
import com.fastasyncworldedit.core.configuration.InvalidConfigurationException;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

public class YamlConfiguration extends FileConfiguration {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected static final String COMMENT_PREFIX = "# ";
   protected static final String BLANK_CONFIG = "{}\n";
   private final DumperOptions yamlOptions = new DumperOptions();
   private final Representer yamlRepresenter = new YamlRepresenter();
   private final Yaml yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);

   public static YamlConfiguration loadConfiguration(final File file) {
      if (file == null) {
         throw new NullPointerException("File cannot be null");
      } else {
         YamlConfiguration config = new YamlConfiguration();

         try {
            config.load(file);
         } catch (IOException | InvalidConfigurationException var6) {
            Exception ex = var6;

            try {
               file.getAbsolutePath();
               File dest = new File(file.getAbsolutePath() + "_broken");
               int i = 0;

               while (dest.exists()) {
                  dest = new File(file.getAbsolutePath() + "_broken_" + i++);
               }

               Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
               LOGGER.error("Could not read {}\nRenamed to {}", file, dest.getAbsolutePath(), ex);
            } catch (IOException var5) {
               var5.printStackTrace();
               var6.printStackTrace();
            }
         }

         return config;
      }
   }

   public static YamlConfiguration loadConfiguration(final Reader reader) {
      if (reader == null) {
         throw new NullPointerException("Reader cannot be null");
      } else {
         YamlConfiguration config = new YamlConfiguration();

         try {
            config.load(reader);
         } catch (InvalidConfigurationException | IOException var3) {
            LOGGER.error("Cannot load configuration from stream", var3);
         }

         return config;
      }
   }

   @Override
   public String saveToString() {
      this.yamlOptions.setIndent(this.options().indent());
      this.yamlOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
      this.yamlRepresenter.setDefaultFlowStyle(FlowStyle.BLOCK);
      String header = this.buildHeader();
      String dump = this.yaml.dump(this.getValues(false));
      if (dump.equals("{}\n")) {
         dump = "";
      }

      return header + dump;
   }

   @Override
   public void loadFromString(final String contents) throws InvalidConfigurationException {
      if (contents == null) {
         throw new NullPointerException("Contents cannot be null");
      } else {
         Map<?, ?> input;
         try {
            input = (Map<?, ?>)this.yaml.load(contents);
         } catch (YAMLException var4) {
            throw new InvalidConfigurationException(var4);
         } catch (ClassCastException var5) {
            throw new InvalidConfigurationException("Top level is not a Map.");
         }

         String header = this.parseHeader(contents);
         if (!header.isEmpty()) {
            this.options().header(header);
         }

         if (input != null) {
            this.convertMapsToSections(input, this);
         }
      }
   }

   protected void convertMapsToSections(final Map<?, ?> input, final ConfigurationSection section) {
      for (Entry<?, ?> entry : input.entrySet()) {
         String key = entry.getKey().toString();
         Object value = entry.getValue();
         if (value instanceof Map) {
            this.convertMapsToSections((Map<?, ?>)value, section.createSection(key));
         } else {
            section.set(key, value);
         }
      }
   }

   protected String parseHeader(final String input) {
      String[] lines = input.split("\r?\n", -1);
      StringBuilder result = new StringBuilder();
      boolean readingHeader = true;
      boolean foundHeader = false;

      for (int i = 0; i < lines.length && readingHeader; i++) {
         String line = lines[i];
         if (line.startsWith("# ")) {
            if (i > 0) {
               result.append("\n");
            }

            if (line.length() > "# ".length()) {
               result.append(line.substring("# ".length()));
            }

            foundHeader = true;
         } else if (foundHeader && line.isEmpty()) {
            result.append("\n");
         } else if (foundHeader) {
            readingHeader = false;
         }
      }

      return result.toString();
   }

   @Override
   protected String buildHeader() {
      String header = this.options().header();
      if (this.options().copyHeader()) {
         Configuration def = this.getDefaults();
         if (def != null && def instanceof FileConfiguration filedefaults) {
            String defaultsHeader = filedefaults.buildHeader();
            if (defaultsHeader != null && !defaultsHeader.isEmpty()) {
               return defaultsHeader;
            }
         }
      }

      if (header == null) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         String[] lines = header.split("\r?\n", -1);
         boolean startedHeader = false;

         for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");
            if (startedHeader || !lines[i].isEmpty()) {
               builder.insert(0, lines[i]);
               builder.insert(0, "# ");
               startedHeader = true;
            }
         }

         return builder.toString();
      }
   }

   public YamlConfigurationOptions options() {
      if (this.options == null) {
         this.options = new YamlConfigurationOptions(this);
      }

      return (YamlConfigurationOptions)this.options;
   }
}
