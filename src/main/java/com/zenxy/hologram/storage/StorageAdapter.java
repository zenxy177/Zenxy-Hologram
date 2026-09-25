package com.zenxy.hologram.storage;

import com.zenxy.hologram.model.HologramData;

import java.util.Collection;

public interface StorageAdapter {

    void init();

    void saveHologram(HologramData hologram);

    void saveAllHolograms(Collection<HologramData> holograms);

    void deleteHologram(String hologramId);

    Collection<HologramData> loadAllHolograms();

    void close();
}
