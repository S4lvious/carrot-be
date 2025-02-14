package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.*;
import com.carrot.Carrot.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categorie")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorie() {
        return ResponseEntity.ok(categoriaService.getAllCategorie());
    }

    @PostMapping
    public ResponseEntity<Void> addCategoria(@RequestBody Categoria categoria) {
        categoriaService.addCategoria(categoria);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateCategoria(@RequestBody Categoria categoria) {
        categoriaService.updateCategoria(categoria);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        categoriaService.deleteCategoria(id);
        return ResponseEntity.ok().build();
    }
}
