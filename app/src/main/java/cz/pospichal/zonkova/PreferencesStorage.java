package cz.pospichal.zonkova;

import net.orange_box.storebox.annotations.method.DefaultValue;
import net.orange_box.storebox.annotations.method.KeyByString;

//This class is Interface for SharedPrefs storage (showing data structure of this app storage and data types)
public interface PreferencesStorage {

    /**
     * Sets number of ticks service needs to check for changes at zonky endpoint
     * @param value (int) number of ticks/secs before next check
     */
    @KeyByString("limit")
    void setLimit(int value);

    /**
     * Gets number of ticks before next service endpoint check
     * @return (int) -> time before next check
     */
    @KeyByString("limit")
    @DefaultValue(R.integer.default_limit_interval)
    int getLimit();

    /**
     * Sets lastID received from api endpoint
     * @param last_id -> lastId received from api endpoint (alternatively 0 to force the check again)
     */
    @KeyByString("last_id")
    void setLastId(int last_id);

    /**
     * Get lastId for check
     * @return (int) -> last_check_id
     */
    @KeyByString("last_id")
    int getLastId();
}
