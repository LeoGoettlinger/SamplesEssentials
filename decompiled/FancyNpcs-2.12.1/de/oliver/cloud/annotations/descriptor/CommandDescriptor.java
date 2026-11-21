package de.oliver.cloud.annotations.descriptor;

import de.oliver.cloud.annotations.SyntaxFragment;
import java.lang.reflect.Method;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
)
@Immutable
public interface CommandDescriptor extends Descriptor {
   @NonNull
   Method method();

   @NonNull
   @Override
   default String name() {
      return this.commandToken();
   }

   @NonNull
   List<SyntaxFragment> syntax();

   @NonNull
   String commandToken();

   @NonNull
   Class<?> requiredSender();
}
