package data.models;

public class DrugSalesRanking {

    private String drugName;
    private int quantitySold;

    public DrugSalesRanking(String drugName, int quantitySold){
        this.drugName = drugName;
        this.quantitySold = quantitySold;
    }

    public String getDrugName(){
        return drugName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }
}
