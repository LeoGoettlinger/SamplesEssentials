package github.nighter.smartspawner.logging.discord;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.logging.SpawnerLogEntry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class DiscordWebhookLogger {
   private static final int MAX_REQUESTS_PER_MINUTE = 25;
   private static final int MAX_EMBEDS_PER_REQUEST = 10;
   private static final int MAX_QUEUE_SIZE = 500;
   private static final long MINUTE_IN_MILLIS = 60000L;
   private final SmartSpawner plugin;
   private volatile DiscordWebhookConfig config;
   private volatile DiscordEmbedConfigManager embedConfigManager;
   private final ConcurrentLinkedQueue<SpawnerLogEntry> webhookQueue = new ConcurrentLinkedQueue<>();
   private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
   private final AtomicLong lastMinuteReset = new AtomicLong(System.currentTimeMillis());
   private final AtomicLong requestsSentThisMinute = new AtomicLong(0L);
   private final AtomicLong rateLimitedUntil = new AtomicLong(0L);
   private Scheduler.Task webhookTask;

   public DiscordWebhookLogger(SmartSpawner plugin, DiscordWebhookConfig config, DiscordEmbedConfigManager embedConfigManager) {
      this.plugin = plugin;
      this.config = config;
      this.embedConfigManager = embedConfigManager;
      if (config.isEnabled()) {
         this.startWebhookTask();
      }
   }

   public void queueWebhook(SpawnerLogEntry entry) {
      if (!this.isShuttingDown.get()) {
         if (this.config.isEnabled() && this.config.isEventEnabled(entry.getEventType())) {
            if (this.webhookQueue.size() >= 500) {
               this.webhookQueue.poll();
            }

            this.webhookQueue.offer(entry);
         }
      }
   }

   public void reload(DiscordWebhookConfig newConfig, DiscordEmbedConfigManager newEmbedManager) {
      this.config = newConfig;
      this.embedConfigManager = newEmbedManager;
      if (this.webhookTask != null) {
         this.webhookTask.cancel();
         this.webhookTask = null;
      }

      if (newConfig.isEnabled()) {
         this.startWebhookTask();
      }
   }

   public void shutdown() {
      this.isShuttingDown.set(true);
      if (this.webhookTask != null) {
         this.webhookTask.cancel();
         this.webhookTask = null;
      }

      int remaining = this.webhookQueue.size();
      this.webhookQueue.clear();
      if (remaining > 0) {
         this.plugin.getLogger().info("Discord webhook: discarded " + remaining + " pending entries at shutdown.");
      }
   }

   private void startWebhookTask() {
      this.webhookTask = Scheduler.runTaskTimerAsync(() -> {
         if (!this.isShuttingDown.get()) {
            this.processWebhookQueue();
         }
      }, 40L, 40L);
   }

   private void processWebhookQueue() {
      if (!this.webhookQueue.isEmpty()) {
         long now = System.currentTimeMillis();
         if (now >= this.rateLimitedUntil.get()) {
            if (now - this.lastMinuteReset.get() >= 60000L) {
               this.requestsSentThisMinute.set(0L);
               this.lastMinuteReset.set(now);
            }

            if (this.requestsSentThisMinute.get() < 25L) {
               String url = this.config.getWebhookUrl();
               if (url != null && !url.isEmpty()) {
                  List<SpawnerLogEntry> batch = new ArrayList<>(10);

                  while (batch.size() < 10) {
                     SpawnerLogEntry entry = this.webhookQueue.poll();
                     if (entry == null) {
                        break;
                     }

                     batch.add(entry);
                  }

                  if (!batch.isEmpty()) {
                     try {
                        List<DiscordEmbed> embeds = new ArrayList<>(batch.size());

                        for (SpawnerLogEntry entry : batch) {
                           DiscordEventEmbedConfig embedCfg = this.embedConfigManager.getEmbedConfig(entry.getEventType());
                           embeds.add(DiscordEmbedBuilder.buildEmbed(entry, embedCfg, this.config, this.plugin));
                        }

                        int responseCode = this.sendHttpRequest(url, DiscordEmbed.buildBatchJson(embeds));
                        if (responseCode == 429) {
                           for (SpawnerLogEntry entry : batch) {
                              if (this.webhookQueue.size() < 500) {
                                 this.webhookQueue.offer(entry);
                              }
                           }
                        } else if (responseCode >= 200 && responseCode < 300) {
                           this.requestsSentThisMinute.incrementAndGet();
                        }
                     } catch (Exception var9) {
                        this.plugin.getLogger().log(Level.WARNING, "Error building/sending Discord webhook batch", (Throwable)var9);
                     }

                     if (this.webhookQueue.size() > 50) {
                        this.plugin.getLogger().warning("Discord webhook queue backing up: " + this.webhookQueue.size() + " pending entries.");
                     }
                  }
               }
            }
         }
      }
   }

   private int sendHttpRequest(String webhookUrl, String jsonPayload) {
      HttpURLConnection conn = null;

      int code;
      try {
         URL url = new URL(webhookUrl);
         conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Content-Type", "application/json");
         conn.setRequestProperty("User-Agent", "SmartSpawner-Logger/1.0");
         conn.setDoOutput(true);
         conn.setConnectTimeout(5000);
         conn.setReadTimeout(5000);

         try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
         }

         code = conn.getResponseCode();
         if (code == 429) {
            long backoffMs = 10000L;
            String retryAfter = conn.getHeaderField("Retry-After");
            if (retryAfter != null) {
               try {
                  backoffMs = Math.max(1000L, (long)(Double.parseDouble(retryAfter) * 1000.0));
               } catch (NumberFormatException var17) {
               }
            }

            this.rateLimitedUntil.set(System.currentTimeMillis() + backoffMs);
            this.plugin.getLogger().warning("Discord webhook rate limited – retrying in " + backoffMs / 1000L + "s (batch will be re-queued).");
         } else if (code < 200 || code >= 300) {
            this.plugin.getLogger().warning("Discord webhook returned HTTP " + code + ".");
         }

         InputStream ignored = conn.getInputStream();
         if (ignored != null) {
            ignored.close();
         }

         return code;
      } catch (IOException var19) {
         this.plugin.getLogger().log(Level.WARNING, "Discord webhook request failed", (Throwable)var19);
         code = -1;
      } finally {
         if (conn != null) {
            conn.disconnect();
         }
      }

      return code;
   }
}
