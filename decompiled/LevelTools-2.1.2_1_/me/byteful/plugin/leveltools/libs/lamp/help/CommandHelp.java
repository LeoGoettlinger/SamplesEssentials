package me.byteful.plugin.leveltools.libs.lamp.help;

import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.exception.InvalidHelpPageException;
import org.jetbrains.annotations.Range;

public interface CommandHelp<T> extends List<T> {
   CommandHelp<T> paginate(int var1, int var2) throws InvalidHelpPageException;

   @Range(
      from = 1L,
      to = Long.MAX_VALUE
   )
   int getPageSize(int var1);
}
