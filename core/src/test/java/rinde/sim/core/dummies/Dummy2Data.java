package rinde.sim.core.dummies;

import rinde.sim.core.model.Data;


public class Dummy2Data implements Data{
    private static int nrGen = 0;
    
    public int nr = nrGen++;
    
    @Override
    public String toString() {
        return super.toString() + "|" + nr;
    }
}
