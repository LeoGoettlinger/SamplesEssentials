package com.artillexstudios.axafkzone.libs.axapi.nms.wrapper;

import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.libs.caffeine.caffeine.cache.Cache;
import com.artillexstudios.axafkzone.libs.axapi.libs.caffeine.caffeine.cache.Caffeine;
import com.artillexstudios.axafkzone.libs.axapi.nms.NMSHandlers;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import java.time.Duration;

public class WrapperRegistry {
   public static final WrapperMapper<ServerPlayerWrapper> SERVER_PLAYER = cachingMapper("server_player", FeatureFlags.SERVER_PLAYER_CACHE_SIZE.get());
   public static final WrapperMapper<ServerWrapper> SERVER = mapper("server");
   public static final WrapperMapper<WorldWrapper> WORLD = mapper("world");
   public static final WrapperMapper<WrappedItemStack> ITEM_STACK = mapper("item_stack");

   public static <T extends Wrapper<?>> WrapperMapper<T> mapper(String id) {
      return NMSHandlers.getNmsHandler().mapper(id);
   }

   public static <T extends Wrapper<?>> WrapperMapper<T> cachingMapper(String id, int maximumSize) {
      return new WrapperMapper<T>() {
         final Cache<Object, T> mapperCache = Caffeine.newBuilder().weakKeys().maximumSize(maximumSize).expireAfterAccess(Duration.ofSeconds(30L)).build();
         final WrapperMapper<T> delegate = WrapperRegistry.mapper(id);

         @Override
         public T map(Object object) {
            return (T)this.mapperCache.get(object, this.delegate::map);
         }
      };
   }
}
