package de.oliver.cloud.annotations.assembler;

import de.oliver.cloud.annotations.descriptor.FlagDescriptor;
import de.oliver.cloud.parser.flag.CommandFlag;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface FlagAssembler {
   @NonNull
   CommandFlag<?> assembleFlag(@NonNull FlagDescriptor descriptor);
}
