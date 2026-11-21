package net.luckperms.api.event.type;

import net.luckperms.api.event.source.Source;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Sourced {
   @Param(-1)
   @NonNull
   Source getSource();
}
