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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.utils.Utils;

public class ShopRestockListener implements Listener {

    private final TradeShop plugin;

    public ShopRestockListener(TradeShop instance) {
        plugin = instance;
    }

    //If it is a shopchest, this updates the sign when the inventory is closed

    //Doesn't update double chests closing --Bug, unsure how to fix
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (ShopChest.isShopChest(event.getInventory())) {
            Shop shop = new ShopChest(event.getInventory().getLocation()).getShop();
            if (shop != null) {
                shop.updateSign();
                shop.saveShop();
                new Utils().scheduleShopDelayUpdate("ShopRestockListener#onInventoryClose", shop, 10L);
            }
        }
    }
}

