package org.mineskin;

import java.util.concurrent.CompletableFuture;
import org.mineskin.data.JobInfo;
import org.mineskin.data.JobReference;
import org.mineskin.request.GenerateRequest;
import org.mineskin.response.JobResponse;
import org.mineskin.response.QueueResponse;

public interface QueueClient {
   CompletableFuture<QueueResponse> submit(GenerateRequest var1);

   CompletableFuture<JobResponse> get(JobInfo var1);

   CompletableFuture<JobResponse> get(String var1);

   CompletableFuture<JobReference> waitForCompletion(JobInfo var1);
}
