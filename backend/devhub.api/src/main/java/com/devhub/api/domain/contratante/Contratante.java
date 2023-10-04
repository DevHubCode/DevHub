package com.devhub.api.domain.contratante;
import com.devhub.api.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Table(name="contratantes")
@Entity(name="Contratante")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Contratante extends Usuario {

    private String cnpj;

    public Contratante(CreateContratanteData data) {
        super();
        this.cnpj = data.cnpj();
    }

    public void excluir(){
        this.ativo = false;
    }

    public void ativarConta(){
        super.ativo = true;
    }
}
