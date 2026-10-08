import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Paleta da identidade "Laranja energia": laranja vibrante (rapidez, o "Ja" do nome),
/// azul-marinho para textos e um creme quente nos fundos.
class AppCores {
  AppCores._();

  static const laranja = Color(0xFFFF6A2B);
  static const laranjaEscuro = Color(0xFFE2501C);
  static const laranjaClaro = Color(0xFFFFE3D3);
  static const marinho = Color(0xFF1E2A4A);
  static const creme = Color(0xFFFFF4EC);
  static const fundo = Color(0xFFFFFAF6);
  static const textoSecundario = Color(0xFF6B7280);
  static const borda = Color(0xFFF2E6DC);
  static const verde = Color(0xFF16A34A);
  static const verdeClaro = Color(0xFFDCFCE7);
  static const whatsapp = Color(0xFF25D366);
  static const estrela = Color(0xFFFFB020);

  static const gradienteMarca = LinearGradient(
    colors: [Color(0xFFFF8A3D), laranja, laranjaEscuro],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
}

class AppTheme {
  AppTheme._();

  static const raio = 16.0;

  static ThemeData claro() {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: AppCores.laranja,
      primary: AppCores.laranja,
      onPrimary: Colors.white,
      secondary: AppCores.marinho,
      surface: Colors.white,
      onSurface: AppCores.marinho,
      brightness: Brightness.light,
    );
    final base = ThemeData(useMaterial3: true, colorScheme: colorScheme);
    final texto = GoogleFonts.plusJakartaSansTextTheme(base.textTheme).apply(
      bodyColor: AppCores.marinho,
      displayColor: AppCores.marinho,
    );
    final bordaCampo = OutlineInputBorder(
      borderRadius: BorderRadius.circular(14),
      borderSide: const BorderSide(color: AppCores.borda),
    );

    return base.copyWith(
      textTheme: texto,
      scaffoldBackgroundColor: AppCores.fundo,
      appBarTheme: AppBarTheme(
        backgroundColor: AppCores.fundo,
        surfaceTintColor: Colors.transparent,
        foregroundColor: AppCores.marinho,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: texto.titleMedium?.copyWith(fontWeight: FontWeight.w800, fontSize: 18),
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(raio),
          side: const BorderSide(color: AppCores.borda),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size(64, 50),
          textStyle: texto.labelLarge?.copyWith(fontWeight: FontWeight.w700, fontSize: 15),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          minimumSize: const Size.fromHeight(52),
          backgroundColor: AppCores.laranja,
          foregroundColor: Colors.white,
          elevation: 0,
          textStyle: texto.labelLarge?.copyWith(fontWeight: FontWeight.w700, fontSize: 15),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          minimumSize: const Size(64, 50),
          foregroundColor: AppCores.marinho,
          side: const BorderSide(color: AppCores.borda, width: 1.5),
          textStyle: texto.labelLarge?.copyWith(fontWeight: FontWeight.w700),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppCores.laranjaEscuro,
          textStyle: texto.labelLarge?.copyWith(fontWeight: FontWeight.w700),
        ),
      ),
      chipTheme: base.chipTheme.copyWith(
        backgroundColor: Colors.white,
        selectedColor: AppCores.laranja,
        side: const BorderSide(color: AppCores.borda),
        labelStyle: texto.labelLarge?.copyWith(color: AppCores.marinho, fontWeight: FontWeight.w600),
        secondaryLabelStyle: texto.labelLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w700),
        checkmarkColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(40)),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.transparent,
        indicatorColor: AppCores.laranjaClaro,
        elevation: 0,
        labelTextStyle: WidgetStateProperty.resolveWith(
          (estados) => texto.labelSmall?.copyWith(
            fontWeight: estados.contains(WidgetState.selected) ? FontWeight.w800 : FontWeight.w600,
            color: estados.contains(WidgetState.selected) ? AppCores.laranjaEscuro : AppCores.textoSecundario,
          ),
        ),
        iconTheme: WidgetStateProperty.resolveWith(
          (estados) => IconThemeData(
            color: estados.contains(WidgetState.selected) ? AppCores.laranjaEscuro : AppCores.textoSecundario,
          ),
        ),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: AppCores.laranja,
        foregroundColor: Colors.white,
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        backgroundColor: AppCores.marinho,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
      dividerTheme: const DividerThemeData(color: AppCores.borda, thickness: 1),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        border: bordaCampo,
        enabledBorder: bordaCampo,
        focusedBorder: bordaCampo.copyWith(borderSide: const BorderSide(color: AppCores.laranja, width: 2)),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 15),
        hintStyle: const TextStyle(color: AppCores.textoSecundario),
      ),
    );
  }
}
