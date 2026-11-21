package vrx.economy.vRXClans.libs.cloud.brigadier.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public interface ArgumentTypeFactory<T> {
   @Nullable
   ArgumentType<T> create();
}
