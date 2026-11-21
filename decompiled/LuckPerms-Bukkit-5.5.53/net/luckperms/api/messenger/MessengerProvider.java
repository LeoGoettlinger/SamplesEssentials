package net.luckperms.api.messenger;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@OverrideOnly
public interface MessengerProvider {
   @NonNull
   String getName();

   @NonNull
   Messenger obtain(@NonNull IncomingMessageConsumer var1);
}
