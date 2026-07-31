package com.servicoja.api.admin;

import com.servicoja.dominio.usuario.Perfil;

import java.time.OffsetDateTime;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record EstatisticasResposta(
            long totalUsuarios,
            long totalClientes,
            long totalEmpresas,
            long totalEmpresasPremium,
            long empresasPendentes,
            long avaliacoesPendentes,
            long avaliacoesAprovadas,
            long totalAvaliacoes) {
    }

    public record UsuarioAdminResposta(
            Long id,
            String nome,
            String email,
            Perfil perfil,
            boolean ativo,
            OffsetDateTime criadoEm) {
    }

    public record LogResposta(
            Long id,
            String usuario,
            String acao,
            String detalhes,
            String ip,
            OffsetDateTime criadoEm) {
    }
}
