package com.example.samiratravelmobile.models;

import java.util.List;

public class Paket {
    private String id;
    private String nama_paket;
    private String deskripsi;
    private List<String> fasilitas;
    private List<String> features;

    public Paket() {} // diperlukan untuk Firestore

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama_paket() { return nama_paket; }
    public void setNama_paket(String nama_paket) { this.nama_paket = nama_paket; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public List<String> getFasilitas() { return fasilitas; }
    public void setFasilitas(List<String> fasilitas) { this.fasilitas = fasilitas; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
}
