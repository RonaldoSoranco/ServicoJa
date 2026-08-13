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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("!prod")
public class DadosIniciaisSeed implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DadosIniciaisSeed.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder codificador;
    private final String cidadePadrao;
    private final String ufPadrao;

    public DadosIniciaisSeed(
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
        criarAdministrador();
        Map<String, Categoria> categorias = criarCategorias();
        criarEmpresasExemplo(categorias);
        LOGGER.info("Dados iniciais carregados com sucesso.");
    }

    private void criarAdministrador() {
        if (usuarioRepository.existsByEmailIgnoreCase("admin@servicoja.com.br")) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail("admin@servicoja.com.br");
        admin.setSenha(codificador.encode("admin1234"));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);
        LOGGER.info("Administrador padrao criado (admin@servicoja.com.br).");
    }

    private Map<String, Categoria> criarCategorias() {
        Map<String, Categoria> categorias = new LinkedHashMap<>();
        List<String[]> dados = List.of(
                new String[]{"Eletricista", "Servicos eletricos residenciais e comerciais", "eletricista"},
                new String[]{"Encanador", "Consertos e instalacoes hidraulicas", "encanador"},
                new String[]{"Salão de Beleza", "Cabelo, unhas e estetica", "beleza"},
                new String[]{"Automecânica", "Manutencao e reparos de veiculos", "mecanica"},
                new String[]{"Limpeza", "Limpeza residencial e comercial", "limpeza"},
                new String[]{"Marido de Aluguel", "Pequenos reparos e instalacoes", "reparos"},
                new String[]{"Costureira", "Costura, ajustes e bordados", "costura"},
                new String[]{"Designer Gráfico", "Identidade visual e artes", "design"},
                new String[]{"Professor Particular", "Aulas particulares de diversas materias", "educacao"},
                new String[]{"Pintor", "Pintura interna e externa", "pintura"});

        for (String[] dado : dados) {
            Categoria categoria = categoriaRepository.existsByNomeIgnoreCase(dado[0])
                    ? categoriaRepository.findFirstByNomeIgnoreCase(dado[0])
                    : categoriaRepository.save(novaCategoria(dado));
            categorias.put(dado[0], categoria);
        }
        return categorias;
    }

    private Categoria novaCategoria(String[] dado) {
        Categoria nova = new Categoria();
        nova.setNome(dado[0]);
        nova.setDescricao(dado[1]);
        nova.setIcone(dado[2]);
        return nova;
    }

    private void criarEmpresasExemplo(Map<String, Categoria> categorias) {
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
            String email = dado.get("email");
            if (usuarioRepository.existsByEmailIgnoreCase(email)) {
                continue;
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
            empresa.setCategoria(categorias.get(dado.get("categoria")));
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
}
