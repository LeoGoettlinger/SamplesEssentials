package de.theredend2000.advancedhunt.lib.kyori.adventure.resource;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.jetbrains.annotations.Contract;

public sealed interface ResourcePackInfo extends ResourcePackInfoLike permits ResourcePackInfoImpl {
   static ResourcePackInfo resourcePackInfo(final UUID id, final URI uri, final String hash) {
      return new ResourcePackInfoImpl(id, uri, hash);
   }

   static ResourcePackInfo.Builder resourcePackInfo() {
      return new ResourcePackInfoImpl.BuilderImpl();
   }

   UUID id();

   URI uri();

   String hash();

   @Override
   default ResourcePackInfo asResourcePackInfo() {
      return this;
   }

   public sealed interface Builder extends AbstractBuilder<ResourcePackInfo>, ResourcePackInfoLike permits ResourcePackInfoImpl.BuilderImpl {
      @Contract("_ -> this")
      ResourcePackInfo.Builder id(final UUID id);

      @Contract("_ -> this")
      ResourcePackInfo.Builder uri(final URI uri);

      @Contract("_ -> this")
      ResourcePackInfo.Builder hash(final String hash);

      ResourcePackInfo build();

      default CompletableFuture<ResourcePackInfo> computeHashAndBuild() {
         return this.computeHashAndBuild(ForkJoinPool.commonPool());
      }

      CompletableFuture<ResourcePackInfo> computeHashAndBuild(final Executor executor);

      @Override
      default ResourcePackInfo asResourcePackInfo() {
         return this.build();
      }
   }
}
