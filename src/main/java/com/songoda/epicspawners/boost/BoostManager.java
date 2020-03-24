package com.songoda.epicspawners.boost;

import com.songoda.epicspawners.boost.types.Boosted;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoostManager {

    private final Set<Boosted> registeredBoosts = new HashSet<>();

    public void addBoost(Boosted boosted) {
        this.registeredBoosts.add(boosted);
    }

    public void removeBoost(Boosted boosted) {
        this.registeredBoosts.remove(boosted);
    }

    public Set<Boosted> getBoosts() {
        return Collections.unmodifiableSet(registeredBoosts);
    }

    public void addBoosts(List<Boosted> boosts) {
        registeredBoosts.addAll(boosts);
    }
}
