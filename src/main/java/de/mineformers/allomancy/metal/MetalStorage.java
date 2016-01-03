package de.mineformers.allomancy.metal;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * MetalStorage
 *
 * @author PaleoCrafter
 */
public class MetalStorage {
    private final TObjectIntMap<AllomanticMetal> _consumedMetals = new TObjectIntHashMap<>();
    private int _impurities;

    public int get(AllomanticMetal metal) {
        if(_consumedMetals.containsKey(metal))
            return _consumedMetals.get(metal);
        return 0;
    }
}
