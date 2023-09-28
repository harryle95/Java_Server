package handlers;

import handlers.PriorityRunnableFuture;

import java.util.Comparator;

public class PriorityRunnableFutureComparator implements Comparator<Runnable> {
    @Override
    public int compare(Runnable o1, Runnable o2) {
        if (o1 == null && o2 == null)
            return 0;
        else if (o1 == null)
            return -1;
        else if (o2 == null)
            return 1;
        else {
            int p1 = ((PriorityRunnableFuture<?>) o1).getPriority();
            int p2 = ((PriorityRunnableFuture<?>) o2).getPriority();
            return Integer.compare(p1, p2);
        }
    }
}
