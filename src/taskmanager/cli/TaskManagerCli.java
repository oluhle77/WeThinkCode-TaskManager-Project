// src/main/java/taskmanager/cli/TaskManagerCli.java
package taskmanager.cli;

// Apache Commons CLI library used to parse command line arguments easily
import org.apache.commons.cli.*;

import taskmanager.app.TaskManager;
import taskmanager.model.Task;
import taskmanager.app.Statistics;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * This class acts as the command-line interface (CLI) for the Task Manager program.
 * It lets the user interact with the application by typing commands in the terminal
 * such as create, list, status, priority, delete, and stats.
 *
 * Basically, it reads the command the user enters, figures out what action is needed,
 * and then calls the relevant methods in the TaskManager class to perform that task.
 */
public class TaskManagerCli {

    //Create a single TaskManager instance that loads/saves tasks from tasks.json
    private static final TaskManager taskManager = new TaskManager("tasks.json");

    public static void main(String[] args) {

        //Options object stores all command-line options supported by the program
        Options options = new Options();

        //Parser reads the arguments the user typed in the terminal
        CommandLineParser parser = new DefaultParser();

        //HelpFormatter prints nicely formatted help text
        HelpFormatter formatter = new HelpFormatter();

        //Global option: allows the user to run the program with -h or --help
        options.addOption(Option.builder("h").longOpt("help").desc("Show help").build());

        try {

            //Parse the input arguments
            CommandLine cmd = parser.parse(options, args, true);

            //If the user typed --help OR no command at all, show help and exit
            if (cmd.hasOption("help") || args.length == 0) {
                showHelp(formatter, options);
                return;
            }

            //First argument is treated as the command (create, list, delete, etc.)
            String command = args[0];

            //Remaining arguments belong to that command
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

            //Send the command to a method that knows how to handle it
            executeCommand(command, commandArgs);

        } catch (ParseException e) {
            //If something goes wrong during argument parsing, show an error
            System.err.println("Error parsing command: " + e.getMessage());
            showHelp(formatter, options);
        }
    }

    /*
     * This method decides which command the user is trying to run.
     * It uses a switch statement to call the correct handler method.
     */
    private static void executeCommand(String command, String[] args) {

        switch (command) {

            case "create":
                handleCreateCommand(args);
                break;

            case "list":
                handleListCommand(args);
                break;

            case "status":
                handleStatusCommand(args);
                break;

            case "priority":
                handlePriorityCommand(args);
                break;

            case "due":
                handleDueCommand(args);
                break;

            case "tag":
                handleTagCommand(args);
                break;

            case "untag":
                handleUntagCommand(args);
                break;

            case "clear-tags":
                handleClearTagsCommand(args);
                break;

            case "show":
                handleShowCommand(args);
                break;

            case "delete":
                handleDeleteCommand(args);
                break;

            case "stats":
                handleStatsCommand();
                break;

            default:
                //If the command doesn't match anything, let the user know
                System.err.println("Unknown command: " + command);
                System.err.println("Available commands: create, list, status, priority, due, tag, untag, clear-tags, show, delete, stats");
        }
    }

    /*
     * Handles creation of a new task.
     * The user must provide at least a title.
     * Other fields like description, priority, due date, and tags are optional.
     */
    private static void handleCreateCommand(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: create <title> [description] [priority] [due_date] [tags]");
            return;
        }

        //Required argument
        String title = args[0];

        //Optional arguments with default values if not provided
        String description = args.length > 1 ? args[1] : "";
        int priority = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        String dueDate = args.length > 3 ? args[3] : null;

        //Tags are provided as comma-separated values and converted into a list
        List<String> tags = args.length > 4 ?
                Arrays.asList(args[4].split(","))
                        .stream()
                        .map(String::trim)
                        .collect(Collectors.toList())
                : null;

        //Call TaskManager to create the task
        String taskId = taskManager.createTask(title, description, priority, dueDate, tags);

