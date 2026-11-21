package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface VerboseSink {
   @NotNull
   VerboseSink key(@NotNull CharSequence var1);

   @NotNull
   VerboseSink num(double var1);

   @NotNull
   VerboseSink num(float var1);

   @NotNull
   VerboseSink num(int var1);

   @NotNull
   VerboseSink num(long var1);

   @NotNull
   VerboseSink bool(boolean var1);

   @NotNull
   VerboseSink text(@NotNull CharSequence var1);

   @NotNull
   static VerboseSink into(@NotNull StringBuilder out) {
      return new VerboseSink.StringBuilderSink(out);
   }

   public static final class StringBuilderSink implements VerboseSink {
      private final StringBuilder out;
      private boolean first = true;

      private StringBuilderSink(@NotNull StringBuilder out) {
         this.out = out;
      }

      @NotNull
      @Override
      public VerboseSink key(@NotNull CharSequence key) {
         if (!this.first) {
            this.out.append(", ");
         }

         this.first = false;
         this.out.append(key).append('=');
         return this;
      }

      @NotNull
      @Override
      public VerboseSink num(double value) {
         this.out.append(value);
         return this;
      }

      @NotNull
      @Override
      public VerboseSink num(float value) {
         this.out.append(value);
         return this;
      }

      @NotNull
      @Override
      public VerboseSink num(int value) {
         this.out.append(value);
         return this;
      }

      @NotNull
      @Override
      public VerboseSink num(long value) {
         this.out.append(value);
         return this;
      }

      @NotNull
      @Override
      public VerboseSink bool(boolean value) {
         this.out.append(value);
         return this;
      }

      @NotNull
      @Override
      public VerboseSink text(@NotNull CharSequence value) {
         this.out.append(value);
         return this;
      }
   }
}
