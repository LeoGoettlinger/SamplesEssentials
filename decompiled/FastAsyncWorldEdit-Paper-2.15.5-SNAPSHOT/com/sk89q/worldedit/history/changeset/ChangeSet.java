package com.sk89q.worldedit.history.changeset;

import com.fastasyncworldedit.core.history.changeset.ChangeSetSummary;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.regions.Region;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import javax.annotation.Nullable;

public interface ChangeSet extends Closeable {
   void add(Change change);

   boolean isRecordingChanges();

   void setRecordChanges(boolean recordChanges);

   Iterator<Change> backwardIterator();

   Iterator<Change> forwardIterator();

   @Deprecated(
      since = "2.11.1"
   )
   int size();

   default long longSize() {
      return this.size();
   }

   @Override
   default void close() throws IOException {
   }

   default void delete() {
   }

   @Nullable
   default ChangeSetSummary summarize(Region region, boolean shallow) {
      return null;
   }

   default boolean isEmpty() {
      return this.size() == 0;
   }
}
