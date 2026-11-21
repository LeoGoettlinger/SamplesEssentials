package de.theredend2000.advancedhunt.lib.cloud.brigadier.permission;

import de.theredend2000.advancedhunt.lib.cloud.SenderMapper;
import de.theredend2000.advancedhunt.lib.cloud.internal.CommandNode;
import de.theredend2000.advancedhunt.lib.cloud.permission.Permission;
import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public final class BrigadierPermissionPredicate<C, S> implements Predicate<S> {
   private final SenderMapper<S, C> senderMapper;
   private final BrigadierPermissionChecker<C> permissionChecker;
   private final CommandNode<?> node;

   public BrigadierPermissionPredicate(
      @NonNull final SenderMapper<S, C> senderMapper, @NonNull final BrigadierPermissionChecker<C> permissionChecker, @NonNull final CommandNode<?> node
   ) {
      this.senderMapper = senderMapper;
      this.permissionChecker = permissionChecker;
      this.node = node;
   }

   @Override
   public boolean test(@NonNull final S source) {
      C cloudSender = this.senderMapper.map(source);
      Map<Type, Permission> accessMap = this.node.nodeMeta().getOrDefault(CommandNode.META_KEY_ACCESS, Collections.emptyMap());

      for (Entry<Type, Permission> entry : accessMap.entrySet()) {
         if (GenericTypeReflector.isSuperType(entry.getKey(), cloudSender.getClass()) && this.permissionChecker.hasPermission(cloudSender, entry.getValue())) {
            return true;
         }
      }

      return false;
   }
}
