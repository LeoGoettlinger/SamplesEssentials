package su.nightexpress.nightcore.user.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.user.UserInfo;
import su.nightexpress.nightcore.user.UserTemplate;

public interface UserDataAccessor<U extends UserTemplate> {
   void addSynchronization(@NonNull Consumer<U> var1);

   @NonNull
   List<U> loadAll();

   @NonNull
   List<UserInfo> loadProfiles();

   @NonNull
   Optional<U> loadByName(@NonNull String var1);

   @NonNull
   Optional<U> loadById(@NonNull UUID var1);

   boolean isExists(@NonNull String var1);

   boolean isExists(@NonNull UUID var1);

   void update(@NonNull U var1);

   void update(@NonNull Collection<U> var1);

   void tinyUpdate(@NonNull U var1);

   void tinyUpdate(@NonNull Collection<U> var1);

   void insert(@NonNull U var1);

   void insert(@NonNull Collection<U> var1);

   void deleteByName(@NonNull String var1);

   void deleteById(@NonNull UUID var1);
}
