package org.mineskin;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mineskin.data.JobInfo;
import org.mineskin.data.JobReference;
import org.mineskin.data.NullJobReference;
import org.mineskin.data.RateLimitInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.data.UserInfo;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.exception.MineskinException;
import org.mineskin.options.IJobCheckOptions;
import org.mineskin.request.GenerateRequest;
import org.mineskin.request.RequestHandler;
import org.mineskin.request.UploadRequestBuilder;
import org.mineskin.request.UrlRequestBuilder;
import org.mineskin.request.UserRequestBuilder;
import org.mineskin.request.source.UploadSource;
import org.mineskin.response.GenerateResponse;
import org.mineskin.response.GenerateResponseImpl;
import org.mineskin.response.JobResponse;
import org.mineskin.response.JobResponseImpl;
import org.mineskin.response.MineSkinResponse;
import org.mineskin.response.QueueResponse;
import org.mineskin.response.QueueResponseImpl;
import org.mineskin.response.SkinResponse;
import org.mineskin.response.SkinResponseImpl;
import org.mineskin.response.UserResponse;
import org.mineskin.response.UserResponseImpl;

public class MineSkinClientImpl implements MineSkinClient {
   public static final Logger LOGGER = Logger.getLogger(MineSkinClient.class.getName());
   private final RequestExecutors executors;
   private final RequestHandler requestHandler;
   private final RequestQueue generateQueue;
   private final RequestQueue getQueue;
   private final QueueClient queueClient = new MineSkinClientImpl.QueueClientImpl();
   private final GenerateClient generateClient = new MineSkinClientImpl.GenerateClientImpl();
   private final SkinsClient skinsClient = new MineSkinClientImpl.SkinsClientImpl();
   private final MiscClient miscClient = new MineSkinClientImpl.MiscClientImpl();

   public MineSkinClientImpl(RequestHandler requestHandler, RequestExecutors executors) {
      this.requestHandler = Preconditions.checkNotNull(requestHandler);
      this.executors = Preconditions.checkNotNull(executors);
      this.generateQueue = new RequestQueue(executors.generateQueueOptions());
      this.getQueue = new RequestQueue(executors.getQueueOptions());
   }

   @Override
   public Logger getLogger() {
      return LOGGER;
   }

   @Override
   public QueueClient queue() {
      return this.queueClient;
   }

   @Override
   public GenerateClient generate() {
      return this.generateClient;
   }

   @Override
   public SkinsClient skins() {
      return this.skinsClient;
   }

   @Override
   public MiscClient misc() {
      return this.miscClient;
   }

   class GenerateClientImpl implements GenerateClient {
      @Override
      public CompletableFuture<GenerateResponse> submitAndWait(GenerateRequest request) {
         if (request instanceof UploadRequestBuilder uploadRequestBuilder) {
            return this.generateUpload(uploadRequestBuilder);
         } else if (request instanceof UrlRequestBuilder urlRequestBuilder) {
            return this.generateUrl(urlRequestBuilder);
         } else if (request instanceof UserRequestBuilder userRequestBuilder) {
            return this.generateUser(userRequestBuilder);
         } else {
            throw new MineskinException("Unknown request builder type: " + request.getClass());
         }
      }

      CompletableFuture<GenerateResponse> generateUpload(UploadRequestBuilder builder) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Adding upload request to internal generate queue: {0}", builder);
         return MineSkinClientImpl.this.generateQueue
            .submit(
               () -> {
                  try {
                     Map<String, String> data = builder.options().toMap();
                     UploadSource source = builder.getUploadSource();
                     Preconditions.checkNotNull(source);

                     GenerateResponseImpl var6;
                     try (InputStream inputStream = source.getInputStream()) {
                        MineSkinClientImpl.LOGGER.log(Level.FINER, "Submitting to MineSkin generate: {0}", builder);
                        GenerateResponseImpl res = MineSkinClientImpl.this.requestHandler
                           .postFormDataFile("/v2/generate", "file", "mineskinjava", inputStream, data, SkinInfo.class, GenerateResponseImpl::new);
                        this.handleGenerateResponse(res);
                        var6 = res;
                     }

                     return var6;
                  } catch (IOException var9) {
                     throw new MineskinException(var9);
                  } catch (MineSkinRequestException var10) {
                     this.handleGenerateResponse(var10.getResponse());
                     throw var10;
                  }
               },
               MineSkinClientImpl.this.executors.generateExecutor()
            );
      }

