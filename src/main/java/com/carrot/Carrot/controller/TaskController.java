package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Task;
import com.carrot.Carrot.dto.MoveTicketDTO;
import com.carrot.Carrot.model.Progetto;
import com.carrot.Carrot.service.TaskService;
import com.carrot.Carrot.service.ProgettoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProgettoService progettoService;

    public TaskController(TaskService taskService, ProgettoService progettoService) {
        this.taskService = taskService;
        this.progettoService = progettoService;
    }

    // 📌 Creazione di un nuovo task e associazione a un progetto
    @PostMapping("/progetto/{progettoId}")
    public ResponseEntity<Task> createTask(@PathVariable Long progettoId, @RequestBody Task task) {
        Optional<Progetto> progettoOpt = progettoService.getProgettoById(progettoId);

        if (progettoOpt.isPresent()) {
            Task nuovoTask = taskService.addTask(task,progettoOpt.get());
            return ResponseEntity.ok(nuovoTask);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<List<Task>> updateTakProject(@PathVariable Long taskId, @RequestBody MoveTicketDTO ticketDTO) {
        taskService.updateTaskProject(taskId, ticketDTO.getProjectId(), ticketDTO.getOldProjectId());
        return ResponseEntity.ok(taskService.getTasksByProject(ticketDTO.getProjectId()));
    }

    // 📌 Recupera i task di un progetto
    @GetMapping("/progetto/{progettoId}")
    public ResponseEntity<List<Task>> getTasksByProgetto(@PathVariable Long progettoId) {
        List<Task> tasks = taskService.getTasksByProject(progettoId);
        return tasks.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tasks);
    }

    // 📌 Recupera i task assegnati a un utente
    @GetMapping("/assegnati/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByUser(userId);
        return tasks.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<List<Task>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }



}
