package com.devhub.api.controller;

import com.devhub.api.domain.especialidade.EspecialidadeDTO;
import com.devhub.api.domain.freelancer.*;
import com.devhub.api.domain.freelancer.dto.*;
import com.devhub.api.service.EmailService;
import com.devhub.api.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/freelancers")
@Tag(name = "Api DevHub")
@EnableAsync
public class FreelancerController {

    @Autowired
    private FreelancerService service;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Realiza a criação do Freelancer", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancer criado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a validação do token"),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createFreelancer(@Valid @RequestBody CreateFreelancerDTO data, UriComponentsBuilder uriBuilder) {
        var freelancer = service.cadastrarFreelancer(data);
        enviarEmailAssincrono(data);
        var uri = uriBuilder.path("/freelancers/{id}").buildAndExpand(freelancer.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailFreelancerDTO(freelancer));
    }

    @Async
    public void enviarEmailAssincrono(CreateFreelancerDTO data) {
        String mensagem = "Olá " + data.nome() + ",\n\n" +
                "Seja muito bem-vindo à plataforma DevHub!\n\n" +
                "Estamos muito felizes em tê-lo conosco e gostaríamos de confirmar o seu cadastro em nossa plataforma.\n\n" +
                "Se você tiver alguma dúvida ou precisar de assistência, não hesite em entrar em contato conosco.\n\n" +
                "Esperamos que você aproveite ao máximo a sua experiência na DevHub!\n\n" +
                "Atenciosamente,\n" +
                "Equipe DevHub";
        emailService.enviarEmailTexto(data.email(), "Seja bem vindo a DevHub", mensagem);
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
    @GetMapping
    public ResponseEntity<List<ListaFreelancerDTO>> listar() {
        var page = service.getFreelancers();
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Realiza a listagenm de um Freelancer especifico por ID", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Freelancer listado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a listagem do freelancer respectivo"),
    })

    @GetMapping(value = "/{id}")
    public ResponseEntity<PerfilFreelancerDTO> listarFreelancerById(@PathVariable Long id) {
        var freelancer = service.getFreelancerById(id);
        return ResponseEntity.ok(freelancer);
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

//    @Transactional
//    @Operation(summary = "Realiza a ativaçao de uma conta de Freelancer", method = "PATCH")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
//            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
//            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
//            @ApiResponse(responseCode = "500", description = "Erro ao realizar a ativaçao da conta"),
//    })
//
//    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity ativarConta(@PathVariable Long id) {
//        var freelancer = repository.getReferenceById(id);
//        freelancer.ativarConta();
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping(value = "/foto/{codigo}")
    public ResponseEntity<byte[]> getFoto(@PathVariable int codigo) {
        return ResponseEntity.status(200).body(service.getFoto(codigo));
    }

    @PatchMapping(value = "/foto/{codigo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> patchFoto(@PathVariable Long codigo,
                                          @RequestPart("image") MultipartFile novaFoto) throws IOException {
        return ResponseEntity.status(service.atualizarFoto(novaFoto, codigo)).build();
    }

    @GetMapping(value = "/benchmarking-one", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PerfilFreelancerDTO>> getFreelancerBySearch(@RequestParam String filter) {
        var freelancers = service.getFreelancersBySearch(filter);
        return freelancers.isEmpty() ?
            ResponseEntity.status(204).build() :
            ResponseEntity.status(200).body(freelancers);
    }

    @GetMapping(value = "/benchmarking-two", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PerfilFreelancerDTO>> compareFreelancers(
            @RequestBody List<EspecialidadeDTO> filters,
            @RequestParam Long compareTo) {
        var freelancers = service.compareFreelancers(filters, compareTo);
        return freelancers.isEmpty() ?
                ResponseEntity.status(204).build() :
                ResponseEntity.status(200).body(freelancers);
    }
}
