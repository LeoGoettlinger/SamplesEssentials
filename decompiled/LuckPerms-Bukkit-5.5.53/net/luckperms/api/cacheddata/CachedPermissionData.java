package net.luckperms.api.cacheddata;

import java.util.Map;
import net.luckperms.api.node.Node;
import net.luckperms.api.util.Tristate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface CachedPermissionData extends CachedData {
   @NonNull
   Result<Tristate, Node> queryPermission(@NonNull String var1);

   @NonNull
   default Tristate checkPermission(@NonNull String permission) {
      return this.queryPermission(permission).result();
   }

   void invalidateCache();

   @NonNull
   @Unmodifiable
   Map<String, Boolean> getPermissionMap();
}
