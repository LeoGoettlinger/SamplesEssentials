package com.oheers.fish.database;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public interface DatabaseAPI {
   boolean hasUser(@NonNull UUID uuid);

   int getUserId(@NonNull UUID uuid);

   UserReport getUserReport(@NonNull UUID uuid);

   boolean hasFishStats(@NonNull IFish fish);

   void incrementFish(@NonNull IFish fish);

   boolean userHasFish(@NonNull String rarity, @NonNull String fish, int id);

   boolean userHasRarity(@NonNull String rarity, int id);

   void createCompetitionReport(@NonNull Competition competition);

   void createSale(@NonNull String transactionId, @NonNull String fishName, @NonNull String fishRarity, int fishAmount, double fishLength, double priceSold);

   void createTransaction(@NonNull String transactionId, int userId, @NonNull Timestamp timestamp);

   UserFishStats getUserFishStats(final int userId, final String fishName, final String fishRarity);

   FishLog getFishLog(final int userId, final String fishName, final String fishRarity, final LocalDateTime time);

   Set<FishLog> getFishLogEntries(final int userId, final String fishName, final String fishRarity);

   void setFishLogEntry(final FishLog fishLogEntry);

   FishStats getFishStats(final String fishName, final String fishRarity);

   void upsertFishStats(final FishStats fishStats);

   void batchInsertFishLogs(Collection<FishLog> logs);

   Integer upsertUserReport(UserReport report);

   void batchUpdateUserFishStats(Collection<UserFishStats> userFishStats);

   void updateCompetition(CompetitionReport competition);

   CompetitionReport getCompetitionReport(int id);

   void batchUpdateCompetitions(Collection<CompetitionReport> competitions);
}
