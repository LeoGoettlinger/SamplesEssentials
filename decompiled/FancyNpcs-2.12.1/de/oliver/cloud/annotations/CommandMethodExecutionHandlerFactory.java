package de.oliver.cloud.annotations;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface CommandMethodExecutionHandlerFactory<C> {
   @NonNull
   MethodCommandExecutionHandler<C> createExecutionHandler(@NonNull MethodCommandExecutionHandler.CommandMethodContext<C> context);
}
