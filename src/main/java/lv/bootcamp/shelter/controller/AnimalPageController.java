package lv.bootcamp.shelter.controller;

import lombok.RequiredArgsConstructor;
import lv.bootcamp.shelter.form.AnimalForm;
import lv.bootcamp.shelter.repository.AnimalRepository;
import lv.bootcamp.shelter.service.AnimalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@Controller
@RequiredArgsConstructor
public class AnimalPageController {
    private final AnimalService animalService;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/animals")
    public String listAnimals(Model model, Authentication authentication){
        model.addAttribute("animals", animalService.findAll());
        model.addAttribute("isAdmin", hasRole(authentication, "ROLE_ADMIN"));
        model.addAttribute("isUser", hasRole(authentication, "ROLE_USER"));
        return "animals";
    }

    @GetMapping("/animals/new")
    public String newAnimalForm(Model model){
        model.addAttribute("form", new AnimalForm(null, null, null, null, null, null));
        return "animals-new";

    }

    @PostMapping("/animals")
    public String createAnimal(@ModelAttribute AnimalForm form){
        animalService.createFromForm(form);
        return "redirect:/animals";
    }

    private Object hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
