package com.viaversion.viaversion.libs.mcstructs.text.events.click.types;

import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import java.net.URI;
import lombok.Generated;

public class OpenUrlClickEvent extends ClickEvent {
   private OpenUrlClickEvent.UrlHolder url;

   public OpenUrlClickEvent(String url) {
      super(ClickEventAction.OPEN_URL);

      try {
         this.url = new OpenUrlClickEvent.UriHolder(new URI(url));
      } catch (Throwable var3) {
         this.url = new OpenUrlClickEvent.StringHolder(url);
      }
   }

   public OpenUrlClickEvent(URI url) {
      super(ClickEventAction.OPEN_URL);
      this.url = new OpenUrlClickEvent.UriHolder(url);
   }

   public OpenUrlClickEvent.UrlHolder getHolder() {
      return this.url;
   }

   public OpenUrlClickEvent setHolder(OpenUrlClickEvent.UrlHolder holder) {
      this.url = holder;
      return this;
   }

   public String asString() {
      return this.url instanceof OpenUrlClickEvent.StringHolder
         ? ((OpenUrlClickEvent.StringHolder)this.url).getUrl()
         : ((OpenUrlClickEvent.UriHolder)this.url).getUri().toString();
   }

   public URI asUri() throws IllegalArgumentException {
      return this.url instanceof OpenUrlClickEvent.StringHolder
         ? URI.create(((OpenUrlClickEvent.StringHolder)this.url).getUrl())
         : ((OpenUrlClickEvent.UriHolder)this.url).getUri();
   }

   public OpenUrlClickEvent setUrl(String url) {
      this.url = new OpenUrlClickEvent.StringHolder(url);
      return this;
   }

   public OpenUrlClickEvent setUrl(URI url) {
      this.url = new OpenUrlClickEvent.UriHolder(url);
      return this;
   }

   @Override
   public String toString() {
      return ToString.of(this).add("action", this.action).add("url", this.url).toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof OpenUrlClickEvent)) {
         return false;
      } else {
         OpenUrlClickEvent other = (OpenUrlClickEvent)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$url = this.url;
            Object other$url = other.url;
            return this$url == null ? other$url == null : this$url.equals(other$url);
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof OpenUrlClickEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $url = this.url;
      return result * 59 + ($url == null ? 43 : $url.hashCode());
   }

   public static class StringHolder implements OpenUrlClickEvent.UrlHolder {
      private final String url;

      @Override
      public String toString() {
         return this.url;
      }

      @Generated
      public String getUrl() {
         return this.url;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof OpenUrlClickEvent.StringHolder)) {
            return false;
         } else {
            OpenUrlClickEvent.StringHolder other = (OpenUrlClickEvent.StringHolder)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$url = this.getUrl();
               Object other$url = other.getUrl();
               return this$url == null ? other$url == null : this$url.equals(other$url);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof OpenUrlClickEvent.StringHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $url = this.getUrl();
         return result * 59 + ($url == null ? 43 : $url.hashCode());
      }

      @Generated
      public StringHolder(String url) {
         this.url = url;
      }
   }

   public static class UriHolder implements OpenUrlClickEvent.UrlHolder {
      private final URI uri;

      @Override
      public String toString() {
         return this.uri.toString();
      }

      @Generated
      public URI getUri() {
         return this.uri;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof OpenUrlClickEvent.UriHolder)) {
            return false;
         } else {
            OpenUrlClickEvent.UriHolder other = (OpenUrlClickEvent.UriHolder)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$uri = this.getUri();
               Object other$uri = other.getUri();
               return this$uri == null ? other$uri == null : this$uri.equals(other$uri);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof OpenUrlClickEvent.UriHolder;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $uri = this.getUri();
         return result * 59 + ($uri == null ? 43 : $uri.hashCode());
      }

      @Generated
      public UriHolder(URI uri) {
         this.uri = uri;
      }
   }

   public interface UrlHolder {
   }
}
