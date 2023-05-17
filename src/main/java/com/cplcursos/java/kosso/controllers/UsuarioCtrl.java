package com.cplcursos.java.kosso.controllers;

import com.cplcursos.java.kosso.DTO.UsuarioDTO;
import com.cplcursos.java.kosso.MyUserDetails;
import com.cplcursos.java.kosso.entities.Usuario;
import com.cplcursos.java.kosso.services.UsuarioSrvcImpl;
import com.cplcursos.java.kosso.utils.FileUploadUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/usuario")
public class UsuarioCtrl {

    @Autowired
    private UsuarioSrvcImpl usuSrvc;

    @GetMapping("/listausus")
    public String listaUsus(Model modelo, @AuthenticationPrincipal MyUserDetails userDetails){

        modelo.addAttribute("usuario", usuSrvc.findByAuth(userDetails));

        modelo.addAttribute("listausuarios", usuSrvc.findAll());
        return "perfilesYUsuarios/listaUsus";
    }

    @GetMapping("/ranking")
    public String ranking(Model modelo, @AuthenticationPrincipal MyUserDetails userDetails){

        modelo.addAttribute("usuario", usuSrvc.findByAuth(userDetails));

        List<Usuario> listaNoOrdenada = usuSrvc.findAll();
        List<Usuario> listaOrdenada = usuSrvc.ordenarPorPuntos(listaNoOrdenada);

        modelo.addAttribute("listaOrdenada", listaOrdenada);
        return "perfilesYUsuarios/ranking";
    }
    @GetMapping(value={"/perfil", "", "/"})
    public String Perfil(Model modelo, @AuthenticationPrincipal MyUserDetails userDetails) {

        Usuario usu = usuSrvc.findByAuth(userDetails);
        String nombreCompleto = usu.getNombre() + " " + usu.getApellidos();

        modelo.addAttribute("usuario", usu);
        modelo.addAttribute("nombreCompleto", nombreCompleto);
        return "perfilesYUsuarios/perfil";
    }

    @GetMapping(value={"/perfil/{id}"})
    public String mostrarUsuario(@PathVariable("id") Long id, @AuthenticationPrincipal MyUserDetails userDetails , Model modelo) {
        Optional<Usuario> usu = usuSrvc.findById(id);
        if (usu.isPresent()){
            if (usuSrvc.findByEmail(userDetails.getUsername()).equals(usu.get())){
                return "redirect:/usuario/perfil";
            }else {
                modelo.addAttribute("usuario", usuSrvc.findByAuth(userDetails));
                modelo.addAttribute("usuarioPerfil", usu.get());
                return "perfilesYUsuarios/usuario";
            }
        }else {
            return "error/error";
        }
    }

    @GetMapping("/registro")
    public String registro(Model modelo) {
        UsuarioDTO usuDTO = new UsuarioDTO();
        modelo.addAttribute("usuarioDto", usuDTO);
        return "registro";
    }

    @PostMapping("/registro")
    public String registerUserAccount(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO,
                                            BindingResult result, Model modelo) {

        Usuario yaRegistrado = usuSrvc.findByEmail(usuarioDTO.getEmail());

        if (yaRegistrado != null) {
            result.rejectValue("email", null, "Ya existe un usuario registrado con ese email");

        }

        if(result.hasErrors()){
            modelo.addAttribute("usuario", usuarioDTO);
            return "registro";
        }

        usuSrvc.saveDto(usuarioDTO);
        return "redirect:/usuario/registro?success";
    }

    @PostMapping ("/borrar/{id}")
    public String eliminarperfil (@PathVariable("id") Long id){
        usuSrvc.deleteById(id);
        return "redirect:/usuario/listausus";
    }

    @GetMapping("/editar")
    public String verPerfilForm (@AuthenticationPrincipal MyUserDetails userDetails, Model model){
        String email = userDetails.getUsername();
        Usuario usu = usuSrvc.findByEmail(email);

        model.addAttribute("usuario", usu);

        return "perfilesYUsuarios/editarPerfil";
    }

    @PostMapping("/editar")
    public String editarPerfil (Usuario user,
                                RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal MyUserDetails userDetails,
                                @RequestParam("image") MultipartFile foto) throws IOException {

        if(!foto.isEmpty()){
            String fileName = StringUtils.cleanPath(foto.getOriginalFilename());
            user.setFoto(fileName);
            Usuario usuGuardado = usuSrvc.save(user);

            String uploadDir = "src/main/resources/static/image/user-photos/" + usuGuardado.getId();
            FileUploadUtil.saveFile(uploadDir, fileName, foto);

            String uploadDir2 = "target/classes/static/image/user-photos/" + usuGuardado.getId();
            FileUploadUtil.saveFile(uploadDir2, fileName, foto);
        }else{
            if(user.getFoto().isEmpty()) user.setFoto(null);
            usuSrvc.save(user);
        }

        userDetails.setFirstName(user.getNombre());
        userDetails.setLastName(user.getApellidos());
        userDetails.setClave(user.getClave());
        userDetails.setEmail(user.getEmail());
        userDetails.setFoto(user.getFoto());
        userDetails.setDescripcion(user.getDescripcion());
        userDetails.setClave(user.getClave());


        redirectAttributes.addAttribute("message", "Perfil actualizado correctamente");

        return "redirect:/usuario/perfil?success";
    }
}
