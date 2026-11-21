package com.artillexstudios.axafkzone.libs.lamp.ktx

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler
import com.artillexstudios.axafkzone.libs.lamp.annotation.dynamic.AnnotationReplacer
import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor
import com.artillexstudios.axafkzone.libs.lamp.command.CommandPermission
import com.artillexstudios.axafkzone.libs.lamp.command.trait.CommandAnnotationHolder
import com.artillexstudios.axafkzone.libs.lamp.process.ContextResolver
import com.artillexstudios.axafkzone.libs.lamp.process.ContextResolverFactory
import com.artillexstudios.axafkzone.libs.lamp.process.ParameterValidator
import com.artillexstudios.axafkzone.libs.lamp.process.ResponseHandler
import com.artillexstudios.axafkzone.libs.lamp.process.ValueResolver
import com.artillexstudios.axafkzone.libs.lamp.process.ValueResolverFactory
import java.util.function.BiConsumer
import java.util.function.Supplier
import kotlin.jvm.functions.Function1
import kotlin.jvm.functions.Function2
import kotlin.jvm.internal.Intrinsics

@JvmSynthetic
public inline fun <reified T> CommandHandler.valueResolver(priority: Int = Integer.MAX_VALUE, resolver: ValueResolver<T>): CommandHandler {
   val `$this$valueResolver_u24lambda_u2d0`: CommandHandler = `$this$valueResolver`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$valueResolver_u24lambda_u2d0`.registerValueResolver(priority, Object::class.java, resolver);
   return `$this$valueResolver`;
}

@JvmSynthetic
fun CommandHandler.`valueResolver$default`(priority: Int, resolver: ValueResolver, `$i$f$valueResolver`: Int, var4: Any): CommandHandler {
   if ((`$i$f$valueResolver` and 1) != 0) {
      priority = Integer.MAX_VALUE;
   }

   val `$this$valueResolver_u24lambda_u2d0`: CommandHandler = `$this$valueResolver_u24default`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$valueResolver_u24lambda_u2d0`.registerValueResolver(priority, Object::class.java, resolver);
   return `$this$valueResolver_u24default`;
}

@JvmSynthetic
public inline fun <reified T : Annotation> CommandHandler.annotationReplacer(replacer: AnnotationReplacer<T>): CommandHandler {
   val `$this$annotationReplacer_u24lambda_u2d1`: CommandHandler = `$this$annotationReplacer`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$annotationReplacer_u24lambda_u2d1`.registerAnnotationReplacer(java.lang.annotation.Annotation::class.java, replacer);
   return `$this$annotationReplacer`;
}

@JvmSynthetic
public inline fun <reified T> CommandHandler.contextResolver(priority: Int = Integer.MAX_VALUE, resolver: ContextResolver<T>): CommandHandler {
   val `$this$contextResolver_u24lambda_u2d2`: CommandHandler = `$this$contextResolver`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$contextResolver_u24lambda_u2d2`.registerContextResolver(priority, Object::class.java, resolver);
   return `$this$contextResolver`;
}

