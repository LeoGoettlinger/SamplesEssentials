package net.luckperms.api.track;

import java.util.List;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface Track {
   @NonNull
   String getName();

   @NonNull
   @Unmodifiable
   List<String> getGroups();

   @Nullable
   String getNext(@NonNull Group var1);

   @Nullable
   String getPrevious(@NonNull Group var1);

   @NonNull
   PromotionResult promote(@NonNull User var1, @NonNull ContextSet var2);

   @NonNull
   DemotionResult demote(@NonNull User var1, @NonNull ContextSet var2);

   @NonNull
   DataMutateResult appendGroup(@NonNull Group var1);

   @NonNull
   DataMutateResult insertGroup(@NonNull Group var1, int var2) throws IndexOutOfBoundsException;

   @NonNull
   DataMutateResult removeGroup(@NonNull Group var1);

   @NonNull
   DataMutateResult removeGroup(@NonNull String var1);

   boolean containsGroup(@NonNull Group var1);

   boolean containsGroup(@NonNull String var1);

   void clearGroups();
}
