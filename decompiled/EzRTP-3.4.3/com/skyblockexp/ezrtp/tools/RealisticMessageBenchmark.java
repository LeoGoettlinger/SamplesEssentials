package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;

public final class RealisticMessageBenchmark {
   private static final Logger LOG = Logger.getLogger("RealisticBenchmark");

   public static void main(String[] args) throws Exception {
      MessageProvider provider = MessageProvider.createDefault("en", LOG);
      int players = 1000;
      int perPlayerMessages = 50;
      int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
      List<RealisticMessageBenchmark.MockPlayer> mockPlayers = new ArrayList<>(players);

      for (int i = 0; i < players; i++) {
         mockPlayers.add(new RealisticMessageBenchmark.MockPlayer("player" + i));
      }

      System.out.println("Benchmark: players=" + players + " messagesPerPlayer=" + perPlayerMessages + " threads=" + threads);
      runTrial(provider, mockPlayers, perPlayerMessages, threads, false);
      PlatformMessageServiceRegistry.unregister();
      long noService = runTrial(provider, mockPlayers, perPlayerMessages, threads, false);
      System.out.println("No service time ms: " + noService);
      RealisticMessageBenchmark.DummyPlatformService svc = new RealisticMessageBenchmark.DummyPlatformService();
      PlatformMessageServiceRegistry.register(svc);
      long withService = runTrial(provider, mockPlayers, perPlayerMessages, threads, false);
      System.out.println("With service time ms: " + withService);
      PlatformMessageServiceRegistry.unregister();
      System.out.println("Done. Service send count: " + svc.sendCount);
   }

   private static long runTrial(
      MessageProvider provider, List<RealisticMessageBenchmark.MockPlayer> players, int perPlayerMessages, int threads, boolean verbose
   ) throws InterruptedException, ExecutionException {
      ExecutorService ex = Executors.newFixedThreadPool(threads);
      List<Callable<Long>> tasks = new ArrayList<>();

      for (RealisticMessageBenchmark.MockPlayer p : players) {
         tasks.add(() -> {
            long t0 = System.nanoTime();

            for (int i = 0; i < perPlayerMessages; i++) {
               Component comp = provider.format(MessageKey.TELEPORTING, Map.of("player", p.getName()), p);
               MessageUtil.send(p, comp);
            }

            long t1 = System.nanoTime();
            return TimeUnit.NANOSECONDS.toMillis(t1 - t0);
         });
      }

      long start = System.nanoTime();
      List<Future<Long>> res = ex.invokeAll(tasks);
      long total = 0L;

      for (Future<Long> f : res) {
         total += f.get();
      }

      ex.shutdown();
      ex.awaitTermination(1L, TimeUnit.MINUTES);
      long end = System.nanoTime();
      return TimeUnit.NANOSECONDS.toMillis(end - start);
   }

   private static final class DummyPlatformService implements PlatformMessageService {
      volatile long sendCount = 0L;

      @Override
      public String resolvePlaceholders(Object player, String text, Logger logger) {
         try {
            Method m = player.getClass().getMethod("getName");
            Object name = m.invoke(player);
            if (name != null) {
               return text.replace("<player>", name.toString());
            }
         } catch (Throwable var6) {
         }

         return text;
      }

      @Override
      public boolean sendToSender(Object sender, Component component) {
         this.sendCount++;
         return true;
      }
   }

   private static final class MockPlayer {
      private final String name;
      private final UUID id = UUID.randomUUID();

      MockPlayer(String name) {
         this.name = name;
      }

      public UUID getUniqueId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public void sendMessage(String s) {
      }

      public void sendMessage(Component c) {
      }
   }
}
