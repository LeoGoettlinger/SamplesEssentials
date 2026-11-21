package net.luckperms.api.event.extension;

import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.extension.Extension;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ExtensionLoadEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   Extension getExtension();
}
