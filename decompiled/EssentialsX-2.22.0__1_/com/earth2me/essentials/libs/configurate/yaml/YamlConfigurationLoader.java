package com.earth2me.essentials.libs.configurate.yaml;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.configurate.loader.AbstractConfigurationLoader;
import com.earth2me.essentials.libs.configurate.loader.CommentHandler;
import com.earth2me.essentials.libs.configurate.loader.CommentHandlers;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.DumperOptions;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.LoaderOptions;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.Yaml;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.constructor.Constructor;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.representer.Representer;
import com.earth2me.essentials.libs.kyori.option.Option;
import com.earth2me.essentials.libs.kyori.option.OptionSchema;
import java.io.BufferedReader;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

public final class YamlConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {
   private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
      Boolean.class, Integer.class, Long.class, BigInteger.class, Double.class, byte[].class, String.class, Date.class, java.sql.Date.class, Timestamp.class
   );
   private final ThreadLocal<Yaml> yaml;

   public static YamlConfigurationLoader.Builder builder() {
      return new YamlConfigurationLoader.Builder();
   }

   private YamlConfigurationLoader(final YamlConfigurationLoader.Builder builder) {
      super(builder, new CommentHandler[]{CommentHandlers.HASH});
      LoaderOptions loaderOpts = new LoaderOptions().setAcceptTabs(true).setProcessComments(false);
      loaderOpts.setCodePointLimit(Integer.MAX_VALUE);
      DumperOptions opts = builder.options;
      opts.setDefaultFlowStyle(NodeStyle.asSnakeYaml(builder.optionState().value(YamlConfigurationLoader.Builder.NODE_STYLE)));
      opts.setIndent(builder.optionState().value(YamlConfigurationLoader.Builder.INDENT));
      this.yaml = ThreadLocal.withInitial(() -> new Yaml(new Constructor(loaderOpts), new Representer(opts), opts, loaderOpts));
   }

   protected void loadInternal(final CommentedConfigurationNode node, final BufferedReader reader) {
      node.raw(this.yaml.get().load(reader));
   }

   @Override
   protected void saveInternal(final ConfigurationNode node, final Writer writer) {
      this.yaml.get().dump(node.raw(), writer);
   }

   public CommentedConfigurationNode createNode(final ConfigurationOptions options) {
      return CommentedConfigurationNode.root(options);
   }

   public static final class Builder extends AbstractConfigurationLoader.Builder<YamlConfigurationLoader.Builder, YamlConfigurationLoader> {
      private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);
      public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();
      public static final Option<NodeStyle> NODE_STYLE = UNSAFE_SCHEMA.enumOption("yaml:node_style", NodeStyle.class, null);
      public static final Option<Integer> INDENT = UNSAFE_SCHEMA.intOption("yaml:indent", 4);
      private final DumperOptions options = new DumperOptions();

      Builder() {
         this.defaultOptions(o -> o.nativeTypes(YamlConfigurationLoader.NATIVE_TYPES));
      }

      @Override
      protected OptionSchema optionSchema() {
         return SCHEMA;
      }

      public YamlConfigurationLoader.Builder indent(final int indent) {
         this.optionStateBuilder().value(INDENT, indent);
         return this;
      }

      public int indent() {
         return this.optionState().value(INDENT);
      }

      public YamlConfigurationLoader.Builder nodeStyle(@Nullable final NodeStyle style) {
         this.optionStateBuilder().value(NODE_STYLE, style);
         return this;
      }

      @Nullable
      public NodeStyle nodeStyle() {
         return this.optionState().value(NODE_STYLE);
      }

      public YamlConfigurationLoader build() {
         return new YamlConfigurationLoader(this);
      }
   }
}
