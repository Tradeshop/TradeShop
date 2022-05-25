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

import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;

/**
 * Implementation of CommandRunner for plugin commands that generate GUI screens
 *
 * @since 2.6.0
 */
public class GUICommand extends CommandRunner {

    //region Util Variables
    //------------------------------------------------------------------------------------------------------------------

    protected final GuiPageElement PREV_BUTTON = new GuiPageElement('p', new ItemStack(Material.POTION), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"),
            NEXT_BUTTON = new GuiPageElement('n', new ItemStack(Material.SPLASH_POTION), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)");
    protected final StaticGuiElement CANCEL_BUTTON = new StaticGuiElement('c', new ItemStack(Material.END_CRYSTAL), click3 -> {
        InventoryGui.goBack(pSender);
        return true;
    }, "Cancel Changes"),
            BACK_BUTTON = new StaticGuiElement('b', new ItemStack(Material.END_CRYSTAL), click3 -> {
                InventoryGui.goBack(pSender);
                return true;
            }, "Back");
    protected final String[] MENU_LAYOUT = {"a b c"},
            EDIT_LAYOUT = {"aggggggga", "ap c s na"},
            ITEM_LAYOUT = {"aggggggga", "aggggggga", "a  cbs  a"},
            WHAT_MENU = {"141125333", "1aaa2bbb3", "11p123n33"};


    //------------------------------------------------------------------------------------------------------------------
    //endregion

    public GUICommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }


}
