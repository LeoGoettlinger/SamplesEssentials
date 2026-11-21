package de.exlll.configlib;

public final class YamlConfigurationProperties extends FileConfigurationProperties {
   public YamlConfigurationProperties(YamlConfigurationProperties.Builder<?> builder) {
      super(builder);
   }

   public static YamlConfigurationProperties.Builder<?> newBuilder() {
      return new YamlConfigurationProperties.BuilderImpl();
   }

   public YamlConfigurationProperties.Builder<?> toBuilder() {
      return new YamlConfigurationProperties.BuilderImpl(this);
   }

   public abstract static class Builder<B extends YamlConfigurationProperties.Builder<B>> extends FileConfigurationProperties.Builder<B> {
      protected Builder() {
      }

      protected Builder(YamlConfigurationProperties properties) {
         super(properties);
      }

      public abstract YamlConfigurationProperties build();

      protected abstract B getThis();
   }

   private static final class BuilderImpl extends YamlConfigurationProperties.Builder<YamlConfigurationProperties.BuilderImpl> {
      private BuilderImpl() {
      }

      private BuilderImpl(YamlConfigurationProperties properties) {
         super(properties);
      }

      protected YamlConfigurationProperties.BuilderImpl getThis() {
         return this;
      }

      @Override
      public YamlConfigurationProperties build() {
         return new YamlConfigurationProperties(this);
      }
   }
}
