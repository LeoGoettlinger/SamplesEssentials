package xyz.lychee.lagfixer.commands;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class BenchmarkCommand extends CommandManager.Subcommand {
   private volatile boolean benchmark = false;

   public BenchmarkCommand(CommandManager commandManager) {
      super(commandManager, "benchmark", "run benchmark and compare it with other servers", "test");
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (this.benchmark) {
         return MessageUtils.sendMessage(true, sender, "&7Benchmark is running, wait for results in console!");
      } else {
         ResourceMonitor monitor = SupportManager.getInstance().getResourceMonitor();
         if (monitor.getMspt() > 10.0) {
            return MessageUtils.sendMessage(true, sender, "&7Server MSPT is too &chigh&7, the result may be incorrect!");
         } else {
            long availableRam = monitor.getRamFree() + (monitor.getRamMax() - monitor.getRamTotal());
            if (availableRam < 2048L) {
               return MessageUtils.sendMessage(true, sender, "&7Server available RAM is too low, you need &c" + availableRam + "&8/&c2048MB");
            } else {
               BukkitTask task = SupportManager.getInstance().getFork().runTimer(true, () -> {
                  if (this.benchmark) {
                     MessageUtils.sendMessage(true, sender, "&7Async benchmark in progress, wait for results...");
                  }
               }, 1L, 2L, TimeUnit.SECONDS);
               this.benchmark = true;
               Thread thread = new Thread(() -> {
                  try {
                     TimingUtil t = TimingUtil.startNew();
                     System.gc();
                     BenchmarkCommand.Benchmark b = this.runBenchmarks(10, 20, 100000000, 10);
                     task.cancel();
                     String result = b.getResult().toString();
                     ErrorsManager.getInstance().sendBenchmark(b);
                     MessageUtils.sendMessage(true, sender, "&7Benchmark done in &f" + t.stop() + "&7, results:&f" + result);
                     this.getCommandManager().getPlugin().getLogger().info(result);
                  } catch (Exception var6x) {
                     MessageUtils.sendMessage(true, sender, "&cBenchmark error: " + var6x.getMessage());
                  }

                  this.benchmark = false;
               });
               thread.setName("LagFixer Benchmark");
               thread.setPriority(10);
               thread.start();
               return true;
            }
         }
      }
   }

   public BenchmarkCommand.Benchmark runBenchmarks(int warmup, int cpu, int arrayLength, int memoryPasses) {
      BenchmarkCommand.Benchmark benchmark = new BenchmarkCommand.Benchmark(cpu);
      benchmark.getResult().append("\n \n&8&m    &r&8[ &eLagFixer Advanced CPU Benchmark &8]&m    &r\n ");

      for (int i = 0; i < warmup; i++) {
         this.cpuTest(1000000);
      }

      double totalScore = 0.0;
      long bestScore = Long.MAX_VALUE;
      long worstScore = Long.MIN_VALUE;
      double checksum = 0.0;

      for (int i = 0; i < cpu; i++) {
         long startTime = System.nanoTime();
         double result = this.cpuTest(10000000);
         long duration = System.nanoTime() - startTime;
         double score = 1.0E10 / duration;
         benchmark.scores[i] = score;
         totalScore += score;
         bestScore = Math.min(bestScore, duration);
         worstScore = Math.max(worstScore, duration);
         checksum += result;
      }

      benchmark.getResult()
         .append("\n &8• &fAverage performance: &e")
         .append(totalScore / cpu)
         .append(" Gop/s")
         .append("\n &8• &fBest time: &e")
         .append(bestScore / 1.0E9)
         .append(" s")
         .append("\n &8• &fWorst time: &e")
         .append(worstScore / 1.0E9)
         .append(" s");
      benchmark.setCpu_checksum(checksum);
      benchmark.setTotalScore(totalScore / cpu);
      benchmark.setBestScore(bestScore);
      benchmark.setWorstScore(worstScore);
      benchmark.getResult().append("\n \n&8&m    &r&8[ &eLagFixer Advanced RAM Benchmark &8]&m    &r\n ");
      long[] array = new long[arrayLength];
      int[] randomIndices = new int[arrayLength];
      Random rand = new Random(2137L);

      for (int i = 0; i < arrayLength; i++) {
         randomIndices[i] = rand.nextInt(arrayLength);
      }

      long writeTime = 0L;

      for (int pass = 0; pass < memoryPasses; pass++) {
         long start = System.nanoTime();

         for (int i = 0; i < arrayLength; i++) {
            array[i] = i + pass;
         }

         writeTime += System.nanoTime() - start;
      }

      double writeSpeed = arrayLength * 4.0 * memoryPasses / 1048576.0 / (writeTime / 1.0E9);
      benchmark.getResult().append(String.format("\n &8• &fSequential write: &e%.2f MB/s", writeSpeed));
      benchmark.setWriteSpeed(writeSpeed);
      long readTime = 0L;
      long readChecksum = 0L;

      for (int pass = 0; pass < memoryPasses; pass++) {
         long start = System.nanoTime();

         for (int i = 0; i < arrayLength; i++) {
            readChecksum += array[i];
         }

         readTime += System.nanoTime() - start;
      }

      double readSpeed = arrayLength * 4.0 * memoryPasses / 1048576.0 / (readTime / 1.0E9);
      benchmark.getResult().append(String.format("\n &8• &fSequential read: &e%.2f MB/s", readSpeed));
      benchmark.setReadSpeed(readSpeed);
      long randomTime = 0L;
      long randomChecksum = 0L;

      for (int pass = 0; pass < memoryPasses; pass++) {
         long start = System.nanoTime();

         for (int i = 0; i < arrayLength; i++) {
            randomChecksum += array[randomIndices[i]];
         }

         randomTime += System.nanoTime() - start;
      }

      double randomSpeed = arrayLength * 4.0 * memoryPasses / 1048576.0 / (randomTime / 1.0E9);
      benchmark.getResult().append(String.format("\n &8• &fRandom access: &e%.2f MB/s\n ", randomSpeed));
      benchmark.setRandomSpeed(randomSpeed);
      return benchmark;
   }

   private double cpuTest(int iterations) {
      double sum = 0.0;

      for (int i = 1; i <= iterations; i++) {
         sum += Math.sqrt(i);
         sum -= Math.sin(i);
         sum *= Math.cos(i);
         sum /= Math.log(i + 1);
      }

      return sum;
   }

   public static class Benchmark {
      private final double[] scores;
      private StringBuilder result = new StringBuilder();
      private double cpu_checksum;
      private double bestScore;
      private double worstScore;
      private double totalScore;
      private double memory_checksum;
      private double writeSpeed;
      private double readSpeed;
      private double randomSpeed;
      private double compressionSpeed;
      private double decompressionSpeed;

      public Benchmark(int cpu) {
         this.scores = new double[cpu];
      }

      @Generated
      public double[] getScores() {
         return this.scores;
      }

      @Generated
      public StringBuilder getResult() {
         return this.result;
      }

      @Generated
      public double getCpu_checksum() {
         return this.cpu_checksum;
      }

      @Generated
      public double getBestScore() {
         return this.bestScore;
      }

      @Generated
      public double getWorstScore() {
         return this.worstScore;
      }

      @Generated
      public double getTotalScore() {
         return this.totalScore;
      }

      @Generated
      public double getMemory_checksum() {
         return this.memory_checksum;
      }

      @Generated
      public double getWriteSpeed() {
         return this.writeSpeed;
      }

      @Generated
      public double getReadSpeed() {
         return this.readSpeed;
      }

      @Generated
      public double getRandomSpeed() {
         return this.randomSpeed;
      }

      @Generated
      public double getCompressionSpeed() {
         return this.compressionSpeed;
      }

      @Generated
      public double getDecompressionSpeed() {
         return this.decompressionSpeed;
      }

      @Generated
      public void setResult(StringBuilder result) {
         this.result = result;
      }

      @Generated
      public void setCpu_checksum(double cpu_checksum) {
         this.cpu_checksum = cpu_checksum;
      }

      @Generated
      public void setBestScore(double bestScore) {
         this.bestScore = bestScore;
      }

      @Generated
      public void setWorstScore(double worstScore) {
         this.worstScore = worstScore;
      }

      @Generated
      public void setTotalScore(double totalScore) {
         this.totalScore = totalScore;
      }

      @Generated
      public void setMemory_checksum(double memory_checksum) {
         this.memory_checksum = memory_checksum;
      }

      @Generated
      public void setWriteSpeed(double writeSpeed) {
         this.writeSpeed = writeSpeed;
      }

      @Generated
      public void setReadSpeed(double readSpeed) {
         this.readSpeed = readSpeed;
      }

      @Generated
      public void setRandomSpeed(double randomSpeed) {
         this.randomSpeed = randomSpeed;
      }

      @Generated
      public void setCompressionSpeed(double compressionSpeed) {
         this.compressionSpeed = compressionSpeed;
      }

      @Generated
      public void setDecompressionSpeed(double decompressionSpeed) {
         this.decompressionSpeed = decompressionSpeed;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof BenchmarkCommand.Benchmark other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (Double.compare(this.getCpu_checksum(), other.getCpu_checksum()) != 0) {
            return false;
         } else if (Double.compare(this.getBestScore(), other.getBestScore()) != 0) {
            return false;
         } else if (Double.compare(this.getWorstScore(), other.getWorstScore()) != 0) {
            return false;
         } else if (Double.compare(this.getTotalScore(), other.getTotalScore()) != 0) {
            return false;
         } else if (Double.compare(this.getMemory_checksum(), other.getMemory_checksum()) != 0) {
            return false;
         } else if (Double.compare(this.getWriteSpeed(), other.getWriteSpeed()) != 0) {
            return false;
         } else if (Double.compare(this.getReadSpeed(), other.getReadSpeed()) != 0) {
            return false;
         } else if (Double.compare(this.getRandomSpeed(), other.getRandomSpeed()) != 0) {
            return false;
         } else if (Double.compare(this.getCompressionSpeed(), other.getCompressionSpeed()) != 0) {
            return false;
         } else if (Double.compare(this.getDecompressionSpeed(), other.getDecompressionSpeed()) != 0) {
            return false;
         } else if (!Arrays.equals(this.getScores(), other.getScores())) {
            return false;
         } else {
            Object this$result = this.getResult();
            Object other$result = other.getResult();
            return this$result == null ? other$result == null : this$result.equals(other$result);
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof BenchmarkCommand.Benchmark;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         long $cpu_checksum = Double.doubleToLongBits(this.getCpu_checksum());
         result = result * 59 + (int)($cpu_checksum >>> 32 ^ $cpu_checksum);
         long $bestScore = Double.doubleToLongBits(this.getBestScore());
         result = result * 59 + (int)($bestScore >>> 32 ^ $bestScore);
         long $worstScore = Double.doubleToLongBits(this.getWorstScore());
         result = result * 59 + (int)($worstScore >>> 32 ^ $worstScore);
         long $totalScore = Double.doubleToLongBits(this.getTotalScore());
         result = result * 59 + (int)($totalScore >>> 32 ^ $totalScore);
         long $memory_checksum = Double.doubleToLongBits(this.getMemory_checksum());
         result = result * 59 + (int)($memory_checksum >>> 32 ^ $memory_checksum);
         long $writeSpeed = Double.doubleToLongBits(this.getWriteSpeed());
         result = result * 59 + (int)($writeSpeed >>> 32 ^ $writeSpeed);
         long $readSpeed = Double.doubleToLongBits(this.getReadSpeed());
         result = result * 59 + (int)($readSpeed >>> 32 ^ $readSpeed);
         long $randomSpeed = Double.doubleToLongBits(this.getRandomSpeed());
         result = result * 59 + (int)($randomSpeed >>> 32 ^ $randomSpeed);
         long $compressionSpeed = Double.doubleToLongBits(this.getCompressionSpeed());
         result = result * 59 + (int)($compressionSpeed >>> 32 ^ $compressionSpeed);
         long $decompressionSpeed = Double.doubleToLongBits(this.getDecompressionSpeed());
         result = result * 59 + (int)($decompressionSpeed >>> 32 ^ $decompressionSpeed);
         result = result * 59 + Arrays.hashCode(this.getScores());
         Object $result = this.getResult();
         return result * 59 + ($result == null ? 43 : $result.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "BenchmarkCommand.Benchmark(scores="
            + Arrays.toString(this.getScores())
            + ", result="
            + this.getResult()
            + ", cpu_checksum="
            + this.getCpu_checksum()
            + ", bestScore="
            + this.getBestScore()
            + ", worstScore="
            + this.getWorstScore()
            + ", totalScore="
            + this.getTotalScore()
            + ", memory_checksum="
            + this.getMemory_checksum()
            + ", writeSpeed="
            + this.getWriteSpeed()
            + ", readSpeed="
            + this.getReadSpeed()
            + ", randomSpeed="
            + this.getRandomSpeed()
            + ", compressionSpeed="
            + this.getCompressionSpeed()
            + ", decompressionSpeed="
            + this.getDecompressionSpeed()
            + ")";
      }
   }
}
