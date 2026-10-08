package com.servicoja.infra.config;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Empresas de exemplo para desenvolvimento e testes. Nunca roda em producao: as contas criadas
 * aqui usam uma senha conhecida. As categorias usadas vem da migracao V3.
 */
@Component
@Profile("!prod")
public class DadosExemploSeed implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DadosExemploSeed.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder codificador;
    private final String cidadePadrao;
    private final String ufPadrao;

    public DadosExemploSeed(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder codificador,
            @Value("${servico-ja.app.cidade-padrao}") String cidadePadrao,
            @Value("${servico-ja.app.uf-padrao}") String ufPadrao) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.empresaRepository = empresaRepository;
        this.codificador = codificador;
        this.cidadePadrao = cidadePadrao;
        this.ufPadrao = ufPadrao;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Map<String, String>> empresas = List.of(
                Map.of("nome", "Elétrica Silva", "categoria", "Eletricista",
                        "email", "eletrica.silva@exemplo.com", "telefone", "(54) 3342-1001",
                        "descricao", "Instalacoes e reparos eletricos em Marau e regiao.",
                        "endereco", "Rua das Flores", "numero", "120", "bairro", "Centro"),
                Map.of("nome", "Hidráulica Marau", "categoria", "Encanador",
                        "email", "hidraulica.marau@exemplo.com", "telefone", "(54) 3342-1002",
                        "descricao", "Desentupimentos, vazamentos e instalacoes hidraulicas.",
                        "endereco", "Av. Julio Borella", "numero", "45", "bairro", "Centro"),
                Map.of("nome", "Beleza & Cia", "categoria", "Salão de Beleza",
                        "email", "beleza.cia@exemplo.com", "telefone", "(54) 3342-1003",
                        "descricao", "Cabeleireira, manicure e estetica facial.",
                        "endereco", "Rua Camilo Cimadon", "numero", "88", "bairro", "Centro"),
                Map.of("nome", "Mecânica do Zé", "categoria", "Automecânica",
                        "email", "mecanica.ze@exemplo.com", "telefone", "(54) 3342-1004",
                        "descricao", "Troca de oleo, freios e manutencao geral.",
                        "endereco", "Rod. RS-324", "numero", "km 6", "bairro", "Zona Rural"),
                Map.of("nome", "Reparos Rápidos", "categoria", "Marido de Aluguel",
                        "email", "reparos.rapidos@exemplo.com", "telefone", "(54) 3342-1005",
                        "descricao", "Montagem de moveis, quadros e pequenos reparos.",
                        "endereco", "Rua Emilio Seleme", "numero", "230", "bairro", "Santa Helena"));

        for (Map<String, String> dado : empresas) {
            criarEmpresaExemplo(dado);
        }
        LOGGER.info("Dados de exemplo carregados com sucesso.");
    }

    private void criarEmpresaExemplo(Map<String, String> dado) {
        String email = dado.get("email");
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Categoria categoria = categoriaRepository.findFirstByNomeIgnoreCase(dado.get("categoria"));
        if (categoria == null) {
            LOGGER.warn("Categoria \"{}\" nao encontrada; empresa de exemplo \"{}\" ignorada.",
                    dado.get("categoria"), dado.get("nome"));
            return;
        }

        Usuario dono = new Usuario();
        dono.setNome("Responsável - " + dado.get("nome"));
        dono.setEmail(email);
        dono.setSenha(codificador.encode("senha123"));
        dono.setTelefone(dado.get("telefone"));
        dono.setPerfil(Perfil.EMPRESA);
        usuarioRepository.save(dono);

        Empresa empresa = new Empresa();
        empresa.setUsuario(dono);
        empresa.setCategoria(categoria);
        empresa.setNome(dado.get("nome"));
        empresa.setDescricaoCurta(dado.get("descricao"));
        empresa.setTelefone(dado.get("telefone"));
        empresa.setWhatsapp(dado.get("telefone"));
        empresa.setEmailContato(email);
        empresa.setEndereco(dado.get("endereco"));
        empresa.setNumero(dado.get("numero"));
        empresa.setBairro(dado.get("bairro"));
        empresa.setCidade(cidadePadrao);
        empresa.setUf(ufPadrao);
        empresa.setAprovada(true);
        empresaRepository.save(empresa);
    }
}
