package net.luckperms.api.model.user;

import java.util.UUID;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.data.DataMutateResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface User extends PermissionHolder {
   @NonNull
   UUID getUniqueId();

   @Nullable
   String getUsername();

   @NonNull
   String getPrimaryGroup();

   @NonNull
   DataMutateResult setPrimaryGroup(@NonNull String var1);
}
