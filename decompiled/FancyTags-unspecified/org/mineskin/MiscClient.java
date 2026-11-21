package org.mineskin;

import java.util.concurrent.CompletableFuture;
import org.mineskin.response.UserResponse;

public interface MiscClient {
   CompletableFuture<UserResponse> getUser();
}
