package com.sk89q.worldedit.function.visitor;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Iterator;

public class EntityVisitor implements Operation {
   private final Iterator<? extends Entity> iterator;
   private final EntityFunction function;
   private int affected = 0;

   public EntityVisitor(Iterator<? extends Entity> iterator, EntityFunction function) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkNotNull(function);
      this.iterator = iterator;
      this.function = function;
   }

   public int getAffected() {
      return this.affected;
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      while (this.iterator.hasNext()) {
         if (this.function.apply(this.iterator.next())) {
            this.affected++;
         }
      }

      return null;
   }

   @Override
   public void cancel() {
   }

   @Override
   public Iterable<Component> getStatusMessages() {
      return ImmutableList.of(Caption.of("worldedit.operation.affected.entity", TextComponent.of(this.getAffected())));
   }
}
