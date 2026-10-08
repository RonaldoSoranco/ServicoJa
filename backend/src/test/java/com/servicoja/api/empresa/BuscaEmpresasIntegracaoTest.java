package com.servicoja.api.empresa;

import com.servicoja.dominio.categoria.CategoriaRepository;
import com.servicoja.dominio.empresa.Empresa;
import com.servicoja.dominio.empresa.EmpresaRepository;
import com.servicoja.dominio.horario.HorarioFuncionamento;
import com.servicoja.dominio.horario.HorarioFuncionamentoRepository;
import com.servicoja.dominio.usuario.Perfil;
import com.servicoja.dominio.usuario.Usuario;
import com.servicoja.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Busca com "aberto agora" e "perto de mim". O relogio fica fixo numa quarta-feira as 10h
 * (horario de Brasilia) e cada teste usa uma cidade propria para nao misturar resultados.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BuscaEmpresasIntegracaoTest {

    private static final int QUARTA = 3;

    @TestConfiguration
    static class RelogioFixo {
        @Bean
        @Primary
        Clock relogioQuartaDezHoras() {
            return Clock.fixed(Instant.parse("2026-10-07T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        }
    }

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private HorarioFuncionamentoRepository horarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void indicaQuemEstaAbertoAgoraEFiltraSoAsAbertas() {
        String cidade = "Cidade Horarios";
        Empresa aberta = criarEmpresa("Aberta de Manha", cidade, null, null);
        adicionarHorario(aberta, QUARTA, "08:00", "12:00");
        Empresa fechada = criarEmpresa("Abre a Tarde", cidade, null, null);
        adicionarHorario(fechada, QUARTA, "13:00", "18:00");
        criarEmpresa("Sem Horario", cidade, null, null);

        var todas = empresaService.buscarPublico(filtro(cidade, null, null, false), 0, 10).conteudo();
        assertThat(todas).extracting(EmpresaDtos.EmpresaSimplesResposta::nome,
                        EmpresaDtos.EmpresaSimplesResposta::abertoAgora, EmpresaDtos.EmpresaSimplesResposta::temHorarios)
                .containsExactlyInAnyOrder(
                        tuple("Aberta de Manha", true, true),
                        tuple("Abre a Tarde", false, true),
                        tuple("Sem Horario", false, false));

        var abertas = empresaService.buscarPublico(filtro(cidade, null, null, true), 0, 10).conteudo();
        assertThat(abertas).extracting(EmpresaDtos.EmpresaSimplesResposta::nome).containsExactly("Aberta de Manha");
    }

    @Test
    void comLocalizacaoOrdenaPelaDistanciaEDeixaSemLocalizacaoPorUltimo() {
        String cidade = "Cidade Proximidade";
        criarEmpresa("Longe", cidade, "-28.5400", "-52.1992");
        criarEmpresa("Sem Localizacao", cidade, null, null);
        criarEmpresa("Perto", cidade, "-28.4500", "-52.1990");
        criarEmpresa("Meio", cidade, "-28.4700", "-52.1992");

        var resultado = empresaService.buscarPublico(filtro(cidade, -28.4489, -52.1992, false), 0, 10).conteudo();

        assertThat(resultado).extracting(EmpresaDtos.EmpresaSimplesResposta::nome)
                .containsExactly("Perto", "Meio", "Longe", "Sem Localizacao");
        assertThat(resultado.getFirst().distanciaKm()).isEqualTo(0.1);
        assertThat(resultado.get(2).distanciaKm()).isEqualTo(10.1);
        assertThat(resultado.getLast().distanciaKm()).isNull();
    }

    @Test
    void semLocalizacaoNaoCalculaDistancia() {
        String cidade = "Cidade Sem Gps";
        criarEmpresa("Qualquer", cidade, "-28.4500", "-52.1990");

        var resultado = empresaService.buscarPublico(filtro(cidade, null, null, false), 0, 10).conteudo();

        assertThat(resultado.getFirst().distanciaKm()).isNull();
        assertThat(resultado.getFirst().latitude()).isEqualByComparingTo("-28.4500");
    }

    private EmpresaDtos.FiltroBusca filtro(String cidade, Double latitude, Double longitude, boolean somenteAbertas) {
        return new EmpresaDtos.FiltroBusca(null, null, cidade, null, latitude, longitude, somenteAbertas);
    }

    private Empresa criarEmpresa(String nome, String cidade, String latitude, String longitude) {
        Usuario dono = new Usuario();
        dono.setNome("Dono " + nome);
        dono.setEmail(nome.toLowerCase().replace(' ', '.') + "@busca.teste");
        dono.setSenha("$2a$10$teste");
        dono.setPerfil(Perfil.EMPRESA);
        usuarioRepository.save(dono);

        Empresa empresa = new Empresa();
        empresa.setUsuario(dono);
        empresa.setCategoria(categoriaRepository.findAll().getFirst());
        empresa.setNome(nome);
        empresa.setCidade(cidade);
        empresa.setUf("RS");
        empresa.setAprovada(true);
        if (latitude != null) {
            empresa.setLatitude(new BigDecimal(latitude));
            empresa.setLongitude(new BigDecimal(longitude));
        }
        return empresaRepository.save(empresa);
    }

    private void adicionarHorario(Empresa empresa, int diaSemana, String abre, String fecha) {
        horarioRepository.save(new HorarioFuncionamento(empresa, diaSemana, LocalTime.parse(abre), LocalTime.parse(fecha)));
    }
}
