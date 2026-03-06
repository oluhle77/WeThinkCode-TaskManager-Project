package taskmanager.app;

import java.util.Collections;
import java.util.Map;

public class Statistics {
    private final int total;
    private final Map<String, Integer> byStatus;
    private final Map<Integer, Integer> byPriority;
    private final int overdue;
    private final int completedLastWeek;

    public Statistics(int total,
                      Map<String, Integer> byStatus,
                      Map<Integer, Integer> byPriority,
                      int overdue,
                      int completedLastWeek) {
        this.total = total;
        this.byStatus = byStatus != null ? byStatus : Collections.emptyMap();
        this.byPriority = byPriority != null ? byPriority : Collections.emptyMap();
        this.overdue = overdue;
        this.completedLastWeek = completedLastWeek;
    }

    public int getTotal() {
        return total;
    }

    public Map<String, Integer> getByStatus() {
        return Collections.unmodifiableMap(byStatus);
    }

    public Map<Integer, Integer> getByPriority() {
        return Collections.unmodifiableMap(byPriority);
    }

    public int getOverdue() {
        return overdue;
    }

    public int getCompletedLastWeek() {
        return completedLastWeek;
    }
}
