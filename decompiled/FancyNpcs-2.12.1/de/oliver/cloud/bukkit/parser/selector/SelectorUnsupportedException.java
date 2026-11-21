package de.oliver.cloud.bukkit.parser.selector;

import de.oliver.cloud.bukkit.BukkitCaptionKeys;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.exception.parsing.ParserException;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE,
   since = "2.0.0"
)
public final class SelectorUnsupportedException extends ParserException {
   public SelectorUnsupportedException(@NonNull final CommandContext<?> context, @NonNull final Class<?> parser) {
      super(parser, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED);
   }
}
