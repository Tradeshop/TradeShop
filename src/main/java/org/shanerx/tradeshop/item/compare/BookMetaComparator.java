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

package org.shanerx.tradeshop.item.compare;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class BookMetaComparator implements ShopItemComparator {
    //private final Debug DEBUGGER = TradeShop.getPlugin().getDebugger(); no debugging may add in later
    @Override
    public boolean checkSimilarity(ShopItemStack source, ItemStack target) {
        return !compare(source, Collections.singletonList(target)).isEmpty();
    }

    @Override
    public List<ItemStack> checkManySimilarity(ShopItemStack source, List<ItemStack> targets) {
        return compare(source, targets);
    }

    @Override
    public boolean needsComparison(ShopItemStack source) {
        if (!(source.getShopSetting(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR).asBoolean() ||
                source.getShopSetting(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES).asBoolean()))
            return false; //Return if comparison is disabled for both items

        return needsComparison(source.getItemStack()); //Check if comparison is necessary for Itemstack

    }

    @Override
    public boolean needsComparison(ItemStack sourceItem) {

        if (!sourceItem.hasItemMeta()) return false; //Return if item doesn't have metadata(cannot be a book)

        ItemMeta sourceMeta = sourceItem.getItemMeta();

        return (sourceMeta instanceof BookMeta); //Return whether the items meta data is book meta
    }

    private List<ItemStack> compare(ShopItemStack source, List<ItemStack> targets) {
        if (source == null || targets == null) return null;

        if (!needsComparison(source)) return null; //Verify source item needs comparison
        targets.removeIf((target) -> target == null || !needsComparison(target));

        BookMeta sourceBookMeta = (BookMeta) source.getItemStack().getItemMeta();

        if (targets.isEmpty() || sourceBookMeta == null)
            return null; //No targets passed or all removed by needs comparison check.

        boolean compareAuthor = source.getShopSetting(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR).asBoolean(),
                comparePages = source.getShopSetting(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES).asBoolean();

        targets.removeIf((target) -> {
            BookMeta temp = (BookMeta) target.getItemMeta();
            if (!compareAuthor || temp == null || temp.hasAuthor() != sourceBookMeta.hasAuthor() || !Objects.equals(temp.getAuthor(), sourceBookMeta.getAuthor()))
                return true; // Remove target if it and the source do not match authors
            return (!comparePages || temp.hasPages() != sourceBookMeta.hasPages() || !Objects.equals(temp.getPages(), sourceBookMeta.getPages())); // Remove target if it and the source do not match pages
        });

        return targets;
    }
}
