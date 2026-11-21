package com.earth2me.essentials.config;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.config.annotations.DeleteIfIncomplete;
import com.earth2me.essentials.config.annotations.DeleteOnEmpty;
import com.earth2me.essentials.config.entities.CommandCooldown;
import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.config.processors.DeleteIfIncompleteProcessor;
import com.earth2me.essentials.config.processors.DeleteOnEmptyProcessor;
import com.earth2me.essentials.config.serializers.BigDecimalTypeSerializer;
import com.earth2me.essentials.config.serializers.CommandCooldownSerializer;
import com.earth2me.essentials.config.serializers.LocationTypeSerializer;
import com.earth2me.essentials.config.serializers.MailMessageSerializer;
import com.earth2me.essentials.config.serializers.MaterialTypeSerializer;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.loader.HeaderMode;
import com.earth2me.essentials.libs.configurate.loader.ParsingException;
import com.earth2me.essentials.libs.configurate.objectmapping.ObjectMapper;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializerCollection;
import com.earth2me.essentials.libs.configurate.yaml.NodeStyle;
import com.earth2me.essentials.libs.configurate.yaml.YamlConfigurationLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.essentialsx.api.v2.services.mail.MailMessage;
import org.bukkit.Location;
import org.bukkit.Material;

public class EssentialsConfiguration {
   private static volatile ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
   private static final ObjectMapper.Factory MAPPER_FACTORY = ObjectMapper.factoryBuilder()
      .addProcessor(DeleteOnEmpty.class, (data, value) -> new DeleteOnEmptyProcessor())
      .addProcessor(DeleteIfIncomplete.class, (data, value) -> new DeleteIfIncompleteProcessor())
      .build();
   private static final TypeSerializerCollection SERIALIZERS = TypeSerializerCollection.defaults()
      .childBuilder()
      .registerAnnotatedObjects(MAPPER_FACTORY)
      .register(BigDecimal.class, new BigDecimalTypeSerializer())
      .register(LazyLocation.class, new LocationTypeSerializer())
      .register(Material.class, new MaterialTypeSerializer())
      .register(CommandCooldown.class, new CommandCooldownSerializer())
      .register(MailMessage.class, new MailMessageSerializer())
      .build();
   private final AtomicInteger pendingWrites = new AtomicInteger(0);
   private final AtomicBoolean transaction = new AtomicBoolean(false);
   private Class<?> resourceClass = EssentialsConfiguration.class;
   protected final File configFile;
   private final YamlConfigurationLoader loader;
   private final String templateName;
   private CommentedConfigurationNode configurationNode;
   private Runnable saveHook;

   public EssentialsConfiguration(final File configFile) {
      this(configFile, null);
   }

   public EssentialsConfiguration(final File configFile, final String templateName) {
      this(configFile, templateName, (String)null);
   }

   public EssentialsConfiguration(final File configFile, final String templateName, final Class<?> resourceClass) {
      this(configFile, templateName, (String)null);
      this.resourceClass = resourceClass;
   }

   public EssentialsConfiguration(final File configFile, final String templateName, final String header) {
      this.configFile = configFile;
      this.loader = YamlConfigurationLoader.builder()
         .defaultOptions(opts -> opts.header(header).serializers(SERIALIZERS))
         .headerMode(HeaderMode.PRESET)
         .nodeStyle(NodeStyle.BLOCK)
         .indent(2)
         .file(configFile)
         .build();
      this.templateName = templateName;
   }

   public CommentedConfigurationNode getRootNode() {
      return this.configurationNode;
   }

   public void setRootHolder(final Class<?> holderClass, final Object holder) {
      try {
         this.getRootNode().set(holderClass, holder);
      } catch (SerializationException var4) {
         Essentials.getWrappedLogger().log(Level.SEVERE, "Error while saving user config: " + this.configFile.getName(), (Throwable)var4);
         throw new RuntimeException(var4);
      }
   }

   public File getFile() {
      return this.configFile;
   }

   public void setProperty(String path, final Location location) {
      this.setInternal(path, LazyLocation.fromLocation(location));
   }

