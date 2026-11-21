package ch.njol.skript.aliases;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Unit;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.iterator.EmptyIterable;
import ch.njol.util.coll.iterator.SingleItemIterable;
import ch.njol.yggdrasil.FieldHandler;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.common.collect.Iterators;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Container.ContainerType(ItemStack.class)
public class ItemType implements Unit, Iterable<ItemData>, Container<ItemStack>, YggdrasilSerializable.YggdrasilExtendedSerializable, AnyNamed, AnyAmount {
   private static final boolean IS_RUNNING_1_21_2 = Skript.isRunningMinecraft(1, 21, 2);
   final ArrayList<ItemData> types = new ArrayList<>(2);
   private boolean all = false;
   private int amount = -1;
   @Nullable
   private ItemType item = null;
   @Nullable
   private ItemType block = null;
   @Nullable
   private ItemMeta globalMeta;
   private static final boolean ITEMMETA_CUSTOMNAME_EXISTS = Skript.methodExists(ItemMeta.class, "customName");
   private static final Random random = new Random();

   void setItem(@Nullable ItemType item) {
      if (this.equals(item)) {
         this.item = null;
      } else {
         if (item != null) {
            if (item.item != null || item.block != null) {
               assert false : this + "; item=" + item + ", item.item=" + item.item + ", item.block=" + item.block;

               this.item = null;
               return;
            }

            item.setAmount(this.amount);
         }

         this.item = item;
      }
   }

   void setBlock(@Nullable ItemType block) {
      if (this.equals(block)) {
         this.block = null;
      } else {
         if (block != null) {
            if (block.item != null || block.block != null) {
               assert false : this + "; block=" + block + ", block.item=" + block.item + ", block.block=" + block.block;

               this.block = null;
               return;
            }

            block.setAmount(this.amount);
         }

         this.block = block;
      }
   }

   public ItemType() {
   }

   public ItemType(Material id) {
      this.add_(new ItemData(id));
   }

   public ItemType(Material... ids) {
      for (Material id : ids) {
         this.add_(new ItemData(id));
      }
   }

   public ItemType(Tag<Material> tag) {
      for (Material id : tag.getValues()) {
         this.add_(new ItemData(id));
      }
   }

   public ItemType(Material id, String tags) {
      this.add_(new ItemData(id, tags));
   }

   public ItemType(ItemData d) {
      this.add_(d.clone());
   }

   public ItemType(ItemStack i) {
      this.amount = i.getAmount();
      this.add_(new ItemData(i));
   }

   @Deprecated(
      since = "2.8.4",
      forRemoval = true
   )
   public ItemType(BlockState blockState) {
      this(blockState.getBlockData());
   }

   public ItemType(BlockData blockData) {
      this.add_(new ItemData(blockData));
   }

   private ItemType(ItemType i) {
      this.setTo(i);
   }

   public void setTo(ItemType i) {
      this.all = i.all;
      this.amount = i.amount;
      ItemType bl = i.block;
      ItemType it = i.item;
      this.block = bl == null ? null : bl.clone();
      this.item = it == null ? null : it.clone();
      this.types.clear();

      for (ItemData d : i) {
         this.types.add(d.clone());
      }
   }

   public ItemType(Block block) {
      this(block.getBlockData());
   }

   public void modified() {
      this.item = this.block = null;
   }

   @Override
   public int getAmount() {
      return Math.abs(this.amount);
   }

   public int getInternalAmount() {
      return this.amount;
   }

   @Override
   public void setAmount(double amount) {
      this.setAmount((int)amount);
   }

   public void setAmount(int amount) {
      this.amount = amount;
      if (this.item != null) {
         this.item.amount = amount;
      }

      if (this.block != null) {
         this.block.amount = amount;
      }
   }

   public boolean isAll() {
      return this.all;
   }

   public void setAll(boolean all) {
      this.all = all;
   }

   public boolean isOfType(@Nullable ItemStack item) {
      return item == null ? this.isOfType(Material.AIR, null) : this.isOfType(new ItemData(item));
   }

   @Deprecated(
      since = "2.8.4",
      forRemoval = true
   )
   public boolean isOfType(@Nullable BlockState blockState) {
      return blockState != null && this.isOfType(blockState.getBlockData());
   }

