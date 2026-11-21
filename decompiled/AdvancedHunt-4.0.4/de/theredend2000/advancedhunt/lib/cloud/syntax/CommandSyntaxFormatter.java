package de.theredend2000.advancedhunt.lib.cloud.syntax;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.internal.CommandNode;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface CommandSyntaxFormatter<C> {
   @NonNull
   String apply(@Nullable C sender, @NonNull List<CommandComponent<C>> commandComponents, @Nullable CommandNode<C> node);
}
