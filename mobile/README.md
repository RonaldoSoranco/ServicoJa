# Serviço Já — App

Aplicativo Flutter do **Serviço Já**, plataforma que conecta clientes a empresas de
serviços. Consome a API REST em [`../backend`](../backend).

## Requisitos

- Flutter 3.35+ (canal stable) / Dart 3.9+
- Um backend do Serviço Já rodando (local ou via `docker compose up` na raiz do
  repositório) — veja `backend/README.md`

## Como rodar

```bash
flutter pub get
flutter run --dart-define=API_BASE_URL=http://localhost:8080
```

Se `API_BASE_URL` não for informado, o app usa um padrão adequado para
desenvolvimento local (`http://10.0.2.2:8080` no emulador Android,
`http://localhost:8080` no restante) — veja `lib/core/config/app_config.dart`.
Para testar em um dispositivo físico na mesma rede, informe o IP da máquina que
roda o backend (ex.: `--dart-define=API_BASE_URL=http://192.168.0.10:8080`).

## Estrutura do projeto

```
mobile/
└── lib
    ├── core/               # Config, cliente HTTP, storage de token, tema, widgets comuns
    ├── features/
    │   ├── auth/           # Login, cadastro, recuperação de senha
    │   ├── empresas/       # Busca, perfil, cadastro/edição, fotos, portfolio
    │   ├── avaliacoes/     # Avaliar empresas, minhas avaliações
    │   ├── favoritos/      # Empresas favoritadas
    │   ├── assinaturas/    # Assinatura Premium (Asaas)
    │   ├── notificacoes/   # Notificações do usuário
    │   ├── perfil/         # Perfil, alterar/recuperar senha
    │   ├── admin/           # Painel administrativo (dentro do próprio app)
    │   └── shell/          # Navegação principal (cliente/empresa vs. admin)
    └── main.dart
```

## Upload de fotos e logo

O envio de fotos (exclusivo para empresas Premium) e do logo é feito escolhendo uma
imagem da galeria (`image_picker`) e enviando via `multipart/form-data` para o backend
(`POST /api/empresas/{id}/fotos` e `POST /api/empresas/{id}/logo`), que grava o
arquivo e devolve a URL pública. Fotos e logo só entram por upload: não há campo para
colar uma URL externa. O logo pode ser enviado depois que a empresa é cadastrada.

No Android, o seletor de imagens usa o Photo Picker do sistema (Android 13+, sem
permissão extra); em versões mais antigas é necessária a permissão
`READ_EXTERNAL_STORAGE`, já declarada no `AndroidManifest.xml`. No iOS, o acesso à
galeria exige `NSPhotoLibraryUsageDescription`, já declarada no `Info.plist`.

## Mapa e localização

Os mapas usam o OpenStreetMap (`flutter_map`), sem chave de API. A localização do aparelho
(`geolocator`) só é pedida quando a pessoa toca em **Perto de mim** ou marca a empresa no
mapa; as permissões já estão declaradas no `AndroidManifest.xml` e no `Info.plist`. Os
botões "Como chegar" e "Abrir no Maps" abrem a rota no Google Maps.

## Identidade visual

Paleta, tipografia (Plus Jakarta Sans, via `google_fonts`) e componentes ficam em
`lib/core/theme/app_theme.dart` e `lib/core/widgets/`. O logo é desenhado em código
(`LogoServicoJa`); os ícones do app saem da mesma geometria:

```bash
python3 tool/gerar_icones.py
dart run flutter_launcher_icons
```

## Exclusão de conta

Clientes e empresas podem excluir a própria conta em **Perfil → Excluir conta**
(exigido pela LGPD e pelas lojas App Store/Google Play). O app pede a senha atual,
chama `POST /api/auth/excluir-conta` e, em caso de sucesso, encerra a sessão. Para
empresas, as empresas cadastradas saem da plataforma e o Premium é cancelado.

## Testes

```bash
flutter analyze
flutter test
```

## CI

O workflow `.github/workflows/mobile-ci.yml` roda `flutter analyze` e `flutter test`
a cada push/PR que toque em `mobile/**`.

## Pendências para publicar nas lojas (fora do escopo deste repositório)

- Ícone do app e splash screen personalizados (hoje usam o padrão do Flutter)
- Keystore de assinatura de release (Android) e certificados de distribuição (iOS)
- Contas de desenvolvedor na Play Store / App Store e ficha da loja (descrição,
  screenshots, política de privacidade)
- Push notifications (hoje as notificações são só in-app) — exigiria um projeto
  Firebase real e integração com FCM/APNs
