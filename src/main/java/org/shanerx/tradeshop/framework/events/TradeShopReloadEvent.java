/*
 *
 *                         Copyright (c) 2016-2019
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

package org.shanerx.tradeshop.framework.events;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.utils.ListManager;
import org.shanerx.tradeshop.utils.debug.Debug;

// TODO javadocs for TradeShopReloadEvent
public class TradeShopReloadEvent extends ServerEvent {

    private static final HandlerList handlers = new HandlerList();

    public TradeShop plugin;
    public CommandSender sender;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public TradeShopReloadEvent(TradeShop plugin, CommandSender sender) {
        super();
        this.plugin = plugin;
        this.sender = sender;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public DataStorage getDataStorage() {
        return plugin.getDataStorage();
    }

    public ListManager getListManager() {
        return plugin.getListManager();
    }

    public Debug getDebugger() {
        return plugin.getDebugger();
    }
}
