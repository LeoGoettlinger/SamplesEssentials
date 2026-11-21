package net.kyori.adventure.resource;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface ResourcePackRequest extends ResourcePackRequestLike permits ResourcePackRequestImpl {
   static ResourcePackRequest addingRequest(final ResourcePackInfoLike first, final ResourcePackInfoLike... others) {
      return resourcePackRequest().packs(first, others).replace(false).build();
   }

   static ResourcePackRequest.Builder resourcePackRequest() {
      return new ResourcePackRequestImpl.BuilderImpl();
   }

   static ResourcePackRequest.Builder resourcePackRequest(final ResourcePackRequest existing) {
      return new ResourcePackRequestImpl.BuilderImpl(Objects.requireNonNull(existing, "existing"));
   }

   List<ResourcePackInfo> packs();

   ResourcePackRequest packs(final Iterable<? extends ResourcePackInfoLike> packs);

   ResourcePackCallback callback();

   ResourcePackRequest callback(final ResourcePackCallback cb);

   boolean replace();

   ResourcePackRequest replace(final boolean replace);

   boolean required();

   @Nullable
   Component prompt();

   @Override
   default ResourcePackRequest asResourcePackRequest() {
      return this;
   }

   public sealed interface Builder extends AbstractBuilder<ResourcePackRequest>, ResourcePackRequestLike permits ResourcePackRequestImpl.BuilderImpl {
      @Contract("_, _ -> this")
      ResourcePackRequest.Builder packs(final ResourcePackInfoLike first, final ResourcePackInfoLike... others);

      @Contract("_ -> this")
      ResourcePackRequest.Builder packs(final Iterable<? extends ResourcePackInfoLike> packs);

      @Contract("_ -> this")
      ResourcePackRequest.Builder callback(final ResourcePackCallback cb);

      @Contract("_ -> this")
      ResourcePackRequest.Builder replace(final boolean replace);

      @Contract("_ -> this")
      ResourcePackRequest.Builder required(final boolean required);

      @Contract("_ -> this")
      ResourcePackRequest.Builder prompt(@Nullable final Component prompt);

      @Override
      default ResourcePackRequest asResourcePackRequest() {
         return this.build();
      }
   }
}
