package de.oliver.fancynpcs.skins.mineskin;

import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import de.oliver.fancynpcs.FancyNpcs;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.CodeAndMessage;
import org.mineskin.data.JobReference;
import org.mineskin.data.SkinInfo;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.request.GenerateRequest;
import org.mineskin.response.MineSkinResponse;
import org.mineskin.response.QueueResponse;

public class MineSkinAPI {
   private final MineSkinClient client;

   public MineSkinAPI(ScheduledExecutorService executor) {
      this.client = MineSkinClient.builder()
         .requestHandler(JsoupRequestHandler::new)
         .apiKey(FancyNpcs.getInstance().getFancyNpcConfig().getMineSkinApiKey())
         .userAgent("FancyNpcs")
         .timeout(FancyNpcs.getInstance().getFancyNpcConfig().getMineSkinTimeout())
         .getExecutor(executor)
         .generateExecutor(executor)
         .generateRequestScheduler(executor)
         .generateRequestScheduler(executor)
         .jobCheckScheduler(executor)
         .build();
   }

   public SkinInfo generateSkin(GenerateRequest req) throws RatelimitException {
      FancyNpcs.getInstance().getFancyLogger().debug("Generating a skin with MineSkinAPI...");
      QueueResponse queueResp = null;
      JobReference jobResp = null;

      try {
         queueResp = this.client.queue().submit(req).get();
         if (queueResp.getRateLimit().limit().remaining() == 0) {
            throw new RatelimitException(System.currentTimeMillis() + 10000L);
         }

         jobResp = queueResp.getJob().waitForCompletion(this.client).get();
         SkinInfo skinInfo = jobResp.getOrLoadSkin(this.client).get();
         FancyNpcs.getInstance().getFancyLogger().debug("Skin generated with MineSkinApi: " + skinInfo.toString());
         return skinInfo;
      } catch (RatelimitException var11) {
         throw var11;
      } catch (ExecutionException var12) {
         Throwable cause = var12.getCause();
         if (cause instanceof MineSkinRequestException requestException) {
            MineSkinResponse<?> response = requestException.getResponse();

            for (CodeAndMessage error : response.getErrors()) {
               if (error.code().equals("rate_limit")) {
                  throw new RatelimitException(System.currentTimeMillis() + 10000L);
               }

               FancyNpcs.getInstance().getFancyLogger().warn("Could not fetch skin: " + error.code() + ": " + error.message(), ThrowableProperty.of(cause));
               if (queueResp != null) {
                  FancyNpcs.getInstance().getFancyLogger().debug("QueueResp: " + queueResp);
               }

               if (jobResp != null) {
                  FancyNpcs.getInstance().getFancyLogger().debug("JobResp: " + jobResp);
               }
            }
         } else {
            if (cause instanceof SocketTimeoutException timeoutException) {
               FancyNpcs.getInstance().getFancyLogger().warn("Timeout while fetching skin: " + timeoutException.getMessage());
               if (queueResp != null) {
                  FancyNpcs.getInstance().getFancyLogger().debug("QueueResp: " + queueResp);
               }

               if (jobResp != null) {
                  FancyNpcs.getInstance().getFancyLogger().debug("JobResp: " + jobResp);
               }

               throw new RatelimitException(System.currentTimeMillis() + 10000L);
            }

            FancyNpcs.getInstance().getFancyLogger().error("Error in mineskin request: " + cause.getMessage(), ThrowableProperty.of(cause));
            if (queueResp != null) {
               FancyNpcs.getInstance().getFancyLogger().debug("QueueResp: " + queueResp);
            }

            if (jobResp != null) {
               FancyNpcs.getInstance().getFancyLogger().debug("JobResp: " + jobResp);
            }
         }
      } catch (InterruptedException var13) {
         FancyNpcs.getInstance().getFancyLogger().error("Thread was interrupted while waiting for skin generation.", ThrowableProperty.of(var13));
         if (queueResp != null) {
            FancyNpcs.getInstance().getFancyLogger().debug("QueueResp: " + queueResp);
         }

         if (jobResp != null) {
            FancyNpcs.getInstance().getFancyLogger().debug("JobResp: " + jobResp);
         }
      } catch (Exception var14) {
         FancyNpcs.getInstance().getFancyLogger().error("Unexpected error in skin generation: " + var14.getMessage(), ThrowableProperty.of(var14));
         if (queueResp != null) {
            FancyNpcs.getInstance().getFancyLogger().debug("QueueResp: " + queueResp);
         }

         if (jobResp != null) {
            FancyNpcs.getInstance().getFancyLogger().debug("JobResp: " + jobResp);
         }
      }

      return null;
   }
}
