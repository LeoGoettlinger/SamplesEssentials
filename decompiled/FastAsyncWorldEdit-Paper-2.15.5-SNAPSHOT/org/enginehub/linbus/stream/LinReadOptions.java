package org.enginehub.linbus.stream;

public final class LinReadOptions {
   private final boolean allowNormalUtf8Encoding;

   public static LinReadOptions.Builder builder() {
      return new LinReadOptions.Builder();
   }

   private LinReadOptions(LinReadOptions.Builder builder) {
      this.allowNormalUtf8Encoding = builder.allowNormalUtf8Encoding;
   }

   public boolean allowNormalUtf8Encoding() {
      return this.allowNormalUtf8Encoding;
   }

   @Override
   public String toString() {
      return "LinReadOptions{allowNormalUtf8Encoding=" + this.allowNormalUtf8Encoding + "}";
   }

   public static final class Builder {
      private boolean allowNormalUtf8Encoding = false;

      private Builder() {
      }

      public LinReadOptions.Builder allowNormalUtf8Encoding(boolean allowNormalUtf8Encoding) {
         this.allowNormalUtf8Encoding = allowNormalUtf8Encoding;
         return this;
      }

      public LinReadOptions build() {
         return new LinReadOptions(this);
      }

      @Override
      public String toString() {
         return "LinReadOptions.Builder{allowNormalUtf8Encoding=" + this.allowNormalUtf8Encoding + "}";
      }
   }
}
