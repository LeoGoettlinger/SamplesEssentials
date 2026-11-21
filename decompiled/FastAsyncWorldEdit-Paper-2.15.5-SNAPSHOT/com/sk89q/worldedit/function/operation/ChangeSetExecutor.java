package com.sk89q.worldedit.function.operation;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet;
import com.fastasyncworldedit.core.history.changeset.ChangeExchangeCoordinator;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import java.util.Iterator;

public class ChangeSetExecutor implements Operation {
   private final Iterator<Change> iterator;
   private final ChangeExchangeCoordinator changeExchangeCoordinator;
   private final ChangeSetExecutor.Type type;
   private final UndoContext context;

   private ChangeSetExecutor(ChangeSet changeSet, ChangeSetExecutor.Type type, UndoContext context, BlockBag blockBag, int inventory) {
      Preconditions.checkNotNull(changeSet);
      Preconditions.checkNotNull(type);
      Preconditions.checkNotNull(context);
      this.type = type;
      this.context = context;
      if (changeSet instanceof AbstractChangeSet abstractChangeSet) {
         if (Settings.settings().EXPERIMENTAL.UNDO_BATCH_SIZE > 0) {
            this.changeExchangeCoordinator = abstractChangeSet.getCoordinatedChanges(blockBag, inventory, type == ChangeSetExecutor.Type.REDO);
            this.iterator = null;
         } else {
            this.iterator = abstractChangeSet.getIterator(blockBag, inventory, type == ChangeSetExecutor.Type.REDO);
            this.changeExchangeCoordinator = null;
         }
      } else if (type == ChangeSetExecutor.Type.UNDO) {
         this.iterator = changeSet.backwardIterator();
         this.changeExchangeCoordinator = null;
      } else {
         this.iterator = changeSet.forwardIterator();
         this.changeExchangeCoordinator = null;
      }
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      if (this.changeExchangeCoordinator != null) {
         Object var12;
         try (ChangeExchangeCoordinator var11 = this.changeExchangeCoordinator) {
            Change[] changes = new Change[Settings.settings().EXPERIMENTAL.UNDO_BATCH_SIZE];

            while ((changes = this.changeExchangeCoordinator.take(changes)) != null) {
               for (Change change : changes) {
                  if (change == null) {
                     return null;
                  }

                  this.type.perform(change, this.context);
               }
            }

            var12 = null;
         }

         return (Operation)var12;
      } else {
         while (this.iterator.hasNext()) {
            Change change = this.iterator.next();
            this.type.perform(change, this.context);
         }

         return null;
      }
   }

   @Override
   public void cancel() {
   }

   public static ChangeSetExecutor create(ChangeSet changeSet, UndoContext context, ChangeSetExecutor.Type type, BlockBag blockBag, int inventory) {
      return new ChangeSetExecutor(changeSet, type, context, blockBag, inventory);
   }

   public static ChangeSetExecutor createUndo(ChangeSet changeSet, UndoContext context) {
      return new ChangeSetExecutor(changeSet, ChangeSetExecutor.Type.UNDO, context, null, 0);
   }

   public static ChangeSetExecutor createRedo(ChangeSet changeSet, UndoContext context) {
      return new ChangeSetExecutor(changeSet, ChangeSetExecutor.Type.REDO, context, null, 0);
   }

   public static enum Type {
      UNDO {
         @Override
         public void perform(Change change, UndoContext context) {
            change.undo(context);
         }
      },
      REDO {
         @Override
         public void perform(Change change, UndoContext context) {
            change.redo(context);
         }
      };

      public void perform(Change change, UndoContext context) {
      }
   }
}
