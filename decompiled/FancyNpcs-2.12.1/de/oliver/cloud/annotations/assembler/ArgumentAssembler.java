package de.oliver.cloud.annotations.assembler;

import de.oliver.cloud.annotations.SyntaxFragment;
import de.oliver.cloud.annotations.descriptor.ArgumentDescriptor;
import de.oliver.cloud.component.CommandComponent;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface ArgumentAssembler<C> {
   @NonNull
   CommandComponent<C> assembleArgument(@NonNull SyntaxFragment syntaxFragment, @NonNull ArgumentDescriptor descriptor);
}
