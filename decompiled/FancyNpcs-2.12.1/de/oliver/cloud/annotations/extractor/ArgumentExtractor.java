package de.oliver.cloud.annotations.extractor;

import de.oliver.cloud.annotations.SyntaxFragment;
import de.oliver.cloud.annotations.descriptor.ArgumentDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface ArgumentExtractor {
   @NonNull
   Collection<ArgumentDescriptor> extractArguments(@NonNull List<SyntaxFragment> syntax, @NonNull Method method);
}
