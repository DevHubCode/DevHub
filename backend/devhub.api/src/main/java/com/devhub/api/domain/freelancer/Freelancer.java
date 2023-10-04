package com.devhub.api.domain.freelancer;

import com.devhub.api.domain.especialidades.Especialidades;
import com.devhub.api.domain.especialidades.EspecialidadesData;
import com.devhub.api.domain.freelancer.CreateFreelancerData;
import com.devhub.api.domain.funcao.Funcao;
import com.devhub.api.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Table(name="freelancers")
@Entity(name="Freelancer")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Freelancer extends Usuario{
    private String cpf;
    @Enumerated(EnumType.STRING)
    private Funcao funcao;
    private Double valorHora;

//    @Embedded
//    private Especialidades especialidades;
    private String descricao;


    public Freelancer(CreateFreelancerData data) {
        super();
        this.cpf = data.cpf();
        this.funcao = data.funcao();
        this.valorHora = data.valorHora();
        this.descricao = data.descricao();
        //       TODO: validar associativa entre tecnologia e freelancer
        //        this.especialidades = new Especialidades(data.especialidades());
    }

    public void excluir(){
        this.ativo = false;
    }

    public void ativarConta(){
        this.ativo = true;
    }

}
