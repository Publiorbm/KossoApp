package com.cplcursos.java.kosso.services;

import com.cplcursos.java.kosso.entities.RespuestaEjOpMul;
import com.cplcursos.java.kosso.repositories.RespuestaEjOpMulRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RespuestaEjOpMulSrvc {
    @Autowired
    private RespuestaEjOpMulRepo respuestaEjOpMulRepo;

    public List<RespuestaEjOpMul> findAll() {
        return respuestaEjOpMulRepo.findAll();
    }

    public Optional<RespuestaEjOpMul> findById(Long id) {
        return respuestaEjOpMulRepo.findById(id);
    }

    public RespuestaEjOpMul save(RespuestaEjOpMul respuestaEjOpMul) {
        return respuestaEjOpMulRepo.save(respuestaEjOpMul);
    }

    public void deleteById(Long id) {
        respuestaEjOpMulRepo.deleteById(id);
    }
}
