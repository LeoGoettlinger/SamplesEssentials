package org.jdbi.v3.core;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jdbi.v3.core.config.JdbiConfig;

public class Handles implements JdbiConfig<Handles> {
   private boolean forceEndTransactions = true;
   private final Set<HandleListener> handleListeners;

   public Handles() {
      this.handleListeners = new CopyOnWriteArraySet<>();
   }

   private Handles(Handles that) {
      this.forceEndTransactions = that.forceEndTransactions;
      this.handleListeners = new CopyOnWriteArraySet<>(that.handleListeners);
   }

   public boolean isForceEndTransactions() {
      return this.forceEndTransactions;
   }

   public void setForceEndTransactions(boolean forceEndTransactions) {
      this.forceEndTransactions = forceEndTransactions;
   }

   public Handles addListener(final HandleListener handleListener) {
      this.handleListeners.add(handleListener);
      return this;
   }

   public Handles removeListener(final HandleListener handleListener) {
      this.handleListeners.remove(handleListener);
      return this;
   }

   public Set<HandleListener> getListeners() {
      return Collections.unmodifiableSet(this.handleListeners);
   }

   CopyOnWriteArraySet<HandleListener> copyListeners() {
      return new CopyOnWriteArraySet<>(this.handleListeners);
   }

   public Handles createCopy() {
      return new Handles(this);
   }
}
