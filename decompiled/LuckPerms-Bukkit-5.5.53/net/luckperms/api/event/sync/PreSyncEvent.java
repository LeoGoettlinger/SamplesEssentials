package net.luckperms.api.event.sync;

import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.Cancellable;

public interface PreSyncEvent extends LuckPermsEvent, Cancellable {
}
