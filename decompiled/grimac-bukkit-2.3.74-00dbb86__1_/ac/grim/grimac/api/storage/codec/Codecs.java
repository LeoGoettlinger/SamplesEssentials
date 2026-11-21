package ac.grim.grimac.api.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public final class Codecs {
   private static final Codecs.Provider THROWING_DEFAULT = new Codecs.Provider() {
      @NotNull
      @Override
      public <R> Codec<R> create(@NotNull Class<R> recordType) {
         throw new UnsupportedOperationException("no codec provider installed yet; grim-internal must be on the classpath, or call Codecs.install() at startup");
      }
   };
   private static volatile Codecs.Provider provider = THROWING_DEFAULT;
   private static volatile boolean bootstrapTried = false;

   private Codecs() {
   }

   @NotNull
   public static <R> Codec<R> of(@NotNull Class<R> recordType) {
      Codecs.Provider p = provider;
      if (p == THROWING_DEFAULT && !bootstrapTried) {
         tryBootstrapFromInternal();
         p = provider;
      }

      return p.create(recordType);
   }

   public static void install(@NotNull Codecs.Provider p) {
      provider = p;
      bootstrapTried = true;
   }

   public static boolean isInstalled() {
      return provider != THROWING_DEFAULT;
   }

   private static synchronized void tryBootstrapFromInternal() {
      if (!bootstrapTried) {
         bootstrapTried = true;

         try {
            Class<?> factory = Class.forName("ac.grim.grimac.internal.storage.codec.MethodHandleCodecFactory");
            factory.getMethod("installAsDefaultProvider").invoke(null);
         } catch (NoSuchMethodException | ClassNotFoundException var1) {
         } catch (ReflectiveOperationException var2) {
            throw new IllegalStateException("failed to lazy-bootstrap MethodHandleCodecFactory; install() it explicitly", var2);
         }
      }
   }

   public interface Provider {
      @NotNull
      <R> Codec<R> create(@NotNull Class<R> var1);
   }
}
