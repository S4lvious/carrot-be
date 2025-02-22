package com.carrot.Carrot.dto;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.model.User.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithoutBankAccounts {
    private Long id;
    private String username;
    private String nome;
    private String cognome;
    private String ragioneSociale; // Se è un'azienda
    private String codiceFiscale;
    private String partitaIva;
    private String indirizzo;
    private String cap;
    private String citta;
    private String provincia;
    private String telefono;
    private String email;
    private Role role;
    private boolean isAzienda;

    public UserWithoutBankAccounts(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nome = user.getNome();
        this.cognome = user.getCognome();
        this.ragioneSociale = user.getRagioneSociale();
        this.codiceFiscale = user.getCodiceFiscale();
        this.partitaIva = user.getPartitaIva();
        this.indirizzo = user.getIndirizzo();
        this.cap = user.getCap();
        this.citta = user.getCitta();
        this.provincia = user.getProvincia();
        this.telefono = user.getTelefono();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isAzienda = user.isAzienda();
    }
}
