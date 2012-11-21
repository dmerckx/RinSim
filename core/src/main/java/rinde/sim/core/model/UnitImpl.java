package rinde.sim.core.model;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public abstract class UnitImpl implements Unit{

    private List<PreTick> preTickers = new ArrayList<PreTick>();
    private List<AfterTick> afterTickers = new ArrayList<AfterTick>();

    @Override
    public void registerForTick(PreTick preTicker) {
        preTickers.add(preTicker);
    }

    @Override
    public void registerAfterTick(AfterTick afterTicker) {
        afterTickers.add(afterTicker);
    }

    @Override
    public void tick(TimeLapse lapse) {
        for(PreTick ticker:preTickers){
            ticker.tick(lapse);
        }
    }

    @Override
    public void afterTick(TimeInterval time) {
        for(AfterTick ticker:afterTickers){
            ticker.afterTick(time);
        }
    }

}