   public boolean isOfType(@Nullable BlockData blockData) {
      return blockData == null ? this.isOfType(Material.AIR, null) : this.isOfType(new ItemData(blockData));
   }

   public boolean isOfType(@Nullable Block block) {
      return block == null ? this.isOfType(Material.AIR, null) : this.isOfType(block.getBlockData());
   }

   public boolean isOfType(ItemData type) {
      for (ItemData myType : this.types) {
         if (myType.equals(type)) {
            return true;
         }
      }

      return false;
   }

   public boolean isOfType(Material id, @Nullable String tags) {
      return this.isOfType(new ItemData(id, tags));
   }

   public boolean isOfType(Material id) {
      return this.isOfType(new ItemData(id, (String)null));
   }

   public boolean isSupertypeOf(ItemType other) {
      return this.types.containsAll(other.types);
   }

   public ItemType getItem() {
      ItemType item = this.item;
      return item == null ? this : item;
   }

   public ItemType getBlock() {
      ItemType block = this.block;
      return block == null ? this : block;
   }

   public boolean hasItem() {
      for (ItemData d : this.types) {
         if (d.type.isItem()) {
            return true;
         }
      }

      return false;
   }

   public boolean hasBlock() {
      for (ItemData d : this.types) {
         if (d.type.isBlock()) {
            return true;
         }
      }

      return false;
   }

   public boolean hasType() {
      return !this.types.isEmpty();
   }

