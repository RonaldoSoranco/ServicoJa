package com.servicoja.infra;

/** Calculos de distancia entre coordenadas geograficas (graus decimais). */
public final class Geolocalizacao {

    private static final double RAIO_TERRA_KM = 6371.0;

    private Geolocalizacao() {
    }

    /** Distancia em linha reta pela formula de haversine, em quilometros. */
    public static double distanciaKm(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLatitude = Math.toRadians(latitude2 - latitude1);
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);
        double a = Math.pow(Math.sin(deltaLatitude / 2), 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.pow(Math.sin(deltaLongitude / 2), 2);
        return 2 * RAIO_TERRA_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /**
     * Fator que corrige a diferenca de longitude pela latitude (cos² da latitude). Permite ordenar
     * por proximidade no banco com uma conta simples, sem funcoes trigonometricas por linha.
     */
    public static double fatorLongitude(double latitude) {
        return Math.pow(Math.cos(Math.toRadians(latitude)), 2);
    }
}
