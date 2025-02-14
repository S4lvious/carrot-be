package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Categoria;
import com.carrot.Carrot.repository.CategoriaRepository;
import com.carrot.Carrot.security.MyUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> getAllCategorie() {
        Long currentUserId = ((MyUserDetails) SecurityContextHolder
                         .getContext()
                         .getAuthentication()
                         .getPrincipal())
                         .getUser().getId();
        return categoriaRepository.findAllByUser_Id(currentUserId);
    }

    public void addCategoria(Categoria categoria) {
        // Recupera l'utente attualmente autenticato
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                                            .getPrincipal();
        categoria.setUser(userDetails.getUser());
        categoriaRepository.save(categoria);
    }


    public void updateCategoria(Categoria categoria) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
// Imposta l'utente sulla categoria
categoria.setUser(userDetails.getUser());

        if (categoriaRepository.existsById(categoria.getId())) {
            categoriaRepository.save(categoria);
        }
    }

    public void deleteCategoria(Long id) {
        // Recupera lo user corrente
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                                    .getPrincipal();
        Long currentUserId = userDetails.getUser().getId();
    
        // Cerca la categoria filtrando per id e per userId
        Optional<Categoria> categoriaOpt = Optional.ofNullable(categoriaRepository.findByIdAndUser_Id(id, currentUserId));
        if (categoriaOpt.isPresent()) {
            categoriaRepository.delete(categoriaOpt.get());
        }
    }
    }
