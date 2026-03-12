package taskmanager.app;

import java.util.Collections;
import java.util.Map;

//This class stores summary statistics about tasks
public class Statistics {
    private final int total;
    private final Map<String, Integer> byStatus; //Map showing how many tasks exist for each status (e.g., "Pending", "Completed")
    private final Map<Integer, Integer> byPriority;
    private final int overdue;
    private final int completedLastWeek;

    //Constructor used to create a Statistics object with all values
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

    //Returns the total number of tasks
    public int getTotal() {
        return total;
    }

    //Returns a read-only version of the status statistics map
    public Map<String, Integer> getByStatus() {
        return Collections.unmodifiableMap(byStatus);
    }

    //Returns a read-only version of the priority statistics map
    public Map<Integer, Integer> getByPriority() {
        return Collections.unmodifiableMap(byPriority);
    }

    //Returns the number of overdue tasks
    public int getOverdue() {
        return overdue;
    }

    //Returns how many tasks were completed in the last week
    public int getCompletedLastWeek() {
        return completedLastWeek;
    }
}
