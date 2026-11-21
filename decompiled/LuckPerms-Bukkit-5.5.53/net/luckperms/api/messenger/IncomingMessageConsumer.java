package net.luckperms.api.messenger;

import net.luckperms.api.messenger.message.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface IncomingMessageConsumer {
   boolean consumeIncomingMessage(@NonNull Message var1);

   boolean consumeIncomingMessageAsString(@NonNull String var1);
}
