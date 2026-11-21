package net.luckperms.api.messenger.message;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface OutgoingMessage extends Message {
   @NonNull
   String asEncodedString();
}
