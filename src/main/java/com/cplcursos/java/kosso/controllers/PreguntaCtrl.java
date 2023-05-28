package com.cplcursos.java.kosso.controllers;

import com.cplcursos.java.kosso.MyUserDetails;
import com.cplcursos.java.kosso.entities.*;
import com.cplcursos.java.kosso.repositories.PreguntaPaginacionRepo;
import com.cplcursos.java.kosso.services.*;
import com.cplcursos.java.kosso.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Log4j2
@RequestMapping("/preguntas")
public class PreguntaCtrl {
    @Autowired
    private PreguntaSrvc preguntaSrvc;

    @Autowired
    private RespuestaSrvc respuestaSrvc;

    @Autowired
    private ComentarioSrvc comentarioSrvc;

    @Autowired
    private CategoriaSrvc categoriaSrvc;

    @Autowired
    private PreguntaPaginacionRepo preguntaPaginacionRepo;

    @Autowired
    private UsuarioSrvcImpl usuSrvc;

    @Autowired
    private PuntosForoSrvc puntosForoSrvc;


    @GetMapping(value = {"/", ""})
    public String mostrarPreguntas (@ModelAttribute("search") String search, @ModelAttribute("filtro") String filtro, Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size,RedirectAttributes redirectAttributes){
        if(usuSrvc.isAuthenticated()){
            return "redirect:/preguntas/logged";
        }

        return mostrarForo(null, search, filtro, model, page, size, redirectAttributes);
    }

    @GetMapping(value = {"/logged", "/logged/"})
    public String mostrarPreguntasLogged (@AuthenticationPrincipal MyUserDetails userDetails, @ModelAttribute("search") String search, @ModelAttribute("filtro") String filtro, Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuSrvc.findByAuth(userDetails);

        return mostrarForo(usuario, search, filtro, model, page, size, redirectAttributes);
    }

    private String mostrarForo(Usuario usuario, String search, String filtro, Model model, Optional<Integer> page, Optional<Integer> size, RedirectAttributes redirectAttributes){
        if(usuario != null){
            model.addAttribute("usuario", usuario);
        }

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("fechaPregunta").descending());

        if (filtro != null) {
            switch (filtro){
                case "oldest":
                    pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("fechaPregunta").ascending());
                    break;
                case "votos":
                    //pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("votos").ascending());

                    break;
                default:
            }
        }

        Page<Pregunta> paginaPreguntas;
        if ((search != null) && (!search.equals(""))){
            paginaPreguntas = preguntaPaginacionRepo.findAllByTituloPreguntaContainingOrTextoPreguntaContaining(search, search, pageable);
        } else {
            paginaPreguntas = preguntaPaginacionRepo.findAll(pageable);
        }

        model.addAttribute("paginaPreguntas", paginaPreguntas);
        model.addAttribute("filtro", filtro);

        int totalPaginas = paginaPreguntas.getTotalPages();
        if (totalPaginas > 0) {
            List<Integer> numeroPaginas = IntStream.rangeClosed(1, totalPaginas)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("numeroPaginas", numeroPaginas);
        }
        return "preguntas/pregunta-list";
    }

    @GetMapping("/preguntas/preguntaPublicada/{id}")
    public String redirectPregunta(){
        return "redirect:/preguntas/preguntaPublicada/{id}";
    }

    @PostMapping(value = "/search")
    public String busqueda(@ModelAttribute("search") String search, @ModelAttribute("filtro") String filtro, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("search", search);
        redirectAttributes.addFlashAttribute("filtro", filtro);
        if (usuSrvc.isAuthenticated()){
        return "redirect:/preguntas/logged";
        }
        return "redirect:/preguntas";
    }

    // Muestra la pregunta publicada por su id
    @GetMapping(value = "/preguntaPublicada/{id}")
    public String verPreguntaPublicada (@PathVariable Long id, Model model){

        if (usuSrvc.isAuthenticated()){
            return "redirect:/preguntaPublicadalogged/{id}";
        }
        return verPregunta(id, model, null);
    }
    @GetMapping(value = "/preguntaPublicadalogged/{id}")
    public String verPreguntaPublicada (@PathVariable Long id, Model model, @AuthenticationPrincipal MyUserDetails userDetails){
        Usuario usuario = usuSrvc.findByAuth(userDetails);
        return verPregunta(id, model, usuario);
    }

    private String verPregunta(Long id, Model model, Usuario usuario){
        if(usuario != null){
            model.addAttribute("usuario", usuario);
        }

        Optional<Pregunta> pregunta = preguntaSrvc.findById(id);
        if(pregunta.isPresent()) {
            model.addAttribute("pregunta", pregunta.get());
            return "preguntas/preguntaPublicada";
        }
        return "redirect:/preguntas/";
    }

    // Publica la pregunta y la muestra
    @PostMapping(value = "/save")
    public String crearPregunta (@ModelAttribute("pregunta") Pregunta pregunta, @RequestParam("image") MultipartFile multipartFile, @AuthenticationPrincipal MyUserDetails userDetails) {
        pregunta.setUsuario(usuSrvc.findByEmail(userDetails.getUsername()));
        String fileName1 = multipartFile.getOriginalFilename();
        if(fileName1 == null){
            fileName1 = pregunta.getTituloPregunta() + "default.png";
        }
        String fileName = StringUtils.cleanPath(fileName1);
        pregunta.setFoto(fileName);
        String uploadDir = "src/main/resources/static/image/pregunta-photos/" + pregunta.getId();
        String uploadDir2 = "target/classes/static/image/pregunta-photos/" + pregunta.getId();
        if (fileName.isEmpty() || fileName == null){
            pregunta.setFoto(null);
            uploadDir = null;
            uploadDir2 = null;
        }
        preguntaSrvc.save(pregunta);
        if (pregunta.getFechaPregunta() == null){
            preguntaSrvc.setFecha(pregunta);
            puntosForoSrvc.puntuarPregunta(pregunta);
        }

        try {

        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
        FileUploadUtil.saveFile(uploadDir2, fileName, multipartFile);
        }catch(Exception exception){
            log.info("No se ha podido guardar la imagen en el directorio o es una pregunta sin foto" + exception);
        }
        return "redirect:/preguntas/preguntaPublicadalogged/" + pregunta.getId();
    }

