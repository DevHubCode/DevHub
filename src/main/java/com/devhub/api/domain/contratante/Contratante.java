package com.devhub.api.domain.contratante;

import com.devhub.api.domain.contratante.dto.CreateContratanteDTO;
import com.devhub.api.domain.contratante.dto.UpdateContratanteDTO;
import com.devhub.api.domain.publicacao.Publicacao;
import com.devhub.api.domain.usuario.UserRole;
import com.devhub.api.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Table(name = "contratante")
@Entity
@Getter
@Setter
public class Contratante extends Usuario {

    private String cnpj;
    public Contratante() { super(); }

    public Contratante(CreateContratanteDTO data) {
        super(data.nome(), data.telefone(), data.email(), data.senha(), UserRole.CONTRATANTE);
        this.cnpj = data.cnpj();
    }

    public void atuallizarInformacoes(UpdateContratanteDTO data) {
        if (data.nome() != null) {
            this.nome = data.nome();
        }
        if (data.telefone() != null) {
            this.telefone = data.telefone();
        }
        if(data.email() != null){
            this.email = data.email();
        }
        if(data.senha() != null){
            this.senha = new BCryptPasswordEncoder().encode(data.senha());;
        }

    }

    public void excluir() {
        this.ativo = false;
    }

    public void ativarConta() {
        this.ativo = true;
    }

}
