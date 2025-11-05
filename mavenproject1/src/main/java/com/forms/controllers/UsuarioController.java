/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.forms.controllers;

import com.forms.repository.UsuarioRepository;
import com.forms.models.Usuario;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author gabriela
 */
@Controller
@RequestMapping("/api/funcionarios")
public class UsuarioController {

    private final UsuarioRepository userRepo;


    public UsuarioController(UsuarioRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return this.userRepo.findAll();
    }

    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable int id) {
        return this.userRepo.findById(id).get();
    }

    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario user) {
        user.setId(0);
        
        return this.userRepo.save(user);
    }

    @PutMapping("/{id}")
    public Usuario updateUsuario(@PathVariable int id, @RequestBody Usuario user) {
        user.setId(id);

        return this.userRepo.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUsuarioById(@PathVariable int id) {    
        Usuario user = this.userRepo.findById(id).get();
        this.userRepo.delete(user);
    }

}