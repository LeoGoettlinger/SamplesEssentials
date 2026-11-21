package net.luckperms.api.model.group;

import java.util.OptionalInt;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Group extends PermissionHolder {
   @NonNull
   String getName();

   @Nullable
   String getDisplayName();

   @Nullable
   String getDisplayName(@NonNull QueryOptions var1);

   @NonNull
   OptionalInt getWeight();
}
