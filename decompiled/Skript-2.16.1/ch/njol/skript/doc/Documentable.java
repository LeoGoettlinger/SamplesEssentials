package ch.njol.skript.doc;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface Documentable {
   @NotNull
   String name();

   @NotNull
   @Unmodifiable
   List<String> description();

   @NotNull
   @Unmodifiable
   List<String> since();

   @NotNull
   @Unmodifiable
   List<String> examples();

   @NotNull
   @Unmodifiable
   List<String> keywords();

   @NotNull
   @Unmodifiable
   List<String> requires();
}
