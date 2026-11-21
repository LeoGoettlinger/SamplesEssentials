package dev.vatten.baserad;

import dev.vatten.baserad.configs.PluginConfig;
import dev.vatten.baserad.symbols.PlayerSymbol;
import dev.vatten.baserad.symbols.Symbol;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Generated;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.JobInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.options.GenerateQueueOptions;
import org.mineskin.options.IQueueOptions;
import org.mineskin.request.GenerateRequest;

public class TagGenerator {
   private final Plugin plugin;
   private final MineSkinClient CLIENT;

   public TagGenerator(Plugin plugin) {
      this.plugin = plugin;
      PluginConfig.MineSkin mineSkinSettings = plugin.getPluginConfig().getMineSkinSettings();
      PluginConfig.MineSkin.Limits limits = mineSkinSettings.getLimits();
      this.CLIENT = MineSkinClient.builder()
         .requestHandler(JsoupRequestHandler::new)
         .userAgent("FancyTags/v1.0")
         .apiKey(mineSkinSettings.getApiKey())
         .generateQueueOptions(
            (IQueueOptions)(limits.getMode() == PluginConfig.MineSkin.Limits.Mode.AUTO
               ? GenerateQueueOptions.createAuto()
               : GenerateQueueOptions.create().withInterval(limits.getIntervalMillis(), TimeUnit.MILLISECONDS).withConcurrency(limits.getConcurrency()))
         )
         .build();
   }

   public CompletableFuture<Tag> generate(String name, BufferedImage bufferedImage, Consumer<TagGenerator.Progress> onUpdate) {
      CompletableFuture<Tag> future = new CompletableFuture<>();
      if (bufferedImage.getHeight() == 8 && bufferedImage.getWidth() % 8 == 0) {
         int parts = bufferedImage.getWidth() / 8;
         TagGenerator.Progress progress = new TagGenerator.Progress(parts);
         Symbol[] symbols = new Symbol[parts];

         for (int i = 0; i < parts; i++) {
            BufferedImage partImage = bufferedImage.getSubimage(i * 8, 0, 8, 8);
            BufferedImage skinImage = new BufferedImage(64, 64, 2);
            skinImage.getGraphics().drawImage(partImage, 8, 8, null);
            int symbolIndex = i;
            this.upload(name, skinImage).thenAccept(skinInfo -> {
               progress.increase();
               symbols[symbolIndex] = new PlayerSymbol(skinInfo.texture().data().value(), skinInfo.texture().data().signature());
               onUpdate.accept(progress);
               if (progress.getCurrent() == progress.getMax()) {
                  future.complete(new Tag(List.of(symbols)));
               }
            }).exceptionally(throwable -> {
               future.completeExceptionally(throwable);
               return null;
            });
         }

         return future;
      } else {
         future.completeExceptionally(
            new IllegalArgumentException(
               "Image height must be 8, and width must be a multiple of 8. Dimensions provided: " + bufferedImage.getHeight() + "x" + bufferedImage.getWidth()
            )
         );
         return future;
      }
   }

   private CompletableFuture<SkinInfo> upload(String name, RenderedImage image) {
      GenerateRequest request = GenerateRequest.upload(image).name(name).visibility(this.plugin.getPluginConfig().getMineSkinSettings().getVisibility());
      return this.CLIENT.queue().submit(request).thenCompose(queueResponse -> {
         JobInfo job = queueResponse.getJob();
         return job.waitForCompletion(this.CLIENT);
      }).thenCompose(jobResponse -> jobResponse.getOrLoadSkin(this.CLIENT));
   }

   public static class Progress {
      private int current = 0;
      private final int max;

      public Progress(int max) {
         this.max = max;
      }

      public void increase() {
         this.current++;
      }

      @Generated
      public int getCurrent() {
         return this.current;
      }

      @Generated
      public int getMax() {
         return this.max;
      }
   }
}
