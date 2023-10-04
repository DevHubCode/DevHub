package com.devhub.api.domain.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUsuarioData(@NotBlank
                                String nome,
                                @NotBlank
                                @CPF
                                String cpf,
                                @NotBlank
                                @Pattern(regexp = "\\d{11}")
                                String telefone,
                                @Email
                                String email,
                                @NotBlank
                                String senha) {

}
