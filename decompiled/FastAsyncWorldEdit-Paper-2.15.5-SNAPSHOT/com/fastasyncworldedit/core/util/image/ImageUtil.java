package com.fastasyncworldedit.core.util.image;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extension.platform.binding.ProvideBindings;
import com.fastasyncworldedit.core.util.MainUtil;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.annotation.Nullable;

public class ImageUtil {
   public static BufferedImage getScaledInstance(BufferedImage image, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
      if (image.getHeight() == targetHeight && image.getWidth() == targetWidth) {
         return image;
      } else {
         int type = image.getTransparency() == 1 ? 1 : 2;
         BufferedImage scaledImage = image;
         int width;
         int height;
         if (higherQuality) {
            width = image.getWidth();
            height = image.getHeight();
         } else {
            width = targetWidth;
            height = targetHeight;
         }

         BufferedImage tmp;
         do {
            if (higherQuality && width > targetWidth) {
               width /= 2;
            }

            if (width < targetWidth) {
               width = targetWidth;
            }

            if (higherQuality && height > targetHeight) {
               height /= 2;
            }

            if (height < targetHeight) {
               height = targetHeight;
            }

            tmp = new BufferedImage(width, height, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g2.drawImage(scaledImage, 0, 0, width, height, null);
            g2.dispose();
            scaledImage = tmp;
         } while (width != targetWidth || height != targetHeight);

         return tmp;
      }
   }

   public static void fadeAlpha(BufferedImage image) {
      int[] raw = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
      int width = image.getWidth();
      int height = image.getHeight();
      int centerX = width / 2;
      int centerZ = height / 2;
      float invRadiusX = 1.0F / centerX;
      float invRadiusZ = 1.0F / centerZ;
      float[] sqrX = new float[width];
      float[] sqrZ = new float[height];

      for (int x = 0; x < width; x++) {
         float distance = Math.abs(x - centerX) * invRadiusX;
         sqrX[x] = distance * distance;
      }

      for (int z = 0; z < height; z++) {
         float distance = Math.abs(z - centerZ) * invRadiusZ;
         sqrZ[z] = distance * distance;
      }

      int z = 0;

      for (int index = 0; z < height; z++) {
         float dz2 = sqrZ[z];

         for (int x = 0; x < width; index++) {
            int color = raw[index];
            int alpha = color >> 24 & 0xFF;
            if (alpha != 0) {
               float dx2 = sqrX[x];
               float distSqr = dz2 + dx2;
               if (distSqr > 1.0F) {
                  raw[index] = 0;
               } else {
                  alpha = (int)(alpha * (1.0F - distSqr));
                  raw[index] = (color & 16777215) + (alpha << 24);
               }
            }

            x++;
         }
      }
   }

   public static void scaleAlpha(BufferedImage image, double alphaScale) {
      int[] raw = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
      int defined = Math.clamp((long)((int)(255.0 * alphaScale)), 0, 255) << 24;

      for (int i = 0; i < raw.length; i++) {
         int color = raw[i];
         int alpha = color >> 24 & 0xFF;
         switch (alpha) {
            case 0:
               break;
            case 255:
               raw[i] = (color & 16777215) + defined;
               break;
            default:
               alpha = Math.clamp((long)((int)(alpha * alphaScale)), 0, 255);
               raw[i] = (color & 16777215) + (alpha << 24);
         }
      }
   }

   public static int getColor(BufferedImage image) {
      int width = image.getWidth();
      int height = image.getHeight();
      long totalRed = 0L;
      long totalGreen = 0L;
      long totalBlue = 0L;
      long totalAlpha = 0L;

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            int color = image.getRGB(x, y);
            totalRed += color >> 16 & 0xFF;
            totalGreen += color >> 8 & 0xFF;
            totalBlue += color >> 0 & 0xFF;
            totalAlpha += color >> 24 & 0xFF;
         }
      }

      int a = width * height;
      int red = (int)(totalRed / a);
      int green = (int)(totalGreen / a);
      int blue = (int)(totalBlue / a);
      int alpha = (int)(totalAlpha / a);
      return (alpha << 24) + (red << 16) + (green << 8) + (blue << 0);
   }

   public static BufferedImage load(@Nullable ProvideBindings.ImageUri uri) throws InputParseException {
      return uri == null ? null : uri.load();
   }

   public static BufferedImage load(URI uri) throws InputParseException {
      try {
         BufferedImage var2;
         try (InputStream stream = getInputStream(uri)) {
            var2 = MainUtil.readImage(stream);
         }

         return var2;
      } catch (IOException var6) {
         throw new InputParseException(TextComponent.of(var6.getMessage()));
      }
   }

   public static InputStream getInputStream(URI uri) throws InputParseException {
      try {
         String uriStr = uri.toString();
         if (uriStr.startsWith("file:/")) {
            File file = new File(uri.getPath());
            return new FileInputStream(file);
         } else {
            return MainUtil.readImageStream(uri);
         }
      } catch (IOException var3) {
         throw new InputParseException(TextComponent.of(var3.getMessage()));
      }
   }

   public static BufferedImage getImage(String arg) throws InputParseException {
      try {
         if (arg.startsWith("http")) {
            if (arg.contains("imgur.com") && !arg.contains("i.imgur.com")) {
               arg = "https://i.imgur.com/" + arg.split("imgur.com/")[1] + ".png";
            }

            URL url = new URL(arg);
            MainUtil.checkImageHost(url.toURI());
            BufferedImage img = MainUtil.readImage(url);
            if (img == null) {
               throw new IOException("Failed to read " + url + ", please try again later");
            } else {
               return img;
            }
         } else if (arg.startsWith("file:/")) {
            arg = arg.replaceFirst("file:/+", "");
            File file = MainUtil.getFile(MainUtil.getFile(Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HEIGHTMAP), arg);
            return MainUtil.readImage(file);
         } else {
            throw new InputParseException(Caption.of("fawe.error.invalid-image", TextComponent.of(arg)));
         }
      } catch (URISyntaxException | IOException var3) {
         throw new InputParseException(TextComponent.of(var3.getMessage()));
      }
   }

   public static URI getImageURI(String arg) throws InputParseException {
      try {
         if (arg.startsWith("http")) {
            if (arg.contains("imgur.com") && !arg.contains("i.imgur.com")) {
               arg = "https://i.imgur.com/" + arg.split("imgur.com/")[1] + ".png";
            }

            URI uri = new URI(arg);
            MainUtil.checkImageHost(uri);
            return uri;
         } else if (arg.startsWith("file:/")) {
            arg = arg.replaceFirst("file:/+", "");
            File file = MainUtil.getFile(MainUtil.getFile(Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HEIGHTMAP), arg);
            if (!file.exists()) {
               throw new InputParseException(Caption.of("fawe.error.file-not-found", TextComponent.of(String.valueOf(file))));
            } else if (file.isDirectory()) {
               throw new InputParseException(Caption.of("fawe.error.file-is-invalid-directory", TextComponent.of(String.valueOf(file))));
            } else {
               return file.toURI();
            }
         } else {
            throw new InputParseException(Caption.of("fawe.error.invalid-image", TextComponent.of(arg)));
         }
      } catch (URISyntaxException | IOException var2) {
         throw new InputParseException(TextComponent.of(var2.getMessage()));
      }
   }
}
