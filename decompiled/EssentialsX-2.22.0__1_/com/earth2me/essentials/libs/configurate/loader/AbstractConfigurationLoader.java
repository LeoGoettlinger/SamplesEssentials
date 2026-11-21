package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.MonotonicNonNull;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.reference.ConfigurationReference;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.earth2me.essentials.libs.kyori.option.Option;
import com.earth2me.essentials.libs.kyori.option.OptionSchema;
import com.earth2me.essentials.libs.kyori.option.OptionState;
import com.earth2me.essentials.libs.kyori.option.value.ValueSource;
import com.google.errorprone.annotations.ForOverride;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public abstract class AbstractConfigurationLoader<N extends ScopedConfigurationNode<N>> implements ConfigurationLoader<N> {
   public static final String CONFIGURATE_LINE_SEPARATOR = "\n";
   public static final Pattern CONFIGURATE_LINE_PATTERN = Pattern.compile("\n");
   protected static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();
   @Nullable
   protected final Callable<BufferedReader> source;
   @Nullable
   protected final Callable<BufferedWriter> sink;
   private final List<CommentHandler> commentHandlers;
   private final HeaderMode headerMode;
   private final ConfigurationOptions defaultOptions;

   protected AbstractConfigurationLoader(final AbstractConfigurationLoader.Builder<?, ?> builder, final CommentHandler[] commentHandlers) {
      this.source = builder.source();
      this.sink = builder.sink();
      this.headerMode = builder.optionState().value(AbstractConfigurationLoader.Builder.HEADER_MODE);
      this.commentHandlers = UnmodifiableCollections.toList(commentHandlers);
      this.defaultOptions = builder.defaultOptions();
   }

   public CommentHandler defaultCommentHandler() {
      return this.commentHandlers.get(0);
   }

   @Override
   public ConfigurationReference<N> loadToReference() throws ConfigurateException {
      return ConfigurationReference.fixed(this);
   }

   public N load(ConfigurationOptions options) throws ParsingException {
      if (this.source == null) {
         throw new ParsingException(-1, -1, "", "No source present to read from!", null);
      } else {
         try {
            BufferedReader reader = this.source.call();

            ScopedConfigurationNode var4;
            try {
               if (this.headerMode == HeaderMode.PRESERVE || this.headerMode == HeaderMode.NONE) {
                  String comment = CommentHandlers.extractComment(reader, this.commentHandlers);
                  if (comment != null && comment.length() > 0) {
                     options = options.header(comment);
                  }
               }

               N node = this.createNode(options);
               this.loadInternal(node, reader);
               var4 = node;
            } catch (Throwable var6) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (reader != null) {
               reader.close();
            }

            return (N)var4;
         } catch (ParsingException var7) {
            throw var7;
         } catch (NoSuchFileException | FileNotFoundException var8) {
            return this.createNode(options);
         } catch (IOException var9) {
            throw new ParsingException(-1, -1, options.header(), null, var9);
         } catch (Exception var10) {
            throw new ParsingException(-1, -1, options.header(), "Unknown error occurred while loading", var10);
         }
      }
   }

   @ForOverride
   protected abstract void loadInternal(N node, BufferedReader reader) throws ParsingException;

   @Override
   public void save(final ConfigurationNode node) throws ConfigurateException {
      if (this.sink == null) {
         throw new ConfigurateException(node, "No sink present to write to!");
      } else {
         this.checkCanWrite(node);

         try {
            Writer writer = this.sink.call();

            try {
               this.writeHeaderInternal(writer);
               if (this.headerMode != HeaderMode.NONE) {
                  String header = node.options().header();
                  if (header != null && !header.isEmpty()) {
                     Iterator<String> lines = this.defaultCommentHandler().toComment(CONFIGURATE_LINE_PATTERN.splitAsStream(header)).iterator();

                     while (lines.hasNext()) {
                        writer.write(lines.next());
                        writer.write(SYSTEM_LINE_SEPARATOR);
                     }

                     writer.write(SYSTEM_LINE_SEPARATOR);
                  }
               }

               this.saveInternal(node, writer);
            } catch (Throwable var6) {
               if (writer != null) {
                  try {
                     writer.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (writer != null) {
               writer.close();
            }
         } catch (ConfigurateException var7) {
            throw var7;
         } catch (Exception var8) {
            throw new ConfigurateException(node, var8);
         }
      }
   }

   @ForOverride
   protected void checkCanWrite(final ConfigurationNode node) throws ConfigurateException {
   }

   @ForOverride
   protected void writeHeaderInternal(final Writer writer) throws IOException {
   }

   @ForOverride
   protected abstract void saveInternal(ConfigurationNode node, Writer writer) throws ConfigurateException;

   @Override
   public ConfigurationOptions defaultOptions() {
      return this.defaultOptions;
   }

   @Override
   public final boolean canLoad() {
      return this.source != null;
   }

   @Override
   public final boolean canSave() {
      return this.sink != null;
   }

   public abstract static class Builder<T extends AbstractConfigurationLoader.Builder<T, L>, L extends AbstractConfigurationLoader<?>> {
      private static final String CONFIGURATE_PREFIX = "configurate";
      private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.emptySchema();
      protected static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();
      public static final Option<HeaderMode> HEADER_MODE = UNSAFE_SCHEMA.enumOption("header", HeaderMode.class, HeaderMode.PRESERVE);
      @Deprecated
      protected HeaderMode headerMode;
      @MonotonicNonNull
      private OptionState.Builder optionBuilder;
      @Nullable
      private OptionState optionState;
      @Nullable
      protected Callable<BufferedReader> source;
      @Nullable
      protected Callable<BufferedWriter> sink;
      protected ConfigurationOptions defaultOptions = ConfigurationOptions.defaults();

      protected Builder() {
      }

      protected OptionState.Builder optionStateBuilder() {
         if (this.optionBuilder == null) {
            this.optionState = null;
            this.optionBuilder = this.optionSchema()
               .stateBuilder()
               .values(ValueSource.systemProperty("configurate"))
               .values(ValueSource.environmentVariable("configurate"));
         }

         return this.optionBuilder;
      }

      public OptionState optionState() {
         if (this.optionState == null) {
            this.optionState = this.optionStateBuilder().build();
         }

         return this.optionState;
      }

      public T optionState(final OptionState state) {
         this.optionBuilder = this.optionSchema().stateBuilder().values(state);
         this.optionState = null;
         return this.self();
      }

      public T editOptions(final Consumer<OptionState.Builder> builderConsumer) {
         this.optionState = null;
         builderConsumer.accept(this.optionStateBuilder());
         return this.self();
      }

      @ForOverride
      protected OptionSchema optionSchema() {
         return SCHEMA;
      }

      private T self() {
         return (T)this;
      }

      public T file(final File file) {
         return this.path(Objects.requireNonNull(file, "file").toPath());
      }

      public T path(final Path path) {
         Path absPath = Objects.requireNonNull(path, "path").toAbsolutePath();
         this.source = () -> Files.newBufferedReader(absPath, StandardCharsets.UTF_8);
         this.sink = AtomicFiles.atomicWriterFactory(absPath, StandardCharsets.UTF_8);
         return this.self();
      }

      public T url(final URL url) {
         Objects.requireNonNull(url, "url");
         this.source = () -> new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
         return this.self();
      }

      public T source(@Nullable final Callable<BufferedReader> source) {
         this.source = source;
         return this.self();
      }

      @Nullable
      public Callable<BufferedReader> source() {
         return this.source;
      }

      public T sink(@Nullable final Callable<BufferedWriter> sink) {
         this.sink = sink;
         return this.self();
      }

      @Nullable
      public Callable<BufferedWriter> sink() {
         return this.sink;
      }

      public T headerMode(final HeaderMode mode) {
         this.optionStateBuilder().value(HEADER_MODE, mode);
         return this.self();
      }

      public HeaderMode headerMode() {
         if (this.headerMode != null) {
            this.optionStateBuilder().value(HEADER_MODE, this.headerMode);
            this.headerMode = null;
         }

         return this.optionState().value(HEADER_MODE);
      }

      public T defaultOptions(final ConfigurationOptions defaultOptions) {
         this.defaultOptions = Objects.requireNonNull(defaultOptions, "defaultOptions");
         return this.self();
      }

      public T defaultOptions(final UnaryOperator<ConfigurationOptions> defaultOptions) {
         this.defaultOptions = Objects.requireNonNull(defaultOptions.apply(this.defaultOptions), "defaultOptions (updated)");
         return this.self();
      }

      public ConfigurationOptions defaultOptions() {
         return this.defaultOptions;
      }

      public abstract L build();

      public ConfigurationNode buildAndLoadString(final String input) throws ConfigurateException {
         return this.source(() -> new BufferedReader(new StringReader(input))).build().load();
      }

      public String buildAndSaveString(final ConfigurationNode output) throws ConfigurateException {
         Objects.requireNonNull(output, "output");
         StringWriter writer = new StringWriter();
         this.sink(() -> new BufferedWriter(writer)).build().save(output);
         return writer.toString();
      }
   }
}
