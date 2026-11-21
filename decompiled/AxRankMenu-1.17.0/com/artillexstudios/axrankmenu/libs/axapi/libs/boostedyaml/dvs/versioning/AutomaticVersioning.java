package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.versioning;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.Pattern;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.dvs.Version;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutomaticVersioning implements Versioning {
   private final Pattern pattern;
   private final Route route;
   private final String strRoute;

   public AutomaticVersioning(@NotNull Pattern pattern, @NotNull Route route) {
      this.pattern = pattern;
      this.route = route;
      this.strRoute = null;
   }

   public AutomaticVersioning(@NotNull Pattern pattern, @NotNull String route) {
      this.pattern = pattern;
      this.route = null;
      this.strRoute = route;
   }

   @Nullable
   @Override
   public Version getDocumentVersion(@NotNull Section document, boolean defaults) {
      return (this.route != null ? document.getOptionalString(this.route) : document.getOptionalString(this.strRoute))
         .map(this.pattern::getVersion)
         .orElse(null);
   }

   @NotNull
   @Override
   public Version getFirstVersion() {
      return this.pattern.getFirstVersion();
   }

   @Override
   public void updateVersionID(@NotNull Section updated, @NotNull Section def) {
      if (this.route != null) {
         updated.set(this.route, def.getString(this.route));
      } else {
         updated.set(this.strRoute, def.getString(this.strRoute));
      }
   }

   @Override
   public String toString() {
      return "AutomaticVersioning{pattern=" + this.pattern + ", route='" + (this.route == null ? this.strRoute : this.route) + '\'' + '}';
   }
}
