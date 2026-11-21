package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import lombok.Generated;

public class ChangePageClickEvent extends ClickEvent {
   private ChangePageClickEvent.PageHolder page;

   public ChangePageClickEvent(String page) {
      super(ClickEventAction.CHANGE_PAGE);

      try {
         this.page = new ChangePageClickEvent.IntHolder(Integer.parseInt(page));
      } catch (Throwable var3) {
         this.page = new ChangePageClickEvent.StringHolder(page);
      }
   }

   public ChangePageClickEvent(int page) {
      super(ClickEventAction.CHANGE_PAGE);
      this.page = new ChangePageClickEvent.IntHolder(page);
   }

   public ChangePageClickEvent.PageHolder getHolder() {
      return this.page;
   }

   public String asString() {
      return this.page instanceof ChangePageClickEvent.StringHolder
         ? ((ChangePageClickEvent.StringHolder)this.page).getPage()
         : String.valueOf(((ChangePageClickEvent.IntHolder)this.page).getPage());
   }

   public int asInt() throws NumberFormatException {
      return this.page instanceof ChangePageClickEvent.StringHolder
         ? Integer.parseInt(((ChangePageClickEvent.StringHolder)this.page).getPage())
         : ((ChangePageClickEvent.IntHolder)this.page).getPage();
   }

   public ChangePageClickEvent setPage(String page) {
      this.page = new ChangePageClickEvent.StringHolder(page);
      return this;
   }

   public ChangePageClickEvent setPage(int page) {
      this.page = new ChangePageClickEvent.IntHolder(page);
      return this;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("page", this.page).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ChangePageClickEvent)) {
         return false;
      } else {
         ChangePageClickEvent other = (ChangePageClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$page = this.page;
            Object other$page = other.page;
            return this$page == null ? other$page == null : this$page.equals(other$page);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ChangePageClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $page = this.page;
      return result * 59 + ($page == null ? 43 : $page.hashCode());
   }

   public static class IntHolder implements ChangePageClickEvent.PageHolder {
      private int page;

      @Override
      public String toString() {
         return String.valueOf(this.page);
      }

      @Generated
      public int getPage() {
         return this.page;
      }

      @Generated
      public void setPage(int page) {
         this.page = page;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ChangePageClickEvent.IntHolder)) {
            return false;
         } else {
            ChangePageClickEvent.IntHolder other = (ChangePageClickEvent.IntHolder)o;
            return !other.canEqual(this) ? false : this.getPage() == other.getPage();
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ChangePageClickEvent.IntHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         return result * 59 + this.getPage();
      }

      @Generated
      public IntHolder(int page) {
         this.page = page;
      }
   }

   public interface PageHolder {
   }

   public static class StringHolder implements ChangePageClickEvent.PageHolder {
      private String page;

      @Override
      public String toString() {
         return this.page;
      }

      @Generated
      public String getPage() {
         return this.page;
      }

      @Generated
      public void setPage(String page) {
         this.page = page;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ChangePageClickEvent.StringHolder)) {
            return false;
         } else {
            ChangePageClickEvent.StringHolder other = (ChangePageClickEvent.StringHolder)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$page = this.getPage();
               Object other$page = other.getPage();
               return this$page == null ? other$page == null : this$page.equals(other$page);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ChangePageClickEvent.StringHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $page = this.getPage();
         return result * 59 + ($page == null ? 43 : $page.hashCode());
      }

      @Generated
      public StringHolder(String page) {
         this.page = page;
      }
   }
}
