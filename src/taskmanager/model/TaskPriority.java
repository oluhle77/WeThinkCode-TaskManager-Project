// src/main/java/taskmanager/model/TaskPriority.java
package taskmanager.model;

/*
 * This enum represents the different priority levels a task can have.
 * Each priority level is linked to a number value so that it can be
 * easily stored, compared, or processed in the program.
 */
public enum TaskPriority {

    //Lowest priority task
    LOW(1),

    //Normal priority task
    MEDIUM(2),

    //High priority task that should be done sooner
    HIGH(3),

    //Most urgent tasks that need immediate attention
    URGENT(4);

    //This stores the numeric value associated with each priority level
    private final int value;

    /*
     * Constructor for the enum.
     * It assigns the numeric value to the priority level.
     */
    TaskPriority(int value) {
        this.value = value;
    }

    /*
     * Returns the numeric value of the priority.
     * This can be useful when saving the priority or comparing levels.
     */
    public int getValue() {
        return value;
    }

    /*
     * This method converts a number into the corresponding TaskPriority.
     * For example, if the value is 2, it will return MEDIUM.
     */
    public static TaskPriority fromValue(int value) {

        //Loop through all priority values to find a match
        for (TaskPriority priority : TaskPriority.values()) {

            //If the numeric value matches, thr priority value is returned
            if (priority.getValue() == value) {
                return priority;
            }
        }

        //If no matching priority is found, throw an error
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }
}
