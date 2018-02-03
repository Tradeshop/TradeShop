package org.shanerx.tradeshop.util;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Tuple<R, L> implements Serializable {

	@SerializedName("right")
	private R r;
	@SerializedName("left")
	private L l;

	public Tuple() {}

	public Tuple(R r, L l) {
		this.r = r;
		this.l = l;
	}

	public Tuple(Tuple<? extends R, ? extends L> t) {
		this.r = t.r;
		this.l = t.l;
	}

	public R getRight() {
		return r;
	}

	public L getLeft() {
		return l;
	}

	public String serialize() {
		return new Gson().toJson(this);
	}

	@Override
	public String toString() {
		return serialize();
	}
}