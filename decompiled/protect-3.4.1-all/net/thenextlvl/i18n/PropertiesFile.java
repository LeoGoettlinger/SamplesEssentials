package net.thenextlvl.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;

class PropertiesFile {
   private final Charset charset;
   private final Path path;
   private final Properties defaultRoot;
   private Properties root;
   private boolean loaded;

   public PropertiesFile(Path path, Charset charset, Properties root) {
      this.charset = charset;
      this.defaultRoot = root;
      this.path = path;
      this.root = root;
   }

   protected Properties load() {
      if (!Files.isRegularFile(this.getFile())) {
         return (Properties)this.getRoot().clone();
      } else {
         try {
            Properties var4;
            try (
               InputStreamReader reader = new InputStreamReader(Files.newInputStream(this.getFile(), StandardOpenOption.READ), this.charset);
               BufferedReader buffer = new BufferedReader(reader);
            ) {
               Properties properties = new Properties();
               properties.load(buffer);
               var4 = properties;
            }

            return var4;
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }
      }
   }

   public PropertiesFile save(FileAttribute<?>... attributes) {
      try {
         Properties root = this.getRoot();
         Files.createDirectories(this.getFile().toAbsolutePath().getParent(), attributes);

         PropertiesFile var4;
         try (BufferedWriter writer = new BufferedWriter(
               new OutputStreamWriter(
                  Files.newOutputStream(this.getFile(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                  this.charset
               )
            )) {
            root.store(writer, null);
            var4 = this;
         }

         return var4;
      } catch (IOException var8) {
         throw new RuntimeException(var8);
      }
   }

   public PropertiesFile validate(ComponentBundle.Scope scope) {
      Properties root = this.getRoot();
      if (root == this.defaultRoot) {
         return this;
      } else {
         if (scope.isFiltering()) {
            root.entrySet().removeIf(entry -> !this.defaultRoot.containsKey(entry.getKey()));
         }

         if (scope.isFilling()) {
            this.merge(this.defaultRoot);
         }

         return this;
      }
   }

   public PropertiesFile setRoot(Properties root) {
      this.loaded = true;
      this.root = root;
      return this;
   }

   public Properties getRoot() {
      if (this.loaded) {
         return this.root;
      } else {
         this.loaded = true;
         return this.root = this.load();
      }
   }

   public Path getFile() {
      return this.path;
   }

   public PropertiesFile merge(Properties properties) {
      Properties root = this.getRoot();
      properties.forEach((key, value) -> {
         if (!root.containsKey(key)) {
            root.put(key, value);
         }
      });
      return this;
   }
}
