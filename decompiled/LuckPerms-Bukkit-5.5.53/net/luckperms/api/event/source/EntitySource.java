package net.luckperms.api.event.source;

import net.luckperms.api.platform.PlatformEntity;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface EntitySource extends Source {
   @NonNull
   PlatformEntity getEntity();
}
