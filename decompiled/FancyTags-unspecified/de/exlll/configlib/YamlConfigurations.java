package de.exlll.configlib;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class YamlConfigurations {
   private YamlConfigurations() {
   }

   public static <T> T load(Path configurationFile, Class<T> configurationType) {
      YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();
      return load(configurationFile, configurationType, properties);
   }

   public static <T> T load(Path configurationFile, Class<T> configurationType, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConfigurer) {
      YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>> builder = (YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>>)YamlConfigurationProperties.newBuilder();
      propertiesConfigurer.accept(builder);
      return load(configurationFile, configurationType, builder.build());
   }

   public static <T> T load(Path configurationFile, Class<T> configurationType, YamlConfigurationProperties properties) {
      YamlConfigurationStore<T> store = new YamlConfigurationStore<>(configurationType, properties);
      return store.load(configurationFile);
   }

   public static <T> T read(InputStream inputStream, Class<T> configurationType) {
      YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();
      return read(inputStream, configurationType, properties);
   }

   public static <T> T read(InputStream inputStream, Class<T> configurationType, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConfigurer) {
      YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>> builder = (YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>>)YamlConfigurationProperties.newBuilder();
      propertiesConfigurer.accept(builder);
      return read(inputStream, configurationType, builder.build());
   }

   public static <T> T read(InputStream inputStream, Class<T> configurationType, YamlConfigurationProperties properties) {
      YamlConfigurationStore<T> store = new YamlConfigurationStore<>(configurationType, properties);
      return store.read(inputStream);
   }

   public static <T> T update(Path configurationFile, Class<T> configurationType) {
      YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();
      return update(configurationFile, configurationType, properties);
   }

   public static <T> T update(Path configurationFile, Class<T> configurationType, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConfigurer) {
      YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>> builder = (YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>>)YamlConfigurationProperties.newBuilder();
      propertiesConfigurer.accept(builder);
      return update(configurationFile, configurationType, builder.build());
   }

   public static <T> T update(Path configurationFile, Class<T> configurationType, YamlConfigurationProperties properties) {
      YamlConfigurationStore<T> store = new YamlConfigurationStore<>(configurationType, properties);
      return store.update(configurationFile);
   }

   public static <T> void save(Path configurationFile, Class<T> configurationType, T configuration) {
      YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();
      save(configurationFile, configurationType, configuration, properties);
   }

   public static <T> void save(
      Path configurationFile, Class<T> configurationType, T configuration, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConfigurer
   ) {
      YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>> builder = (YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>>)YamlConfigurationProperties.newBuilder();
      propertiesConfigurer.accept(builder);
      save(configurationFile, configurationType, configuration, builder.build());
   }

   public static <T> void save(Path configurationFile, Class<T> configurationType, T configuration, YamlConfigurationProperties properties) {
      YamlConfigurationStore<T> store = new YamlConfigurationStore<>(configurationType, properties);
      store.save(configuration, configurationFile);
   }

   public static <T> void write(OutputStream outputStream, Class<T> configurationType, T configuration) {
      YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();
      write(outputStream, configurationType, configuration, properties);
   }

   public static <T> void write(
      OutputStream outputStream, Class<T> configurationType, T configuration, Consumer<YamlConfigurationProperties.Builder<?>> propertiesConfigurer
   ) {
      YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>> builder = (YamlConfigurationProperties.Builder<? extends YamlConfigurationProperties.Builder<?>>)YamlConfigurationProperties.newBuilder();
      propertiesConfigurer.accept(builder);
      write(outputStream, configurationType, configuration, builder.build());
   }

   public static <T> void write(OutputStream outputStream, Class<T> configurationType, T configuration, YamlConfigurationProperties properties) {
      YamlConfigurationStore<T> store = new YamlConfigurationStore<>(configurationType, properties);
      store.write(configuration, outputStream);
   }
}
