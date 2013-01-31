package rinde.sim.core.model;

import java.util.Collection;
import java.util.Iterator;

public interface SafeIterator<T> {
    
    boolean hasNext();

    T next();
    
    
    
    public static class Std<T> implements SafeIterator<T>{
        private final Iterator<T> it;
        
        public Std(Iterator<T> it){
            this.it = it;
        }
        
        public Std(Collection<T> coll){
            this.it = coll.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public T next() {
            return it.next();
        }
    }
}
