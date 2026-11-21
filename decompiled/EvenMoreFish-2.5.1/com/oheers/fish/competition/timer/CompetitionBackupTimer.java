package com.oheers.fish.competition.timer;

import com.oheers.fish.api.EMFTimer;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;

public class CompetitionBackupTimer extends EMFTimer {
   private final Competition competition;

   public CompetitionBackupTimer(@NonNull Competition competition) {
      super(TimeUnit.SECONDS, MainConfig.getInstance().getCompetitionBackupInterval());
      this.competition = competition;
   }

   @Override
   public void run() {
      this.competition.saveToFile();
   }
}
