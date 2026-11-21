package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class BufferedSavingStrategy<T> implements DataSavingStrategy<T> {
   private final Consumer<T> createSaveFunction;
   private final Consumer<Collection<T>> batchSaveFunction;
   private final boolean writeThroughOnCreate;
   private final Executor executor;

   public BufferedSavingStrategy(Consumer<T> createSaveFunction, Consumer<Collection<T>> batchSaveFunction, boolean writeThroughOnCreate) {
      this(createSaveFunction, batchSaveFunction, writeThroughOnCreate, null);
   }

   public BufferedSavingStrategy(Consumer<T> createSaveFunction, Consumer<Collection<T>> batchSaveFunction, boolean writeThroughOnCreate, Executor executor) {
      this.createSaveFunction = createSaveFunction;
      this.batchSaveFunction = batchSaveFunction;
      this.writeThroughOnCreate = writeThroughOnCreate;
      this.executor = executor;
   }

   @Override
   public void save(T data) {
      if (this.createSaveFunction != null) {
         this.dispatch(() -> this.createSaveFunction.accept(data));
      } else {
         this.dispatch(() -> this.batchSaveFunction.accept(List.of(data)));
      }
   }

   @Override
   public void saveAll(Collection<T> data) {
      if (!data.isEmpty()) {
         List<T> snapshot = List.copyOf(data);
         this.dispatch(() -> this.batchSaveFunction.accept(snapshot));
      }
   }

   @Override
   public boolean writesThrough() {
      return false;
   }

   @Override
   public boolean writesThroughOnCreate() {
      return this.writeThroughOnCreate;
   }

   private void dispatch(Runnable write) {
      if (this.executor != null) {
         this.executor.execute(write);
      } else {
         write.run();
      }
   }
}
