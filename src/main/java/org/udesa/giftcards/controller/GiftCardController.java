package org.udesa.giftcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udesa.giftcards.model.GifCardFacade;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/giftcards")
@Tag(name = "GiftCards", description = "API para gestión de tarjetas de regalo")
public class GiftCardController {

    @Autowired
    private GifCardFacade giftCardFacade;

    @Operation(summary = "Inicia sesión y obtiene un token",
            description = "Valida usuario y contraseña contra el sistema y retorna un token UUID temporal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Nombre de usuario") @RequestParam String user,
            @Parameter(description = "Contraseña") @RequestParam String pass) {

        UUID token = giftCardFacade.login(user, pass);
        return ResponseEntity.ok(Map.of("token", token.toString()));
    }

    @Operation(summary = "Reclama una tarjeta",
            description = "Asocia una tarjeta de regalo al usuario dueño del token actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarjeta reclamada correctamente"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Tarjeta ya tiene dueño (Conflict)"),
            @ApiResponse(responseCode = "500", description = "Error interno o token inválido")
    })
    @PostMapping("/{cardId}/redeem")
    public ResponseEntity<String> redeemCard(
            @Parameter(description = "Token Bearer (Auth)") @RequestHeader("Authorization") String header,
            @Parameter(description = "Número de serie de la tarjeta (ej: GC1)") @PathVariable String cardId) {

        UUID token = extractToken(header);
        giftCardFacade.redeem(token, cardId);
        return ResponseEntity.ok("Card redeemed");
    }

    @Operation(summary = "Consulta saldo",
            description = "Devuelve el saldo actual de una tarjeta propia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo obtenido",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Tarjeta no pertenece al usuario"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<Map<String, Object>> balance(
            @Parameter(description = "Token Bearer (Auth)") @RequestHeader("Authorization") String header,
            @Parameter(description = "Número de serie de la tarjeta") @PathVariable String cardId) {

        UUID token = extractToken(header);
        int balance = giftCardFacade.balance(token, cardId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @Operation(summary = "Ver movimientos",
            description = "Lista los detalles de gastos de una tarjeta propia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @GetMapping("/{cardId}/details")
    public ResponseEntity<Map<String, Object>> details(
            @Parameter(description = "Token Bearer (Auth)") @RequestHeader("Authorization") String header,
            @Parameter(description = "Número de serie de la tarjeta") @PathVariable String cardId) {

        UUID token = extractToken(header);
        List<String> movements = giftCardFacade.details(token, cardId);
        return ResponseEntity.ok(Map.of("details", movements));
    }

    @Operation(summary = "Realizar cargo (Merchant)",
            description = "Un comercio registrado realiza un cobro sobre una tarjeta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cargo aplicado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comercio o Tarjeta no encontrados"),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente o tarjeta sin dueño"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @PostMapping("/{cardId}/charge")
    public ResponseEntity<String> charge(
            @Parameter(description = "Código del comercio (ej: M1)") @RequestParam String merchant,
            @Parameter(description = "Monto a cobrar") @RequestParam int amount,
            @Parameter(description = "Descripción de la compra") @RequestParam String description,
            @Parameter(description = "Número de serie de la tarjeta") @PathVariable String cardId) {

        giftCardFacade.charge(merchant, cardId, amount, description);
        return ResponseEntity.ok("Charge applied");
    }

    // Manejo de Excepciones Global (Igual que en TusLibros)
    @ExceptionHandler(RuntimeException.class)
    @ApiResponse(responseCode = "500", description = "Error del servidor capturado")
    public ResponseEntity<String> handleRuntimeExceptions(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    private UUID extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException(GifCardFacade.InvalidToken);
        }
        try {
            return UUID.fromString(header.substring(7));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(GifCardFacade.InvalidToken);
        }
    }
}