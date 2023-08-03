package com.craftaro.epicspawners.api.boosts.types;

import com.craftaro.core.database.Data;

public interface Boosted extends Data {
    int getAmountBoosted();

    long getEndTime();
}
