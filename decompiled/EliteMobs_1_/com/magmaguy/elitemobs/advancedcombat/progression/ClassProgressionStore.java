package com.magmaguy.elitemobs.advancedcombat.progression;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ClassProgressionStore {
   StoredClassProfile loadOrCreateProfile(UUID var1, int var2) throws SQLException;

   List<StoredClassProgress> loadAllProgress(UUID var1) throws SQLException;

   StoredClassProgress loadProgressOrZero(UUID var1, String var2, int var3) throws SQLException;

   void saveProfile(StoredClassProfile var1) throws SQLException;

   void saveProgress(StoredClassProgress var1) throws SQLException;

   void savePlayerAggregate(StoredClassProfile var1, Collection<StoredClassProgress> var2) throws SQLException;
}
