package com.taskwebapp.taskmanager.controller;

import com.taskwebapp.taskmanager.model.Task;
import com.taskwebapp.taskmanager.model.User;
import com.taskwebapp.taskmanager.repository.TaskRepository;
import com.taskwebapp.taskmanager.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Get all tasks for logged-in user
    @GetMapping
    public List<Task> getAllTasks(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        return userOpt.map(user -> taskRepository.findByUserId(user.getId())).orElse(new ArrayList<>());
    }

    // ✅ Get task by ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    // ✅ Create a new task
    @PostMapping
    public String createTask(@RequestBody Task task, @AuthenticationPrincipal UserDetails userDetails) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return "Task title is required";
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isPresent()) {
            task.setUser(userOpt.get());
            task.setStatus(task.getStatus() != null ? task.getStatus() : "pending");
            taskRepository.save(task);
            return "Task created successfully";
        } else {
            return "User not found";
        }
    }

    // ✅ Update task
    @PutMapping("/{id}")
    public String updateTask(@PathVariable Long id, @RequestBody Task updatedTask, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) return "Task not found";

        Task task = taskOpt.get();
        if (!task.getUser().getEmail().equals(userDetails.getUsername())) {
            return "Access denied";
        }

        if (updatedTask.getTitle() != null) task.setTitle(updatedTask.getTitle());
        if (updatedTask.getDescription() != null) task.setDescription(updatedTask.getDescription());
        if (updatedTask.getStatus() != null) task.setStatus(updatedTask.getStatus());

        taskRepository.save(task);
        return "Task updated successfully";
    }

    // ✅ Delete task
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) return "Task not found";

        Task task = taskOpt.get();
        if (!task.getUser().getEmail().equals(userDetails.getUsername())) {
            return "Access denied";
        }

        taskRepository.delete(task);
        return "Task deleted successfully";
    }

    // ✅ Filter tasks by status (optional)
    @GetMapping("/filter")
    public List<Task> filterByStatus(@RequestParam String status, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return new ArrayList<>();

        List<Task> tasks = taskRepository.findByUserId(userOpt.get().getId());
        List<Task> filtered = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getStatus() != null && t.getStatus().equalsIgnoreCase(status)) {
                filtered.add(t);
            }
        }
        return filtered;
    }
}
