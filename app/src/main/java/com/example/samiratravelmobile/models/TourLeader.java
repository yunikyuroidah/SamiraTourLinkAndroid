package com.example.samiratravelmobile.models;

public class TourLeader {
    private String nama;
    private String telepon;
    private String gambar;

    public TourLeader() {
        // Default constructor required for calls to DataSnapshot.getValue(TourLeader.class)
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTelepon() {
        return telepon;
    }

    public void setTelepon(String telepon) {
        this.telepon = telepon;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }
}
