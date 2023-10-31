package mobi.audax.tupi.passageiro.bin.bean;

import java.io.Serializable;
import java.util.List;

import mobi.audax.tupi.passageiro.bin.dto.Coordenadas;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.PrimaryKey;

@Entity
public class MinhaRota implements Serializable {

    @PrimaryKey
    private int id;
    private String nomeRota;
    private String partida;
    private String destino;
    private String data;
    private String hora;
    private String imagemMapa;
    private String fotoPerfilMotorista;

    private String nomeMotorista,placaVeiculo,modeloVeiculo;
    private int anoVeiculo;
    private long embarcadoAt;


    public String getNomeMotorista() {
        return nomeMotorista;
    }

    public void setNomeMotorista(String nomeMotorista) {
        this.nomeMotorista = nomeMotorista;
    }

    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }

    public String getModeloVeiculo() {
        return modeloVeiculo;
    }

    public void setModeloVeiculo(String modeloVeiculo) {
        this.modeloVeiculo = modeloVeiculo;
    }

    public int getAnoVeiculo() {
        return anoVeiculo;
    }

    public void setAnoVeiculo(int anoVeiculo) {
        this.anoVeiculo = anoVeiculo;
    }

    public long getEmbarcadoAt() {
        return embarcadoAt;
    }

    public void setEmbarcadoAt(long embarcadoAt) {
        this.embarcadoAt = embarcadoAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagemMapa() {
        return imagemMapa;
    }

    public void setImagemMapa(String imagemMapa) {
        this.imagemMapa = imagemMapa;
    }

    public String getNomeRota() {
        return nomeRota;
    }

    public void setNomeRota(String nomeRota) {
        this.nomeRota = nomeRota;
    }

    public String getPartida() {
        return partida;
    }

    public void setPartida(String partida) {
        this.partida = partida;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
