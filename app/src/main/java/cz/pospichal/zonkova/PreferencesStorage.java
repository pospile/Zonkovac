package cz.pospichal.zonkova;

import net.orange_box.storebox.annotations.method.DefaultValue;
import net.orange_box.storebox.annotations.method.KeyByString;

public interface PreferencesStorage {
    @KeyByString("limit")
    void setLimit(int value);

    @KeyByString("limit")
    @DefaultValue(R.integer.default_limit_interval)
    int getLimit();

    @KeyByString("last_id")
    void setLastId(int last_id);

    @KeyByString("last_id")
    int getLastId();
}
