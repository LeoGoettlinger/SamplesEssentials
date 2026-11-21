package net.luckperms.api.messenger.message.type;

import net.luckperms.api.actionlog.Action;
import net.luckperms.api.messenger.message.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ActionLogMessage extends Message {
   @NonNull
   Action getAction();
}
