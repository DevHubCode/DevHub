package com.devhub.api.controller;

import com.devhub.api.domain.especialidade.EspecialidadeDTO;
import com.devhub.api.domain.especialidade.EspecialidadeRepository;
import com.devhub.api.domain.especialidade.EspecialidadesEnum;
import com.devhub.api.domain.freelancer.*;
import com.devhub.api.domain.freelancer.dto.CreateFreelancerDTO;
import com.devhub.api.domain.freelancer.dto.DetailFreelancerDTO;
import com.devhub.api.domain.freelancer.dto.ListFreelancerDTO;
import com.devhub.api.domain.freelancer.dto.UpdateFreelancerDTO;
import com.devhub.api.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/freelancers", produces = {"application/json"})
@Tag(name = "Api DevHub")
public class FreelancerController {

    @Autowired
    private FreelancerService service;
    @Autowired
    private FreelancerRepository repository;

    @Operation(summary = "Realiza a criaçâo do freelancer", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancer criado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a validação do token"),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createFreelancer(@Valid @RequestBody CreateFreelancerDTO data, UriComponentsBuilder uriBuilder) {
        var freelancer = service.cadastrarFreelancer(data);
        var uri = uriBuilder.path("/freelancers/{id}").buildAndExpand(freelancer.getId()).toUri();
        return ResponseEntity.created(uri).body(freelancer);
    }

    @PostMapping("/{id}/especialidades")
    public ResponseEntity createEspecialidades(@RequestBody List<String> data, @PathVariable Long id) {
        var especialidades = service.cadastrarEspecialidades(data, id);
        return ResponseEntity.status(201).body(especialidades);
    }

    @Operation(summary = "Realiza a listagenm dos Freelancers", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancers listados com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a listagem dos Freelancers"),
    })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ListFreelancerDTO>> listar(@PageableDefault(size = 5, sort = {"nome"}) Pageable paginacao) {
        var page = service.getFreelancers(paginacao);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Realiza a listagenm de um Freelancer especifico por ID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancer listado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a listagem do freelancer respectivo"),
    })

    @GetMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListFreelancerDTO> listarFreelancerById(@PathVariable Long id) {
        var freelancer = service.getFreelancerById(id);
        return ResponseEntity.ok(new ListFreelancerDTO(freelancer));
    }

    @Operation(summary = "Realiza a atualização de um dos freelancers", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancer atualizado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a atualizaçao do freelancer"),
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DetailFreelancerDTO> atualizarFreelancer(@Valid @RequestBody UpdateFreelancerDTO data, @PathVariable Long id) {
        var freelancer = service.atualizar(data, id);
        return ResponseEntity.ok(new DetailFreelancerDTO(freelancer));
    }

    @Operation(summary = "Realiza a exclusao de um freelancer", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancer excluido com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a exclusão de um Freelancer"),
    })
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @Operation(summary = "Realiza a ativaçao de uma conta de Freelancer", method = "PATCH")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a ativaçao da conta"),
    })

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity ativarConta(@PathVariable Long id) {
        var freelancer = repository.getReferenceById(id);
        freelancer.ativarConta();
        return ResponseEntity.noContent().build();
    }
}
