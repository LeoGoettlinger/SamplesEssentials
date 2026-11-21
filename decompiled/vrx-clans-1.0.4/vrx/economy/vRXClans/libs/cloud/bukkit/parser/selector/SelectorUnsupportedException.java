package vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public final class SelectorUnsupportedException extends ParserException {
   public SelectorUnsupportedException(@NonNull final CommandContext<?> context, @NonNull final Class<?> parser) {
      super(parser, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED);
   }
}
