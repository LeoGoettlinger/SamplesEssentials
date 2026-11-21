package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.book.BookType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientSetRecipeBookState extends PacketWrapper<WrapperPlayClientSetRecipeBookState> {
   private BookType bookType;
   private boolean bookOpen;
   private boolean filterActive;

   public WrapperPlayClientSetRecipeBookState(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientSetRecipeBookState(BookType bookType, boolean bookOpen, boolean filterActive) {
      super(PacketType.Play.Client.SET_RECIPE_BOOK_STATE);
      this.bookType = bookType;
      this.bookOpen = bookOpen;
      this.filterActive = filterActive;
   }

   @Override
   public void read() {
      this.bookType = BookType.getById(this.readVarInt());
      this.bookOpen = this.readBoolean();
      this.filterActive = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeVarInt(this.bookType.getId());
      this.writeBoolean(this.bookOpen);
      this.writeBoolean(this.filterActive);
   }

   public void copy(WrapperPlayClientSetRecipeBookState wrapper) {
      this.bookType = wrapper.bookType;
      this.bookOpen = wrapper.bookOpen;
      this.filterActive = wrapper.filterActive;
   }

   public BookType getBookType() {
      return this.bookType;
   }

   public void setBookType(BookType bookType) {
      this.bookType = bookType;
   }

   public boolean isBookOpen() {
      return this.bookOpen;
   }

   public void setBookOpen(boolean bookOpen) {
      this.bookOpen = bookOpen;
   }

   public boolean isFilterActive() {
      return this.filterActive;
   }

   public void setFilterActive(boolean filterActive) {
      this.filterActive = filterActive;
   }
}
