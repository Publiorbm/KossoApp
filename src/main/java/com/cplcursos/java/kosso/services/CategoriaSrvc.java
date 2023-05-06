package com.cplcursos.java.kosso.services;

import com.cplcursos.java.kosso.entities.Categoria;
import com.cplcursos.java.kosso.repositories.CategoriaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaSrvc {
    @Autowired
    private CategoriaRepo categoriaRepo;

    public List<Categoria> findAll() {
        return categoriaRepo.findAll();
    }

    public Optional<Categoria> findById(Integer id) {
        return categoriaRepo.findById(id);
    }

    public Categoria save(Categoria categoria) {
        return categoriaRepo.save(categoria);
    }

    public void deleteById(Integer id) {
        categoriaRepo.deleteById(id);
    }
}
