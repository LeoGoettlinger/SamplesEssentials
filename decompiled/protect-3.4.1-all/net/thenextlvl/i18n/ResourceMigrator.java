package net.thenextlvl.i18n;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface ResourceMigrator {
   @CheckReturnValue
   @Nullable
   default ResourceMigrator.Migration migrate(Locale locale, String key, String message) {
      return null;
   }

   @CheckReturnValue
   default boolean shouldMigrate(String resource, Properties properties) {
      return true;
   }

   @CheckReturnValue
   @Nullable
   default Path getOldPath() throws ResourceMigrationException {
      return null;
   }

   @CheckReturnValue
   @Nullable
   default String getOldResourceName(Locale locale) {
      return null;
   }

   public record Migration(@Nullable String key, @Nullable String message) {
      public static final ResourceMigrator.Migration DROP = new ResourceMigrator.Migration(null, null);

      @Contract(
         pure = true
      )
      public boolean drop() {
         return this.key == null && this.message == null;
      }
   }
}
