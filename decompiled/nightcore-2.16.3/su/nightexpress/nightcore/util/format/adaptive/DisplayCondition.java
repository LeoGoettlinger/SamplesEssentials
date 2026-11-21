package su.nightexpress.nightcore.util.format.adaptive;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface DisplayCondition<T> {
   boolean check(@NonNull T var1, @NonNull Player var2);
}
