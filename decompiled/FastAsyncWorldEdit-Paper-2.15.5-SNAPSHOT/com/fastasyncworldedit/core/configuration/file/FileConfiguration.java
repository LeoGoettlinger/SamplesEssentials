package com.fastasyncworldedit.core.configuration.file;

import com.fastasyncworldedit.core.configuration.Configuration;
import com.fastasyncworldedit.core.configuration.InvalidConfigurationException;
import com.fastasyncworldedit.core.configuration.MemoryConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public abstract class FileConfiguration extends MemoryConfiguration {
   public FileConfiguration() {
   }

   public FileConfiguration(Configuration defaults) {
      super(defaults);
   }

   public void save(File file) throws IOException {
      if (file == null) {
         throw new NullPointerException("File cannot be null");
      } else {
         file.getParentFile().mkdirs();
         String data = this.saveToString();

         try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(data);
         }
      }
   }

   public void save(String file) throws IOException {
      if (file == null) {
         throw new NullPointerException("File cannot be null");
      } else {
         this.save(new File(file));
      }
   }

   public abstract String saveToString();

   public void load(File file) throws IOException, InvalidConfigurationException {
      if (file == null) {
         throw new NullPointerException("File cannot be null");
      } else {
         try (FileInputStream stream = new FileInputStream(file)) {
            this.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
         }
      }
   }

   public void load(Reader reader) throws IOException, InvalidConfigurationException {
      StringBuilder builder = new StringBuilder();

      String line;
      try (BufferedReader input = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader)) {
         while ((line = input.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
         }
      }

      this.loadFromString(builder.toString());
   }

   public void load(String file) throws IOException, InvalidConfigurationException {
      if (file == null) {
         throw new NullPointerException("File cannot be null");
      } else {
         this.load(new File(file));
      }
   }

   public abstract void loadFromString(String contents) throws InvalidConfigurationException;

   protected abstract String buildHeader();

   public FileConfigurationOptions options() {
      if (this.options == null) {
         this.options = new FileConfigurationOptions(this);
      }

      return (FileConfigurationOptions)this.options;
   }
}
