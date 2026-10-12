package com.spring.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El nombre es obligatorio ")
    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio ")
    @Size(max = 150, message = "El apellido no debe superar los 150 caracteres")
    private String lastName;

    @NotBlank(message = "El correo es obligatorio ")
    @Email(message = "El formato del correo no es valido")
    @Size(max = 150, message = "El correo no debe superar los 150 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 150, message = "La contraseña debe tener entre 6 y 150 caracteres")
    private String password;
}
