package com.oheers.fish.competition.leaderboard;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

public class Leaderboard {
   private final CompetitionType type;
   private final TreeSet<CompetitionEntry> entries;

   public Leaderboard(CompetitionType type) {
      this.type = type;
      this.entries = new TreeSet<>(this.createComparator());
   }

   public List<CompetitionEntry> getEntries() {
      return new ArrayList<>(this.entries);
   }

   public void addEntry(@NonNull UUID player, @NonNull IFish fish) {
      CompetitionEntry entry = new CompetitionEntry(player, fish, this.type);
      this.addEntry(entry);
   }

   public void addEntry(@NonNull CompetitionEntry entry) {
      CompetitionEntry initialTopEntry = this.getTopEntry();
      this.entries.add(entry);
      CompetitionEntry newTopEntry = this.getTopEntry();
      if (initialTopEntry != null && newTopEntry != null) {
         UUID initialPlayer = initialTopEntry.getPlayer();
         UUID newPlayer = newTopEntry.getPlayer();
         if (!newPlayer.equals(initialPlayer)) {
            IFish newFish = newTopEntry.getFish();
            EMFMessage message = ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION.getMessage();
            message.setPerPlayer(false);
            message.setPlayer(Bukkit.getOfflinePlayer(newPlayer));
            message.setLength(Float.toString(newFish.getLength()));
            message.setFishCaught(newFish.getId());
            message.setRarity(newFish.getRarity().getDisplayName());
            if (CompetitionManager.getInstance().isDoingFirstPlaceActionBar()) {
               message.broadcastActionBar();
            } else {
               message.broadcast();
            }
         }
      }
   }

   public void clear() {
      this.entries.clear();
   }

   public boolean contains(CompetitionEntry entry) {
      return this.entries.contains(entry);
   }

   public CompetitionEntry getEntry(UUID player) {
      for (CompetitionEntry entry : this.entries) {
         if (entry.getPlayer().equals(player)) {
            return entry;
         }
      }

      return null;
   }

   public CompetitionEntry getEntry(int place) {
      try {
         return this.getEntries().get(place - 1);
      } catch (IndexOutOfBoundsException var3) {
         return null;
      }
   }

   public int getSize() {
      return this.entries.size();
   }

   public boolean hasEntry(UUID player) {
      return this.getEntry(player) != null;
   }

   public void removeEntry(CompetitionEntry entry) {
      this.entries.remove(entry);
   }

   public CompetitionEntry getTopEntry() {
      return this.getEntries().isEmpty() ? null : this.getEntries().getFirst();
   }

   public CompetitionEntry trackFish(@NonNull CompetitionEntry entry, @NonNull IFish fish) {
      CompetitionEntry newEntry = new CompetitionEntry(entry);
      newEntry.trackFish(fish);
      this.addEntry(newEntry);
      this.removeEntry(entry);
      return newEntry;
   }

   private Comparator<CompetitionEntry> createComparator() {
      Comparator<CompetitionEntry> comparator = Comparator.comparingDouble(CompetitionEntry::getValue)
         .thenComparing(Comparator.comparingLong(CompetitionEntry::getTime).reversed());
      return this.type.shouldReverseLeaderboard() ? comparator : comparator.reversed();
   }
}
