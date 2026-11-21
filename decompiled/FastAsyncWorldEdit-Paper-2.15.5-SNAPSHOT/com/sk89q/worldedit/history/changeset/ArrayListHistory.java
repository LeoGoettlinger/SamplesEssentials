package com.sk89q.worldedit.history.changeset;

import com.fastasyncworldedit.core.history.changeset.ChangeSetSummary;
import com.fastasyncworldedit.core.history.changeset.SimpleChangeSetSummary;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayListHistory implements ChangeSet {
   private final List<Change> changes = new ArrayList<>();
   private boolean recordChanges = true;

   @Override
   public void add(Change change) {
      Preconditions.checkNotNull(change);
      if (this.recordChanges) {
         this.changes.add(change);
      }
   }

   @Override
   public boolean isRecordingChanges() {
      return this.recordChanges;
   }

   @Override
   public void setRecordChanges(boolean recordChanges) {
      this.recordChanges = recordChanges;
   }

   @Override
   public Iterator<Change> backwardIterator() {
      return Lists.reverse(this.changes).iterator();
   }

   @Override
   public Iterator<Change> forwardIterator() {
      return this.changes.iterator();
   }

   @Override
   public int size() {
      return this.changes.size();
   }

   @Override
   public ChangeSetSummary summarize(Region region, boolean shallow) {
      SimpleChangeSetSummary summary = new SimpleChangeSetSummary();

      for (Change change : this.changes) {
         if (change instanceof BlockChange blockChange) {
            BlockVector3 pos = blockChange.getPosition();
            summary.add(pos.x(), pos.z(), blockChange.getCurrent().getOrdinal());
         }
      }

      return summary;
   }
}
