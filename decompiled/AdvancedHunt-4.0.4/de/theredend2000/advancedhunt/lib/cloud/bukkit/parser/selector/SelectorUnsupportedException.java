package de.theredend2000.advancedhunt.lib.cloud.bukkit.parser.selector;

import de.theredend2000.advancedhunt.lib.cloud.bukkit.BukkitCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.ParserException;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public final class SelectorUnsupportedException extends ParserException {
   public SelectorUnsupportedException(@NonNull final CommandContext<?> context, @NonNull final Class<?> parser) {
      super(parser, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED);
   }
}
