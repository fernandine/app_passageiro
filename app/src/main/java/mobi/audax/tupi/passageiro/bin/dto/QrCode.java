package mobi.audax.tupi.passageiro.bin.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QrCode implements Serializable {

    private String id;
    private Amount amount;
    private String text;  //chave pix decodificada
    private String status;
    private List<Link> links = new ArrayList<>();
    private String expiration_date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }
}
