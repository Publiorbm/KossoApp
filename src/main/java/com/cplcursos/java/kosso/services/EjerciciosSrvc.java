package com.cplcursos.java.kosso.services;

import com.cplcursos.java.kosso.entities.EjercicioOpMul;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cplcursos.java.kosso.repositories.EjercicioRepo;

import java.util.List;
import java.util.Optional;

@Service
public class EjerciciosSrvc {
    @Autowired
    private EjercicioRepo ejerciciosRepository;

    public List<EjercicioOpMul> findAll() {
        return ejerciciosRepository.findAll();
    }

    public Optional<EjercicioOpMul> findById(Long id) {
        return ejerciciosRepository.findById(id);
    }

    public EjercicioOpMul save(EjercicioOpMul ejercicioOpMul) {
        return ejerciciosRepository.save(ejercicioOpMul);
    }

    public void deleteById(Long id) {
        ejerciciosRepository.deleteById(id);
    }

    //Método para encontrar el siguiente ejercicio
    public Optional<EjercicioOpMul> findNextEjercicio(Long id) {
        Long nextId = id + 1;
        Optional<EjercicioOpMul> nextEj = ejerciciosRepository.findById(nextId);

        while(!nextEj.isPresent() && nextId > 0 && nextId < 500) {
            nextId++;
            nextEj = ejerciciosRepository.findById(nextId);
        }

        return nextEj;
    }

}
