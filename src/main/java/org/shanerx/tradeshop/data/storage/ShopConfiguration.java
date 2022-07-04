package org.shanerx.tradeshop.data.storage;

import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.List;

public interface ShopConfiguration {

    void save(Shop shop);
    void remove(ShopLocation loc);
    Shop load(ShopLocation loc);
    int size();

    /**
     * Turns old overstacked itemstacks into individual stacks in a list
     *
     * @param oldB64 old B64 string to check/fix
     * @return new list of ItemStacks
     * @deprecated
     */
    default List<ShopItemStack> b64OverstackFixer(String oldB64) {
        ShopItemStack oldStack = new ShopItemStack(oldB64);

        if (oldStack.hasBase64())
            return null;

        if (!(oldStack.getItemStack().getAmount() > oldStack.getItemStack().getMaxStackSize())) {
            return Lists.newArrayList(oldStack);
        } else {
            List<ShopItemStack> newStacks = new ArrayList<>();
            int amount = oldStack.getItemStack().getAmount();

            while (amount > 0) {
                if (oldStack.getItemStack().getMaxStackSize() < amount) {
                    ItemStack itm = oldStack.getItemStack().clone();
                    itm.setAmount(oldStack.getItemStack().getMaxStackSize());
                    newStacks.add(new ShopItemStack(itm));
                    amount -= oldStack.getItemStack().getMaxStackSize();
                } else {
                    ItemStack itm = oldStack.getItemStack().clone();
                    itm.setAmount(amount);
                    newStacks.add(new ShopItemStack(itm));
                    amount -= amount;
                }
            }

            return newStacks;
        }
    }
}
