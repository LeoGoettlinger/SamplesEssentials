package com.artillexstudios.axafkzone.libs.axapi.libraries;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record Library(String group, String artifactId, String version, String classifier, List<Library> transitiveDependencies) {
   @Override
   public boolean equals(Object o) {
      return !(o instanceof Library library)
         ? false
         : Objects.equals(this.group, library.group)
            && Objects.equals(this.version, library.version)
            && Objects.equals(this.artifactId, library.artifactId)
            && Objects.equals(this.classifier, library.classifier);
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.group);
      result = 31 * result + Objects.hashCode(this.artifactId);
      result = 31 * result + Objects.hashCode(this.version);
      return 31 * result + Objects.hashCode(this.classifier);
   }

   @NonNull
   @Override
   public String toString() {
      return this.group + ":" + this.artifactId + ":" + this.version;
   }
}