@JvmSynthetic
fun CommandHandler.`contextResolver$default`(priority: Int, resolver: ContextResolver, `$i$f$contextResolver`: Int, var4: Any): CommandHandler {
   if ((`$i$f$contextResolver` and 1) != 0) {
      priority = Integer.MAX_VALUE;
   }

   val `$this$contextResolver_u24lambda_u2d2`: CommandHandler = `$this$contextResolver_u24default`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$contextResolver_u24lambda_u2d2`.registerContextResolver(priority, Object::class.java, resolver);
   return `$this$contextResolver_u24default`;
}

public fun CommandHandler.valueResolverFactory(priority: Int = Integer.MAX_VALUE, factory: ValueResolverFactory): CommandHandler {
   `$this$valueResolverFactory`.registerValueResolverFactory(priority, factory);
   return `$this$valueResolverFactory`;
}

@JvmSynthetic
fun `valueResolverFactory$default`(var0: CommandHandler, var1: Int, var2: ValueResolverFactory, var3: Int, var4: Any): CommandHandler {
   if ((var3 and 1) != 0) {
      var1 = Integer.MAX_VALUE;
   }

   return valueResolverFactory(var0, var1, var2);
}

public fun CommandHandler.contextResolverFactory(priority: Int = Integer.MAX_VALUE, factory: ContextResolverFactory): CommandHandler {
   `$this$contextResolverFactory`.registerContextResolverFactory(priority, factory);
   return `$this$contextResolverFactory`;
}

@JvmSynthetic
fun `contextResolverFactory$default`(var0: CommandHandler, var1: Int, var2: ContextResolverFactory, var3: Int, var4: Any): CommandHandler {
   if ((var3 and 1) != 0) {
      var1 = Integer.MAX_VALUE;
   }

   return contextResolverFactory(var0, var1, var2);
}

@JvmSynthetic
public inline fun <reified T : Throwable> CommandHandler.handleException(crossinline handler: (CommandActor, T) -> Unit): CommandHandler {
   val `$this$handleException_u24lambda_u2d5`: CommandHandler = `$this$handleException`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$handleException_u24lambda_u2d5`.registerExceptionHandler(java.lang.Throwable::class.java, new BiConsumer(handler) {
      {
         this.$handler = `$handler`;
      }

      public final void accept(CommandActor actor, T exception) {
         val var10000: Function2 = this.$handler;
         var10000.invoke(actor, exception);
      }
   });
   return `$this$handleException`;
}

@JvmSynthetic
public inline fun <reified T> CommandHandler.parameterValidator(validator: ParameterValidator<T>): CommandHandler {
   val `$this$parameterValidator_u24lambda_u2d6`: CommandHandler = `$this$parameterValidator`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$parameterValidator_u24lambda_u2d6`.registerParameterValidator(Object::class.java, validator);
   return `$this$parameterValidator`;
}

@JvmSynthetic
public inline fun <reified T> CommandHandler.dependency(value: T): CommandHandler {
   val `$this$dependency_u24lambda_u2d7`: CommandHandler = `$this$dependency`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$dependency_u24lambda_u2d7`.registerDependency(Object::class.java, new Supplier(value) {
      {
         this.$value = (T)`$value`;
      }

      @Override
      public final T get() {
         return this.$value;
      }
   });
   return `$this$dependency`;
}

@JvmSynthetic
public inline fun <reified T> CommandHandler.dependency(crossinline supplier: () -> T): CommandHandler {
   val `$this$dependency_u24lambda_u2d8`: CommandHandler = `$this$dependency`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$dependency_u24lambda_u2d8`.registerDependency(Object::class.java, new Supplier(supplier) {
      {
         this.$supplier = `$supplier`;
      }

      @Override
      public final T get() {
         return (T)this.$supplier.invoke();
      }
   });
   return `$this$dependency`;
}

public fun CommandHandler.permissionReader(reader: (CommandAnnotationHolder) -> CommandPermission): CommandHandler {
   `$this$permissionReader`.registerPermissionReader(Command_handlerKt::permissionReader$lambda-10$lambda-9);
   return `$this$permissionReader`;
}

@JvmSynthetic
public inline fun <reified T> CommandHandler.responseHandler(handler: ResponseHandler<T>): CommandHandler {
   val `$this$responseHandler_u24lambda_u2d11`: CommandHandler = `$this$responseHandler`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$responseHandler_u24lambda_u2d11`.registerResponseHandler(Object::class.java, handler);
   return `$this$responseHandler`;
}

public fun CommandHandler.supportSuspendFunctions(): CommandHandler {
   val var10000: CommandHandler = `$this$supportSuspendFunctions`.accept(SuspendFunctionsSupport.INSTANCE);
   return var10000;
}

fun `permissionReader$lambda-10$lambda-9`(`$tmp0`: Function1, command: CommandAnnotationHolder): CommandPermission {
   return `$tmp0`.invoke(command) as CommandPermission;
}
