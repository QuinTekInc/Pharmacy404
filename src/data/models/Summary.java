package data.models;

import java.time.LocalDate;

public class Summary{

    public static class DailySalesSummary {

        private LocalDate saleDate;
        private double totalAmount;

        public DailySalesSummary(LocalDate saleDate, double totalAmount){
            this.saleDate = saleDate;
            this.totalAmount = totalAmount;
        }

        public LocalDate getSaleDate(){
            return saleDate;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }


    public static class WeeklySalesSummary{

        private int weekNumber;
        private double totalAmount;

        public WeeklySalesSummary(int weekNumber, double totalAmount){
            this.weekNumber = weekNumber;
            this.totalAmount = totalAmount;
        }

        public int getWeekNumber() {
            return weekNumber;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

    }


    public static class MonthlySalesSummary{

        private int monthNumber;
        private double totalAmount;

        public MonthlySalesSummary(int monthNumber, double amount){
            this.monthNumber = monthNumber;
            this.totalAmount = amount;
        }

        public int getMonthNumber() {
            return monthNumber;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

    }

    public static class YearlySalesSummary{

        private int yearNumber;
        private double totalAmount;

        public YearlySalesSummary(int yearNumber, double totalAmount){
            this.yearNumber = yearNumber;
            this.totalAmount = totalAmount;
        }

        public int getYearNumber() {
            return yearNumber;
        }

        public void setYearNumber(int yearNumber) {
            this.yearNumber = yearNumber;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

}