      CompletableFuture<GenerateResponse> generateUrl(UrlRequestBuilder builder) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Adding url request to internal generate queue: {0}", builder);
         return MineSkinClientImpl.this.generateQueue.submit(() -> {
            try {
               JsonObject body = builder.options().toJson();
               URL url = builder.getUrl();
               Preconditions.checkNotNull(url);
               body.addProperty("url", url.toString());
               MineSkinClientImpl.LOGGER.log(Level.FINER, "Submitting to MineSkin generate: {0}", builder);
               GenerateResponseImpl res = MineSkinClientImpl.this.requestHandler.postJson("/v2/generate", body, SkinInfo.class, GenerateResponseImpl::new);
               this.handleGenerateResponse(res);
               return res;
            } catch (IOException var5) {
               throw new MineskinException(var5);
            } catch (MineSkinRequestException var6) {
               this.handleGenerateResponse(var6.getResponse());
               throw var6;
            }
         }, MineSkinClientImpl.this.executors.generateExecutor());
      }

      CompletableFuture<GenerateResponse> generateUser(UserRequestBuilder builder) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Adding user request to internal generate queue: {0}", builder);
         return MineSkinClientImpl.this.generateQueue.submit(() -> {
            try {
               JsonObject body = builder.options().toJson();
               UUID uuid = builder.getUuid();
               Preconditions.checkNotNull(uuid);
               body.addProperty("user", uuid.toString());
               MineSkinClientImpl.LOGGER.log(Level.FINER, "Submitting to MineSkin generate: {0}", builder);
               GenerateResponseImpl res = MineSkinClientImpl.this.requestHandler.postJson("/v2/generate", body, SkinInfo.class, GenerateResponseImpl::new);
               this.handleGenerateResponse(res);
               return res;
            } catch (IOException var5) {
               throw new MineskinException(var5);
            } catch (MineSkinRequestException var6) {
               this.handleGenerateResponse(var6.getResponse());
               throw var6;
            }
         }, MineSkinClientImpl.this.executors.generateExecutor());
      }

      private void handleGenerateResponse(MineSkinResponse<?> response0) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Handling generate response: {0}", response0);
         if (response0 instanceof GenerateResponse response) {
            RateLimitInfo rateLimit = response.getRateLimit();
            if (rateLimit != null) {
               long nextRelative = rateLimit.next().relative();
               if (nextRelative > 0L) {
                  MineSkinClientImpl.this.generateQueue
                     .setNextRequest(Math.max(MineSkinClientImpl.this.generateQueue.getNextRequest(), System.currentTimeMillis() + nextRelative));
               }
            }
         }
      }
   }

   class MiscClientImpl implements MiscClient {
      @Override
      public CompletableFuture<UserResponse> getUser() {
         return MineSkinClientImpl.this.getQueue.submit(() -> {
            try {
               return MineSkinClientImpl.this.requestHandler.getJson("/v2/me", UserInfo.class, UserResponseImpl::new);
            } catch (IOException var2) {
               throw new MineskinException(var2);
            }
         }, MineSkinClientImpl.this.executors.getExecutor());
      }
   }

   class QueueClientImpl implements QueueClient {
      @Override
      public CompletableFuture<QueueResponse> submit(GenerateRequest request) {
         if (request instanceof UploadRequestBuilder uploadRequestBuilder) {
            return this.queueUpload(uploadRequestBuilder);
         } else if (request instanceof UrlRequestBuilder urlRequestBuilder) {
            return this.queueUrl(urlRequestBuilder);
         } else if (request instanceof UserRequestBuilder userRequestBuilder) {
            return this.queueUser(userRequestBuilder);
         } else {
            throw new MineskinException("Unknown request builder type: " + request.getClass());
         }
      }

      CompletableFuture<QueueResponse> queueUpload(UploadRequestBuilder builder) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Adding upload request to internal queue: {0}", builder);
         return MineSkinClientImpl.this.generateQueue
            .submit(
               () -> {
                  try {
                     Map<String, String> data = builder.options().toMap();
                     UploadSource source = builder.getUploadSource();
                     Preconditions.checkNotNull(source);

                     QueueResponseImpl var6;
                     try (InputStream inputStream = source.getInputStream()) {
                        MineSkinClientImpl.LOGGER.log(Level.FINER, "Submitting to MineSkin queue: {0}", builder);
                        QueueResponseImpl res = MineSkinClientImpl.this.requestHandler
                           .postFormDataFile("/v2/queue", "file", "mineskinjava", inputStream, data, JobInfo.class, QueueResponseImpl::new);
                        this.handleGenerateResponse(res);
                        var6 = res;
                     }

                     return var6;
                  } catch (IOException var9) {
                     throw new MineskinException(var9);
                  } catch (MineSkinRequestException var10) {
                     this.handleGenerateResponse(var10.getResponse());
                     throw var10;
                  }
               },
               MineSkinClientImpl.this.executors.generateExecutor()
            );
      }

      CompletableFuture<QueueResponse> queueUrl(UrlRequestBuilder builder) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Adding url request to internal queue: {0}", builder);
         return MineSkinClientImpl.this.generateQueue.submit(() -> {
            try {
               JsonObject body = builder.options().toJson();
               URL url = builder.getUrl();
               Preconditions.checkNotNull(url);
               body.addProperty("url", url.toString());
               MineSkinClientImpl.LOGGER.log(Level.FINER, "Submitting to MineSkin queue: {0}", builder);
               QueueResponseImpl res = MineSkinClientImpl.this.requestHandler.postJson("/v2/queue", body, JobInfo.class, QueueResponseImpl::new);
               this.handleGenerateResponse(res);
               return res;
            } catch (IOException var5) {
               throw new MineskinException(var5);
            } catch (MineSkinRequestException var6) {
               this.handleGenerateResponse(var6.getResponse());
               throw var6;
            }
         }, MineSkinClientImpl.this.executors.generateExecutor());
      }

      CompletableFuture<QueueResponse> queueUser(UserRequestBuilder builder) {
         MineSkinClientImpl.LOGGER.log(Level.FINER, "Adding user request to internal queue: {0}", builder);
         return MineSkinClientImpl.this.generateQueue.submit(() -> {
            try {
               JsonObject body = builder.options().toJson();
               UUID uuid = builder.getUuid();
               Preconditions.checkNotNull(uuid);
               body.addProperty("user", uuid.toString());
               MineSkinClientImpl.LOGGER.log(Level.FINER, "Submitting to MineSkin queue: {0}", builder);
               QueueResponseImpl res = MineSkinClientImpl.this.requestHandler.postJson("/v2/queue", body, JobInfo.class, QueueResponseImpl::new);
               this.handleGenerateResponse(res);
               return res;
            } catch (IOException var5) {
               throw new MineskinException(var5);
            } catch (MineSkinRequestException var6) {
               this.handleGenerateResponse(var6.getResponse());
               throw var6;
            }
         }, MineSkinClientImpl.this.executors.generateExecutor());
      }

      private void handleGenerateResponse(MineSkinResponse<?> response0) {
         if (response0 instanceof QueueResponse response) {
            RateLimitInfo rateLimit = response.getRateLimit();
            if (rateLimit != null) {
               long nextRelative = rateLimit.next().relative();
               if (nextRelative > 0L) {
                  MineSkinClientImpl.this.generateQueue
                     .setNextRequest(Math.max(MineSkinClientImpl.this.generateQueue.getNextRequest(), System.currentTimeMillis() + nextRelative));
               }
            }
         }
      }

      @Override
      public CompletableFuture<JobResponse> get(JobInfo jobInfo) {
         Preconditions.checkNotNull(jobInfo);
         return this.get(jobInfo.id());
      }

      @Override
      public CompletableFuture<JobResponse> get(String id) {
         Preconditions.checkNotNull(id);
         return CompletableFuture.supplyAsync(() -> {
            try {
               return MineSkinClientImpl.this.requestHandler.getJson("/v2/queue/" + id, JobInfo.class, JobResponseImpl::new);
            } catch (IOException var3) {
               throw new MineskinException(var3);
            }
         }, MineSkinClientImpl.this.executors.getExecutor());
      }

      @Override
      public CompletableFuture<JobReference> waitForCompletion(JobInfo jobInfo) {
         Preconditions.checkNotNull(jobInfo);
         if (jobInfo.id() == null) {
            return CompletableFuture.completedFuture(new NullJobReference(jobInfo));
         } else {
            IJobCheckOptions options = MineSkinClientImpl.this.executors.jobCheckOptions();
            return new JobChecker(MineSkinClientImpl.this, jobInfo, options).check();
         }
      }
   }

   class SkinsClientImpl implements SkinsClient {
      @Override
      public CompletableFuture<SkinResponse> get(UUID uuid) {
         Preconditions.checkNotNull(uuid);
         return this.get(uuid.toString());
      }

      @Override
      public CompletableFuture<SkinResponse> get(String uuid) {
         Preconditions.checkNotNull(uuid);
         return MineSkinClientImpl.this.getQueue.submit(() -> {
            try {
               return MineSkinClientImpl.this.requestHandler.getJson("/v2/skins/" + uuid, SkinInfo.class, SkinResponseImpl::new);
            } catch (IOException var3) {
               throw new MineskinException(var3);
            }
         }, MineSkinClientImpl.this.executors.getExecutor());
      }
   }
}
