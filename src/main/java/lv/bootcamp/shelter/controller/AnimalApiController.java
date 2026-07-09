package lv.bootcamp.shelter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.service.AnimalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for shelter animal endpoints.
 * Returns JSON — does not render HTML pages.
 */
@Tag(name= "Animals", description = "Adopt animals at this shelter")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/animals")
public class AnimalApiController {

    private final AnimalService animalService;

    @Operation(summary = "Get all the animals", description = "Return all of the animals")
    @ApiResponse(responseCode = "200", description = "List returned")
    @GetMapping
    public List<AnimalResponse> findAll() {
        return animalService.findAll();
    }

    @Operation(summary = "Returns one animal by its ID", description = "Returns a single animal's full details")
    @ApiResponse(responseCode = "200", description = "Animal found")
    @ApiResponse(responseCode = "404", description = "No animal with that ID", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> findById(@PathVariable Long id) {
        return animalService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists adopted animals. Restricted to ROLE_ADMIN — see SecurityConfig.
     * Read-only, so it's a good endpoint for testing role-based JWT authorization:
     * calling it repeatedly (e.g. with/without a token, or with a ROLE_USER token)
     * has no side effects, unlike {@code POST /api/animals}.
     */
    @Operation(summary = "Get all adopted animals", description = "Admin only, returns every animal that is adopted")
    @ApiResponse(responseCode = "200", description = "List of adopted animals")
    @GetMapping("/adopted")
    public List<AnimalResponse> findAdopted() {
        return animalService.findAdopted();
    }

    /**
     * Creates a new animal. Restricted to ROLE_ADMIN — see SecurityConfig.
     */

    @Operation(summary = "Register a new animal", description = "Admin only, creates a new animal.")
    @ApiResponse(responseCode = "201", description = "Animal registered")
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalResponse create(@RequestBody @Valid AnimalCreateRequest request) {
        return animalService.create(request);
    }

    /**
     * Adopts an animal as the currently logged-in user. Restricted to ROLE_USER
     * (not ROLE_ADMIN) — see SecurityConfig.
     */
    @Operation(summary = "Adopt an animal", description = "User only, marks the animal ADOPTED."
    )
    @ApiResponse(responseCode = "200", description = "Adopted successfully")
    @ApiResponse(responseCode = "404", description = "No animal with that ID", content = @Content)
    @PostMapping("/{id}/adopt")
    public ResponseEntity<AnimalResponse> adopt(@PathVariable Long id, Authentication authentication) {
        return animalService.adopt(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleAlreadyAdopted(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
