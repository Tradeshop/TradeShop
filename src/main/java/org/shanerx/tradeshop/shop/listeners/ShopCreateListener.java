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

package org.shanerx.tradeshop.shop.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

@SuppressWarnings("unused")
public class ShopCreateListener extends Utils implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {

        if (event.isCancelled())
            return;

        Sign shopSign = (Sign) event.getBlock().getState();
        shopSign.setLine(0, event.getLine(0));
        shopSign.setLine(1, event.getLine(1));
        shopSign.setLine(2, event.getLine(2));
        shopSign.setLine(3, event.getLine(3));

        if (!ShopType.isShop(shopSign)) {
            return;
        }

        ShopType shopType = ShopType.getType(shopSign);
        Player p = event.getPlayer();

        // Clear the first line since we already know it is going to be a Shop, and we have the type to pass separately
        // Required as the createShop method needs to make sure the first line is blank for commands to avoid overwriting existing shops
        shopSign.setLine(0, "");

        Shop shop = createShop(shopSign, p, shopType, lineCheck(ShopItemSide.COST, event.getLine(2)), lineCheck(ShopItemSide.PRODUCT, event.getLine(1)), event);

        if (shop == null) {
            failedSignReset(event, shopType);
        }
    }

    private ItemStack lineCheck(ShopItemSide side, String line) {
        Debug debug = TradeShop.getPlugin().getVarManager().getDebugger();
        if (line == null || line.isEmpty()) {
            debug.log("LineCheck - Failed due to empty line", DebugLevels.SHOP_CREATION);
            return null;
        }

        String[] info = line.split(" ");
        int amount = 0;
        Material material = null;

        for (String str : info) {
            ObjectHolder<String> temp = new ObjectHolder<>(str);
            if (temp.canBeInteger()) {
                amount = temp.asInteger();
            } else if (temp.canBeMaterial()) {
                material = temp.asMaterial();
            }
        }

        if (material == null || TradeShop.getPlugin().getVarManager().getListManager().isIllegal(side, material)) {
            debug.log("LineCheck - Failed due missing material. Line Text: " + line, DebugLevels.SHOP_CREATION);
            return null;
        }

        return new ItemStack(material, amount > 0 ? amount : 1);
    }
}