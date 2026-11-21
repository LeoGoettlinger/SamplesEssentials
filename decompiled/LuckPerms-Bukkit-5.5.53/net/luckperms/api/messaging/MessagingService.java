package net.luckperms.api.messaging;

import net.luckperms.api.model.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MessagingService {
   @NonNull
   String getName();

   void pushUpdate();

   void pushUserUpdate(@NonNull User var1);

   void sendCustomMessage(@NonNull String var1, @NonNull String var2);
}
