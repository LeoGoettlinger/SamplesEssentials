package vrx.economy.vRXClans.libs.cloud.meta;

import java.util.HashMap;
import java.util.Map;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;

@API(
   status = API.Status.STABLE
)
public class CommandMetaBuilder {
   private final Map<CloudKey<?>, Object> map = new HashMap<>();

   CommandMetaBuilder() {
   }

   @This
   @NonNull
   public CommandMetaBuilder with(@NonNull final CommandMeta commandMeta) {
      this.map.putAll(commandMeta.all());
      return this;
   }

   @This
   @NonNull
   public <V> CommandMetaBuilder with(@NonNull final CloudKey<V> key, @NonNull final V value) {
      this.map.put(key, value);
      return this;
   }

   @NonNull
   public CommandMeta build() {
      return new SimpleCommandMeta(this.map);
   }
}
