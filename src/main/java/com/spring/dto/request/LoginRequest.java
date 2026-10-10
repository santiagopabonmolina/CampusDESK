package com.spring.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El formato del correo no es valido")
    private String email;

    @NotBlank(message = "La contraseño es obligatorio")
    private String password;
}
