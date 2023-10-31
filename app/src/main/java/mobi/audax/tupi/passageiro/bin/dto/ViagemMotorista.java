package mobi.audax.tupi.passageiro.bin.dto;

import java.io.Serializable;
import java.util.List;

public class ViagemMotorista implements Serializable {

    private Coordenadas coordenadaPartida;
    private Coordenadas coordenadaDestino;
    private List<Coordenadas> listCoordenadas;
    private Coordenadas coordenadasDestinoPassageiro;

    public Coordenadas getCoordenadasDestinoPassageiro() {
        return coordenadasDestinoPassageiro;
    }

    public void setCoordenadasDestinoPassageiro(Coordenadas coordenadasDestinoPassageiro) {
        this.coordenadasDestinoPassageiro = coordenadasDestinoPassageiro;
    }

    public Coordenadas getCoordenadaPartida() {
        return coordenadaPartida;
    }

    public void setCoordenadaPartida(Coordenadas coordenadaPartida) {
        this.coordenadaPartida = coordenadaPartida;
    }

    public Coordenadas getCoordenadaDestino() {
        return coordenadaDestino;
    }

    public void setCoordenadaDestino(Coordenadas coordenadaDestino) {
        this.coordenadaDestino = coordenadaDestino;
    }

    public List<Coordenadas> getListCoordenadas() {
        return listCoordenadas;
    }

    public void setListCoordenadas(List<Coordenadas> listCoordenadas) {
        this.listCoordenadas = listCoordenadas;
    }
}
