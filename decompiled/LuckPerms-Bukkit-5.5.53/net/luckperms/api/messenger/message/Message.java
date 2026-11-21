package net.luckperms.api.messenger.message;

import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface Message {
   @NonNull
   UUID getId();
}
