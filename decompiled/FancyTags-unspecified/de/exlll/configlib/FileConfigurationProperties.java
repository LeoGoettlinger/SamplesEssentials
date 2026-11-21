package de.exlll.configlib;

import java.nio.charset.Charset;

public class FileConfigurationProperties extends ConfigurationProperties {
   private final String header;
   private final String footer;
   private final boolean createParentDirectories;
   private final Charset charset;

   protected FileConfigurationProperties(FileConfigurationProperties.Builder<?> builder) {
      super(builder);
      this.header = builder.header;
      this.footer = builder.footer;
      this.createParentDirectories = builder.createParentDirectories;
      this.charset = builder.charset;
   }

   public static FileConfigurationProperties.Builder<?> newBuilder() {
      return new FileConfigurationProperties.BuilderImpl();
   }

   public FileConfigurationProperties.Builder<?> toBuilder() {
      return new FileConfigurationProperties.BuilderImpl(this);
   }

   public final String getHeader() {
      return this.header;
   }

   public final String getFooter() {
      return this.footer;
   }

   public final boolean createParentDirectories() {
      return this.createParentDirectories;
   }

   public final Charset getCharset() {
      return this.charset;
   }

   public abstract static class Builder<B extends FileConfigurationProperties.Builder<B>> extends ConfigurationProperties.Builder<B> {
      private String header = null;
      private String footer = null;
      private boolean createParentDirectories = true;
      private Charset charset = Charset.defaultCharset();

      protected Builder() {
      }

      protected Builder(FileConfigurationProperties properties) {
         super(properties);
         this.header = properties.header;
         this.footer = properties.footer;
         this.createParentDirectories = properties.createParentDirectories;
         this.charset = properties.charset;
      }

      public final B header(String header) {
         this.header = header;
         return this.getThis();
      }

      public final B footer(String footer) {
         this.footer = footer;
         return this.getThis();
      }

      public final B createParentDirectories(boolean createParentDirectories) {
         this.createParentDirectories = createParentDirectories;
         return this.getThis();
      }

      public final B charset(Charset charset) {
         this.charset = charset;
         return this.getThis();
      }

      public abstract FileConfigurationProperties build();

      protected abstract B getThis();
   }

   private static final class BuilderImpl extends FileConfigurationProperties.Builder<FileConfigurationProperties.BuilderImpl> {
      private BuilderImpl() {
      }

      private BuilderImpl(FileConfigurationProperties properties) {
         super(properties);
      }

      protected FileConfigurationProperties.BuilderImpl getThis() {
         return this;
      }

      @Override
      public FileConfigurationProperties build() {
         return new FileConfigurationProperties(this);
      }
   }
}
