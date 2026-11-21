package de.tr7zw.nbtapi.plugin.tests.proxy;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.handler.NBTHandlers;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.wrapper.NBTProxy;
import de.tr7zw.nbtapi.wrapper.NBTTarget;
import de.tr7zw.nbtapi.wrapper.ProxyList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SimpleProxyTest implements Test {
   @Override
   public void test() throws Exception {
      ItemStack item = new ItemStack(Material.STONE);
      NBT.modify(item, SimpleProxyTest.TestInterface.class, ti -> {
         if (ti.hasKills()) {
            throw new NbtApiException("Item reported to have kills before setting data!");
         } else {
            ti.setKills(42);
            ti.addKill();
            if (!ti.hasKills()) {
               throw new NbtApiException("Item reported to not have kills after setting data!");
            } else if (!"{Kills:43}".equals(ti.toString())) {
               throw new NbtApiException("ToString returned the wrong string. " + ti.toString());
            }
         }
      });
      if (new NBTItem(item).getInteger("Kills") != 43) {
         throw new NbtApiException("The item was not modified correctly by the proxy!");
      } else {
         NBT.modify(item, SimpleProxyTest.TestInterface.class, ti -> {
            if (ti.theKillsWithADifferentMethodNameAndNoGet() != 43) {
               throw new NbtApiException("The annotation didn't work correctly!");
            } else {
               SimpleProxyTest.Statistic jumps = ti.getJumps();
               jumps.setPoints(9000);
               jumps.addPoint();
               if (ti.getJumps().getPoints() != 9001) {
                  throw new NbtApiException("The stacked proxy didn't work correctly!");
               } else {
                  ItemStack stack = new ItemStack(Material.STONE, 42);
                  ti.setItem(stack);
                  if (!stack.equals(ti.getItem())) {
                     throw new NbtApiException("The handler in the proxy didn't work correctly!");
                  }
               }
            }
         });
         NBT.modify(item, SimpleProxyTest.TestInterface.class, ti -> {
            SimpleProxyTest.Statistic a = ti.getStatistics().addCompound();
            a.setPoints(1);
            a.addPoint();
            a.addPoint();
            ti.getStatistics().addCompound();
            if (ti.getStatistics().size() != 2) {
               throw new NbtApiException("List size not as expected!");
            } else {
               ti.getStatistics().remove(1);
               if (ti.getStatistics().size() != 1) {
                  throw new NbtApiException("List size not as expected!");
               } else if (ti.getStatistics().iterator().next().getPoints() != 3) {
                  throw new NbtApiException("Points not as expected!");
               }
            }
         });
      }
   }

   public interface Statistic extends NBTProxy {
      void setPoints(int var1);

      int getPoints();

      default void addPoint() {
         this.setPoints(this.getPoints() + 1);
      }
   }

   public interface TestInterface extends NBTProxy {
      @Override
      default void init() {
         this.registerHandler(ItemStack.class, NBTHandlers.ITEM_STACK);
      }

      boolean hasKills();

      void setKills(int var1);

      int getKills();

      default void addKill() {
         this.setKills(this.getKills() + 1);
      }

      @NBTTarget(
         value = "Kills",
         type = NBTTarget.Type.GET
      )
      int theKillsWithADifferentMethodNameAndNoGet();

      SimpleProxyTest.Statistic getJumps();

      ItemStack getItem();

      void setItem(ItemStack var1);

      ProxyList<SimpleProxyTest.Statistic> getStatistics();
   }
}
