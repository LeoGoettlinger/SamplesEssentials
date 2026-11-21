package net.luckperms.api.messenger;

import net.luckperms.api.messenger.message.OutgoingMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@OverrideOnly
public interface Messenger extends AutoCloseable {
   void sendOutgoingMessage(@NonNull OutgoingMessage var1);

   @Override
   default void close() {
   }
}
