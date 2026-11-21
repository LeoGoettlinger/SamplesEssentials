package com.sk89q.worldedit.function.operation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;

public class DelegateOperation implements Operation {
   private final Operation original;
   private Operation delegate;

   public DelegateOperation(Operation original, Operation delegate) {
      Preconditions.checkNotNull(original);
      Preconditions.checkNotNull(delegate);
      this.original = original;
      this.delegate = delegate;
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      this.delegate = this.delegate.resume(run);
      return (Operation)(this.delegate != null ? this : this.original);
   }

   @Override
   public void cancel() {
      this.delegate.cancel();
      this.original.cancel();
   }

   @Override
   public Iterable<Component> getStatusMessages() {
      return Iterables.concat(this.original.getStatusMessages(), this.delegate.getStatusMessages());
   }
}
