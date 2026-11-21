package de.oliver.cloud.annotations.extractor;

import de.oliver.cloud.annotations.descriptor.FlagDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface FlagExtractor {
   @NonNull
   Collection<FlagDescriptor> extractFlags(@NonNull Method method);
}
