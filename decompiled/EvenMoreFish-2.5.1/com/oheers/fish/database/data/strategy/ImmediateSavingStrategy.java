package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ImmediateSavingStrategy<T> implements DataSavingStrategy<T> {
   private final Consumer<T> saveFunction;
   private final Executor executor;

   public ImmediateSavingStrategy(Consumer<T> saveFunction) {
      this(saveFunction, null);
   }

   public ImmediateSavingStrategy(Consumer<T> saveFunction, Executor executor) {
      this.saveFunction = saveFunction;
      this.executor = executor;
   }

   @Override
   public void save(T data) {
      if (this.executor != null) {
         this.executor.execute(() -> this.saveFunction.accept(data));
      } else {
         this.saveFunction.accept(data);
      }
   }

   @Override
   public void saveAll(Collection<T> data) {
      data.forEach(this::save);
   }
}
