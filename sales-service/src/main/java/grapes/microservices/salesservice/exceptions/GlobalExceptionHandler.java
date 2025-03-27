package grapes.microservices.salesservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 Erreurs de validation @Valid (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 🟠 Mauvais format JSON ou valeur incorrecte (ex: texte au lieu d’un nombre)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>("Données malformées : vérifie le format du JSON ou les types de champs.", HttpStatus.BAD_REQUEST);
    }

    // 🟡 Paramètres @PathVariable ou @RequestParam manquants
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        return new ResponseEntity<>("Paramètre manquant : " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    // ⚪ Erreurs métier (par exemple une IllegalArgumentException dans un service)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>("Erreur de logique métier : " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ⚫ Erreurs génériques (à mettre tout en bas pour ne pas cacher les autres)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return new ResponseEntity<>("Une erreur est survenue : " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
