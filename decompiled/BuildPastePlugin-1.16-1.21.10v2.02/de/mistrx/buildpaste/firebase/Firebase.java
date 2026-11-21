package de.mistrx.buildpaste.firebase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import de.mistrx.buildpaste.util.Debug;
import de.mistrx.buildpaste.util.Functions;
import de.mistrx.buildpaste.util.Variables;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.util.Vector;

public class Firebase {
   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static String sendStructureData(
      String uuid, List<Object> blockIDs, List<String> blockData, String nbt, List<Integer> size, String lookDirection, String buildname
   ) throws IOException {
      Debug.Log("Start sendStructureData");
      String responseString = "-";
      URL url;
      if (buildname == "") {
         url = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/pluginaddbuild/");
      } else {
         url = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/pluginaddbuild/" + buildname);
      }

      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/POST");
      con.setRequestProperty("Accept", "application/json");
      con.setDoOutput(true);
      String jsonInputString = "{\"uuid\": \""
         + uuid
         + "\""
         + ",\"blocks\": "
         + blockIDs
         + ",\"data\": "
         + blockData
         + ", \"size\": "
         + size
         + ",\"direction\": "
         + "\""
         + lookDirection
         + "\","
         + "\"nbt\":"
         + nbt
         + "}";
      con.setRequestProperty("Content-Length", Integer.toString(jsonInputString.length()));
      con.getOutputStream().write(jsonInputString.getBytes("UTF8"));
      int code = con.getResponseCode();
      Debug.Log("Code: " + code);
      Throwable var12 = null;
      Object var13 = null;

      try {
         BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

         try {
            StringBuilder response = new StringBuilder();
            String responseLine = null;

            while ((responseLine = br.readLine()) != null) {
               response.append(responseLine.trim());
            }

            responseString = response.toString();
         } finally {
            if (br != null) {
               br.close();
            }
         }
      } catch (Throwable var22) {
         if (var12 == null) {
            var12 = var22;
         } else if (var12 != var22) {
            var12.addSuppressed(var22);
         }

         throw var12;
      }

      Debug.Log(responseString);
      return responseString;
   }

   public static String getBuilding(String uuid, String id, boolean setBlocksOnFinish, Vector pos, String direction, String pasteModifier) {
      if (Objects.equals(Variables.GetBuildID(uuid), id)) {
         Debug.Log(Variables.GetBuildID(uuid) + " equals " + id);
         if (setBlocksOnFinish) {
            Functions.pasteCurrentBuilding(
               uuid,
               pos,
               Variables.GetBuildSize(uuid),
               Variables.GetBuildDirection(uuid),
               Variables.GetBuildIDs(uuid),
               Variables.GetBuildData(uuid),
               Variables.GetBuildNBT(uuid),
               direction,
               pasteModifier,
               false,
               false
            );
         }

         return "success";
      } else {
         HttpURLConnection httpURLConnection = null;

         try {
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/get/" + id);
            Debug.Log(obj.toString());
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            Debug.Log("Response: " + responseCode);
            if (responseCode == 200) {
               BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
               StringBuffer response = new StringBuffer();

               String inputLine;
               while ((inputLine = in.readLine()) != null) {
                  response.append(inputLine);
               }

               in.close();
               String responseString = response.toString();
               JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
               JsonElement sizeJsonElement = responseJson.get("size");
               JsonElement blockIDJsonElement = responseJson.get("blocks");
               JsonElement blockDataJsonElement = responseJson.get("data");
               JsonObject blockNBT;
               if (responseJson.has("nbt")) {
                  blockNBT = responseJson.getAsJsonObject("nbt");
               } else {
                  blockNBT = new JsonObject();
               }

               Type listType = (new TypeToken<List<Integer>>() {}).getType();
               List<Integer> sizeList = (List<Integer>)new Gson().fromJson(sizeJsonElement, listType);
               listType = (new TypeToken<List<Object>>() {}).getType();
               List<Object> blockIDList = (List<Object>)new Gson().fromJson(blockIDJsonElement, listType);
               listType = (new TypeToken<List<String>>() {}).getType();
               List<String> blockDataList = (List<String>)new Gson().fromJson(blockDataJsonElement, listType);
               Vector size = new Vector(sizeList.get(0), sizeList.get(1), sizeList.get(2));
               Debug.Log("size: " + size);
               String uploadDirection = responseJson.get("direction").toString().replaceAll("\"", "");
               List<Object> blockIDs = new ArrayList<>(blockIDList);
               List<String> blockData = new ArrayList<>(blockDataList);
               Variables.SetBuildDirection(uuid, uploadDirection);
               Variables.SetBuildSize(uuid, size);
               Variables.SetBuildIDs(uuid, blockIDs);
               Variables.SetBuildData(uuid, blockData);
               Variables.SetBuildNBT(uuid, blockNBT);
               Variables.SetBuildID(uuid, id);
               if (setBlocksOnFinish) {
                  Debug.Log("finished getting data, now pasting the building");
                  Functions.pasteCurrentBuilding(uuid, pos, size, uploadDirection, blockIDs, blockData, blockNBT, direction, pasteModifier, false, false);
               }

               return "success";
            }

            if (responseCode == 500) {
               return "error";
            }

            if (responseCode == 404) {
               return "error-404";
            }
         } catch (IOException var27) {
            var27.printStackTrace();
         }

         return "error";
      }
   }

