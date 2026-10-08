package com.servicoja.infra.config;

import com.servicoja.dominio.categoria.Categoria;
import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.horario.HorarioFuncionamento;
import com.servicoja.dominio.horario.HorarioFuncionamentoRepository;
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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Empresas de exemplo para desenvolvimento e testes, com localizacao em Marau e horarios de
 * funcionamento. Nunca roda em producao: as contas criadas aqui usam uma senha conhecida.
 * As categorias usadas vem da migracao V3.
 */
@Component
@Profile("!prod")
public class DadosExemploSeed implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DadosExemploSeed.class);

    private record Horario(int diaSemana, LocalTime abre, LocalTime fecha) {
    }

    private record EmpresaExemplo(
            String nome, String categoria, String email, String telefone, String descricao,
            String endereco, String numero, String bairro, String latitude, String longitude,
            List<Horario> horarios) {
    }

    private static final List<EmpresaExemplo> EMPRESAS = List.of(
            new EmpresaExemplo("Elétrica Silva", "Eletricista", "eletrica.silva@exemplo.com", "(54) 3342-1001",
                    "Instalacoes e reparos eletricos em Marau e regiao.",
                    "Rua das Flores", "120", "Centro", "-28.4475", "-52.2010",
                    juntar(diasUteis("08:00", "12:00"), diasUteis("13:30", "18:00"), sabado("08:00", "12:00"))),
            new EmpresaExemplo("Hidráulica Marau", "Encanador", "hidraulica.marau@exemplo.com", "(54) 3342-1002",
                    "Desentupimentos, vazamentos e instalacoes hidraulicas. Atendimento 24 horas.",
                    "Av. Julio Borella", "45", "Centro", "-28.4502", "-52.1968",
                    todosOsDias("00:00", "23:59")),
            new EmpresaExemplo("Beleza & Cia", "Salão de Beleza", "beleza.cia@exemplo.com", "(54) 3342-1003",
                    "Cabeleireira, manicure e estetica facial.",
                    "Rua Camilo Cimadon", "88", "Centro", "-28.4468", "-52.1979",
                    intervalo(2, 6, "09:00", "19:00")),
            new EmpresaExemplo("Mecânica do Zé", "Automecânica", "mecanica.ze@exemplo.com", "(54) 3342-1004",
                    "Troca de oleo, freios e manutencao geral.",
                    "Rod. RS-324", "km 6", "Zona Rural", "-28.4630", "-52.2205",
                    juntar(diasUteis("07:30", "18:00"), sabado("07:30", "12:00"))),
            new EmpresaExemplo("Reparos Rápidos", "Marido de Aluguel", "reparos.rapidos@exemplo.com", "(54) 3342-1005",
                    "Montagem de moveis, quadros e pequenos reparos.",
                    "Rua Emilio Seleme", "230", "Santa Helena", "-28.4540", "-52.2050",
                    diasUteis("08:00", "17:00")));

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final HorarioFuncionamentoRepository horarioRepository;
    private final PasswordEncoder codificador;
    private final String cidadePadrao;
    private final String ufPadrao;

    public DadosExemploSeed(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            EmpresaRepository empresaRepository,
            HorarioFuncionamentoRepository horarioRepository,
            PasswordEncoder codificador,
            @Value("${servico-ja.app.cidade-padrao}") String cidadePadrao,
            @Value("${servico-ja.app.uf-padrao}") String ufPadrao) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.empresaRepository = empresaRepository;
        this.horarioRepository = horarioRepository;
        this.codificador = codificador;
        this.cidadePadrao = cidadePadrao;
        this.ufPadrao = ufPadrao;
    }

    @Override
    @Transactional
    public void run(String... args) {
        EMPRESAS.forEach(this::criarOuCompletar);
        LOGGER.info("Dados de exemplo carregados com sucesso.");
    }

    /** Cria a empresa de exemplo ou, se ela ja existir, preenche localizacao e horarios que faltarem. */
    private void criarOuCompletar(EmpresaExemplo exemplo) {
        Empresa empresa = usuarioRepository.findByEmailIgnoreCase(exemplo.email())
                .map(dono -> empresaRepository.findByUsuarioIdAndExcluidaEmIsNull(dono.getId()))
                .flatMap(empresas -> empresas.stream().findFirst())
                .orElse(null);
        if (empresa == null) {
            if (usuarioRepository.existsByEmailIgnoreCase(exemplo.email())) {
                return;
            }
            empresa = criar(exemplo);
            if (empresa == null) {
                return;
            }
        }
        if (!empresa.temLocalizacao()) {
            empresa.setLatitude(new BigDecimal(exemplo.latitude()));
            empresa.setLongitude(new BigDecimal(exemplo.longitude()));
            empresaRepository.save(empresa);
        }
        if (horarioRepository.findByEmpresaIdOrderByDiaSemanaAscAbreAsc(empresa.getId()).isEmpty()) {
            Empresa dona = empresa;
            horarioRepository.saveAll(exemplo.horarios().stream()
                    .map(h -> new HorarioFuncionamento(dona, h.diaSemana(), h.abre(), h.fecha()))
                    .toList());
        }
    }

    private Empresa criar(EmpresaExemplo exemplo) {
        Categoria categoria = categoriaRepository.findFirstByNomeIgnoreCase(exemplo.categoria());
        if (categoria == null) {
            LOGGER.warn("Categoria \"{}\" nao encontrada; empresa de exemplo \"{}\" ignorada.",
                    exemplo.categoria(), exemplo.nome());
            return null;
        }

        Usuario dono = new Usuario();
        dono.setNome("Responsável - " + exemplo.nome());
        dono.setEmail(exemplo.email());
        dono.setSenha(codificador.encode("senha123"));
        dono.setTelefone(exemplo.telefone());
        dono.setPerfil(Perfil.EMPRESA);
        usuarioRepository.save(dono);

        Empresa empresa = new Empresa();
        empresa.setUsuario(dono);
        empresa.setCategoria(categoria);
        empresa.setNome(exemplo.nome());
        empresa.setDescricaoCurta(exemplo.descricao());
        empresa.setTelefone(exemplo.telefone());
        empresa.setWhatsapp(exemplo.telefone());
        empresa.setEmailContato(exemplo.email());
        empresa.setEndereco(exemplo.endereco());
        empresa.setNumero(exemplo.numero());
        empresa.setBairro(exemplo.bairro());
        empresa.setCidade(cidadePadrao);
        empresa.setUf(ufPadrao);
        empresa.setAprovada(true);
        return empresaRepository.save(empresa);
    }

    private static List<Horario> intervalo(int primeiroDia, int ultimoDia, String abre, String fecha) {
        return IntStream.rangeClosed(primeiroDia, ultimoDia)
                .mapToObj(dia -> new Horario(dia, LocalTime.parse(abre), LocalTime.parse(fecha)))
                .toList();
    }

    private static List<Horario> diasUteis(String abre, String fecha) {
        return intervalo(1, 5, abre, fecha);
    }

    private static List<Horario> sabado(String abre, String fecha) {
        return intervalo(6, 6, abre, fecha);
    }

    private static List<Horario> todosOsDias(String abre, String fecha) {
        return intervalo(1, 7, abre, fecha);
    }

    @SafeVarargs
    private static List<Horario> juntar(List<Horario>... listas) {
        List<Horario> todos = new ArrayList<>();
        for (List<Horario> lista : listas) {
            todos.addAll(lista);
        }
        return todos;
    }
}
