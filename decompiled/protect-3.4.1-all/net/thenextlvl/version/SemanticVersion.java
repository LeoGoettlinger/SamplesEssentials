package net.thenextlvl.version;

import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public record SemanticVersion(int major, int minor, int patch, @Nullable String preRelease) implements Version {
   public static final String PATTERN = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

   @Contract(
      value = "_ -> new",
      pure = true
   )
   public static SemanticVersion parse(String string) {
      String[] parts = string.split("-", 2);
      String[] split = parts[0].split("\\.", 3);
      int major = Integer.parseInt(split[0]);
      int minor = Integer.parseInt(split[1]);
      int patch = Integer.parseInt(split[2]);
      return new SemanticVersion(major, minor, patch, parts.length == 2 ? parts[1] : null);
   }

   @Contract(
      pure = true
   )
   public int compareTo(Version version) {
      return this.major() != version.major()
         ? Integer.compare(this.major(), version.major())
         : (
            this.minor() != version.minor()
               ? Integer.compare(this.minor(), version.minor())
               : (
                  this.patch() != version.patch()
                     ? Integer.compare(this.patch(), version.patch())
                     : (Objects.equals(version.preRelease(), this.preRelease()) ? 0 : (version.preRelease() != null && this.preRelease() == null ? 1 : -1))
               )
         );
   }

   @Contract(
      pure = true
   )
   @Override
   public String toString() {
      String string = this.major() + "." + this.minor() + "." + this.patch();
      return this.preRelease() != null ? string + "-" + this.preRelease() : string;
   }
}
