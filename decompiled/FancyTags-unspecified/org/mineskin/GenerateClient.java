package org.mineskin;

import java.util.concurrent.CompletableFuture;
import org.mineskin.request.GenerateRequest;
import org.mineskin.response.GenerateResponse;

public interface GenerateClient {
   CompletableFuture<GenerateResponse> submitAndWait(GenerateRequest var1);
}
