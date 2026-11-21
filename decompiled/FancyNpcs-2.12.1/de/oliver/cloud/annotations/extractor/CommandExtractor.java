package de.oliver.cloud.annotations.extractor;

import de.oliver.cloud.annotations.descriptor.CommandDescriptor;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface CommandExtractor {
   @NonNull
   Collection<CommandDescriptor> extractCommands(@NonNull Object instance);
}
