package com.oheers.fish.competition.timer;

import com.oheers.fish.api.EMFTimer;
import com.oheers.fish.competition.Competition;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;

public class CompetitionTimer extends EMFTimer {
   private final Competition competition;

   public CompetitionTimer(@NonNull Competition competition) {
      super(TimeUnit.SECONDS, 1L);
      this.competition = competition;
   }

   @Override
   public void run() {
      this.competition.getStatusBar().timerUpdate(this.competition.getTimeLeft(), this.competition.getMaxDuration());
      if (this.competition.decreaseTime()) {
         this.stop();
      }
   }
}
