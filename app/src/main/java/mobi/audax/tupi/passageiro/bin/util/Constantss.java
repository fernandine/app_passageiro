package mobi.audax.tupi.passageiro.bin.util;

import java.util.ArrayList;
import java.util.List;

import mobi.audax.tupi.passageiro.bin.bean.Cartao;

public class Constantss {

    public List<Cartao> cartoes;

    public void mockCartoes() {
        Cartao cartao1 = new Cartao();
        cartao1.setCvv("123");
        cartao1.setAnoExpiracao("24");
        cartao1.setHolder("123");
        cartao1.setMesExpiracao("01");
        cartao1.setNumero("12345677899");
        cartao1.setTipoCartao(1);
        cartao1.setId(1);

        Cartao cartao2 = new Cartao();
        cartao2.setCvv("345");
        cartao2.setAnoExpiracao("24");
        cartao2.setHolder("345");
        cartao2.setMesExpiracao("01");
        cartao2.setNumero("987654321");
        cartao2.setTipoCartao(2);
        cartao2.setId(2);

        cartoes = new ArrayList<>(List.of(cartao1, cartao2));
    }

}