   public LazyLocation getLocation(final String path) {
      CommentedConfigurationNode node = path == null ? this.getRootNode() : this.getSection(path);
      if (node == null) {
         return null;
      } else {
         try {
            return node.get(LazyLocation.class);
         } catch (SerializationException var4) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var4.getMessage(), (Throwable)var4);
            return null;
         }
      }
   }

   public Map<String, LazyLocation> getLocationSectionMap(final String path) {
      CommentedConfigurationNode node = this.getSection(path);
      Map<String, LazyLocation> result = new HashMap<>();

      for (Entry<String, CommentedConfigurationNode> entry : ConfigurateUtil.getMap(node).entrySet()) {
         CommentedConfigurationNode jailNode = entry.getValue();

         try {
            result.put(entry.getKey().toLowerCase(Locale.ENGLISH), jailNode.get(LazyLocation.class));
         } catch (SerializationException var8) {
            Essentials.getWrappedLogger().log(Level.WARNING, "Error serializing key " + entry.getKey(), (Throwable)var8);
         }
      }

      return result;
   }

   public void setProperty(final String path, final List<?> list) {
      this.setInternal(path, list);
   }

   public <T> void setExplicitList(final String path, final List<T> list, final Type type) {
      try {
         this.toSplitRoot(path, this.configurationNode).set(type, list);
      } catch (SerializationException var5) {
         Essentials.getWrappedLogger().log(Level.SEVERE, var5.getMessage(), (Throwable)var5);
      }
   }

   public <T> List<T> getList(final String path, Class<T> type) {
      CommentedConfigurationNode node = this.getInternal(path);
      if (node == null) {
         return new ArrayList<>();
      } else {
         try {
            List<T> list = node.getList(type);
            return (List<T>)(list == null ? new ArrayList<>() : list);
         } catch (SerializationException var5) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var5.getMessage(), (Throwable)var5);
            return new ArrayList<>();
         }
      }
   }

   public boolean isList(String path) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node != null && node.isList();
   }

   public void setProperty(final String path, final String value) {
      this.setInternal(path, value);
   }

   public String getString(final String path, final String def) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? def : node.getString();
   }

   public void setProperty(final String path, final boolean value) {
      this.setInternal(path, value);
   }

   public boolean getBoolean(final String path, final boolean def) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? def : node.getBoolean();
   }

   public boolean isBoolean(final String path) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node != null && node.raw() instanceof Boolean;
   }

   public void setProperty(final String path, final long value) {
      this.setInternal(path, value);
   }

   public long getLong(final String path, final long def) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? def : node.getLong();
   }

   public void setProperty(final String path, final int value) {
      this.setInternal(path, value);
   }

   public int getInt(final String path, final int def) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? def : node.getInt();
   }

   public void setProperty(final String path, final double value) {
      this.setInternal(path, value);
   }

   public double getDouble(final String path, final double def) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? def : node.getDouble();
   }

   public void setProperty(final String path, final float value) {
      this.setInternal(path, value);
   }

   public float getFloat(final String path, final float def) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? def : node.getFloat();
   }

   public void setProperty(final String path, final BigDecimal value) {
      this.setInternal(path, value);
   }

   public BigDecimal getBigDecimal(final String path, final BigDecimal def) {
      CommentedConfigurationNode node = this.getInternal(path);
      if (node == null) {
         return def;
      } else {
         try {
            return node.get(BigDecimal.class);
         } catch (SerializationException var5) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var5.getMessage(), (Throwable)var5);
            return def;
         }
      }
   }

   public void setRaw(final String path, final Object value) {
      this.setInternal(path, value);
   }

   public Object get(final String path) {
      CommentedConfigurationNode node = this.getInternal(path);
      return node == null ? null : node.raw();
   }

   public CommentedConfigurationNode getSection(final String path) {
      CommentedConfigurationNode node = this.toSplitRoot(path, this.configurationNode);
      return node.virtual() ? null : node;
   }

   public CommentedConfigurationNode newSection() {
      return this.loader.createNode();
   }

   public Set<String> getKeys() {
      return ConfigurateUtil.getKeys(this.configurationNode);
   }

   public Map<String, CommentedConfigurationNode> getMap() {
      return ConfigurateUtil.getMap(this.configurationNode);
   }

   public Map<String, String> getStringMap(String path) {
      CommentedConfigurationNode node = this.getInternal(path);
      if (node != null && node.isMap()) {
         Map<String, String> map = new LinkedHashMap<>();

         for (Entry<Object, CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
            map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue().rawScalar()));
         }

         return map;
      } else {
         return Collections.emptyMap();
      }
   }

   public void removeProperty(String path) {
      CommentedConfigurationNode node = this.getInternal(path);
      if (node != null) {
         try {
            node.set(null);
         } catch (SerializationException var4) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var4.getMessage(), (Throwable)var4);
         }
      }
   }

   private void setInternal(final String path, final Object value) {
      try {
         this.toSplitRoot(path, this.configurationNode).set(value);
      } catch (SerializationException var4) {
         Essentials.getWrappedLogger().log(Level.SEVERE, var4.getMessage(), (Throwable)var4);
      }
   }

   private CommentedConfigurationNode getInternal(final String path) {
      CommentedConfigurationNode node = this.toSplitRoot(path, this.configurationNode);
      return node.virtual() ? null : node;
   }

   public boolean hasProperty(final String path) {
      return !this.toSplitRoot(path, this.configurationNode).isNull();
   }

   public CommentedConfigurationNode toSplitRoot(String path, final CommentedConfigurationNode node) {
      if (path == null) {
         return node;
      } else {
         path = path.startsWith(".") ? path.substring(1) : path;
         return node.node((Object[])(path.contains(".") ? path.split("\\.") : new Object[]{path}));
      }
   }

   public synchronized void load() {
      if (this.pendingWrites.get() != 0) {
         Essentials.getWrappedLogger()
            .log(
               Level.INFO,
               "Parsing config file {0} has been aborted due to {1} current pending write(s).",
               new Object[]{this.configFile, this.pendingWrites.get()}
            );
      } else if (this.configFile.getParentFile() != null && !this.configFile.getParentFile().exists() && !this.configFile.getParentFile().mkdirs()) {
         Essentials.getWrappedLogger()
            .log(Level.SEVERE, AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("failedToCreateConfig", this.configFile.toString())));
      } else {
         if (!this.configFile.exists()) {
            if (this.legacyFileExists()) {
               this.convertLegacyFile();
            } else if (this.altFileExists()) {
               this.convertAltFile();
            } else if (this.templateName != null) {
               try {
                  InputStream is = this.resourceClass.getResourceAsStream(this.templateName);

                  try {
                     Essentials.getWrappedLogger()
                        .log(
                           Level.INFO, AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("creatingConfigFromTemplate", this.configFile.toString()))
                        );
                     Files.copy(is, this.configFile.toPath());
                  } catch (Throwable var15) {
                     if (is != null) {
                        try {
                           is.close();
                        } catch (Throwable var11) {
                           var15.addSuppressed(var11);
                        }
                     }

                     throw var15;
                  }

                  if (is != null) {
                     is.close();
                  }
               } catch (IOException var16) {
                  Essentials.getWrappedLogger()
                     .log(
                        Level.SEVERE,
                        AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("failedToWriteConfig", this.configFile.toString())),
                        (Throwable)var16
                     );
               }
            }
         }

         try {
            this.configurationNode = this.loader.load();
            return;
         } catch (ParsingException var12) {
            File broken = new File(this.configFile.getAbsolutePath() + ".broken." + System.currentTimeMillis());
            if (!this.configFile.renameTo(broken)) {
               Essentials.getWrappedLogger()
                  .log(Level.SEVERE, "The file " + this.configFile + " is broken. A backup file has failed to be created", var12.getCause());
               return;
            }

            Essentials.getWrappedLogger().log(Level.SEVERE, "The file " + this.configFile + " is broken, it has been renamed to " + broken, var12.getCause());
         } catch (ConfigurateException var13) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var13.getMessage(), (Throwable)var13);
            return;
         } finally {
            if (this.configurationNode == null) {
               this.configurationNode = this.loader.createNode();
            }
         }
      }
   }

   public boolean legacyFileExists() {
      return false;
   }

   public void convertLegacyFile() {
   }

   public boolean altFileExists() {
      return false;
   }

   public void convertAltFile() {
   }

   public void startTransaction() {
      this.transaction.set(true);
   }

   public void stopTransaction() {
      this.stopTransaction(false);
   }

   public void stopTransaction(final boolean blocking) {
      this.transaction.set(false);
      if (blocking) {
         this.blockingSave();
      } else {
         this.save();
      }
   }

   public boolean isTransaction() {
      return this.transaction.get();
   }

   public void setSaveHook(Runnable saveHook) {
      this.saveHook = saveHook;
   }

   public synchronized void save() {
      if (!this.transaction.get()) {
         this.delaySave();
      }
   }

   public synchronized void blockingSave() {
      try {
         this.delaySave();
         getExecutor().submit(() -> {}).get();
      } catch (ExecutionException | InterruptedException var2) {
         Essentials.getWrappedLogger().log(Level.SEVERE, var2.getMessage(), (Throwable)var2);
      }
   }

   private void delaySave() {
      if (this.saveHook != null) {
         this.saveHook.run();
      }

      this.pendingWrites.incrementAndGet();

      try {
         getExecutor().submit(new ConfigurationSaveTask(this.loader, () -> this.configurationNode.copy(), this.pendingWrites));
      } catch (RejectedExecutionException var12) {
         try {
            synchronized (this.loader) {
               this.loader.save(this.configurationNode.copy());
            }
         } catch (ConfigurateException var10) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var10.getMessage(), (Throwable)var10);
         } finally {
            this.pendingWrites.decrementAndGet();
         }
      }
   }

   public static void shutdownExecutor() {
      ExecutorService exec = EXECUTOR_SERVICE;
      exec.shutdown();

      try {
         if (!exec.awaitTermination(5L, TimeUnit.SECONDS)) {
            exec.shutdownNow();
         }
      } catch (InterruptedException var2) {
         exec.shutdownNow();
      }
   }

   private static synchronized ExecutorService getExecutor() {
      if (EXECUTOR_SERVICE == null || EXECUTOR_SERVICE.isShutdown() || EXECUTOR_SERVICE.isTerminated()) {
         EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
      }

      return EXECUTOR_SERVICE;
   }
}
