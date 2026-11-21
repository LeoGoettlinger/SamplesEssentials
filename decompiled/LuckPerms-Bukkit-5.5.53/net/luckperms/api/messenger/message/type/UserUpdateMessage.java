package net.luckperms.api.messenger.message.type;

import java.util.UUID;
import net.luckperms.api.messenger.message.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserUpdateMessage extends Message {
   @NonNull
   UUID getUserUniqueId();
}
