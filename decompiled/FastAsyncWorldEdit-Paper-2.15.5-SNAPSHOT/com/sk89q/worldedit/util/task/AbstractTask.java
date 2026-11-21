package com.sk89q.worldedit.util.task;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;

public abstract class AbstractTask<V> extends AbstractFuture<V> implements Task<V> {
   private final UUID uniqueId = UUID.randomUUID();
   private final String name;
   private final Object owner;
   private final Date creationDate = new Date();

   protected AbstractTask(String name, @Nullable Object owner) {
      Preconditions.checkNotNull(name);
      this.name = name;
      this.owner = owner;
   }

   @Override
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Nullable
   @Override
   public Object getOwner() {
      return this.owner;
   }

   @Override
   public Date getCreationDate() {
      return this.creationDate;
   }
}
