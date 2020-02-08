package org.shanerx.tradeshop.framework;

/**
 * This enum's entries represent the possible changes the {@link org.shanerx.tradeshop.framework.events.PlayerShopChangeEvent} is fired upon.
 */
public enum ShopChange {
	
	ADD_MANAGER,
	ADD_MEMBER,
	ADD_PRODUCT,
	ADD_COST,
	REMOVE_USER,
	REMOVE_PRODUCT,
	REMOVE_COST,
	SET_PRODUCT,
	SET_COST
}
