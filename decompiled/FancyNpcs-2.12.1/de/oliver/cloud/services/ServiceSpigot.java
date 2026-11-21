package de.oliver.cloud.services;

import de.oliver.cloud.services.type.ConsumerService;
import de.oliver.cloud.services.type.Service;
import de.oliver.cloud.services.type.SideEffectService;
import io.leangen.geantyref.TypeToken;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ServiceSpigot<Context, Result> {
   private final Context context;
   private final ServicePipeline pipeline;
   private final ServiceRepository<Context, Result> repository;

   ServiceSpigot(@NonNull final ServicePipeline pipeline, @NonNull final Context context, @NonNull final TypeToken<? extends Service<Context, Result>> type) {
      this.context = context;
      this.pipeline = pipeline;
      this.repository = pipeline.getRepository(type);
   }

   public Result complete() throws IllegalStateException, PipelineException {
      LinkedList<? extends ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>> queue = this.repository.queue();
      queue.sort(null);
      boolean consumerService = false;

      ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>> wrapper;
      while ((wrapper = (ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>)queue.pollLast()) != null) {
         consumerService = wrapper.implementation() instanceof ConsumerService;
         if (ServiceFilterHandler.INSTANCE.passes(wrapper, this.context)) {
            Result result;
            try {
               result = wrapper.implementation().handle(this.context);
            } catch (Exception var6) {
               throw new PipelineException(String.format("Failed to retrieve result from %s", wrapper), var6);
            }

            if (wrapper.implementation() instanceof SideEffectService) {
               if (result == null) {
                  throw new IllegalStateException(String.format("SideEffectService '%s' returned null", wrapper));
               }

               if (result == State.ACCEPTED) {
                  return result;
               }
            } else if (result != null) {
               return result;
            }
         }
      }

      if (consumerService) {
         return (Result)State.ACCEPTED;
      } else {
         throw new IllegalStateException("No service consumed the context. This means that the pipeline was not constructed properly.");
      }
   }

   public void complete(@NonNull final BiConsumer<Result, Throwable> consumer) {
      try {
         consumer.accept(this.complete(), null);
      } catch (PipelineException var3) {
         consumer.accept(null, var3.getCause());
      } catch (Exception var4) {
         consumer.accept(null, var4);
      }
   }

   @NonNull
   public CompletableFuture<Result> completeAsynchronously() {
      return CompletableFuture.supplyAsync(this::complete, this.pipeline.executor());
   }

   @NonNull
   public ServicePump<Result> forward() {
      return this.pipeline.pump(this.complete());
   }

   @NonNull
   public CompletableFuture<ServicePump<Result>> forwardAsynchronously() {
      return this.completeAsynchronously().thenApply(this.pipeline::pump);
   }
}
