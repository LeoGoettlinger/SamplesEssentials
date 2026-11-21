package revxrsal.commands.ktx

import java.util.Arrays
import java.util.LinkedList
import kotlin.jvm.internal.Intrinsics
import revxrsal.commands.CommandHandler
import revxrsal.commands.command.ArgumentStack
import revxrsal.commands.command.CommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.command.ExecutableCommand
import revxrsal.commands.command.trait.CommandAnnotationHolder
import revxrsal.commands.command.trait.PermissionHolder
import revxrsal.commands.core.CommandPath
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.exception.SendMessageException
import revxrsal.commands.util.Strings

public final val actor: CommandActor
   public final inline get() {
      val var10000: CommandActor = `$this$actor`.actor();
      return var10000;
   }


public final val arguments: ArgumentStack
   public final inline get() {
      val var10000: ArgumentStack = `$this$arguments`.arguments();
      return var10000;
   }


public final val command: ExecutableCommand
   public final inline get() {
      val var10000: ExecutableCommand = `$this$command`.command();
      return var10000;
   }


public final val commandHandler: CommandHandler
   public final inline get() {
      val var10000: CommandHandler = `$this$commandHandler`.commandHandler();
      return var10000;
   }


public final val input: List<String>
   public final inline get() {
      val var10000: java.util.List = `$this$input`.input();
      return var10000;
   }


public final val parameter: CommandParameter
   public final inline get() {
      val var10000: CommandParameter = `$this$parameter`.parameter();
      return var10000;
   }


@JvmSynthetic
public inline fun <reified T : Annotation> CommandAnnotationHolder.hasAnnotation(): Boolean {
   Intrinsics.reifiedOperationMarker(4, "T");
   return `$this$hasAnnotation`.hasAnnotation(java.lang.annotation.Annotation::class.java);
}

@JvmSynthetic
public inline fun <reified T : Annotation> CommandAnnotationHolder.getAnnotation(): T? {
   Intrinsics.reifiedOperationMarker(4, "T");
   return (T)`$this$getAnnotation`.getAnnotation(java.lang.annotation.Annotation::class.java);
}

public fun commandError(message: String): Nothing {
   throw new CommandErrorException(message);
}

public fun returnWithMessage(message: String): Nothing {
   throw new SendMessageException(message);
}

public operator fun CommandHandler.plusAssign(instance: Any) {
   `$this$plusAssign`.register(instance);
}

public operator fun CommandHandler.minusAssign(path: CommandPath) {
   `$this$minusAssign`.unregister(path);
}

public operator fun CommandHandler.minusAssign(path: String) {
   `$this$minusAssign`.unregister(path);
}

@JvmSynthetic
public inline fun <reified T> classOf(): Class<T> {
   Intrinsics.reifiedOperationMarker(4, "T");
   return (Class<T>)Object::class.java;
}

public fun pathOf(vararg values: String): CommandPath {
   val var10000: CommandPath = CommandPath.get(Arrays.copyOf(values, values.length));
   return var10000;
}

@JvmSynthetic
public inline fun <reified T : CommandActor> CommandActor.getAs(): T {
   Intrinsics.reifiedOperationMarker(1, "T");
   return (T)(`$this$getAs` as CommandActor);
}

public fun String.colorize(): String {
   val var10000: java.lang.String = Strings.colorize(`$this$colorize`);
   return var10000;
}

public fun String.splitBySpace(): LinkedList<String> {
   val var10000: LinkedList = Strings.splitBySpace(`$this$splitBySpace`);
   return var10000;
}

public infix fun CommandActor.canAccess(permissionHolder: PermissionHolder): Boolean {
   return permissionHolder.hasPermission(`$this$canAccess`);
}
