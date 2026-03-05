# TaskManager CLI

TaskManager is a command-line tool for managing tasks, built in Java. It lets users create, update, track, and manage tasks along with their priority, status, due dates, and tags. This project demonstrates a simple, extensible design using object-oriented principles and domain-driven patterns.

---

## Features

- Create tasks with a title, description, priority, due date, and tags.  
- Update tasks using partial updates (only non-null fields take effect).  
- Mark tasks as done and track completion timestamps.  
- Check for overdue tasks while ignoring archived tasks.  
- Add and remove tags using defensive copying to maintain internal invariants.  
- List, show, and delete tasks via a command-line interface.  
- Provide statistics on tasks by status, priority, overdue status, and completion in the last week.

---

## Project Structure


src/
└─ main/
└─ java/
└─ taskmanager/
├─ app/
│ ├─ TaskManager.java
│ └─ Statistics.java
├─ cli/
│ └─ TaskManagerCli.java
└─ model/
├─ Task.java
├─ TaskPriority.java
└─ TaskStatus.java


- **model/** contains the core classes: `Task`, `TaskStatus`, `TaskPriority`.  
- **app/** contains the service layer (`TaskManager`) and statistics logic.  
- **cli/** contains the command-line interface implementation.

---

## Design Notes

### Task Lifecycle

- **Task Creation:** `createTask` constructs a new task from the provided fields and stores it in `TaskStorage`. Defaults are applied for missing fields (e.g., `priority = MEDIUM`, `status = TODO`).  
- **Partial Updates:** `update()` applies only non-null fields. Be aware that the `Task` constructor fills in defaults, which could overwrite unexpected fields.  
- **Mark as Done:** `markAsDone()` sets status to `DONE` and updates `completedAt` and `updatedAt`. Repeated calls overwrite timestamps, which may not be ideal for tracking completion history.  
- **Overdue Check:** `isOverdue()` returns `false` if `dueDate` is null, or if the task is `DONE` or `ARCHIVED`, preventing null-pointer errors and reflecting the rule that only active tasks with deadlines can be overdue.  
- **Tag Management:** `getTags()` returns a copy of the internal list. Tags can only be modified through `addTag()` and `removeTag()`, protecting internal invariants.

### Storage Layer

- `TaskStorage` initially loaded tasks in the constructor, which caused a “this-escape” warning. It was refactored to an `initialize()` method called after construction.  
- Tags are stored as `List<String>` to allow multiple labels per task; defensive copying ensures safe updates.

---

## Compilation & Warnings

- Initial compilation produced three warnings:  
  1. `TaskStorage.java` – calling `load()` in the constructor before initialization completed.  
  2. `TaskManagerCli.java` – unchecked casts from `Object` to `Map` types.  
- Fixed by adding an `initialize()` method in `TaskManager` and using type-safe maps.  
- Final compilation is clean.

---

## Developer Insights

- CLI and service layer are decoupled: CLI handles input, `TaskManager` handles domain logic.  
- Defensive coding prevents common errors (null due dates, direct list mutation).  
- The system assumes the CLI will not call `markAsDone()` repeatedly, but future versions could safeguard timestamps.  
- Understanding `update()`, `markAsDone()`, `isOverdue()`, and tag management is essential for safely extending the app.

---

## Example Usage

```bash
# Create a new task
java -cp target/classes taskmanager.cli.TaskManagerCli create "Write README" "Document project" 2 "2026-03-05" "documentation"

# Mark a task as done
java -cp target/classes taskmanager.cli.TaskManagerCli markAsDone <task_id>

# List overdue tasks
java -cp target/classes taskmanager.cli.TaskManagerCli list -o
Key Takeaways from LLM Analysis

Clarified partial updates, defensive copying for tags, and edge cases like null due dates.

Highlighted subtleties, e.g., repeated calls to markAsDone() overwrite timestamps.

Confirmed the mental model of the project: domain logic lives in Task and TaskManager, CLI simply orchestrates commands.
