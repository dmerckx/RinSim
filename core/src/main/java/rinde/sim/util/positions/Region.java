package rinde.sim.util.positions;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Region {
    public final int x;
    public final int y;
    private final int hashCode;
    
    @SuppressWarnings("hiding") 
    public Region(int x, int y) {
        this.x = x;
        this.y = y;
        hashCode = new HashCodeBuilder(17, 37).append(x).append(y).toHashCode();
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Region)) return false;
        
        return x == ((Region) obj).x && y == ((Region) obj).y;
    }
    
    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }
}