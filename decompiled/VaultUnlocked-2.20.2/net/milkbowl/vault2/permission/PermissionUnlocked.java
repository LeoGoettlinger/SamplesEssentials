package net.milkbowl.vault2.permission;

import java.util.concurrent.CompletableFuture;
import net.milkbowl.vault2.helper.TriState;
import net.milkbowl.vault2.helper.context.Context;
import net.milkbowl.vault2.helper.subject.Subject;
import org.jetbrains.annotations.NotNull;

public interface PermissionUnlocked {
   boolean isEnabled();

   @NotNull
   String getName();

   boolean hasGroupSupport();

   boolean hasSuperPermsSupport();

   boolean copyPermissions(@NotNull Context context, @NotNull Subject from, @NotNull Subject to, boolean includeTransient);

   CompletableFuture<Boolean> copyPermissionsAsync(@NotNull Context context, @NotNull Subject from, @NotNull Subject to, boolean includeTransient);

   @NotNull
   TriState has(@NotNull Context context, @NotNull Subject subject, @NotNull String permission);

   @NotNull
   CompletableFuture<TriState> hasAsync(@NotNull final Context context, @NotNull final Subject subject, @NotNull final String permission);

   boolean setPermission(@NotNull Context context, @NotNull Subject subject, @NotNull String permission, @NotNull TriState value);

   @NotNull
   CompletableFuture<Boolean> setPermissionAsync(@NotNull Context context, @NotNull Subject subject, @NotNull String permission, @NotNull TriState value);

   boolean setTransientPermission(@NotNull Context context, @NotNull Subject subject, @NotNull String permission, @NotNull TriState value);

   @NotNull
   CompletableFuture<Boolean> setTransientPermissionAsync(
      @NotNull Context context, @NotNull Subject subject, @NotNull String permission, @NotNull TriState value
   );

   @NotNull
   String[] groups();

   String primaryGroup(@NotNull Context context, @NotNull Subject subject);

   @NotNull
   CompletableFuture<String> primaryGroupAsync(@NotNull Context context, @NotNull Subject subject);

   @NotNull
   String[] getGroups(@NotNull Context context, @NotNull Subject subject);

   @NotNull
   CompletableFuture<String[]> getGroupsAsync(@NotNull Context context, @NotNull Subject subject);

   boolean copyGroups(@NotNull Context context, @NotNull Subject from, @NotNull Subject to);

   CompletableFuture<Boolean> copyGroupsAsync(@NotNull Context context, @NotNull Subject from, @NotNull Subject to);

   boolean inGroup(@NotNull Context context, @NotNull Subject subject);

   boolean inGroup(@NotNull Context context, @NotNull Subject subject, @NotNull String group);

   @NotNull
   CompletableFuture<Boolean> inGroupAsync(@NotNull Context context, @NotNull Subject subject);

   @NotNull
   CompletableFuture<Boolean> inGroupAsync(@NotNull Context context, @NotNull Subject subject, @NotNull String group);

   boolean addGroup(@NotNull Context context, @NotNull Subject subject, @NotNull String group);

   @NotNull
   CompletableFuture<Boolean> addGroupAsync(@NotNull Context context, @NotNull Subject subject, @NotNull String group);

   boolean removeGroup(@NotNull Context context, @NotNull Subject subject, @NotNull String group);

   @NotNull
   CompletableFuture<Boolean> removeGroupAsync(@NotNull Context context, @NotNull Subject subject, @NotNull String group);

   @NotNull
   TriState groupHas(@NotNull Context context, @NotNull String group, @NotNull String permission);

   @NotNull
   CompletableFuture<TriState> groupHasAsync(@NotNull Context context, @NotNull String group, @NotNull String permission);

   boolean groupSetPermission(@NotNull Context context, @NotNull String group, @NotNull String permission, @NotNull TriState value);

   @NotNull
   CompletableFuture<Boolean> groupSetPermissionAsync(@NotNull Context context, @NotNull String group, @NotNull String permission, @NotNull TriState value);

   boolean groupSetTransientPermission(@NotNull Context context, @NotNull String group, @NotNull String permission, @NotNull TriState value);

   @NotNull
   CompletableFuture<Boolean> groupSetTransientPermissionAsync(
      @NotNull Context context, @NotNull String group, @NotNull String permission, @NotNull TriState value
   );
}
