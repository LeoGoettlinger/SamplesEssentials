package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.wrapper;

import com.artillexstudios.axtrade.libs.axapi.collections.RegistrationFailedException;
import com.artillexstudios.axtrade.libs.axapi.collections.Registry;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.ItemStackSerializer;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.items.WrappedItemStack;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.Wrapper;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.WrapperMapper;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.exception.UnknownWrappedException;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WrapperMapperRegistry {
   private static final Registry<String, WrapperMapper<?>> mappers = new Registry<>();
   public static final WrapperMapper<ServerPlayerWrapper> SERVER_PLAYER = register("server_player", object -> {
      if (object instanceof ServerPlayerWrapper serverPlayerWrapper) {
         return serverPlayerWrapper;
      } else if (object instanceof Player player) {
         return new ServerPlayerWrapper(player);
      } else if (object instanceof EntityPlayer serverPlayer) {
         return new ServerPlayerWrapper(serverPlayer);
      } else {
         throw new UnknownWrappedException("Could not wrap " + object + ", due to there being no mapper for it's class: " + object.getClass() + "!");
      }
   });
   public static final WrapperMapper<WorldWrapper> WORLD = register("world", object -> {
      if (object instanceof WorldWrapper worldWrapper) {
         return worldWrapper;
      } else if (object instanceof World world) {
         return new WorldWrapper(world);
      } else if (object instanceof WorldServer serverLevel) {
         return new WorldWrapper(serverLevel);
      } else {
         throw new UnknownWrappedException("Could not wrap " + object + ", due to there being no mapper for it's class: " + object.getClass() + "!");
      }
   });
   public static final WrapperMapper<ServerWrapper> SERVER = register("server", object -> ServerWrapper.INSTANCE);
   public static final WrapperMapper<WrappedItemStack> ITEM_STACK = register("item_stack", object -> {
      return switch (object) {
         case WrappedItemStack wrapped -> wrapped;
         case ItemStack itemStack -> new WrappedItemStack(itemStack);
         case String snbt -> new WrappedItemStack(ItemStackSerializer.INSTANCE.deserializeFromSnbt(snbt));
         case byte[] bytes -> new WrappedItemStack(ItemStackSerializer.INSTANCE.deserializeFromBytes(bytes));
         default -> throw new UnknownWrappedException("Could not wrap " + object + ", due to there being no mapper for it's class: " + object.getClass() + "!");
      };
   });

   public static <Z extends Wrapper<?>, T extends WrapperMapper<Z>> T register(String id, WrapperMapper<Z> mapper) {
      try {
         mappers.register(id, mapper);
      } catch (RegistrationFailedException var3) {
         LogUtils.error("An exception occurred while registering mapper {}!", id, var3);
      }

      return (T)mapper;
   }

   public static <T extends WrapperMapper<?>> T mapper(String id) {
      try {
         return (T)mappers.get(id);
      } catch (RegistrationFailedException var2) {
         LogUtils.error("Failed to find mapper {}! This is an issue with the code, and it should be reported to the developer of the plugin!", id, var2);
         return null;
      }
   }
}
