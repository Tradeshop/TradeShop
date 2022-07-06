package org.shanerx.tradeshop.data.storage;

import org.shanerx.tradeshop.data.storage.sqlite.SQLitePlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.util.UUID;

public interface PlayerConfiguration {

    void save(PlayerSetting playerSetting);
    PlayerSetting load();

    void remove();
}
