package org.shanerx.tradeshop.utils;

public class ObjectHolder<Type> {
	
	private Type obj;
	
	public ObjectHolder(Type obj) {
		this.obj = obj;
	}
	
	public Type getObject() {
		return obj;
	}
}
