package org.mineskin.response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.mineskin.MineSkinClient;
import org.mineskin.data.JobInfo;
import org.mineskin.data.JobReference;
import org.mineskin.data.SkinInfo;

public interface JobResponse extends MineSkinResponse<JobInfo>, JobReference {
   @Override
   JobInfo getJob();

   @Override
   Optional<SkinInfo> getSkin();

   @Override
   CompletableFuture<SkinInfo> getOrLoadSkin(MineSkinClient var1);
}
