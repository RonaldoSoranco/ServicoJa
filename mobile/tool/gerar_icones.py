"""Gera os icones do app a partir da mesma geometria do LogoServicoJa (lib/core/widgets/marca.dart).

Uso (na pasta mobile/):
    python3 tool/gerar_icones.py
    dart run flutter_launcher_icons

Saidas em assets/icone/:
    icone.png        1024x1024, fundo laranja de ponta a ponta (iOS, Android antigo e web)
    icone_frente.png 1024x1024, so o pino sobre fundo transparente (frente do icone adaptativo do Android)
"""

import math
from pathlib import Path

from PIL import Image, ImageDraw

TAMANHO = 1024
ESCALA = 4  # desenha em 4x e reduz, para bordas suaves
LARANJA_CLARO = (0xFF, 0x8A, 0x3D)
LARANJA = (0xFF, 0x6A, 0x2B)
LARANJA_ESCURO = (0xE2, 0x50, 0x1C)
BRANCO = (255, 255, 255, 255)

DESTINO = Path(__file__).resolve().parent.parent / "assets" / "icone"


def bezier(p0, p1, p2, p3, passos=80):
    pontos = []
    for i in range(passos + 1):
        t = i / passos
        u = 1 - t
        x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
        pontos.append((x, y))
    return pontos


def contorno_pino(s, deslocamento_y=0.0):
    """Mesmo desenho do _PinoComRaio: circulo no alto e ponta embaixo."""
    cx, cy, r, ponta = s * 0.5, s * 0.42 + deslocamento_y, s * 0.25, s * 0.82 + deslocamento_y
    lado_esquerdo = bezier((cx, ponta), (cx - r * 0.3, ponta - (ponta - cy) * 0.42), (cx - r, cy + r * 0.62), (cx - r, cy))
    arco = [(cx + r * math.cos(a), cy + r * math.sin(a)) for a in (math.pi + math.pi * i / 80 for i in range(81))]
    lado_direito = bezier((cx + r, cy), (cx + r, cy + r * 0.62), (cx + r * 0.3, ponta - (ponta - cy) * 0.42), (cx, ponta))
    raio = [
        (cx + r * 0.18, cy - r * 0.72), (cx - r * 0.42, cy + r * 0.10), (cx - r * 0.02, cy + r * 0.10),
        (cx - r * 0.20, cy + r * 0.72), (cx + r * 0.44, cy - r * 0.14), (cx + r * 0.04, cy - r * 0.14),
    ]
    return lado_esquerdo + arco + lado_direito, raio


def gradiente(tamanho):
    """Gradiente diagonal (alto-esquerda -> baixo-direita) como no AppCores.gradienteMarca."""
    base = 256
    imagem = Image.new("RGB", (base, base))
    pixels = imagem.load()
    for y in range(base):
        for x in range(base):
            t = (x + y) / (2 * (base - 1))
            inicio, fim, local = (LARANJA_CLARO, LARANJA, t * 2) if t < 0.5 else (LARANJA, LARANJA_ESCURO, (t - 0.5) * 2)
            pixels[x, y] = tuple(round(a + (b - a) * local) for a, b in zip(inicio, fim))
    return imagem.resize((tamanho, tamanho), Image.BILINEAR)


def icone_completo():
    s = TAMANHO * ESCALA
    imagem = gradiente(s).convert("RGBA")
    desenho = ImageDraw.Draw(imagem)
    pino, raio = contorno_pino(s, deslocamento_y=s * 0.005)
    desenho.polygon(pino, fill=BRANCO)
    desenho.polygon(raio, fill=LARANJA + (255,))
    return imagem.convert("RGB").resize((TAMANHO, TAMANHO), Image.LANCZOS)


def icone_frente():
    """O Android recorta o icone adaptativo; o pino fica reduzido dentro da area segura central."""
    s = TAMANHO * ESCALA
    camada = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    desenho = ImageDraw.Draw(camada)
    pino, raio = contorno_pino(s, deslocamento_y=s * 0.005)
    desenho.polygon(pino, fill=BRANCO)
    desenho.polygon(raio, fill=LARANJA + (255,))
    reduzido = camada.resize((int(s * 0.62), int(s * 0.62)), Image.LANCZOS)
    frente = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    frente.paste(reduzido, ((s - reduzido.width) // 2, (s - reduzido.height) // 2), reduzido)
    return frente.resize((TAMANHO, TAMANHO), Image.LANCZOS)


if __name__ == "__main__":
    DESTINO.mkdir(parents=True, exist_ok=True)
    icone_completo().save(DESTINO / "icone.png")
    icone_frente().save(DESTINO / "icone_frente.png")
    print(f"Icones gerados em {DESTINO}")
