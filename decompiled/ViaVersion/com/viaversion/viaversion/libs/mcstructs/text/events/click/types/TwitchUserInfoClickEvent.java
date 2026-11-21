package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import lombok.Generated;

public class TwitchUserInfoClickEvent extends ClickEvent {
   private String user;

   public TwitchUserInfoClickEvent(String user) {
      super(ClickEventAction.TWITCH_USER_INFO);
      this.user = user;
   }

   public String getUser() {
      return this.user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("user", this.user).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof TwitchUserInfoClickEvent)) {
         return false;
      } else {
         TwitchUserInfoClickEvent other = (TwitchUserInfoClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$user = this.getUser();
            Object other$user = other.getUser();
            return this$user == null ? other$user == null : this$user.equals(other$user);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof TwitchUserInfoClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $user = this.getUser();
      return result * 59 + ($user == null ? 43 : $user.hashCode());
   }
}
