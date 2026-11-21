package de.exlll.configlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Queue;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.snakeyaml.engine.v2.constructor.ConstructYamlNull;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonBool;
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

public final class YamlConfigurationStore<T> implements FileConfigurationStore<T>, IOStreamConfigurationStore<T> {
   private static final Dump YAML_DUMPER = newYamlDumper();
   private static final Load YAML_LOADER = newYamlLoader();
   private final YamlConfigurationProperties properties;
   private final RootSerializer<T> serializer;
   private final CommentNodeExtractor extractor;

   public YamlConfigurationStore(Class<T> configurationType, YamlConfigurationProperties properties) {
      this(configurationType, properties, new Environment.SystemEnvironment());
   }

   YamlConfigurationStore(Class<T> configurationType, YamlConfigurationProperties properties, Environment environment) {
      Validator.requireNonNull(configurationType, "configuration type");
      this.properties = Validator.requireNonNull(properties, "properties");
      this.serializer = new RootSerializer<>(configurationType, properties, environment);
      this.extractor = new CommentNodeExtractor(properties);
   }

   @Override
   public void write(T configuration, OutputStream outputStream) {
      Validator.requireNonNull(configuration, "configuration");
      Validator.requireNonNull(outputStream, "output stream");
      Queue<CommentNode> extractedCommentNodes = this.extractor.extractCommentNodes(configuration);
      String dumpedYaml = this.tryDump(configuration);
      YamlWriter yamlFileWriter = new YamlWriter(outputStream, this.properties);
      yamlFileWriter.writeYaml(dumpedYaml, extractedCommentNodes);
   }

   @Override
   public void save(T configuration, Path configurationFile) {
      Validator.requireNonNull(configuration, "configuration");
      Validator.requireNonNull(configurationFile, "configuration file");
      this.tryCreateParentDirectories(configurationFile);
      Queue<CommentNode> extractedCommentNodes = this.extractor.extractCommentNodes(configuration);
      String dumpedYaml = this.tryDump(configuration);
      YamlWriter yamlFileWriter = new YamlWriter(configurationFile, this.properties);
      yamlFileWriter.writeYaml(dumpedYaml, extractedCommentNodes);
   }

   void tryCreateParentDirectories(Path configurationFile) {
      Path parent = configurationFile.getParent();
      if (this.properties.createParentDirectories() && parent != null && !Files.exists(parent)) {
         try {
            Files.createDirectories(parent);
         } catch (IOException var4) {
            throw new RuntimeException(var4);
         }
      }
   }

   private String tryDump(T configuration) {
      Map<?, ?> serializedConfiguration = this.serializer.serialize(configuration);

      try {
         return YAML_DUMPER.dumpToString(serializedConfiguration);
      } catch (YamlEngineException var5) {
         String msg = "The given configuration could not be converted into YAML. \nDo all custom serializers produce valid target types?";
         throw new ConfigurationException(msg, var5);
      }
   }

   @Override
   public T read(InputStream inputStream) {
      Validator.requireNonNull(inputStream, "input stream");

      try {
         Object yaml = YAML_LOADER.loadFromInputStream(inputStream);
         Map<?, ?> conf = this.requireYamlMapForRead(yaml);
         return this.serializer.deserialize(conf);
      } catch (YamlEngineException var4) {
         String msg = "The input stream does not contain valid YAML.";
         throw new ConfigurationException(msg, var4);
      }
   }

   private Map<?, ?> requireYamlMapForRead(Object yaml) {
      if (yaml == null) {
         String msg = "The input stream is empty or only contains null.";
         throw new ConfigurationException(msg);
      } else if (yaml instanceof Map<?, ?> map) {
         return map;
      } else {
         String msg = "The contents of the input stream do not represent a configuration. A valid configuration contains a YAML map but instead a '"
            + yaml.getClass()
            + "' was found.";
         throw new ConfigurationException(msg);
      }
   }

