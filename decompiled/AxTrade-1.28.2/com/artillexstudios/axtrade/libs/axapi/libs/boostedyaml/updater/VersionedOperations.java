package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.updater;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs.Version;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.dvs.versioning.Versioning;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.updater.operators.Mapper;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.updater.operators.Relocator;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class VersionedOperations {
   public static boolean run(@NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings settings, char separator) {
      Versioning versioning = settings.getVersioning();
      if (versioning == null) {
         return false;
      } else {
         Version documentVersion = versioning.getDocumentVersion(document, false);
         Version defaultsVersion = Objects.requireNonNull(
            versioning.getDocumentVersion(defaults, true), "Version ID of the defaults cannot be null! Is it malformed or not specified?"
         );
         int compared = documentVersion != null ? documentVersion.compareTo(defaultsVersion) : -1;
         if (compared > 0 && !settings.isEnableDowngrading()) {
            throw new UnsupportedOperationException(String.format("Downgrading is not enabled (%s > %s)!", defaultsVersion.asID(), documentVersion.asID()));
         } else if (compared == 0) {
            return true;
         } else {
            if (compared < 0) {
               iterate(document, documentVersion != null ? documentVersion : versioning.getFirstVersion(), defaultsVersion, settings, separator);
            }

            settings.getIgnoredRoutes(defaultsVersion.asID(), separator)
               .forEach(route -> document.getOptionalBlock(route).ifPresent(block -> block.setIgnored(true)));
            return false;
         }
      }
   }

   private static void iterate(
      @NotNull Section document, @NotNull Version documentVersion, @NotNull Version defaultsVersion, @NotNull UpdaterSettings settings, char separator
   ) {
      Version current = documentVersion.copy();

      while (current.compareTo(defaultsVersion) <= 0) {
         current.next();
         Relocator.apply(document, settings.getRelocations(current.asID(), separator));
         Mapper.apply(document, settings.getMappers(current.asID(), separator));
         settings.getCustomLogic(current.asID()).forEach(consumer -> consumer.accept(document.getRoot()));
      }
   }
}
