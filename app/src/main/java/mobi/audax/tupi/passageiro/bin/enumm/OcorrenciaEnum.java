package mobi.audax.tupi.passageiro.bin.enumm;

public enum OcorrenciaEnum {

    ACIDENTE("Eu me envolvi em um acidente"),
    VEICULO_QUEBROU("Meu veículo quebrou"),
    ASSALTO("Ocorreu um assalto"),
    CLIENTE_NAO_SUBIU("Cliente não subiu no veículo"),
    ITENS_PERDIDOS("Itens perdidos"),
    ALGO_DIFERENTE("Algo diferente aconteceu");
	
    private final String name;

    private OcorrenciaEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}