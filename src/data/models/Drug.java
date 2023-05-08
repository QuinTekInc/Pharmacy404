package data.models;

import java.time.LocalDate;

public class Drug {

    int id;
    int quantity;
    String drugName;
    LocalDate manufactureDate;
    LocalDate expiringDate;
    DrugType drugType;
    String manufacturer;
    double price;
    double retailPrice;

    public Drug(int quantity, String drugName, DrugType drugType, String manufacturer) {
        this.quantity = quantity;
        this.drugName = drugName;
        this.drugType = drugType;
        this.manufacturer = manufacturer;
    }

    public Drug(int id, int quantity, String drugName, DrugType drugType, String manufacturer) {
        this.id = id;
        this.quantity = quantity;
        this.drugName = drugName;
        this.drugType = drugType;
        this.manufacturer = manufacturer;
    }

    //remember the id field requires a setter but for some odd reason, I've taken it out of the program model
    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public DrugType getDrugType() {
        return drugType;
    }

    public void setDrugType(DrugType drugType) {
        this.drugType = drugType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(LocalDate manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public LocalDate getExpiringDate() {
        return expiringDate;
    }

    public void setExpiringDate(LocalDate expiringDate) {
        this.expiringDate = expiringDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice){
        this.retailPrice = retailPrice;
    }
}
