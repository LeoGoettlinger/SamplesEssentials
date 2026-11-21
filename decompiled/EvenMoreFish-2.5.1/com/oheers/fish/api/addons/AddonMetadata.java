package com.oheers.fish.api.addons;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record AddonMetadata(
   @NonNull String name, @NonNull String version, @NonNull List<String> authors, String description, String website, List<String> dependencies
) {
   @NonNull
   @Override
   public String toString() {
      return "AddonMetadata{name='"
         + this.name
         + "', version='"
         + this.version
         + "', authors="
         + this.authors
         + ", description='"
         + this.description
         + "', website='"
         + this.website
         + "', dependencies="
         + this.dependencies
         + "}";
   }
}
