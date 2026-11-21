package de.theredend2000.advancedhunt.lib.kyori.adventure.resource;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.MonkeyBars;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

record ResourcePackRequestImpl(List<ResourcePackInfo> packs, ResourcePackCallback callback, boolean replace, boolean required, @Nullable Component prompt)
   implements ResourcePackRequest {
   @Override
   public ResourcePackRequest packs(final Iterable<? extends ResourcePackInfoLike> packs) {
      return this.packs.equals(packs)
         ? this
         : new ResourcePackRequestImpl(
            MonkeyBars.toUnmodifiableList(ResourcePackInfoLike::asResourcePackInfo, packs), this.callback, this.replace, this.required, this.prompt
         );
   }

   @Override
   public ResourcePackRequest callback(final ResourcePackCallback cb) {
      return cb == this.callback ? this : new ResourcePackRequestImpl(this.packs, Objects.requireNonNull(cb, "cb"), this.replace, this.required, this.prompt);
   }

   @Override
   public ResourcePackRequest replace(final boolean replace) {
      return replace == this.replace ? this : new ResourcePackRequestImpl(this.packs, this.callback, replace, this.required, this.prompt);
   }

   static final class BuilderImpl implements ResourcePackRequest.Builder {
      private List<ResourcePackInfo> packs;
      private ResourcePackCallback cb;
      private boolean replace;
      private boolean required;
      @Nullable
      private Component prompt;

      BuilderImpl() {
         this.packs = List.of();
         this.cb = ResourcePackCallback.noOp();
         this.replace = false;
      }

      BuilderImpl(final ResourcePackRequest req) {
         this.packs = req.packs();
         this.cb = req.callback();
         this.replace = req.replace();
         this.required = req.required();
         this.prompt = req.prompt();
      }

      @Override
      public ResourcePackRequest.Builder packs(final ResourcePackInfoLike first, final ResourcePackInfoLike... others) {
         this.packs = MonkeyBars.nonEmptyArrayToList(ResourcePackInfoLike::asResourcePackInfo, first, others);
         return this;
      }

      @Override
      public ResourcePackRequest.Builder packs(final Iterable<? extends ResourcePackInfoLike> packs) {
         this.packs = MonkeyBars.toUnmodifiableList(ResourcePackInfoLike::asResourcePackInfo, packs);
         return this;
      }

      @Override
      public ResourcePackRequest.Builder callback(final ResourcePackCallback cb) {
         this.cb = Objects.requireNonNull(cb, "cb");
         return this;
      }

      @Override
      public ResourcePackRequest.Builder replace(final boolean replace) {
         this.replace = replace;
         return this;
      }

      @Override
      public ResourcePackRequest.Builder required(final boolean required) {
         this.required = required;
         return this;
      }

      @Override
      public ResourcePackRequest.Builder prompt(@Nullable final Component prompt) {
         this.prompt = prompt;
         return this;
      }

      public ResourcePackRequest build() {
         return new ResourcePackRequestImpl(this.packs, this.cb, this.replace, this.required, this.prompt);
      }
   }
}
