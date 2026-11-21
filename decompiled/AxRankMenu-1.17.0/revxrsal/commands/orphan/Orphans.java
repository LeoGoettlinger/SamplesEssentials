package revxrsal.commands.orphan;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Strings;

public final class Orphans {
   private final List<CommandPath> path;

   public static Orphans path(@NotNull String path) {
      Preconditions.notNull(path, "path");
      return new Orphans(Collections.singletonList(CommandPath.get(Strings.splitBySpace(path.trim()))));
   }

   public static Orphans path(@NotNull String... paths) {
      Preconditions.notNull(paths, "paths");
      return new Orphans(Arrays.stream(paths).map(text -> Strings.splitBySpace(text.trim())).map(CommandPath::get).collect(Collectors.toList()));
   }

   public static Orphans path(@NotNull CommandPath path) {
      Preconditions.notNull(path, "path");
      return new Orphans(Collections.singletonList(path));
   }

   public OrphanRegistry handler(OrphanCommand handler) {
      Preconditions.notNull(handler, "orphan command");
      return new OrphanRegistry(this.path, handler);
   }

   public Orphans(List<CommandPath> path) {
      this.path = path;
   }
}
