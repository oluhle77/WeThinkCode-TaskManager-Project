// src/main/java/taskmanager/app/TaskManager.java
package taskmanager.app;

import taskmanager.model.Task;
import taskmanager.model.TaskPriority;
import taskmanager.model.TaskStatus;
import taskmanager.storage.TaskStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final TaskStorage storage;

    public TaskManager(String storagePath) {
        this.storage = new TaskStorage(storagePath);
        this.storage.initialize();
    }

    public String createTask(String title, String description, int priorityValue,
                             String dueDateStr, List<String> tags) {
        // validation: title must not be null/empty; A task cannot be created without a title.
        if (title == null || title.trim().isEmpty()) {
            System.err.println("Title cannot be empty");
            return null;
        }

        TaskPriority priority = TaskPriority.fromValue(priorityValue);
        LocalDateTime dueDate = null;

        if (dueDateStr != null && !dueDateStr.isEmpty()) {
            try {
                LocalDate localDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_DATE);
                dueDate = LocalDateTime.of(localDate, LocalTime.MAX);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Use YYYY-MM-DD");
                return null;
            }
        }

        // log tags for debugging/validation purposes
        System.out.println("Creating task with tags: " + tags);

        Task task = new Task(title, description, priority, dueDate, tags);
        return storage.addTask(task);
    }

    public List<Task> listTasks(String statusFilter, Integer priorityFilter, boolean showOverdue) {
        if (showOverdue) {
            return storage.getOverdueTasks();
        }

        if (statusFilter != null) {
            TaskStatus status = TaskStatus.fromValue(statusFilter);
            return storage.getTasksByStatus(status);
        }

        if (priorityFilter != null) {
            TaskPriority priority = TaskPriority.fromValue(priorityFilter);
            return storage.getTasksByPriority(priority);
        }

        return storage.getAllTasks();
    }

    //Using boolean in this function to determine if the due date had been reached by means of true/false
    public boolean updateTaskStatus(String taskId, String newStatusValue) {
        TaskStatus newStatus = TaskStatus.fromValue(newStatusValue);

        if (newStatus == TaskStatus.DONE) {
            Task task = storage.getTask(taskId);
            if (task != null) {
                task.markAsDone();
                storage.save();
                return true;
            }
            return false;
        } else {
            Task updates = new Task("tempTitle");
            updates.setStatus(newStatus);
            return storage.updateTask(taskId, updates);
        }
    }

    public boolean updateTaskPriority(String taskId, int newPriorityValue) {
        TaskPriority newPriority = TaskPriority.fromValue(newPriorityValue);

        Task updates = new Task("tempTitle");
        updates.setPriority(newPriority);
        return storage.updateTask(taskId, updates);
    }

    public boolean updateTaskDueDate(String taskId, String dueDateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_DATE);
            LocalDateTime dueDate = LocalDateTime.of(localDate, LocalTime.MAX);

            Task updates = new Task("tempTitle");
            updates.setDueDate(dueDate);
            return storage.updateTask(taskId, updates);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format. Use YYYY-MM-DD");
            return false;
        }
    }

    public boolean deleteTask(String taskId) {
        return storage.deleteTask(taskId);
    }

    public Task getTaskDetails(String taskId) {
        return storage.getTask(taskId);
    }

    public boolean addTagToTask(String taskId, String tag) {
        Task task = storage.getTask(taskId);
        if (task != null) {
            task.addTag(tag);
            storage.save();
            return true;
        }
        return false;
    }

    public boolean removeTagFromTask(String taskId, String tag) {
        Task task = storage.getTask(taskId);
        if (task != null && task.removeTag(tag)) {
            storage.save();
            return true;
        }
        return false;
    }

    /**
     * Helper used by CLI or tests to clear all tags from a task.
     * Does not modify other fields, just replaces the tag list with an empty one.
     */
    public boolean clearTags(String taskId) {
        Task task = storage.getTask(taskId);
        if (task != null) {
            task.setTags(new ArrayList<>());
            storage.save();
            return true;
        }
        return false;
    }

    public Statistics getStatistics() {
        List<Task> tasks = storage.getAllTasks();
        int total = tasks.size();

        // Count by status
        Map<String, Integer> statusCounts = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusCounts.put(status.getValue(), 0);
        }

        for (Task task : tasks) {
            String statusValue = task.getStatus().getValue();
            statusCounts.put(statusValue, statusCounts.get(statusValue) + 1);
        }

        // Count by priority
        Map<Integer, Integer> priorityCounts = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            priorityCounts.put(priority.getValue(), 0);
        }

        for (Task task : tasks) {
            int priorityValue = task.getPriority().getValue();
            priorityCounts.put(priorityValue, priorityCounts.get(priorityValue) + 1);
        }

        // Count overdue
        int overdueCount = (int) tasks.stream().filter(Task::isOverdue).count();

        // Count completed in last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int completedRecently = (int) tasks.stream()
                .filter(task -> task.getCompletedAt() != null && task.getCompletedAt().isAfter(sevenDaysAgo))
                .count();

        return new Statistics(total, statusCounts, priorityCounts, overdueCount, completedRecently);
    }
}
