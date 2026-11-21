package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.NonNull;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

@FunctionalInterface
public interface ConfigurationTransformation {
   Object WILDCARD_OBJECT = new Object();

   static ConfigurationTransformation empty() {
      return node -> {};
   }

   static ConfigurationTransformation.Builder builder() {
      return new ConfigurationTransformation.Builder();
   }

   static ConfigurationTransformation.VersionedBuilder versionedBuilder() {
      return new ConfigurationTransformation.VersionedBuilder();
   }

   static ConfigurationTransformation chain(final ConfigurationTransformation... transformations) {
      if (Objects.requireNonNull(transformations, "transformations").length == 0) {
         throw new IllegalArgumentException("Cannot chain an empty array of transformations!");
      } else {
         return (ConfigurationTransformation)(transformations.length == 1 ? transformations[0] : new ChainedConfigurationTransformation(transformations));
      }
   }

   void apply(ConfigurationNode node) throws ConfigurateException;

   public static final class Builder {
      private MoveStrategy strategy = MoveStrategy.OVERWRITE;
      private final NavigableMap<NodePath, TransformAction> actions = new TreeMap<>(NodePathComparator.INSTANCE);

      Builder() {
      }

      public ConfigurationTransformation.Builder addAction(final NodePath path, final TransformAction action) {
         this.actions.put(Objects.requireNonNull(path, "path"), Objects.requireNonNull(action, "action"));
         return this;
      }

      public MoveStrategy moveStrategy() {
         return this.strategy;
      }

      public ConfigurationTransformation.Builder moveStrategy(final MoveStrategy strategy) {
         this.strategy = Objects.requireNonNull(strategy, "strategy");
         return this;
      }

      public ConfigurationTransformation build() {
         return (ConfigurationTransformation)(this.actions.isEmpty()
            ? ConfigurationTransformation.empty()
            : new SingleConfigurationTransformation(this.actions, this.strategy));
      }
   }

   public interface Versioned extends ConfigurationTransformation {
      int VERSION_UNKNOWN = -1;

      NodePath versionKey();

      int latestVersion();

      default int version(final ConfigurationNode node) {
         return node.node(this.versionKey()).getInt(-1);
      }
   }

   public static final class VersionedBuilder {
      private NodePath versionKey = NodePath.path("version");
      private final NavigableMap<Integer, ConfigurationTransformation> versions = new TreeMap<>();

      VersionedBuilder() {
      }

      public ConfigurationTransformation.VersionedBuilder versionKey(final Object... versionKey) {
         this.versionKey = NodePath.of(versionKey);
         return this;
      }

      @NonNull
      public ConfigurationTransformation.VersionedBuilder addVersion(final int version, @NonNull final ConfigurationTransformation transformation) {
         if (version < 0) {
            throw new IllegalArgumentException("Version must be at least 0");
         } else if (this.versions.putIfAbsent(version, Objects.requireNonNull(transformation, "transformation")) != null) {
            throw new IllegalArgumentException("Version '" + version + "' has been specified multiple times.");
         } else {
            return this;
         }
      }

      @NonNull
      public ConfigurationTransformation.VersionedBuilder addVersion(final int version, final ConfigurationTransformation... transformations) {
         return this.addVersion(version, ConfigurationTransformation.chain(transformations));
      }

      @NonNull
      public ConfigurationTransformation.VersionedBuilder makeVersion(
         final int version, @NonNull final Consumer<? super ConfigurationTransformation.Builder> maker
      ) {
         ConfigurationTransformation.Builder builder = ConfigurationTransformation.builder();
         maker.accept(builder);
         return this.addVersion(version, builder.build());
      }

      @NonNull
      public ConfigurationTransformation.Versioned build() {
         if (this.versions.isEmpty()) {
            throw new IllegalArgumentException("At least one version must be specified to build a transformation");
         } else {
            return new VersionedTransformation(this.versionKey, this.versions);
         }
      }
   }
}
