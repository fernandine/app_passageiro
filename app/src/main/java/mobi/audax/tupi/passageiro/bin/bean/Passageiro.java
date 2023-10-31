package mobi.audax.tupi.passageiro.bin.bean;

import java.io.Serializable;
import java.util.Date;

import mobi.audax.tupi.passageiro.bin.enumm.OcorrenciaEnum;
import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.PrimaryKey;
import mobi.stos.podataka_lib.annotations.Transient;

public class Passageiro implements Serializable {
    private int id;
    private int pessoaId;
    private String senha;
    private String token;
    private boolean google;
    private boolean icloud;
    private String email;
    private String nome;
    private String cpf;
    private String rg;
    private String hash;
    private String cidade;
    private int dddTelefone;
    private Date ultimoAcesso;
    private String fotoPerfil;
    private long numeroCelular;
    private int enderecoId;
    //cartao
    private String numeroCartao;
    private String cvv;
    private String validade;
    private String nomeCartao;
    private int idCartao;
    private String dataDeValidadeMes;
    private String dataDeValidadeAno;
    private String cep;
    private String uId;
    private double lat;
    private double longitude;
    private boolean embarcou;

    @Transient
    private OcorrenciaEnum ocorrenciaEnum;

    public int getIdCartao() {
        return idCartao;
    }

    public void setIdCartao(int idCartao) {
        this.idCartao = idCartao;
    }

    public String getDataDeValidadeMes() {
        return dataDeValidadeMes;
    }

    public void setDataDeValidadeMes(String dataDeValidadeMes) {
        this.dataDeValidadeMes = dataDeValidadeMes;
    }

    public String getDataDeValidadeAno() {
        return dataDeValidadeAno;
    }

    public void setDataDeValidadeAno(String dataDeValidadeAno) {
        this.dataDeValidadeAno = dataDeValidadeAno;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public OcorrenciaEnum getOcorrenciaEnum() {
        return ocorrenciaEnum;
    }

    public void setOcorrenciaEnum(OcorrenciaEnum ocorrenciaEnum) {
        this.ocorrenciaEnum = ocorrenciaEnum;
    }

    public boolean isEmbarcou() {
        return embarcou;
    }

    public void setEmbarcou(boolean embarcou) {
        this.embarcou = embarcou;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getToken() {
        return token;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getValidade() {
        return validade;
    }

    public void setValidade(String validade) {
        this.validade = validade;
    }

    public String getNomeCartao() {
        return nomeCartao;
    }

    public void setNomeCartao(String nomeCartao) {
        this.nomeCartao = nomeCartao;
    }

    public int getDddTelefone() {
        return dddTelefone;
    }

    public void setDddTelefone(int dddTelefone) {
        this.dddTelefone = dddTelefone;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public int getPessoaId() {
        return pessoaId;
    }

    public void setPessoaId(int pessoaId) {
        this.pessoaId = pessoaId;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isGoogle() {
        return google;
    }

    public void setGoogle(boolean google) {
        this.google = google;
    }

    public boolean isIcloud() {
        return icloud;
    }

    public void setIcloud(boolean icloud) {
        this.icloud = icloud;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public Date getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(Date ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public long getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(long numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public int getEnderecoId() {
        return enderecoId;
    }

    public void setEnderecoId(int enderecoId) {
        this.enderecoId = enderecoId;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}