   @Override
   public T load(Path configurationFile) {
      Validator.requireNonNull(configurationFile, "configuration file");

      try {
         Object var5;
         try (BufferedReader reader = Files.newBufferedReader(configurationFile, this.properties.getCharset())) {
            Object yaml = YAML_LOADER.loadFromReader(reader);
            Map<?, ?> conf = this.requireYamlMapForLoad(yaml, configurationFile);
            var5 = this.serializer.deserialize(conf);
         }

         return (T)var5;
      } catch (YamlEngineException var8) {
         String msg = "The configuration file at %s does not contain valid YAML.";
         throw new ConfigurationException(msg.formatted(configurationFile), var8);
      } catch (IOException var9) {
         throw new RuntimeException(var9);
      }
   }

   private Map<?, ?> requireYamlMapForLoad(Object yaml, Path configurationFile) {
      if (yaml == null) {
         String msg = "The configuration file at %s is empty or only contains null.";
         throw new ConfigurationException(msg.formatted(configurationFile));
      } else if (yaml instanceof Map<?, ?> map) {
         return map;
      } else {
         String msg = "The contents of the YAML file at %s do not represent a configuration. A valid configuration file contains a YAML map but instead a '"
            + yaml.getClass()
            + "' was found.";
         throw new ConfigurationException(msg.formatted(configurationFile));
      }
   }

   @Override
   public T update(Path configurationFile) {
      Validator.requireNonNull(configurationFile, "configuration file");
      if (Files.exists(configurationFile)) {
         T configuration = this.load(configurationFile);
         this.save(configuration, configurationFile);
         return configuration;
      } else {
         T defaultConfiguration = this.serializer.newDefaultInstance();
         this.save(defaultConfiguration, configurationFile);
         return this.load(configurationFile);
      }
   }

   static Dump newYamlDumper() {
      DumpSettings settings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).setIndent(2).build();
      return new Dump(settings, new YamlConfigurationStore.YamlConfigurationRepresenter(settings));
   }

   static Load newYamlLoader() {
      LoadSettings settings = LoadSettings.builder().build();
      return new Load(settings, new YamlConfigurationStore.YamlConfigurationConstructor(settings));
   }

   static final class YamlConfigurationConstructor extends StandardConstructor {
      private final Map<Tag, ConstructNode> tagCtors;

      public YamlConfigurationConstructor(LoadSettings settings) {
         super(settings);
         this.tagConstructors.clear();
         this.tagConstructors.put(Tag.NULL, new ConstructYamlNull());
         this.tagConstructors.put(Tag.BOOL, new ConstructYamlJsonBool());
         this.tagConstructors.put(Tag.STR, new StandardConstructor.ConstructYamlStr());
         this.tagConstructors.put(Tag.SEQ, new StandardConstructor.ConstructYamlSeq());
         this.tagConstructors.put(Tag.MAP, new StandardConstructor.ConstructYamlMap());
         this.tagConstructors.put(Tag.INT, new YamlConfigurationStore.YamlConfigurationConstructor.YamlConfigurationConstructYamlJsonInt());
         this.tagConstructors.put(Tag.FLOAT, new ConstructYamlJsonFloat());
         this.tagCtors = Map.copyOf(this.tagConstructors);
      }

      Map<Tag, ConstructNode> getTagCtors() {
         return this.tagCtors;
      }

      static final class YamlConfigurationConstructYamlJsonInt extends ConstructScalar {
         @Override
         public Object construct(Node node) {
            String value = this.constructScalar(node);
            return Long.valueOf(value);
         }
      }
   }

   static final class YamlConfigurationRepresenter extends StandardRepresenter {
      public YamlConfigurationRepresenter(DumpSettings settings) {
         super(settings);
      }

      @Override
      protected Node representSequence(Tag tag, Iterable<?> sequence, FlowStyle flowStyle) {
         Node node = super.representSequence(tag, sequence, flowStyle);
         this.representedObjects.clear();
         return node;
      }

      @Override
      protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
         Node node = super.representMapping(tag, mapping, flowStyle);
         this.representedObjects.clear();
         return node;
      }
   }
}
