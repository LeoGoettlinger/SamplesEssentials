package ac.grim.grimac.checks.impl.timer;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "NegativeTimer",
   stableKey = "grim.timer.negative",
   description = "Sent movement packets slower than the expected client tick rate",
   setback = -1.0,
   experimental = true
)
public class NegativeTimer extends Timer implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("-{ulong}ms");

   public NegativeTimer(GrimPlayer player) {
      super(player);
      this.timerBalanceRealTime = System.nanoTime() + this.clockDrift;
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.uncertaintyHandler.lastPointThree.hasOccurredSince(2) || !predictionComplete.isChecked()) {
         this.timerBalanceRealTime = System.nanoTime() + this.clockDrift;
      }

      if (this.timerBalanceRealTime < this.lastMovementPlayerClock - this.clockDrift) {
         int lostMS = (int)((System.nanoTime() - this.timerBalanceRealTime) / 1000000.0);
         this.flagWithSetback(V.write(this.verbose()).ulong(lostMS));
         this.timerBalanceRealTime = (long)(this.timerBalanceRealTime + 5.0E7);
      }
   }

   @Override
   public void doCheck(PacketReceiveEvent event) {
   }

   @Override
   public void onReload(ConfigManager config) {
      this.clockDrift = (long)(config.getDoubleElse(this.getConfigName() + ".drift", 1200.0) * 1000000.0);
   }
}