// agregar al edit las categorias que ya tenía la pregunta
    @GetMapping(value = "/edit/{id}")
    public String editarPregunta( @PathVariable("id") Long id, @AuthenticationPrincipal MyUserDetails userDetails, Model model){

        model.addAttribute("usuario", usuSrvc.findByAuth(userDetails));

        Optional<Pregunta> pregunta = preguntaSrvc.findById(id);
        if(pregunta.isPresent() && usuSrvc.findByEmail(userDetails.getUsername()).equals(pregunta.get().getUsuario())){
            model.addAttribute("pregunta", pregunta.get());
            model.addAttribute("categorias", categoriaSrvc.findAll());

        }
        else{
            return "error-page";
        }
        return "preguntas/pregunta-form";
    }

    @GetMapping(value = "/new")
    public String verFormularioPregunta (Model model, @AuthenticationPrincipal MyUserDetails userDetails){

        model.addAttribute("usuario", usuSrvc.findByAuth(userDetails));

        model.addAttribute("pregunta", new Pregunta());
        model.addAttribute("categorias", categoriaSrvc.findAll());
        return "preguntas/pregunta-form";
    }

    @PostMapping("/new")
    public String guardarPregunta (@PathVariable("id") Long id, Model model){
        model.addAttribute("pregunta", preguntaSrvc.findById(id));
        return "preguntas/preguntaPublicada";
    }

    @GetMapping(value = "/delete/{id}")
    public String borrarPregunta (@PathVariable("id") Long id, @AuthenticationPrincipal MyUserDetails userDetails){
        Optional<Pregunta> pregunta = preguntaSrvc.findById(id);
        if(pregunta.isPresent() && usuSrvc.findByEmail(userDetails.getUsername()).equals(pregunta.get().getUsuario())){
        preguntaSrvc.borrarPregunta(id);}
        else {
            return "error/error";
        }
        return "redirect:/preguntas";
    }

    // Controlador para Respuestas

    @PostMapping(value = "/respuestasave")
    public String crearRespuesta (@RequestParam(name = "idPregunta") Long id, @RequestParam(name = "textoRespuesta") String textoRespuesta, @AuthenticationPrincipal MyUserDetails userDetails, Model model){
        Respuesta respuesta = new Respuesta();
        Pregunta pregunta = new Pregunta();
        pregunta.setId(id);
        respuesta.setUsuario(usuSrvc.findByEmail(userDetails.getUsername()));
        respuesta.setPregunta(pregunta);
        respuesta.setTextoRespuesta(textoRespuesta);
        respuesta.setFechaRespuesta(LocalDate.now());
        respuestaSrvc.save(respuesta);
        puntosForoSrvc.puntuarRespuesta(respuesta);
        return "redirect:/preguntas/preguntaPublicadalogged/" + id;
    }

    // Controlador para Comentarios
    @PostMapping(value = "/comentariosave")
    public String crearComentario(@RequestParam(name = "idPregunta") Long idPregunta, @RequestParam(name = "idRespuesta") Long idRespuesta, @RequestParam(name = "textoComentario") String textoComentario, @AuthenticationPrincipal MyUserDetails userDetails ,Model model){
        Comentario comentario = new Comentario();
        Respuesta respuesta = new Respuesta();
        respuesta.setId(idRespuesta);
        comentario.setUsuario(usuSrvc.findByEmail(userDetails.getUsername()));
        comentario.setRespuesta(respuesta);
        comentario.setTextoComentario(textoComentario);
        comentario.setFechaComentario(LocalDate.now());
        comentarioSrvc.save(comentario);
        puntosForoSrvc.puntuarComentario(comentario);
        return "redirect:/preguntas/preguntaPublicadalogged/" + idPregunta;
    }

}
