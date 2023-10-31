package mobi.audax.tupi.passageiro.bin.dto;

import java.io.Serializable;

public class Parada implements Serializable {
    private double latitude;
    private double longitude;
    private int paradaDestinoId;
    private int paradaPartidaId;

    public int getParadaDestinoId() {
        return paradaDestinoId;
    }

    public void setParadaDestinoId(int paradaDestinoId) {
        this.paradaDestinoId = paradaDestinoId;
    }

    public int getParadaPartidaId() {
        return paradaPartidaId;
    }

    public void setParadaPartidaId(int paradaPartidaId) {
        this.paradaPartidaId = paradaPartidaId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
