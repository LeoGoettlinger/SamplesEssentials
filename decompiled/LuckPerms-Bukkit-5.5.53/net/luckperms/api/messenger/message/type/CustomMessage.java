package net.luckperms.api.messenger.message.type;

import net.luckperms.api.messenger.message.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CustomMessage extends Message {
   @NonNull
   String getChannelId();

   @NonNull
   String getPayload();
}
