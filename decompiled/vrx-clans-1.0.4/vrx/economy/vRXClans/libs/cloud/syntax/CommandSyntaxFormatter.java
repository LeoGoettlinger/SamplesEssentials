package vrx.economy.vRXClans.libs.cloud.syntax;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.internal.CommandNode;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface CommandSyntaxFormatter<C> {
   @NonNull
   String apply(@Nullable C sender, @NonNull List<CommandComponent<C>> commandComponents, @Nullable CommandNode<C> node);
}