        if (taskId != null) {
            System.out.println("Created task with ID: " + taskId);
        }
    }

    /*
     * Handles the "list" command which displays tasks.
     * Users can filter results by status, priority, or show only overdue tasks.
     */
    private static void handleListCommand(String[] args) {

        Options options = new Options();

        //Filtering tasks by status
        options.addOption(Option.builder("s").longOpt("status").hasArg().desc("Filter by status").build());

        //Filtering tasks by priority
        options.addOption(Option.builder("p").longOpt("priority").hasArg().desc("Filter by priority").build());

        //Showing only overdue tasks
        options.addOption(Option.builder("o").longOpt("overdue").desc("Show only overdue tasks").build());

        try {

            CommandLine cmd = new DefaultParser().parse(options, args);

            //Extracting filter values
            String status = cmd.getOptionValue("status");
            Integer priority = cmd.hasOption("priority") ? Integer.valueOf(cmd.getOptionValue("priority")) : null;
            boolean showOverdue = cmd.hasOption("overdue");

            //Getting filtered tasks from TaskManager
            List<Task> tasks = taskManager.listTasks(status, priority, showOverdue);

            if (tasks.isEmpty()) {
                System.out.println("No tasks found matching the criteria.");
                return;
            }

            //Print each task in a formatted way
            for (Task task : tasks) {
                System.out.println(formatTask(task));
                System.out.println("-".repeat(50));
            }

        } catch (ParseException e) {
            System.err.println("Error parsing list options: " + e.getMessage());
        }
    }

    /*
     * Updates the status of a task (example: TO DO list --> DONE).
     */
    private static void handleStatusCommand(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: status <task_id> <new_status>");
            return;
        }

        String taskId = args[0];
        String newStatus = args[1];

        if (taskManager.updateTaskStatus(taskId, newStatus)) {
            System.out.println("Updated task status to " + newStatus);
        } else {
            System.out.println("Failed to update task status. Task not found.");
        }
    }

    /*
     * Updates the priority level of a task.
     */
    private static void handlePriorityCommand(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: priority <task_id> <new_priority>");
            return;
        }

        String taskId = args[0];
        int newPriority = Integer.parseInt(args[1]);

        if (taskManager.updateTaskPriority(taskId, newPriority)) {
            System.out.println("Updated task priority to " + newPriority);
        } else {
            System.out.println("Failed to update task priority. Task not found.");
        }
    }

    /*
     * Updates the due date for a specific task.
     */
    private static void handleDueCommand(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: due <task_id> <new_due_date>");
            return;
        }

        String taskId = args[0];
        String newDueDate = args[1];

        if (taskManager.updateTaskDueDate(taskId, newDueDate)) {
            System.out.println("Updated task due date to " + newDueDate);
        } else {
            System.out.println("Failed to update task due date. Task not found or invalid date.");
        }
    }

    /*
     * Adds a tag to a task.
     */
    private static void handleTagCommand(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: tag <task_id> <tag>");
            return;
        }

        String taskId = args[0];
        String tag = args[1];

        if (taskManager.addTagToTask(taskId, tag)) {
            System.out.println("Added tag '" + tag + "' to task");
        } else {
            System.out.println("Failed to add tag. Task not found.");
        }
    }

    /*
     * Removes a specific tag from a task.
     */
    private static void handleUntagCommand(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: untag <task_id> <tag>");
            return;
        }

        String taskId = args[0];
        String tag = args[1];

        if (taskManager.removeTagFromTask(taskId, tag)) {
            System.out.println("Removed tag '" + tag + "' from task");
        } else {
            System.out.println("Failed to remove tag. Task or tag not found.");
        }
    }

    /*
     * Removes all tags from a task.
     */
    private static void handleClearTagsCommand(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: clear-tags <task_id>");
            return;
        }

        String taskId = args[0];

        if (taskManager.clearTags(taskId)) {
            System.out.println("Cleared all tags from task");
        } else {
            System.out.println("Failed to clear tags. Task not found.");
        }
    }

    /*
     * Shows full details of a single task.
     */
    private static void handleShowCommand(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: show <task_id>");
            return;
        }

        String taskId = args[0];

        Task task = taskManager.getTaskDetails(taskId);

        if (task != null) {
            System.out.println(formatTask(task));
        } else {
            System.out.println("Task not found.");
        }
    }

    /*
     * Deletes a task permanently from the system.
     */
    private static void handleDeleteCommand(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: delete <task_id>");
            return;
        }

        String taskId = args[0];

        if (taskManager.deleteTask(taskId)) {
            System.out.println("Deleted task " + taskId);
        } else {
            System.out.println("Failed to delete task. Task not found.");
        }
    }

    /*
     * Displays statistics about all tasks,
     * such as how many exist, how many are overdue, etc.
     */
    private static void handleStatsCommand() {

        Statistics stats = taskManager.getStatistics();

        System.out.println("Total tasks: " + stats.getTotal());

        System.out.println("By status:");
        for (Map.Entry<String, Integer> entry : stats.getByStatus().entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("By priority:");
        for (Map.Entry<Integer, Integer> entry : stats.getByPriority().entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("Overdue tasks: " + stats.getOverdue());
        System.out.println("Completed in last 7 days: " + stats.getCompletedLastWeek());
    }

    /*
     * Prints help information showing the available commands
     * and how to use them.
     */
    private static void showHelp(HelpFormatter formatter, Options options) {

        System.out.println("Task Manager CLI");
        System.out.println("Available commands:");

        System.out.println("  create <title> [description] [priority] [due_date] [tags] - Create a new task");
        System.out.println("  list [-s <status>] [-p <priority>] [-o] - List tasks");
        System.out.println("  status <task_id> <new_status> - Update task status");
        System.out.println("  priority <task_id> <new_priority> - Update task priority");
        System.out.println("  due <task_id> <new_due_date> - Update task due date");
        System.out.println("  tag <task_id> <tag> - Add tag to task");
        System.out.println("  untag <task_id> <tag> - Remove tag from task");
        System.out.println("  clear-tags <task_id> - Remove all tags from task");
        System.out.println("  show <task_id> - Show task details");
        System.out.println("  delete <task_id> - Delete a task");
        System.out.println("  stats - Show task statistics");
    }

    /*
     * Converts a Task object into a nicely formatted string
     * so it prints cleanly in the terminal.
     */
    private static String formatTask(Task task) {

        //Converting task status into a simple visual symbol
        String statusSymbol;

        switch (task.getStatus()) {
            case TODO:
                statusSymbol = "[ ]";
                break;
            case IN_PROGRESS:
                statusSymbol = "[>]";
                break;
            case REVIEW:
                statusSymbol = "[?]";
                break;
            case DONE:
                statusSymbol = "[✓]";
                break;
            case ARCHIVED:
                statusSymbol = "[→]";
                break;
            default:
                statusSymbol = "[-]";
        }

        //Converting priority level into exclamation marks
        String prioritySymbol;

        switch (task.getPriority()) {
            case LOW:
                prioritySymbol = "!";
                break;
            case MEDIUM:
                prioritySymbol = "!!";
                break;
            case HIGH:
                prioritySymbol = "!!!";
                break;
            case URGENT:
                prioritySymbol = "!!!!";
                break;
            default:
                prioritySymbol = "";
        }

        //Formating due date if one exists
        String dueStr = task.getDueDate() != null ?
                "Due: " + task.getDueDate().format(DateTimeFormatter.ISO_DATE) :
                "No due date";

        //Formating tags list if tags exist
        String tagsStr = !task.getTags().isEmpty() ?
                "Tags: " + String.join(", ", task.getTags()) :
                "No tags";

        //Building the desired final formatted output
        return statusSymbol + " " + task.getId().substring(0, 8) + " - " + prioritySymbol + " " + task.getTitle() + "\n" +
                "  " + task.getDescription() + "\n" +
                "  " + dueStr + " | " + tagsStr + "\n" +
                "  Created: " + task.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
