package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.CategoriaMovimento;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.security.MyUserDetails;
import com.carrot.Carrot.service.CategoriaMovimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorie")
public class CategoriaMovimentoController {

    @Autowired
    private CategoriaMovimentoService categoriaMovimentoService;


        private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }


    /**
     * Recupera tutte le categorie di un utente specifico.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<CategoriaMovimento>> getCategorieByUser(@PathVariable Long userId) {
        User user = getCurrentUser();
        user.setId(userId); // Creiamo un oggetto User con solo l'ID
        return ResponseEntity.ok(categoriaMovimentoService.getCategorieByUser(user));
    }

    /**
     * Crea una nuova categoria.
     */
    @PostMapping("/{userId}")
    public ResponseEntity<CategoriaMovimento> createCategoria(@PathVariable Long userId, @RequestBody CategoriaMovimento categoriaRequest) {
        User user = getCurrentUser();
        user.setId(userId);
        CategoriaMovimento categoria = categoriaMovimentoService.createCategoria(categoriaRequest.getNome(), user);
        return ResponseEntity.ok(categoria);
    }

    /**
     * Aggiorna una categoria esistente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaMovimento> updateCategoria(@PathVariable Long id, @RequestBody CategoriaMovimento categoriaRequest) {
        return ResponseEntity.ok(categoriaMovimentoService.updateCategoria(id, categoriaRequest.getNome()));
    }

    /**
     * Elimina una categoria.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        categoriaMovimentoService.deleteCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