   public static String getBuildName(String buildId) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/_api/build/" + buildId);
         httpURLConnection = (HttpURLConnection)obj.openConnection();
         httpURLConnection.setRequestMethod("GET");
         int responseCode = httpURLConnection.getResponseCode();
         if (responseCode != 200) {
            return responseCode == 404 ? null : null;
         } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }

            in.close();
            String responseString = response.toString();
            JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
            String buildName = responseJson.get("name").getAsString();
            Debug.Warning(buildName);
            return buildName;
         }
      } catch (IOException var10) {
         var10.printStackTrace();
         return null;
      }
   }

   public static Integer connectAccounts(String uuid, String email) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/verify/" + Variables.GetMcname(uuid) + "/" + uuid + "/" + email);
         httpURLConnection = (HttpURLConnection)obj.openConnection();
         httpURLConnection.setRequestMethod("GET");
         int responseCode = httpURLConnection.getResponseCode();
         if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }

            in.close();
            String var8 = response.toString();
         }

         return responseCode;
      } catch (IOException var9) {
         var9.printStackTrace();
         return 0;
      }
   }

   public static Integer connectAccounts(String uuid) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/verify/" + Variables.GetMcname(uuid) + "/" + uuid);
         httpURLConnection = (HttpURLConnection)obj.openConnection();
         httpURLConnection.setRequestMethod("GET");
         int responseCode = httpURLConnection.getResponseCode();
         if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }

            in.close();
            String var7 = response.toString();
         }

         return responseCode;
      } catch (IOException var8) {
         var8.printStackTrace();
         return 0;
      }
   }

   public static Integer setSelectedBuildingID(String uuid, String buildID) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/setselectedbuild/" + uuid + "/" + buildID);
         httpURLConnection = (HttpURLConnection)obj.openConnection();
         httpURLConnection.setRequestMethod("GET");
         int responseCode = httpURLConnection.getResponseCode();
         return responseCode == 404 ? 0 : 1;
      } catch (IOException var5) {
         var5.printStackTrace();
         return 0;
      }
   }

   public static String getSelectedBuildingID(String uuid) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/build/" + uuid);
         httpURLConnection = (HttpURLConnection)obj.openConnection();
         httpURLConnection.setRequestMethod("GET");
         int responseCode = httpURLConnection.getResponseCode();
         String responseString = "";
         if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }

            in.close();
            return response.toString();
         }

         if (responseCode == 404) {
            Debug.Log("not connected, connecting");
            int responseCode_ = connectAccounts(uuid);
            if (responseCode_ == 200) {
               Debug.Log("Connected");
               return getSelectedBuildingID(uuid);
            }

            return "error";
         }
      } catch (IOException var8) {
         var8.printStackTrace();
      }

      return null;
   }
}