   public boolean setBlock(Block block, boolean applyPhysics) {
      int i = random.nextInt(this.types.size());

      ItemMeta itemMeta;
      while (true) {
         if (i >= this.types.size()) {
            return false;
         }

         ItemData data = this.types.get(i);
         Material blockType = ItemUtils.asBlock(data.type);
         if (blockType != null && BlockUtils.set(block, blockType, data.getBlockValues(), applyPhysics)) {
            itemMeta = this.getItemMeta();
            if (!(itemMeta instanceof SkullMeta skullMeta)) {
               break;
            }

            OfflinePlayer offlinePlayer = skullMeta.getOwningPlayer();
            if (offlinePlayer != null) {
               Skull skull = (Skull)block.getState();
               if (offlinePlayer.getName() != null) {
                  skull.setOwningPlayer(offlinePlayer);
               } else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
                  skull.setOwnerProfile(Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), ""));
               } else {
                  skull.setOwner("");
               }

               skull.update(false, applyPhysics);
               break;
            }
         }

         i++;
      }

      this.copyContainerState(block, itemMeta);
      return true;
   }

   private void copyContainerState(@NotNull Block block, @NotNull ItemMeta itemMeta) {
      if (block.getState() instanceof org.bukkit.block.Container blockContainer) {
         if (ITEMMETA_CUSTOMNAME_EXISTS) {
            if (itemMeta.hasCustomName()) {
               blockContainer.customName(itemMeta.customName());
               blockContainer.update();
            }
         } else if (itemMeta.hasDisplayName()) {
            blockContainer.customName(itemMeta.displayName());
            blockContainer.update();
         }

         if (itemMeta instanceof BlockStateMeta blockStateMeta && blockStateMeta.hasBlockState()) {
            if (blockStateMeta.getBlockState() instanceof org.bukkit.block.Container itemContainer) {
               this.copyInventories(itemContainer.getSnapshotInventory(), blockContainer.getSnapshotInventory());
               blockContainer.update();
            }
         }
      }
   }

   private void copyInventories(@NotNull Inventory from, @NotNull Inventory to) {
      for (int i = 0; i < from.getSize(); i++) {
         ItemStack item = from.getItem(i);
         if (item != null) {
            to.setItem(i, item.clone());
         }
      }
   }

   public void sendBlockChange(Player player, Location location) {
      for (int i = random.nextInt(this.types.size()); i < this.types.size(); i++) {
         ItemData d = this.types.get(i);
         Material blockType = ItemUtils.asBlock(d.type);
         if (blockType != null) {
            BlockUtils.sendBlockChange(player, location, blockType, d.getBlockValues());
         }
      }
   }

   @Nullable
   public ItemType intersection(ItemType other) {
      ItemType r = new ItemType();

      for (ItemData d1 : this.types) {
         for (ItemData d2 : other.types) {
            assert d2 != null;

            r.add_(d1.intersection(d2));
         }
      }

      return r.types.isEmpty() ? null : r;
   }

   public void add(@Nullable ItemData type) {
      if (type != null) {
         this.add_(type.clone());
      }
   }

   private void add_(@Nullable ItemData type) {
      if (type != null) {
         this.types.add(type);
         this.modified();
      }
   }

   public void addAll(Collection<ItemData> types) {
      this.types.addAll(types);
      this.modified();
   }

   public void remove(ItemData type) {
      if (this.types.remove(type)) {
         this.modified();
      }
   }

   void remove(int index) {
      this.types.remove(index);
      this.modified();
   }

   @Override
   public Iterator<ItemStack> containerIterator() {
      return new Iterator<ItemStack>() {
         final Iterator<ItemData> iter = ItemType.this.types.iterator();
         ItemStack nextItem = null;

         @Override
         public boolean hasNext() {
            while (this.nextItem == null && this.iter.hasNext()) {
               ItemData data = this.iter.next();
               ItemStack is = data.getStack();
               if (is != null) {
                  this.nextItem = is.clone();
                  this.nextItem.setAmount(ItemType.this.getAmount());
               }
            }

            return this.nextItem != null;
         }

         public ItemStack next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               ItemStack result = this.nextItem;
               this.nextItem = null;
               return result;
            }
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   public Iterable<ItemStack> getAll() {
      if (!this.isAll()) {
         ItemStack i = this.getRandom();
         return (Iterable<ItemStack>)(i == null ? EmptyIterable.get() : new SingleItemIterable<>(i));
      } else {
         return this::containerIterator;
      }
   }

   public boolean satisfies(Predicate<ItemStack> predicate) {
      if (this.isAll()) {
         Iterator<ItemStack> it = this.containerIterator();

         while (it.hasNext()) {
            ItemStack stack = it.next();
            if (!predicate.test(stack)) {
               return false;
            }
         }

         return true;
      } else {
         Iterator<ItemStack> it = this.containerIterator();

         while (it.hasNext()) {
            ItemStack stack = it.next();
            if (predicate.test(stack)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   public ItemStack removeAll(@Nullable ItemStack item) {
      boolean wasAll = this.all;
      int oldAmount = this.amount;
      this.all = true;
      this.amount = -1;

      ItemStack var4;
      try {
         var4 = this.removeFrom(item);
      } finally {
         this.all = wasAll;
         this.amount = oldAmount;
      }

      return var4;
   }

   @Nullable
   public ItemStack removeFrom(@Nullable ItemStack item) {
      if (item == null) {
         return null;
      } else if (!this.isOfType(item)) {
         return item;
      } else if (this.all) {
         return null;
      } else {
         int a = item.getAmount() - this.getAmount();
         if (a <= 0) {
            return null;
         } else {
            item.setAmount(a);
            return item;
         }
      }
   }

   @Nullable
   public ItemStack addTo(@Nullable ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         if (this.isOfType(item)) {
            item.setAmount(Math.min(item.getAmount() + this.getAmount(), item.getMaxStackSize()));
         }

         return item;
      } else {
         return this.getRandom();
      }
   }

   public ItemType clone() {
      return new ItemType(this);
   }

   @Nullable
   public ItemStack getRandom() {
      List<ItemData> datas = this.types.stream().filter(data -> data.stack != null).collect(Collectors.toList());
      if (datas.isEmpty()) {
         return null;
      } else {
         ItemStack is = datas.get(random.nextInt(datas.size())).getStack();

         assert is != null;

         is = is.clone();
         is.setAmount(this.getAmount());
         return is;
      }
   }

   public Object getRandomStackOrMaterial() {
      ItemData randomData = this.types.get(random.nextInt(this.types.size()));
      ItemStack stack = randomData.getStack();
      if (stack == null) {
         return randomData.getType();
      } else {
         stack = stack.clone();
         stack.setAmount(this.getAmount());
         return stack;
      }
   }

   public boolean hasSpace(Inventory invi) {
      return !this.isAll() && this.getItem().types.size() != 1 ? false : this.addTo(getStorageContents(invi));
   }

   public static ItemStack[] getCopiedContents(Inventory invi) {
      ItemStack[] buf = invi.getContents();

      for (int i = 0; i < buf.length; i++) {
         if (buf[i] != null) {
            buf[i] = buf[i].clone();
         }
      }

      return buf;
   }

   public static ItemStack[] getStorageContents(Inventory inventory) {
      ItemStack[] buf = inventory.getStorageContents();

      for (int i = 0; i < buf.length; i++) {
         if (buf[i] != null) {
            buf[i] = buf[i].clone();
         }
      }

      return buf;
   }

   public List<ItemData> getTypes() {
      return Collections.unmodifiableList(this.types);
   }

   public int numTypes() {
      return this.types.size();
   }

   public int numItems() {
      return this.types.size();
   }

   @Override
   public Iterator<ItemData> iterator() {
      return new Iterator<ItemData>() {
         private int next = 0;

         @Override
         public boolean hasNext() {
            return this.next < ItemType.this.types.size();
         }

         public ItemData next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return ItemType.this.types.get(this.next++);
            }
         }

         @Override
         public void remove() {
            if (this.next <= 0) {
               throw new IllegalStateException();
            } else {
               ItemType.this.remove(--this.next);
            }
         }
      };
   }

   public boolean isContainedIn(Iterable<ItemStack> items) {
      int needed = this.getAmount();
      int found = 0;

      for (ItemStack item : items) {
         if (item != null && new ItemType(item).isSimilar(this)) {
            found += item.getAmount();
            if (found >= needed) {
               if (!this.all) {
                  return true;
               }
               break;
            }
         }
      }

      return this.all && found < this.amount ? false : this.all;
   }

   public boolean isContainedIn(ItemStack[] items) {
      int needed = this.getAmount();
      int found = 0;

      for (ItemStack item : items) {
         if (item != null && new ItemType(item).isSimilar(this)) {
            found += item.getAmount();
            if (found >= needed) {
               if (!this.all) {
                  return true;
               }
               break;
            }
         }
      }

      return this.all && found < this.amount ? false : this.all;
   }

   public boolean removeAll(Inventory invi) {
      boolean wasAll = this.all;
      int oldAmount = this.amount;
      this.all = true;
      this.amount = -1;

      boolean var4;
      try {
         var4 = this.removeFrom(invi);
      } finally {
         this.all = wasAll;
         this.amount = oldAmount;
      }

      return var4;
   }

   public boolean removeFrom(Inventory invi) {
      ItemStack[] buf = getCopiedContents(invi);
      boolean ok = this.removeFrom(Arrays.asList(buf));
      invi.setContents(buf);
      return ok;
   }

   @SafeVarargs
   public final boolean removeAll(List<ItemStack>... lists) {
      return this.removeAll(true, lists);
   }

   @SafeVarargs
   public final boolean removeAll(boolean replaceWithNull, List<ItemStack>... lists) {
      boolean wasAll = this.all;
      int oldAmount = this.amount;
      this.all = true;
      this.amount = -1;

      boolean var5;
      try {
         var5 = this.removeFrom(replaceWithNull, lists);
      } finally {
         this.all = wasAll;
         this.amount = oldAmount;
      }

      return var5;
   }

   @SafeVarargs
   public final boolean removeFrom(List<ItemStack>... lists) {
      return this.removeFrom(true, lists);
   }

   @SafeVarargs
   public final boolean removeFrom(boolean replaceWithNull, List<ItemStack>... lists) {
      int removed = 0;
      boolean ok = true;

      for (ItemData d : this.types) {
         if (this.all) {
            removed = 0;
         }

         for (List<ItemStack> list : lists) {
            if (list != null) {
               assert list instanceof RandomAccess;

               Iterator<ItemStack> listIterator = list.iterator();
               int index = -1;

               while (listIterator.hasNext()) {
                  ItemStack is = listIterator.next();
                  if (replaceWithNull) {
                     index++;
                  }

                  ItemData other = is != null ? new ItemData(is) : null;
                  if (other != null) {
                     boolean plain = d.isPlain() != other.isPlain();
                     if (d.matchPlain(other)
                        || other.matchAlias(d)
                           .isAtLeast(plain ? MatchQuality.EXACT : (d.isAlias() && !other.isAlias() ? MatchQuality.SAME_MATERIAL : MatchQuality.SAME_ITEM))) {
                        if (this.all && this.amount == -1) {
                           if (replaceWithNull) {
                              list.set(index, null);
                           } else {
                              listIterator.remove();
                           }

                           removed = 1;
                        } else {
                           int toRemove = Math.min(is.getAmount(), this.getAmount() - removed);
                           removed += toRemove;
                           if (toRemove == is.getAmount()) {
                              if (replaceWithNull) {
                                 list.set(index, null);
                              } else {
                                 listIterator.remove();
                              }
                           } else {
                              is.setAmount(is.getAmount() - toRemove);
                           }

                           if (removed == this.getAmount()) {
                              if (!this.all) {
                                 return true;
                              }
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (this.all) {
            ok &= removed == this.getAmount();
         }
      }

      return !this.all ? false : ok;
   }

   public void addTo(List<ItemStack> list) {
      if (!this.isAll()) {
         ItemStack random = this.getItem().getRandom();
         if (random != null) {
            list.add(this.getItem().getRandom());
         }
      } else {
         for (ItemStack is : this.getItem().getAll()) {
            list.add(is);
         }
      }
   }

   public boolean addTo(Inventory inventory) {
      if (!IS_RUNNING_1_21_2) {
         ItemStack[] buf = inventory.getContents();
         ItemStack[] tBuf = (ItemStack[])buf.clone();
         if (inventory instanceof PlayerInventory) {
            buf = new ItemStack[36];

            for (int i = 0; i < 36; i++) {
               buf[i] = tBuf[i];
            }
         }

         boolean b = this.addTo(buf);
         if (inventory instanceof PlayerInventory) {
            buf = Arrays.copyOf(buf, tBuf.length);

            for (int i = tBuf.length - 5; i < tBuf.length; i++) {
               buf[i] = tBuf[i];
            }
         }

         assert buf != null;

         inventory.setContents(buf);
         return b;
      } else if (this.isAll()) {
         return inventory.addItem((ItemStack[])Iterators.toArray(this.getItem().getAll().iterator(), ItemStack.class)).isEmpty();
      } else {
         ItemStack random = this.getItem().getRandom();
         return random == null || inventory.addItem(new ItemStack[]{random}).isEmpty();
      }
   }

   private static boolean addTo(@Nullable ItemStack is, ItemStack[] buf) {
      if (is != null && is.getType() != Material.AIR) {
         int added = 0;

         for (int i = 0; i < buf.length; i++) {
            if (ItemUtils.itemStacksEqual(is, buf[i])) {
               int toAdd = Math.min(buf[i].getMaxStackSize() - buf[i].getAmount(), is.getAmount() - added);
               added += toAdd;
               buf[i].setAmount(buf[i].getAmount() + toAdd);
               if (added == is.getAmount()) {
                  return true;
               }
            }
         }

         for (int ix = 0; ix < buf.length; ix++) {
            if (buf[ix] == null) {
               int toAdd = Math.min(is.getMaxStackSize(), is.getAmount() - added);
               added += toAdd;
               buf[ix] = is.clone();
               buf[ix].setAmount(toAdd);
               if (added == is.getAmount()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean addTo(ItemStack[] buf) {
      if (!this.isAll()) {
         ItemStack random = this.getItem().getRandom();
         if (random != null) {
            return addTo(this.getItem().getRandom(), buf);
         }
      }

      boolean ok = true;

      for (ItemStack is : this.getItem().getAll()) {
         ok &= addTo(is, buf);
      }

      return ok;
   }

   public static boolean isSubset(ItemType[] set, ItemType[] sub) {
      label30:
      for (ItemType i : sub) {
         assert i != null;

         for (ItemType t : set) {
            if (t.isSupertypeOf(i)) {
               continue label30;
            }
         }

         return false;
      }

      return true;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof ItemType other)) {
         return false;
      } else if (this.all != other.all) {
         return false;
      } else {
         return this.amount != other.amount ? false : this.types.equals(other.types);
      }
   }

   public boolean isSimilar(ItemType other) {
      if (this.isAll() != other.isAll()) {
         return false;
      } else {
         for (ItemData myType : this.getTypes()) {
            for (ItemData otherType : other.getTypes()) {
               if (myType.matchPlain(otherType)) {
                  return true;
               }

               MatchQuality minimumQuality;
               if (myType.isPlain() != otherType.isPlain()) {
                  minimumQuality = MatchQuality.EXACT;
               } else if ((!otherType.isAlias() || myType.isAlias())
                  && (!myType.itemForm || otherType.blockValues == null || otherType.blockValues.isDefault())) {
                  minimumQuality = MatchQuality.SAME_ITEM;
               } else {
                  minimumQuality = MatchQuality.SAME_MATERIAL;
               }

               if (myType.matchAlias(otherType).isAtLeast(minimumQuality)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.all ? 1231 : 1237);
      result = 31 * result + this.amount;
      return 31 * result + this.types.hashCode();
   }

   @Override
   public String toString() {
      return this.toString(false, 0, null);
   }

   @Override
   public String toString(int flags) {
      return this.toString(false, flags, null);
   }

   public String toString(int flags, @Nullable Adjective a) {
      return this.toString(false, flags, a);
   }

   private String toString(boolean debug, int flags, @Nullable Adjective a) {
      StringBuilder b = new StringBuilder();
      boolean plural = this.amount != 1 && this.amount != -1 || (flags & 1) != 0;
      if (this.amount != -1 && this.amount != 1) {
         b.append(this.amount + " ");
      } else {
         b.append(Noun.getArticleWithSpace(this.types.get(0).getGender(), flags));
      }

      if (a != null) {
         b.append(a.toString(this.types.get(0).getGender(), flags));
      }

      for (int i = 0; i < this.types.size(); i++) {
         if (i != 0) {
            if (i == this.types.size() - 1) {
               b.append(" " + (this.isAll() ? GeneralWords.and : GeneralWords.or) + " ");
            } else {
               b.append(", ");
            }
         }

         b.append(this.types.get(i).toString(debug, plural));
      }

      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
   }

   public static String toString(ItemStack i) {
      return new ItemType(i).toString();
   }

   public static String toString(ItemStack i, int flags) {
      return new ItemType(i).toString(flags);
   }

   public static String toString(Block b, int flags) {
      return new ItemType(b).toString(flags);
   }

   public String getDebugMessage() {
      return this.toString(true, 0, null);
   }

   @Override
   public Fields serialize() throws NotSerializableException {
      return new Fields(this);
   }

   @Override
   public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
      fields.setFields(this);
      if (!this.types.isEmpty()) {
         ArrayList noGenerics = this.types;
         if (noGenerics.get(0).getClass().equals(ItemData.OldItemData.class)) {
            for (int i = 0; i < this.types.size(); i++) {
               ItemData.OldItemData old = (ItemData.OldItemData)this.types.get(i);
               Material mat = BukkitUnsafe.getMaterialFromId(old.typeid);
               if (mat == null) {
                  throw new NotSerializableException("item with id " + old.typeid + " could not be converted to new alias system");
               }

               ItemData data = new ItemData(mat);
               this.types.set(i, data);
            }
         }
      }
   }

   public List<String> getRawNames() {
      List<String> rawNames = new ArrayList<>();

      for (ItemData data : this.types) {
         assert data != null;

         String id = Aliases.getMinecraftId(data);
         if (id != null) {
            rawNames.add(id);
         }
      }

      return rawNames;
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   @Nullable
   public Map<Enchantment, Integer> getEnchantments() {
      if (this.globalMeta == null) {
         return null;
      } else {
         assert this.globalMeta != null;

         Map<Enchantment, Integer> enchants = this.globalMeta.getEnchants();
         return enchants.isEmpty() ? null : enchants;
      }
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   public void addEnchantments(Map<Enchantment, Integer> enchantments) {
      if (this.globalMeta == null) {
         this.globalMeta = ItemData.itemFactory.getItemMeta(Material.STONE);
      }

      for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
         assert this.globalMeta != null;

         this.globalMeta.addEnchant(entry.getKey(), entry.getValue(), true);
      }
   }

   @Nullable
   public EnchantmentType[] getEnchantmentTypes() {
      Set<Entry<Enchantment, Integer>> enchants = this.getItemMeta().getEnchants().entrySet();
      return enchants.stream().map(enchant -> new EnchantmentType(enchant.getKey(), enchant.getValue())).toArray(EnchantmentType[]::new);
   }

   @Nullable
   public EnchantmentType getEnchantmentType(Enchantment enchantment) {
      Set<Entry<Enchantment, Integer>> enchants = this.getItemMeta().getEnchants().entrySet();
      return enchants.stream()
         .filter(entry -> entry.getKey().equals(enchantment))
         .map(enchant -> new EnchantmentType(enchant.getKey(), enchant.getValue()))
         .findFirst()
         .orElse(null);
   }

   public boolean hasEnchantments() {
      return this.getItemMeta().hasEnchants();
   }

   public boolean hasEnchantments(Enchantment... enchantments) {
      if (!this.hasEnchantments()) {
         return false;
      } else {
         ItemMeta meta = this.getItemMeta();

         for (Enchantment enchantment : enchantments) {
            if (!meta.hasEnchant(enchantment)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean hasAnyEnchantments(Enchantment... enchantments) {
      if (!this.hasEnchantments()) {
         return false;
      } else {
         ItemMeta meta = this.getItemMeta();

         for (Enchantment enchantment : enchantments) {
            assert enchantment != null;

            if (meta.hasEnchant(enchantment)) {
               return true;
            }
         }

         return false;
      }
   }

   @Deprecated(
      since = "2.12"
   )
   public boolean hasEnchantments(EnchantmentType... enchantments) {
      return this.hasEnchantmentsOrBetter(true, enchantments);
   }

   @Deprecated(
      since = "2.12"
   )
   public boolean hasEnchantments(boolean all, EnchantmentType... enchantments) {
      return this.hasEnchantmentsOrBetter(all, enchantments);
   }

   public boolean hasEnchantmentsOrBetter(EnchantmentType... enchantments) {
      return this.hasEnchantmentsOrBetter(true, enchantments);
   }

   public boolean hasEnchantmentsOrBetter(boolean all, EnchantmentType... enchantments) {
      return this.hasEnchantments((itemLevel, typeLevel) -> itemLevel >= typeLevel, all, enchantments);
   }

   public boolean hasEnchantmentsOrWorse(EnchantmentType... enchantments) {
      return this.hasEnchantmentsOrWorse(true, enchantments);
   }

   public boolean hasEnchantmentsOrWorse(boolean all, EnchantmentType... enchantments) {
      return this.hasEnchantments((itemLevel, typeLevel) -> itemLevel <= typeLevel, all, enchantments);
   }

   public boolean hasExactEnchantments(EnchantmentType... enchantments) {
      return this.hasExactEnchantments(true, enchantments);
   }

   public boolean hasExactEnchantments(boolean all, EnchantmentType... enchantments) {
      return this.hasEnchantments(Integer::equals, all, enchantments);
   }

   private boolean hasEnchantments(BiPredicate<Integer, Integer> levelMatchingCondition, boolean all, EnchantmentType... enchantments) {
      if (!this.hasEnchantments()) {
         return false;
      } else {
         ItemMeta meta = this.getItemMeta();

         for (EnchantmentType enchantment : enchantments) {
            Enchantment type = enchantment.getType();

            assert type != null;

            if (!meta.hasEnchant(type) && all) {
               return false;
            }

            if (enchantment.getInternalLevel() != -1 && !levelMatchingCondition.test(meta.getEnchantLevel(type), enchantment.getLevel())) {
               if (all) {
                  return false;
               }
            } else if (!all) {
               return true;
            }
         }

         return all;
      }
   }

   public void addEnchantments(EnchantmentType... enchantments) {
      ItemMeta meta = this.getItemMeta();

      for (EnchantmentType enchantment : enchantments) {
         Enchantment type = enchantment.getType();

         assert type != null;

         meta.addEnchant(type, enchantment.getLevel(), true);
      }

      this.setItemMeta(meta);
   }

   public void removeEnchantments(EnchantmentType... enchantments) {
      ItemMeta meta = this.getItemMeta();

      for (EnchantmentType enchantment : enchantments) {
         Enchantment type = enchantment.getType();

         assert type != null;

         meta.removeEnchant(type);
      }

      this.setItemMeta(meta);
   }

   public void clearEnchantments() {
      ItemMeta meta = this.getItemMeta();

      for (Enchantment ench : meta.getEnchants().keySet()) {
         assert ench != null;

         meta.removeEnchant(ench);
      }

      this.setItemMeta(meta);
   }

   public ItemMeta getItemMeta() {
      return this.globalMeta != null ? this.globalMeta : this.types.get(0).getItemMeta();
   }

   public void setItemMeta(ItemMeta meta) {
      this.globalMeta = meta;

      for (ItemData data : this.types) {
         data.setItemMeta(meta);
      }
   }

   public void clearItemMeta() {
      this.globalMeta = null;
   }

   public Material getMaterial() {
      ItemData data = this.types.get(random.nextInt(this.types.size()));
      if (data == null) {
         throw new IllegalStateException("material not found");
      } else {
         return data.getType();
      }
   }

   public Material[] getMaterials() {
      Set<Material> materials = new HashSet<>();

      for (ItemData data : this.types) {
         materials.add(data.getType());
      }

      return materials.toArray(new Material[0]);
   }

   public Material getBlockMaterial() {
      List<ItemData> blockItemDatas = new ArrayList<>();

      for (ItemData d : this.types) {
         if (d.type.isBlock()) {
            blockItemDatas.add(d);
         }
      }

      if (blockItemDatas.isEmpty()) {
         throw new IllegalStateException("This ItemType does not represent a material. ItemType#hasBlock() should return true before invoking this method.");
      } else {
         return blockItemDatas.get(random.nextInt(blockItemDatas.size())).getType();
      }
   }

   public ItemType getBaseType() {
      ItemType copy = new ItemType();

      for (ItemData data : this.types) {
         copy.add_(data.getBaseCopy());
      }

      return copy;
   }

   public ItemType getPlainType() {
      ItemType copy = this.getBaseType();

      for (ItemData data : copy.types) {
         data.setPlain(true);
      }

      return copy;
   }

   @Nullable
   @Override
   public String name() {
      ItemMeta meta = this.getItemMeta();
      return meta.hasDisplayName() ? meta.getDisplayName() : null;
   }

   @UnknownNullability
   @Override
   public Component nameComponent() {
      ItemMeta meta = this.getItemMeta();
      return meta.hasDisplayName() ? meta.displayName() : null;
   }

   @Override
   public boolean supportsNameChange() {
      return true;
   }

   @Override
   public void setName(String name) {
      ItemMeta meta = this.getItemMeta();
      meta.setDisplayName(name);
      this.setItemMeta(meta);
   }

   @Override
   public void setName(Component name) {
      ItemMeta meta = this.getItemMeta();
      meta.displayName(name);
      this.setItemMeta(meta);
   }

   @NotNull
   @Override
   public Number amount() {
      return this.getAmount();
   }

   @Override
   public boolean supportsAmountChange() {
      return true;
   }

   @Override
   public void setAmount(@Nullable Number amount) {
      this.setAmount(amount != null ? amount.intValue() : 0);
   }

   static {
      Variables.yggdrasil.registerFieldHandler(new FieldHandler() {
         @Override
         public boolean missingField(Object o, Field field) throws StreamCorruptedException {
            return !(o instanceof ItemType) && !(o instanceof ItemData) ? false : field.getName().equals("globalMeta");
         }

         @Override
         public boolean incompatibleField(Object o, Field f, Fields.FieldContext field) throws StreamCorruptedException {
            return false;
         }

         @Override
         public boolean excessiveField(Object o, Fields.FieldContext field) throws StreamCorruptedException {
            if (!(o instanceof ItemType) && !(o instanceof ItemData)) {
               return false;
            } else {
               String id = field.getID();
               return id.equals("meta") || id.equals("enchantments") || id.equals("ignoreMeta") || id.equals("numItems");
            }
         }
      });
   }
}
