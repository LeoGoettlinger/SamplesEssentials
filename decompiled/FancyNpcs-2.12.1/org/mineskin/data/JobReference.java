package org.mineskin.data;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.mineskin.MineSkinClient;
import org.mineskin.exception.MineskinException;
import org.mineskin.response.SkinResponse;

public interface JobReference {
   JobInfo getJob();

   Optional<SkinInfo> getSkin();

   default CompletableFuture<SkinInfo> getOrLoadSkin(MineSkinClient client) {
      if (this.getJob().status() == JobStatus.FAILED) {
         throw new MineskinException("Job failed").withBreadcrumb(this.getJob().getBreadcrumb());
      } else {
         return this.getSkin().isPresent()
            ? CompletableFuture.completedFuture(this.getSkin().get())
            : this.getJob().getSkin(client).thenApply(SkinResponse::getSkin);
      }
   }
}
