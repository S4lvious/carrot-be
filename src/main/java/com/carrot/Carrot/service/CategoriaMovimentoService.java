package com.carrot.Carrot.service;

import com.carrot.Carrot.model.CategoriaMovimento;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.CategoriaMovimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaMovimentoService {

    @Autowired
    private CategoriaMovimentoRepository categoriaMovimentoRepository;

    /**
     * Recupera tutte le categorie di un utente.
     */
    public List<CategoriaMovimento> getCategorieByUser(User user) {
        return categoriaMovimentoRepository.findByUser(user);
    }

    /**
     * Trova una categoria per ID.
     */
    public Optional<CategoriaMovimento> getCategoriaById(Long id) {
        return categoriaMovimentoRepository.findById(id);
    }

    /**
     * Crea una nuova categoria per un utente.
     */
    public CategoriaMovimento createCategoria(String nome, User user) {
        CategoriaMovimento categoria = new CategoriaMovimento();
        categoria.setNome(nome);
        categoria.setUser(user);
        return categoriaMovimentoRepository.save(categoria);
    }

    /**
     * Aggiorna una categoria esistente.
     */
    public CategoriaMovimento updateCategoria(Long id, String nuovoNome) {
        Optional<CategoriaMovimento> optionalCategoria = categoriaMovimentoRepository.findById(id);

        if (optionalCategoria.isPresent()) {
            CategoriaMovimento categoria = optionalCategoria.get();
            categoria.setNome(nuovoNome);
            return categoriaMovimentoRepository.save(categoria);
        } else {
            throw new RuntimeException("Categoria non trovata con ID: " + id);
        }
    }

    /**
     * Elimina una categoria per ID.
     */
    public void deleteCategoria(Long id) {
        if (categoriaMovimentoRepository.existsById(id)) {
            categoriaMovimentoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Categoria non trovata con ID: " + id);
        }
    }

    /**
     * Conta le categorie di un utente.
     */
    public long countCategorieByUser(User user) {
        return categoriaMovimentoRepository.countByUser(user);
    }
}
