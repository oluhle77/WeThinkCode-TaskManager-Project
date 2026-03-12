// src/main/java/taskmanager/model/TaskStatus.java
package taskmanager.model;

/*
 * Represents the different states a task can have in the system.
 * Using an enum helps keep the task statuses consistent across the program.
 */
public enum TaskStatus {

    //Possible states a task can move through
    TODO("todo"),
    IN_PROGRESS("in_progress"),
    REVIEW("review"),
    DONE("done"),
    ARCHIVED("archived");

    //String value used when storing or displaying the status
    private final String value;

    //Constructor used to assign the text value to each status
    TaskStatus(String value) {
        this.value = value;
    }

    //Returns the string value of the status
    public String getValue() {
        return value;
    }

    /*
     * Converts a string value into the matching TaskStatus.
     * If the value does not match any status, an error is thrown.
     */
    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}
