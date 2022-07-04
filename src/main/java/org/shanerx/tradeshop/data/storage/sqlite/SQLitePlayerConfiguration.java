package org.shanerx.tradeshop.data.storage.sqlite;

import org.apache.commons.lang.NotImplementedException;
import org.shanerx.tradeshop.data.storage.PlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.util.UUID;

public class SQLitePlayerConfiguration implements PlayerConfiguration {

    private UUID uuid;

    public SQLitePlayerConfiguration(UUID uuid) {
        this.uuid = uuid;
        throw new NotImplementedException("not implemented");
    }

    @Override
    public void save(PlayerSetting playerSetting) {

    }

    @Override
    public PlayerSetting load() {
        return null;
    }

    @Override
    public void remove() {

    }
}
