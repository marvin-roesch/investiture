package de.mineformers.investiture.allomancy.metal;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.*;

/**
 *
 */
public class MetalReserves
{

    public final static int MAX_STORAGE = 100;
    public final static int BURN_TIME = 20;
    public final static int FLARE_TIME = 10;

    private final TObjectIntMap<Metal> reserves = new TObjectIntHashMap<>();
    private final SortedSetMultimap<Metal, MetalSource> consumed = TreeMultimap.create(Ordering.natural(), (Comparator<MetalSource>) (o1, o2) -> o1.purity - o2.purity);
    private final Map<Metal, Boolean> burning = new HashMap<>();
    private final TObjectIntMap<Metal> timers = new TObjectIntHashMap<>();
    private final Set<Metal> bursting = new HashSet<>();

    /**
     *
     * @param metal
     * @param quantity
     * @param purity
     *
     * @todo make this abide MAX_STORAGE
     */
    public void add(Metal metal, int quantity, int purity)
    {
        reserves.adjustOrPutValue(metal, quantity, quantity);
        MetalSource source = new MetalSource(metal, quantity, purity);
        Optional<MetalSource> currentSource = getConsumed(source);

        if(currentSource.isPresent()) {
            currentSource.get().modify(source.quantity);
        } else {
            consumed.put(metal, source);
        }
    }

    private Optional<MetalSource> getConsumed(MetalSource source)
    {
        if(consumed.containsKey(source.metal)) {
            return FluentIterable.from(consumed.get(source.metal)).firstMatch(s -> s.equals(source));
        }

        return Optional.absent();
    }

    /**
     * Burn a metal.
     * Essentially just queues it up to be burnt during the next iteration.
     *
     * @param metal
     * @return
     */
    public boolean burn(Metal metal)
    {
        return queue(metal, true, BURN_TIME);
    }

    /**
     * Flare a metal.
     * Essentially just queues it up to be flared during the next iteration.
     *
     * @param metal
     * @return
     */
    public boolean flare(Metal metal)
    {
        return queue(metal, true, FLARE_TIME);
    }

    /**
     * Burst the currently burning metals, would be called when targeted with Nicrosil or when
     * burning Duralumin.
     */
    public void burst()
    {
        Set<Metal> toBurst = burning.keySet();
        bursting.addAll(toBurst);

        for(Metal metal : toBurst) {
            queue(metal, false, BURN_TIME);
        }
    }

    /**
     * Wipe internal allomantic reserves, would be called when targeted with Chromium or when
     * burning Aluminium.
     */
    public Set<Metal> wipe()
    {
        Set<Metal> remove = new HashSet<>(reserves.keySet());

        for(Metal metal : remove) {
            burning.remove(metal);
            reserves.put(metal, 0);
            consumed.get(metal).clear();
            timers.remove(metal);
        }

        return remove;
    }

    private boolean queue(Metal metal, boolean flaring, int timer)
    {
        if(reserves.get(metal) > 0) {
            burning.put(metal, flaring);

            if(!timers.containsKey(metal)) {
                timers.put(metal, timer);
            }

            return true;
        }

        return false;
    }

    /**
     * Perform the burn action.
     * This is an update method called every tick.
     */
    public void burn()
    {
        for(Iterator<Map.Entry<Metal, Boolean>> it = burning.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Metal, Boolean> entry = it.next();
            Metal metal = entry.getKey();
            boolean flaring = entry.getValue();
            timers.adjustValue(metal, -1);

            if(timers.get(metal) <= 0) {
                if(bursting.contains(metal)) {
                    bursting.remove(metal);
                    reserves.put(metal, 0);
                    consumed.get(metal).clear();
                    timers.remove(metal);
                    it.remove();
                    continue;
                }

                MetalSource source = consumed.get(metal).first();
                source.modify(-1);

                if(source.quantity == 0) {
                    consumed.get(metal).remove(source);
                }

                reserves.adjustOrPutValue(metal, -1, 0);

                if(reserves.get(metal) == 0) {
                    it.remove();
                } else {
                    timers.put(metal, flaring ? FLARE_TIME : BURN_TIME);
                }
            }
        }
    }

    /**
     * Check whether or not the metal is currently burning.
     *
     * @param metal
     * @return
     */
    public boolean isBurning(Metal metal)
    {
        return burning.containsKey(metal);
    }

    /**
     * Check whether or not the metal is currently flaring.
     *
     * @param metal
     * @return
     */
    public boolean isFlaring(Metal metal)
    {
        return isBurning(metal) ? burning.get(metal) : false;
    }

    /**
     * Check whether or not the metal is currently being burst.
     *
     * @param metal
     * @return
     */
    public boolean isBursting(Metal metal)
    {
        return bursting.contains(metal);
    }

    /**
     * Reference class used for consuming metal reserves in order of impurest first.
     */
    private class MetalSource implements Comparable<MetalSource>
    {
        public final Metal metal;
        public int quantity;
        public final int purity;

        private MetalSource(Metal metal, int quantity, int purity)
        {
            this.metal = metal;
            this.quantity = quantity;
            this.purity = purity;
        }

        public int modify(int quantity)
        {
            return quantity;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof MetalSource) {
                MetalSource source = (MetalSource) obj;

                return source.purity == purity && metal.equals(source.metal);
            }

            return false;
        }

        @Override
        public int compareTo(MetalSource o)
        {
            return purity - o.purity;
        }
    }

}
