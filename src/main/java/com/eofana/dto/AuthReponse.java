package com.eofana.dto;

public class AuthReponse {
    private Long idUser;
    private String token;
    private String email;
    private String prenom;
    private String nom;

    public AuthReponse() {}
    public AuthReponse(Long idUser, String token, String email, String prenom, String nom) {
        this.idUser = idUser;
        this.token = token;
        this.email = email;
        this.prenom = prenom;
        this.nom = nom;
    }

    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
