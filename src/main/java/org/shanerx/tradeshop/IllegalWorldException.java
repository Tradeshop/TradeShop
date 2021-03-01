package org.shanerx.tradeshop;

import org.shanerx.tradeshop.objects.WorldlessLocation;

public class IllegalWorldException extends IllegalStateException {

    private WorldlessLocation loc;

    public IllegalWorldException(String msg, WorldlessLocation loc) {
        super(msg);
        this.loc = loc;
    }

    public WorldlessLocation getLoc() {
        return loc;
    }
}
