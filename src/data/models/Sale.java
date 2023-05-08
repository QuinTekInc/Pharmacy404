package data.models;

import java.time.LocalDate;

public class Sale {

    int id;
    LocalDate saleDate;
    int drugId;
    String drugName;
    int quantity;
    double price;
    double amount;

    public Sale(LocalDate saleDate, int quantity, int drugId, String drugSold, double price, double amount){

        this.saleDate = saleDate;
        this.quantity = quantity;
        this.drugId = drugId;
        this.drugName = drugSold;
        this.price = price;
        this.amount = amount;

    }
    public Sale(int id, LocalDate saleDate,  int quantity , int drugId, String drugSold, double price, double amount) {
        this.id = id;
        this.drugId = drugId;
        this.saleDate = saleDate;
        this.drugName = drugSold;
        this.quantity = quantity;
        this.price = price;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public int getDrugId() {
        return drugId;
    }

    public void setDrugId(int drugId) {
        this.drugId = drugId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
