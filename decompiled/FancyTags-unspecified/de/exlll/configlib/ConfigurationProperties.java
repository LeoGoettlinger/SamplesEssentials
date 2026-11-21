package de.exlll.configlib;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ConfigurationProperties {
   private final Map<Class<?>, Serializer<?, ?>> serializersByType;
   private final Map<Class<?>, Function<? super SerializerContext, ? extends Serializer<?, ?>>> serializerFactoriesByType;
   private final Map<Predicate<? super Type>, Serializer<?, ?>> serializersByCondition;
   private final Map<Predicate<? super ConfigurationElement<?>>, UnaryOperator<?>> postProcessorsByCondition;
   private final NameFormatter formatter;
   private final FieldFilter filter;
   private final boolean outputNulls;
   private final boolean inputNulls;
   private final boolean serializeSetsAsLists;
   private final ConfigurationProperties.EnvVarResolutionConfiguration envVarResolutionConfiguration;

   protected ConfigurationProperties(ConfigurationProperties.Builder<?> builder) {
      this.serializersByType = Map.copyOf(builder.serializersByType);
      this.serializerFactoriesByType = Map.copyOf(builder.serializerFactoriesByType);
      this.serializersByCondition = Collections.unmodifiableMap(new LinkedHashMap<>(builder.serializersByCondition));
      this.postProcessorsByCondition = Collections.unmodifiableMap(new LinkedHashMap<>(builder.postProcessorsByCondition));
      this.formatter = Validator.requireNonNull(builder.formatter, "name formatter");
      this.filter = Validator.requireNonNull(builder.filter, "field filter");
      this.outputNulls = builder.outputNulls;
      this.inputNulls = builder.inputNulls;
      this.serializeSetsAsLists = builder.serializeSetsAsLists;
      this.envVarResolutionConfiguration = Validator.requireNonNull(builder.envVarResolutionConfiguration, "environment variable resolution configuration");
   }

   public static ConfigurationProperties.Builder<?> newBuilder() {
      return new ConfigurationProperties.BuilderImpl();
   }

   public ConfigurationProperties.Builder<?> toBuilder() {
      return new ConfigurationProperties.BuilderImpl(this);
   }

   public final FieldFilter getFieldFilter() {
      return this.filter;
   }

   public final NameFormatter getNameFormatter() {
      return this.formatter;
   }

   public final Map<Class<?>, Serializer<?, ?>> getSerializers() {
      return this.serializersByType;
   }

   public final Map<Class<?>, Function<? super SerializerContext, ? extends Serializer<?, ?>>> getSerializerFactories() {
      return this.serializerFactoriesByType;
   }

   final Map<Predicate<? super Type>, Serializer<?, ?>> getSerializersByCondition() {
      return this.serializersByCondition;
   }

   public final Map<Predicate<? super ConfigurationElement<?>>, UnaryOperator<?>> getPostProcessorsByCondition() {
      return this.postProcessorsByCondition;
   }

   public final boolean outputNulls() {
      return this.outputNulls;
   }

   public final boolean inputNulls() {
      return this.inputNulls;
   }

   final boolean serializeSetsAsLists() {
      return this.serializeSetsAsLists;
   }

   public final ConfigurationProperties.EnvVarResolutionConfiguration getEnvVarResolutionConfiguration() {
      return this.envVarResolutionConfiguration;
   }

   public abstract static class Builder<B extends ConfigurationProperties.Builder<B>> {
      private final Map<Class<?>, Serializer<?, ?>> serializersByType = new HashMap<>();
      private final Map<Class<?>, Function<? super SerializerContext, ? extends Serializer<?, ?>>> serializerFactoriesByType = new HashMap<>();
      private final Map<Predicate<? super Type>, Serializer<?, ?>> serializersByCondition = new LinkedHashMap<>();
      private final Map<Predicate<? super ConfigurationElement<?>>, UnaryOperator<?>> postProcessorsByCondition = new LinkedHashMap<>();
      private NameFormatter formatter = NameFormatters.IDENTITY;
      private FieldFilter filter = FieldFilters.DEFAULT;
      private boolean outputNulls = false;
      private boolean inputNulls = false;
      private boolean serializeSetsAsLists = true;
      private ConfigurationProperties.EnvVarResolutionConfiguration envVarResolutionConfiguration = ConfigurationProperties.EnvVarResolutionConfiguration.disabled();

      protected Builder() {
      }

      protected Builder(ConfigurationProperties properties) {
         this.serializersByType.putAll(properties.serializersByType);
         this.serializerFactoriesByType.putAll(properties.serializerFactoriesByType);
         this.serializersByCondition.putAll(properties.serializersByCondition);
         this.postProcessorsByCondition.putAll(properties.postProcessorsByCondition);
         this.formatter = properties.formatter;
         this.filter = properties.filter;
         this.outputNulls = properties.outputNulls;
         this.inputNulls = properties.inputNulls;
         this.serializeSetsAsLists = properties.serializeSetsAsLists;
         this.envVarResolutionConfiguration = properties.envVarResolutionConfiguration;
      }

      public final B setFieldFilter(FieldFilter filter) {
         this.filter = Validator.requireNonNull(filter, "field filter");
         return this.getThis();
      }

      public final B setNameFormatter(NameFormatter formatter) {
         this.formatter = Validator.requireNonNull(formatter, "name formatter");
         return this.getThis();
      }

      public final <T> B addSerializer(Class<T> serializedType, Serializer<? super T, ?> serializer) {
         Validator.requireNonNull(serializedType, "serialized type");
         Validator.requireNonNull(serializer, "serializer");
         this.serializersByType.put(serializedType, serializer);
         return this.getThis();
      }

      public final <T> B addSerializerFactory(Class<T> serializedType, Function<? super SerializerContext, ? extends Serializer<T, ?>> serializerFactory) {
         Validator.requireNonNull(serializedType, "serialized type");
         Validator.requireNonNull(serializerFactory, "serializer factory");
         this.serializerFactoriesByType.put(serializedType, serializerFactory);
         return this.getThis();
      }

      final B addSerializerByCondition(Predicate<? super Type> condition, Serializer<?, ?> serializer) {
         Validator.requireNonNull(condition, "condition");
         Validator.requireNonNull(serializer, "serializer");
         this.serializersByCondition.put(condition, serializer);
         return this.getThis();
      }

      public final B addPostProcessor(Predicate<? super ConfigurationElement<?>> condition, UnaryOperator<?> postProcessor) {
         Validator.requireNonNull(condition, "condition");
         Validator.requireNonNull(postProcessor, "post-processor");
         this.postProcessorsByCondition.put(condition, postProcessor);
         return this.getThis();
      }

      public final B outputNulls(boolean outputNulls) {
         this.outputNulls = outputNulls;
         return this.getThis();
      }

      public final B inputNulls(boolean inputNulls) {
         this.inputNulls = inputNulls;
         return this.getThis();
      }

      final B serializeSetsAsLists(boolean serializeSetsAsLists) {
         this.serializeSetsAsLists = serializeSetsAsLists;
         return this.getThis();
      }

      public final B setEnvVarResolutionConfiguration(ConfigurationProperties.EnvVarResolutionConfiguration configuration) {
         this.envVarResolutionConfiguration = Validator.requireNonNull(configuration, "environment variable resolution configuration");
         return this.getThis();
      }

      public abstract ConfigurationProperties build();

      protected abstract B getThis();
   }

   private static final class BuilderImpl extends ConfigurationProperties.Builder<ConfigurationProperties.BuilderImpl> {
      private BuilderImpl() {
      }

      private BuilderImpl(ConfigurationProperties properties) {
         super(properties);
      }

      protected ConfigurationProperties.BuilderImpl getThis() {
         return this;
      }

      @Override
      public ConfigurationProperties build() {
         return new ConfigurationProperties(this);
      }
   }

   public static final class EnvVarResolutionConfiguration {
      private static final ConfigurationProperties.EnvVarResolutionConfiguration DISABLED = new ConfigurationProperties.EnvVarResolutionConfiguration();
      private final boolean resolveEnvVars;
      private final String prefix;
      private final boolean caseSensitive;

      public static ConfigurationProperties.EnvVarResolutionConfiguration disabled() {
         return DISABLED;
      }

      public static ConfigurationProperties.EnvVarResolutionConfiguration resolveEnvVarsWithPrefix(String prefix, boolean caseSensitive) {
         return new ConfigurationProperties.EnvVarResolutionConfiguration(true, prefix, caseSensitive);
      }

      private EnvVarResolutionConfiguration() {
         this(false, "", false);
      }

      private EnvVarResolutionConfiguration(boolean resolveEnvVars, String prefix, boolean caseSensitive) {
         this.resolveEnvVars = resolveEnvVars;
         this.prefix = Validator.requireNonNull(prefix, "prefix");
         this.caseSensitive = caseSensitive;
      }

      public boolean resolveEnvVars() {
         return this.resolveEnvVars;
      }

      public String prefix() {
         return this.prefix;
      }

      public boolean caseSensitive() {
         return this.caseSensitive;
      }
   }
}
