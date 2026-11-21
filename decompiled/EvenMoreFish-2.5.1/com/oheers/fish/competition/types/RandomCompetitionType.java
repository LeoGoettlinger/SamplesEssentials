package com.oheers.fish.competition.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.CompetitionTypeRegistry;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class RandomCompetitionType extends CompetitionType.Random {
   @NonNull
   @Override
   public CompetitionType getRandomType(@NonNull Competition competition) {
      List<CompetitionType> types = this.getValidTypes(competition).filter(typex -> {
         if (typex instanceof RandomCompetitionType) {
            return false;
         } else {
            try {
               return typex.isUsable(competition);
            } catch (Exception var3x) {
               EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, var3x.getMessage(), (Throwable)var3x);
               return false;
            }
         }
      }).toList();
      if (types.isEmpty()) {
         EvenMoreFish.getInstance().getLogger().warning("No competition types available for random strategy. Defaulting to LARGEST_FISH.");
         return CompetitionType.DEFAULT;
      } else {
         int type = EvenMoreFish.RANDOM.nextInt(types.size());
         return types.get(type);
      }
   }

   @NonNull
   @Override
   public String getKey() {
      return "random";
   }

   private Stream<CompetitionType> getValidTypes(@NonNull Competition competition) {
      List<String> types = competition.getCompetitionFile().getConfig().getStringList("random-selection");
      return types != null && !types.isEmpty()
         ? types.stream().map(CompetitionTypeRegistry.getInstance()::get).filter(Objects::nonNull)
         : CompetitionTypeRegistry.getInstance().getRegistry().values().stream();
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }

   @NonNull
   @Override
   public Plugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
