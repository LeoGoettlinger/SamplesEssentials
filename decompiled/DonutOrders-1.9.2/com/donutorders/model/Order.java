package com.donutorders.model;

import com.donutorders.util.MessageHelper;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.inventory.ItemStack;

public class Order {
   private final UUID orderId;
   private final UUID buyerUUID;
   private final String buyerName;
   private final ItemStack itemTemplate;
   private final int amountRequested;
   private volatile int amountFulfilled;
   private final double pricePerItem;
   private volatile double remainingFunds;
   private final long createdAt;
   private final long expiresAt;
   private volatile OrderStatus status;
   private volatile long claimedAt;
   private volatile UUID claimedBy;
   private volatile long updatedAt;
   private final transient AtomicBoolean processingDelivery = new AtomicBoolean(false);
   private final transient AtomicBoolean processingClaim = new AtomicBoolean(false);

   public Order(
      UUID orderId,
      UUID buyerUUID,
      String buyerName,
      ItemStack itemTemplate,
      int amountRequested,
      int amountFulfilled,
      double pricePerItem,
      double remainingFunds,
      long createdAt,
      long expiresAt,
      OrderStatus status
   ) {
      this(
         orderId,
         buyerUUID,
         buyerName,
         itemTemplate,
         amountRequested,
         amountFulfilled,
         pricePerItem,
         remainingFunds,
         createdAt,
         expiresAt,
         status,
         0L,
         null,
         createdAt
      );
   }

   public Order(
      UUID orderId,
      UUID buyerUUID,
      String buyerName,
      ItemStack itemTemplate,
      int amountRequested,
      int amountFulfilled,
      double pricePerItem,
      double remainingFunds,
      long createdAt,
      long expiresAt,
      OrderStatus status,
      long claimedAt,
      UUID claimedBy
   ) {
      this(
         orderId,
         buyerUUID,
         buyerName,
         itemTemplate,
         amountRequested,
         amountFulfilled,
         pricePerItem,
         remainingFunds,
         createdAt,
         expiresAt,
         status,
         claimedAt,
         claimedBy,
         Math.max(createdAt, claimedAt)
      );
   }

   public Order(
      UUID orderId,
      UUID buyerUUID,
      String buyerName,
      ItemStack itemTemplate,
      int amountRequested,
      int amountFulfilled,
      double pricePerItem,
      double remainingFunds,
      long createdAt,
      long expiresAt,
      OrderStatus status,
      long claimedAt,
      UUID claimedBy,
      long updatedAt
   ) {
      this.orderId = orderId;
      this.buyerUUID = buyerUUID;
      this.buyerName = buyerName;
      this.itemTemplate = itemTemplate;
      this.amountRequested = amountRequested;
      this.amountFulfilled = amountFulfilled;
      this.pricePerItem = pricePerItem;
      this.remainingFunds = remainingFunds;
      this.createdAt = createdAt;
      this.expiresAt = expiresAt;
      this.status = status;
      this.claimedAt = claimedAt;
      this.claimedBy = claimedBy;
      this.updatedAt = updatedAt;
   }

   public int getAmountRemaining() {
      return this.amountRequested - this.amountFulfilled;
   }

   public boolean isFullyFulfilled() {
      return this.amountFulfilled >= this.amountRequested;
   }

   public String getFormattedExpiry() {
      return MessageHelper.formatExpiry(this.expiresAt);
   }

   public boolean tryLockDelivery() {
      return this.processingDelivery.compareAndSet(false, true);
   }

   public void unlockDelivery() {
      this.processingDelivery.set(false);
   }

   public boolean tryLockClaim() {
      return this.processingClaim.compareAndSet(false, true);
   }

   public void unlockClaim() {
      this.processingClaim.set(false);
   }

   public UUID getOrderId() {
      return this.orderId;
   }

   public UUID getBuyerUUID() {
      return this.buyerUUID;
   }

   public String getBuyerName() {
      return this.buyerName;
   }

   public ItemStack getItemTemplate() {
      return this.itemTemplate;
   }

   public int getAmountRequested() {
      return this.amountRequested;
   }

   public int getAmountFulfilled() {
      return this.amountFulfilled;
   }

   public double getPricePerItem() {
      return this.pricePerItem;
   }

   public double getRemainingFunds() {
      return this.remainingFunds;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public long getExpiresAt() {
      return this.expiresAt;
   }

   public OrderStatus getStatus() {
      return this.status;
   }

   public long getClaimedAt() {
      return this.claimedAt;
   }

   public UUID getClaimedBy() {
      return this.claimedBy;
   }

   public long getUpdatedAt() {
      return this.updatedAt;
   }

   public void setAmountFulfilled(int amountFulfilled) {
      this.amountFulfilled = amountFulfilled;
   }

   public void setRemainingFunds(double remainingFunds) {
      this.remainingFunds = remainingFunds;
   }

   public void setStatus(OrderStatus status) {
      this.status = status;
   }

   public void setClaimedAt(long claimedAt) {
      this.claimedAt = claimedAt;
   }

   public void setClaimedBy(UUID claimedBy) {
      this.claimedBy = claimedBy;
   }

   public void setUpdatedAt(long updatedAt) {
      this.updatedAt = updatedAt;
   }

   public void applyRemoteState(Order fromDb) {
      this.amountFulfilled = fromDb.amountFulfilled;
      this.remainingFunds = fromDb.remainingFunds;
      this.status = fromDb.status;
      this.claimedAt = fromDb.claimedAt;
      this.claimedBy = fromDb.claimedBy;
      this.updatedAt = fromDb.updatedAt;
   }
}
