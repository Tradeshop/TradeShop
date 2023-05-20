/*
 *
 *                         Copyright (c) 2016-2023
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

package org.shanerx.tradeshop.utils.logging.transactionlogging;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.framework.events.PlayerSuccessfulTradeEvent;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.utils.logging.LoggerOutputType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionLogger {

    private final TradeShop tradeShop;
    private LoggerOutputType loggerOutputType;
    private boolean newFile = false;
    private final String localFormat;

    public TransactionLogger(TradeShop tradeShop) {
        this.tradeShop = tradeShop;

        try {
            loggerOutputType = LoggerOutputType.valueOf(Setting.OUTPUT_TYPE.getString());
        } catch (IllegalArgumentException | NullPointerException ex) {
            loggerOutputType = LoggerOutputType.TSV;
        }

        localFormat = Setting.TRANSACTION_LOG_FORMAT.getString().replaceAll("_@_", loggerOutputType.getDelimiter());
    }

    private PrintWriter buildWriter() {
        File file = new File(tradeShop.getDataFolder(), "TransactionLogs" + File.separator + "TradeshopTransactionLog-" + getFileTimeString() + "." + loggerOutputType.getFileExtension());

        try {
            Files.createParentDirs(file);

            if (!file.exists()) {
                file.createNewFile();
                newFile = true;
            }

            FileWriter fileWriter = new FileWriter(file, true);
            return new PrintWriter(fileWriter);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void logTransaction(PlayerSuccessfulTradeEvent event) {
        PrintWriter printWriter = buildWriter();
        if (printWriter != null) {
            Shop shop = event.getShop();

            if (newFile) {
                printWriter.println(localFormat.replaceAll("%", ""));
                newFile = false;
            }

            String owner = shop.getOwner().getName();

            if (owner == null)
                owner = "-Unknown-";

            printWriter.println(localFormat
                    .replaceAll("%Date", getTransactionDate())
                    .replaceAll("%Time", getTransactionTime())
                    .replaceAll("%ShopType", shop.getShopType().toString())
                    .replaceAll("%Owner", owner)
                    .replaceAll("%TradingPlayer", event.getPlayer().getName())
                    .replaceAll("%ShopLocation", shop.getShopLocationAsSL().toString())
                    .replaceAll("%World", shop.getShopLocationAsSL().getWorldName())
                    .replaceAll("%X", String.valueOf(shop.getShopLocationAsSL().getX()))
                    .replaceAll("%Y", String.valueOf(shop.getShopLocationAsSL().getY()))
                    .replaceAll("%Z", String.valueOf(shop.getShopLocationAsSL().getZ()))
                    .replaceAll("%CostList", getItemListAsString(event.getCost()))
                    .replaceAll("%ProductList", getItemListAsString(event.getProduct()))
                    .replace("}\"}", "}}")
                    .replace("\\\"", "\"")
                    .replace("}]\"}", "}]}")
                    .replace("{\"\",", "[")
            );
            printWriter.close();
        }
    }

    private String getItemListAsString(List<ItemStack> itemList) {
        try {
            final Gson gson = new GsonBuilder().create();
            JsonObject jsonObj = new JsonObject();
            for (int i = 0; i < itemList.size(); i++) {
                JsonObject temp = gson.toJsonTree(new ItemStack(itemList.get(i))).getAsJsonObject();

                // remove unneeded tags
                if (temp.has("meta")) {
                    JsonObject tempMeta = temp.getAsJsonObject("meta");
                    tempMeta.remove("placeableKeys");
                    tempMeta.remove("destroyableKeys");

                    if (tempMeta.has("persistentDataContainer")) {
                        JsonObject tempPerData = tempMeta.getAsJsonObject("persistentDataContainer");
                        tempPerData.remove("registry");
                        tempPerData.remove("adapterContext");
                    }
                }

                jsonObj.add("Item #" + i, temp);
            }

            String ret = gson.toJson(jsonObj);

            Matcher m = Pattern.compile("(/).+( [\\[{])").matcher(ret);

            while (m.find()) {
                String temp = m.group(),
                        rep = temp.replaceAll(" [\\[{]", "\", \"commandJson\":" + temp.charAt(temp.length() - 1));
                ret = ret.replace(temp, rep);
            }

            Matcher nonStringedKeys = Pattern.compile("([,{]\\w+:)").matcher(ret);

            // Adds quotations around Keys that need them
            while (nonStringedKeys.find()) {
                String temp = nonStringedKeys.group(),
                        rep = temp.charAt(0) + "\"" + temp.replaceAll("[,{:]", "") + "\"" + temp.charAt(temp.length() - 1);
                ret = ret.replace(temp, rep);
            }

            Matcher nonStringedValues = Pattern.compile("(:[a-zA-Z.]+[,}])").matcher(ret);

            // Adds quotations around values that need them
            while (nonStringedValues.find()) {
                String temp = nonStringedValues.group(),
                        test = temp.replaceAll("[,}:]", "");

                // Only add "" if the value is not true|false
                if (!(test.equals("true") || test.equals("false"))) {
                    String rep = temp.charAt(0) + "\"" + temp.replaceAll("[,}:]", "") + "\"" + temp.charAt(temp.length() - 1);
                    ret = ret.replace(temp, rep);
                }
            }

        /*
        // Replaces get rid of " that break the json output
        // Remove " from :"{
        ret = ret.replace(":\"{", ":{");
        // Remove " from }",
        ret = ret.replace("}\",", "},");
        // Remove " from ,"{
        ret = ret.replace(",\"{", ",{");
        // Remove " from ["{
        ret = ret.replace("[\"{", "[{");
        // Remove " from }"]
        ret = ret.replace("}\"]", "}]");
        // Remove " from }"}
        ret = ret.replace("}\"}", "}}");
        // Replace empty "" with "unknown"
        ret = ret.replace("{\"\",", "[");
        */

            return ret
                    // Remove " from :"{
                    .replace(":\"{", ":{")
                    // Remove " from }",
                    .replace("}\",", "},")
                    // Remove " from ,"{
                    .replace(",\"{", ",{")
                    // Remove " from ["{
                    .replace("[\"{", "[{")
                    // Remove " from }"]
                    .replace("}\"]", "}]")
                    // Remove " from {"{
                    .replace("{\"{", "{{")
                    // Remove " from }"}
                    .replace("}\"}", "}}")
                    // Replace empty "" with "unknown"
                    .replace("\"\",", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + Base64.getEncoder().encodeToString(e.getStackTrace().toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String getFileTimeString() {
        String dateFormat = "yyyy";
        switch (Setting.LOG_TIME_SEPARATION.getString()) {
            case "M":
                dateFormat += "-MM";
                break;
            case "d":
                dateFormat += "-MM-dd";
                break;
            case "m":
                dateFormat += "-MM-dd'T'HH-mm";
                break;
            case "s":
                dateFormat += "-MM-dd'T'HH-mm-ss";
                break;
            default:
            case "H":
                dateFormat += "-MM-dd'T'HH";
                break;
        }

        return new SimpleDateFormat(dateFormat).format(new Date());
    }

    private String getTransactionTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    private String getTransactionDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

}
