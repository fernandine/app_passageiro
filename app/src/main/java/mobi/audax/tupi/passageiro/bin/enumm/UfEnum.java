package mobi.audax.tupi.passageiro.bin.enumm;

public enum UfEnum {
    VAZIO(""),
    MT("MT"),
    RO("RO"),
    AC("AC"),
    AM("AM"),
    RR("RR"),
    PA("PA"),
    AP("AP"),
    TO("TO"),
    MA("MA"),
    PI("PI"),
    CE("CE"),
    RN("RN"),
    PB("PB"),
    PE("PE"),
    AL("AL"),
    SE("SE"),
    BA("BA"),
    MG("MG"),
    ES("ES"),
    RJ("RJ"),
    SP("SP"),
    PR("PR"),
    SC("SC"),
    RS("RS"),
    MS("MS"),
    GO("GO"),
    DF("DF");

    private final String uf;
    UfEnum(String uf) {
        this.uf = uf;
    }

    public String getUfs() { return uf; }

    @Override
    public String toString() {
        return uf;
    }


}
