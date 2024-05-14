package com.devhub.api.controller;

import com.devhub.api.domain.contratante.*;
import com.devhub.api.domain.contratante.dto.CreateContratanteDTO;
import com.devhub.api.domain.contratante.dto.DetailContratanteDTO;
import com.devhub.api.domain.contratante.dto.ListContratanteDTO;
import com.devhub.api.domain.contratante.dto.UpdateContratanteDTO;
import com.devhub.api.domain.freelancer.dto.PerfilFreelancerDTO;
import com.devhub.api.service.ContratanteService;
import com.devhub.api.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/contratantes", produces = {"application/json"})
@Tag(name = "Api DevHub")
@EnableAsync
public class ContratanteController {

    @Autowired
    private ContratanteService service;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Realiza a criação do Contratante", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contratante criado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a criaçao de um novo contratante"),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createContratante(@Valid @RequestBody CreateContratanteDTO data, UriComponentsBuilder uriBuilder) {
        var contratante = service.cadastrarContratante(data);
        enviarEmailAssincrono(data);
        var uri = uriBuilder.path("/contratantes/{id}").buildAndExpand(contratante.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailContratanteDTO(contratante));
    }

    @Async
    public void enviarEmailAssincrono(CreateContratanteDTO data) {
        String mensagem = "Olá " + data.nome() + ",\n\n" +
                "Seja muito bem-vindo à plataforma DevHub!\n\n" +
                "Estamos muito felizes em tê-lo conosco e gostaríamos de confirmar o seu cadastro em nossa plataforma.\n\n" +
                "Se você tiver alguma dúvida ou precisar de assistência, não hesite em entrar em contato conosco.\n\n" +
                "Esperamos que você aproveite ao máximo a sua experiência na DevHub!\n\n" +
                "Atenciosamente,\n" +
                "Equipe DevHub";
        emailService.enviarEmailTexto(data.email(), "Seja bem vindo a DevHub", mensagem);
    }

    @Operation(summary = "Realiza a listagenm dos Contratantes", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contratante listados com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a listagem dos contratantes"),
    })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListContratanteDTO>> listar() {
        var page = service.getContratantes();
        return ResponseEntity.ok(page);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ListContratanteDTO> listarFreelancerById(@PathVariable Long id) {
        ListContratanteDTO contratante = service.getContratanteById(id);
        return ResponseEntity.ok(contratante);
    }

    @Operation(summary = "Realiza a atualizaçao de um dos Contratantes", method = "PUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contratante atualizado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a atualizaçao do contratante"),
    })
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DetailContratanteDTO> atualizar(@Valid @RequestBody UpdateContratanteDTO data, @PathVariable Long id) {
        var contratante = service.atualizar(data, id);
        return ResponseEntity.ok(new DetailContratanteDTO(contratante));
    }

    @Operation(summary = "Realiza a exclusao de um contratante", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contratante excluido com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro ao realizar a exclusao de um contratante"),
    })
    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

//    @Transactional
//    @Operation(summary = "Realiza a ativaçao de uma conta de Contratante", method = "PATCH")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
//            @ApiResponse(responseCode = "422", description = "Dados de requisição inválida"),
//            @ApiResponse(responseCode = "400", description = "Parametros inválidos"),
//            @ApiResponse(responseCode = "500", description = "Erro ao realizar a ativação da conta"),
//    })
//    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity ativarConta(@PathVariable Long id) {
//        var contratante = repository.getReferenceById(id);
//        contratante.ativarConta();
//        return ResponseEntity.noContent().build();
//    }

    @PatchMapping(value = "/foto/{codigo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> patchFoto(@PathVariable Long codigo,
                                          @RequestPart("image") MultipartFile novaFoto) throws IOException {
        return ResponseEntity.status(service.atualizarFoto(novaFoto, codigo)).build();
    }

    @GetMapping(value = "/foto/{codigo}")
    public ResponseEntity<byte[]> getFoto(@PathVariable int codigo) {
        return ResponseEntity.status(200).body(service.getFoto(codigo));
    }
}
