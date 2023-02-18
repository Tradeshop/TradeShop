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

package org.shanerx.tradeshop.commands.commandrunners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.shop.ShopType;

/**
 * Implementation of CommandRunner for plugin commands that create new shops
 *
 * @since 2.6.0
 */
public class CreateCommand extends CommandRunner {

    public CreateCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Create a regular shop from a sign in front of the player
     */
    public void createTrade() {
        Sign sign = findSign();

        if (sign == null)
            return;

        createShop(sign, pSender, ShopType.TRADE);
    }

    /**
     * Create a BiTrade shop from a sign in front of the player
     */
    public void createBiTrade() {
        Sign sign = findSign();

        if (sign == null)
            return;

        createShop(sign, pSender, ShopType.BITRADE);
    }

    /**
     * Create a iTrade shop from a sign in front of the player
     */
    public void createITrade() {
        Sign sign = findSign();

        if (sign == null)
            return;

        createShop(sign, pSender, ShopType.ITRADE);
    }

    //region Util Methods
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the Sign the player is looking at
     *
     * @return null if Sign is not found, Sign object if it is
     */
    protected Sign findSign() {
        if (pSender == null) {
            Message.PLAYER_ONLY_COMMAND.sendMessage(pSender);
            return null;
        }

        Block b = pSender.getTargetBlockExact(Setting.MAX_EDIT_DISTANCE.getInt());
        try {
            if (b == null)
                throw new NoSuchFieldException();

            if (plugin.getSigns().getSignTypes().contains(b.getType())) {
                return (Sign) b.getState();

            } else
                throw new NoSuchFieldException();

        } catch (NoSuchFieldException ex) {
            Message.NO_SIGN_FOUND.sendMessage(pSender);
            return null;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //endregion
}
