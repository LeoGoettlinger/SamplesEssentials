package com.sk89q.worldedit.command.util;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.exception.ExceptionConverter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.task.FutureForwardingTask;
import com.sk89q.worldedit.util.task.Supervisor;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.exception.CommandExecutionException;

public final class AsyncCommandBuilder<T> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Callable<T> callable;
   private final Actor sender;
   @Nullable
   private Supervisor supervisor;
   @Nullable
   private String description;
   @Nullable
   private Component delayMessage;
   @Nullable
   private Component workingMessage;
   @Nullable
   private Component successMessage;
   @Nullable
   private Consumer<T> consumer;
   @Nullable
   private Component failureMessage;
   @Nullable
   private ExceptionConverter exceptionConverter;

   private AsyncCommandBuilder(Callable<T> callable, Actor sender) {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(sender);
      this.callable = callable;
      this.sender = sender;
   }

   public static <T> AsyncCommandBuilder<T> wrap(Callable<T> callable, Actor sender) {
      return new AsyncCommandBuilder<>(callable, sender);
   }

   public AsyncCommandBuilder<T> registerWithSupervisor(Supervisor supervisor, String description) {
      this.supervisor = (Supervisor)Preconditions.checkNotNull(supervisor);
      this.description = (String)Preconditions.checkNotNull(description);
      return this;
   }

   @Deprecated
   public AsyncCommandBuilder<T> sendMessageAfterDelay(String message) {
      return this.sendMessageAfterDelay(TextComponent.of((String)Preconditions.checkNotNull(message)));
   }

   @Deprecated
   public AsyncCommandBuilder<T> sendMessageAfterDelay(Component message) {
      return this.setDelayMessage(message);
   }

   public AsyncCommandBuilder<T> setDelayMessage(Component message) {
      this.delayMessage = (Component)Preconditions.checkNotNull(message);
      return this;
   }

   public AsyncCommandBuilder<T> setWorkingMessage(Component message) {
      Preconditions.checkNotNull(this.delayMessage, "Must have a delay message if using a working message");
      this.workingMessage = (Component)Preconditions.checkNotNull(message);
      return this;
   }

   public AsyncCommandBuilder<T> onSuccess(@Nullable Component message, @Nullable Consumer<T> consumer) {
      Preconditions.checkArgument(message != null || consumer != null, "Can't have null message AND consumer");
      this.successMessage = message;
      this.consumer = consumer;
      return this;
   }

   public AsyncCommandBuilder<T> onSuccess(@Nullable String message, @Nullable Consumer<T> consumer) {
      Preconditions.checkArgument(message != null || consumer != null, "Can't have null message AND consumer");
      this.successMessage = message == null ? null : TextComponent.of(message, TextColor.LIGHT_PURPLE);
      this.consumer = consumer;
      return this;
   }

   public AsyncCommandBuilder<T> onFailure(@Nullable Component message, @Nullable ExceptionConverter exceptionConverter) {
      Preconditions.checkArgument(message != null || exceptionConverter != null, "Can't have null message AND exceptionConverter");
      this.failureMessage = message;
      this.exceptionConverter = exceptionConverter;
      return this;
   }

   public AsyncCommandBuilder<T> onFailure(@Nullable String message, @Nullable ExceptionConverter exceptionConverter) {
      Preconditions.checkArgument(message != null || exceptionConverter != null, "Can't have null message AND exceptionConverter");
      this.failureMessage = message == null ? null : ErrorFormat.wrap(message);
      this.exceptionConverter = exceptionConverter;
      return this;
   }

   public ListenableFuture<T> buildAndExec(ListeningExecutorService executor) {
      ListenableFuture<T> future = ((ListeningExecutorService)Preconditions.checkNotNull(executor)).submit(this::runTask);
      if (this.delayMessage != null) {
         FutureProgressListener.addProgressListener(future, this.sender, this.delayMessage);
      }

      if (this.supervisor != null && this.description != null) {
         this.supervisor.monitor(FutureForwardingTask.create(future, this.description, this.sender));
      }

      return future;
   }

   private T runTask() {
      T result = null;

      try {
         result = this.callable.call();
         if (this.consumer != null) {
            this.consumer.accept(result);
         }

         if (this.successMessage != null) {
            this.sender.print(this.successMessage);
         }
      } catch (Throwable var8) {
         Throwable orig = var8;
         Component failure = (Component)(this.failureMessage != null ? this.failureMessage : TextComponent.of("An error occurred"));

         try {
            if (this.exceptionConverter == null) {
               throw orig;
            }

            try {
               if (orig instanceof CommandException) {
                  throw new CommandExecutionException(orig, ImmutableList.of());
               }

               this.exceptionConverter.convert(orig);
               throw orig;
            } catch (org.enginehub.piston.exception.CommandException var6) {
               Component message = this.tryExtractOldCommandException(var6);
               if (message == null) {
                  if (Strings.isNullOrEmpty(var6.getMessage())) {
                     message = Caption.of("worldedit.error.unknown");
                  } else {
                     message = var6.getRichMessage();
                  }
               }

               this.sender.print(failure.append(TextComponent.of(": ")).append(message));
            }
         } catch (Throwable var7) {
            this.sender.print(failure.append(Caption.of("worldedit.command.error.report")));
            LOGGER.error("Uncaught exception occurred in task: " + this.description, var8);
         }
      }

      return result;
   }

   private Component tryExtractOldCommandException(org.enginehub.piston.exception.CommandException converted) {
      Component message = null;
      if (converted instanceof CommandExecutionException) {
         Throwable parentCause = converted;

         while ((parentCause = parentCause.getCause()) != null) {
            if (parentCause instanceof CommandException) {
               String msg = parentCause.getMessage();
               if (!Strings.isNullOrEmpty(msg)) {
                  message = TextComponent.of(msg);
               }
               break;
            }
         }
      }

      return message;
   }
}
