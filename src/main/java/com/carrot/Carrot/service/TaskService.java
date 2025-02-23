package com.carrot.Carrot.service;
import com.carrot.Carrot.model.Progetto;
import com.carrot.Carrot.model.Task;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.ProgettoRepository;
import com.carrot.Carrot.repository.TaskRepository;
import com.carrot.Carrot.repository.UserRepository;
import com.carrot.Carrot.security.MyUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProgettoRepository progettoRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
    }

            private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }

    // 📌 Recupera tutti i task assegnati a un utente
    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssegnatoA_Id(userId);
    }

    public List<Task> getTasksByProject(Long userId) {
        return taskRepository.findByProgetto_Id(userId);
    }

    public void updateTaskProject(Long taskId, Long projectId, Long oldProjectId){
        taskRepository.updateTaskProject(taskId, projectId);
    }



    // 📌 Recupera i task di un utente in base allo stato
    public List<Task> getTasksByStatus(Long userId, String stato) {
        return taskRepository.findByStatoAndAssegnatoA_Id(stato, userId);
    }

    // 📌 Aggiungi un task
    @Transactional
    public Task addTask(Task task, Progetto progetto) {
        Long userId = getCurrentUser().getId();
        Optional<User> user = userRepository.findById(userId);
        task.getAssegnatoA().add(user.get());
        task.setProgetto(progetto);
        return taskRepository.save(task);
    }


    // 📌 Elimina un task
    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}
