package com.sk89q.util.yaml;

import com.sk89q.util.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

public class YAMLProcessor extends YAMLNode {
   public static final String LINE_BREAK = LineBreak.getPlatformLineBreak().getString();
   public static final char COMMENT_CHAR = '#';
   protected final Yaml yaml;
   protected final File file;
   protected String header = null;
   protected YAMLFormat format;
   private final Map<String, String> comments = new HashMap<>();

   public YAMLProcessor(File file, boolean writeDefaults, YAMLFormat format) {
      super(new LinkedHashMap<>(), writeDefaults);
      this.format = format;
      DumperOptions dumperOptions = new DumperOptions();
      dumperOptions.setIndent(4);
      dumperOptions.setDefaultFlowStyle(format.getStyle());
      Representer representer = new YAMLProcessor.FancyRepresenter();
      representer.setDefaultFlowStyle(format.getStyle());
      LoaderOptions loaderOptions = new LoaderOptions();

      try {
         int yamlAliasLimit = Integer.getInteger("worldedit.yaml.aliasLimit", 50);
         loaderOptions.setMaxAliasesForCollections(yamlAliasLimit);
         int yamlCodePointLimit = Integer.getInteger("worldedit.yaml.codePointLimit", 67108864);
         loaderOptions.setCodePointLimit(yamlCodePointLimit);
      } catch (NoSuchMethodError var9) {
      }

      this.yaml = new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
      this.file = file;
   }

   public YAMLProcessor(File file, boolean writeDefaults) {
      this(file, writeDefaults, YAMLFormat.COMPACT);
   }

   public void load() throws IOException {
      try (InputStream stream = this.getInputStream()) {
         if (stream == null) {
            throw new IOException("Stream is null!");
         }

         this.read(this.yaml.load(new UnicodeReader(stream)));
      } catch (YAMLProcessorException var6) {
         this.root = new LinkedHashMap<>();
      }
   }

   public void setHeader(String... headerLines) {
      StringBuilder header = new StringBuilder();

      for (String line : headerLines) {
         if (header.length() > 0) {
            header.append(LINE_BREAK);
         }

         header.append(line);
      }

      this.setHeader(header.toString());
   }

   public void setHeader(String header) {
      this.header = header;
   }

   public String getHeader() {
      return this.header;
   }

   public boolean save() {
      File parent = this.file.getParentFile();
      if (parent != null) {
         parent.mkdirs();
      }

      try {
         boolean var11;
         try (OutputStream stream = this.getOutputStream()) {
            if (stream == null) {
               return false;
            }

            OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
            if (this.header != null) {
               writer.append(this.header);
               writer.append(LINE_BREAK);
            }

            if (!this.comments.isEmpty() && this.format == YAMLFormat.EXTENDED) {
               for (Entry<String, Object> entry : this.root.entrySet()) {
                  String comment = this.comments.get(entry.getKey());
                  if (comment != null) {
                     writer.append(LINE_BREAK);
                     writer.append(comment);
                     writer.append(LINE_BREAK);
                  }

                  this.yaml.dump(Collections.singletonMap(entry.getKey(), entry.getValue()), writer);
               }
            } else {
               this.yaml.dump(this.root, writer);
            }

            var11 = true;
         }

         return var11;
      } catch (IOException var9) {
         return false;
      }
   }

   private void read(Object input) throws YAMLProcessorException {
      try {
         if (null == input) {
            this.root = new LinkedHashMap<>();
         } else {
            this.root = new LinkedHashMap<>((Map<? extends String, ? extends Object>)input);
         }
      } catch (ClassCastException var3) {
         throw new YAMLProcessorException("Root document must be an key-value structure");
      }
   }

   public InputStream getInputStream() throws IOException {
      return new FileInputStream(this.file);
   }

   public OutputStream getOutputStream() throws IOException {
      return new FileOutputStream(this.file);
   }

   public String getComment(String key) {
      return this.comments.get(key);
   }

   public void setComment(String key, String comment) {
      if (comment != null) {
         this.setComment(key, comment.split("\\r?\\n"));
      } else {
         this.comments.remove(key);
      }
   }

   public void setComment(String key, String... comment) {
      if (comment != null && comment.length > 0) {
         for (int i = 0; i < comment.length; i++) {
            if (!comment[i].matches("^# ?")) {
               comment[i] = "# " + comment[i];
            }
         }

         String s = StringUtil.joinString(comment, LINE_BREAK);
         this.comments.put(key, s);
      } else {
         this.comments.remove(key);
      }
   }

   public Map<String, String> getComments() {
      return Collections.unmodifiableMap(this.comments);
   }

   public void setComments(Map<String, String> comments) {
      this.comments.clear();
      if (comments != null) {
         this.comments.putAll(comments);
      }
   }

   public static YAMLNode getEmptyNode(boolean writeDefaults) {
      return new YAMLNode(new LinkedHashMap<>(), writeDefaults);
   }

   private static class FancyRepresenter extends Representer {
      private FancyRepresenter() {
         super(new DumperOptions());
         this.nullRepresenter = o -> this.representScalar(Tag.NULL, "");
      }
   }
}
