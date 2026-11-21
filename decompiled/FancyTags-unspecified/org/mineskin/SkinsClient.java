package org.mineskin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.mineskin.response.SkinResponse;

public interface SkinsClient {
   CompletableFuture<SkinResponse> get(UUID var1);

   CompletableFuture<SkinResponse> get(String var1);
}
