package de.oliver.cloud.annotations;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class SyntaxFragment {
   private final String major;
   private final List<String> minor;
   private final ArgumentMode argumentMode;

   public SyntaxFragment(@NonNull final String major, @NonNull final List<String> minor, @NonNull final ArgumentMode argumentMode) {
      this.major = major;
      this.minor = minor;
      this.argumentMode = argumentMode;
   }

   @NonNull
   public String major() {
      return this.major;
   }

   @NonNull
   public List<String> minor() {
      return this.minor;
   }

   @NonNull
   public ArgumentMode argumentMode() {
      return this.argumentMode;
   }
}
