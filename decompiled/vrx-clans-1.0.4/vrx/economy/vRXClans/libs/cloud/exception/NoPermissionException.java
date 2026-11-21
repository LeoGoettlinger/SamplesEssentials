package vrx.economy.vRXClans.libs.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.permission.Permission;
import vrx.economy.vRXClans.libs.cloud.permission.PermissionResult;

@API(
   status = API.Status.STABLE
)
public class NoPermissionException extends CommandParseException {
   private final PermissionResult result;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public NoPermissionException(
      @NonNull final PermissionResult permissionResult, @NonNull final Object commandSender, @NonNull final List<CommandComponent<?>> currentChain
   ) {
      super(commandSender, currentChain);
      if (permissionResult.allowed()) {
         throw new IllegalArgumentException("Provided permission result was one that succeeded instead of failed");
      } else {
         this.result = permissionResult;
      }
   }

   @Override
   public final String getMessage() {
      return String.format("Missing permission '%s'", this.missingPermission());
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public Permission missingPermission() {
      return this.result.permission();
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public PermissionResult permissionResult() {
      return this.result;
   }

   @Override
   public final synchronized Throwable fillInStackTrace() {
      return this;
   }

   @Override
   public final synchronized Throwable initCause(final Throwable cause) {
      return this;
   }
}
