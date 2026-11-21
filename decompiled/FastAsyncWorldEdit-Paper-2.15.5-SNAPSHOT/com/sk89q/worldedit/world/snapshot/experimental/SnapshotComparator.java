package com.sk89q.worldedit.world.snapshot.experimental;

import java.util.Comparator;

public class SnapshotComparator {
   private static final Comparator<Snapshot> COMPARATOR = Comparator.comparing(Snapshot::getInfo);

   public static Comparator<Snapshot> getInstance() {
      return COMPARATOR;
   }

   private SnapshotComparator() {
   }
}
