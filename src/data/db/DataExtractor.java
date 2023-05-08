package data.db;

import data.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import main.MainWindowController;
import data.models.Summary.DailySalesSummary;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataExtractor {

    public static boolean insertAdminPassword(String password, String key){

        String query = "insert into admin_password(encrypted_password, encryption_key) values(?, ?)";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, password);
            ps.setString(2, key);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map.Entry<String, String> getAdminPassword(){

        String query = "select * from admin_password";
        try {
            Map.Entry<String, String> entry = null;
            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();
            if(rs.next()){
                String encryptionKey = rs.getString("encryption_key");
                String encryptedPassword = rs.getString("encrypted_password");
                entry = new AbstractMap.SimpleEntry<>(encryptionKey, encryptedPassword);

            }

            return entry;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean updateAdminPassword(String newEncryptedPassword, String key){
        String query = "update admin_password set encrypted_password = ?, encryption_key = ?";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, newEncryptedPassword);
            ps.setString(2, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(DataExtractor.class.getName()).log(Level.SEVERE, e.toString());
            return false;
        }
    }

    public static List<Sale> getSales(Date saleDate){
        List<Sale> salesList = new ArrayList<>();

        String query = "select * from sales where sale_date = ? order by id";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setDate(1, saleDate);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                int saleId = rs.getInt("id");
                int quantity = rs.getInt("quantity");
                String drugName = rs.getString("drug_name");
                int drugId = rs.getInt("drug_id");
                double price = rs.getDouble("price");
                double amount = rs.getDouble("amount");

                salesList.add(
                        new Sale(saleId,
                                LocalDate.parse(saleDate.toString()),
                                quantity, drugId, drugName, price, amount));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return salesList;

    }

    public static boolean addSale(Sale sale){

        try {
            int quantity = sale.getQuantity();
            Date saleDate = java.sql.Date.valueOf(sale.getSaleDate()) ;
            String drugName = sale.getDrugName();
            double price = sale.getPrice();
            double amount= sale.getAmount();


            String query = "insert into sales(sale_date, quantity, drug_id, drug_name, price, amount) " +
                    "values (?, ?, ?, ?, ?,?)";
            PreparedStatement pst = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            pst.setDate(1, Date.valueOf(String.valueOf(saleDate)));
            pst.setInt(2, quantity);
            pst.setInt(3, sale.getDrugId());
            pst.setString(4, drugName);
            pst.setDouble(5, price);
            pst.setDouble(6, amount);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateSale(Sale selectedSale){

        String query = "update sales set quantity = ?, drug_name = ?, price = ?, amount = ? where id = ?";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setInt(1, selectedSale.getQuantity());
            ps.setString(2, selectedSale.getDrugName());
            ps.setDouble(3, selectedSale.getPrice());
            ps.setDouble(4, selectedSale.getAmount());
            ps.setInt(5, selectedSale.getId());

            return ps.executeUpdate() >0;
        } catch (SQLException e) {
           e.printStackTrace();
        }

        return false;
    }

    public static boolean removeSale(int id){

        String query = "delete from sales where id = " + id;
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            return  ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean clearAllSalesExceptToday(){
        String query = "delete from sales where sale_date != strftime('%Y-%m-%d', 'now')";
        try {
            return DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean clearSalesExceptToday(LocalDate selectedDate){

        try {
            String query = "delete from sales where sale_date = ?";
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setDate(1, Date.valueOf(selectedDate));

            return ps.executeUpdate() > 0;
        }catch (SQLException sqlex){
            throw new RuntimeException(sqlex);
        }

    }

    public static boolean addDrug(Drug drug){

        try{
            String query = "insert into drugs(drug_name, drug_type, manufacturer, manufacture_date, " +
                    "expiration_date, quantity, wholesale_price, retail_price) values(?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drug.getDrugName());
            ps.setString(2, drug.getDrugType().toString());
            ps.setString(3, drug.getManufacturer());
            ps.setDate(4, Date.valueOf(drug.getManufactureDate()));
            ps.setDate(5, Date.valueOf(drug.getExpiringDate()));
            ps.setInt(6, drug.getQuantity());
            ps.setDouble(7, drug.getPrice());
            ps.setDouble(8, drug.getRetailPrice());

            return  ps.executeUpdate() > 0;
        }catch (SQLException sqlex){
            sqlex.printStackTrace();
        }

        return false;
    }

    public static boolean updateDrugInfo(Drug drug){

        String query = "update drugs set drug_name = ?, drug_type = ?, manufacturer = ?, manufacture_date = ?, " +
                "expiration_date = ?, quantity = ?, wholesale_price = ?, retail_price = ? where id = " + drug.getId();

        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drug.getDrugName());
            ps.setString(2, drug.getDrugType().toString());
            ps.setString(3, drug.getManufacturer());
            ps.setDate(4, Date.valueOf(drug.getManufactureDate()));
            ps.setDate(5, Date.valueOf(drug.getExpiringDate()));
            ps.setInt(6, drug.getQuantity());
            ps.setDouble(7, drug.getPrice());
            ps.setDouble(8, drug.getRetailPrice());

            return  ps.executeUpdate() > 0;

        }catch (SQLException sqlex){
            throw new RuntimeException(sqlex);
        }

    }

    public static Drug getDrug(int drugId){

        try {
            String query = String.format("select * from drugs where id = %d", drugId);
            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();

            Drug drug = null;

            if (rs.next()) {
                String drugName = rs.getString("drug_name");
                String manufacturer = rs.getString("manufacturer");
                String drugType = rs.getString("drug_type");
                LocalDate manufactureDate = LocalDate.parse(rs.getDate("manufacture_date").toString());
                int remainingQuantity = rs.getInt("quantity");
                double price = rs.getDouble("wholesale_price");
                double retailPrice = rs.getDouble("retail_price");

                drug = new Drug(drugId, remainingQuantity, drugName,
                        DrugType.drugValueOf(drugType), manufacturer);
                drug.setManufactureDate(manufactureDate);
                drug.setPrice(price);
                drug.setRetailPrice(retailPrice);

            }

            return drug;

        }catch (SQLException sqlex){
           throw new RuntimeException(sqlex);
        }

    }

    public static int getDrugId(String drugName, String drugType, String manufacturer, Date batchDate){

        String query = "select id from drugs where drug_name = ? and drug_type = ? and manufacturer = ? and manufacture_date = ?";

        try {

            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setString(1, drugName);
            ps.setString(2, drugType);
            ps.setString(3, manufacturer);
            ps.setDate(4, batchDate);

            ResultSet rs = ps.executeQuery();
            return rs.getInt("id");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateDrugQuantity(int id, int newQuantity) {

        String query = "update drugs set quantity = ? where id = ? ";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setInt(1, newQuantity);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean updateDrugQuantity(String drugName, String drugType, String manufacturer, LocalDate manufactureDate,
                                      int newQuantity){

        String query = "update drugs set quantity = ? where drug_name = ? and " +
                "drug_type = ? and manufacturer = ? and manufacture_date = ?";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setInt(1, newQuantity);
            ps.setString(2, drugName);
            ps.setString(3, drugType);
            ps.setString(4, manufacturer);
            ps.setDate(5, Date.valueOf(manufactureDate));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getDrugQuantity(int id){

        String query = "select quantity from drugs where id = ?";

        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.getInt("quantity");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static ObservableList<Drug> getDrugs(){

        ObservableList<Drug> drugs = FXCollections.observableArrayList();

        String query = "select * from drugs order by drug_name, drug_type, manufacturer";
        try {
            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();

            while(rs.next()){

                int drugId = rs.getInt("id");
                int quantity = rs.getInt("quantity");
                String drugName = rs.getString("drug_name");
                DrugType drugType = DrugType.drugValueOf(rs.getString("drug_type"));
                String manufacturer = rs.getString("manufacturer");
                LocalDate manufatureDate = LocalDate.parse(rs.getDate("manufacture_date").toString());
                LocalDate expiringDate = LocalDate.parse(rs.getDate("expiration_date").toString());
                double wholesalePrice = rs.getDouble("wholesale_price");
                double drugPrice = rs.getDouble("retail_price");

                Drug drug = new Drug(drugId,  quantity, drugName, drugType, manufacturer);
                drug.setManufactureDate(manufatureDate);
                drug.setExpiringDate(expiringDate);
                drug.setPrice(wholesalePrice);
                drug.setRetailPrice(drugPrice);

                drugs.add(drug);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return drugs;
    }


    public static ObservableList<XYChart.Data<String, Double>> getYearlySalesData(){

        ObservableList<XYChart.Data<String, Double>> data = FXCollections.observableArrayList();
        Map<Integer, Double> yearlySaleMap = getYearlySalesMap();
        //passing the data to the observable list
        for(Integer key: yearlySaleMap.keySet()){
            data.add(new XYChart.Data<>(String.valueOf(key), yearlySaleMap.get(key)));
        }

        return data;
    }

    public static ObservableList<XYChart.Data<String, Double>> getMonthlySalesData(){

       ObservableList<XYChart.Data<String, Double>> data = FXCollections.observableArrayList();
       Map<Integer, Double> monthlySaleMap = getThisYearMonthlySalesMap();

       int thisMonth = LocalDate.now().getMonthValue();

       for(int key: monthlySaleMap.keySet()){

           String month = MainWindowController.getMonth(key - 1);

           if(key == thisMonth){
                month += "(This month)";
           }


           data.add(new XYChart.Data<>(month, monthlySaleMap.get(key)));
       }

       return data;
    }

    public static ObservableList<XYChart.Data<String, Double>> getWeeklySalesData(){

        ObservableList<XYChart.Data<String, Double>> data = FXCollections.observableArrayList();

        Map<Integer, Double> weeksMap = thisYearWeeklySalesMap();

        int thisWeek = Builder.getWeek(LocalDate.now());

        for(int weekNumber: weeksMap.keySet()){

            String weekNumberString = String.valueOf(weekNumber);

            if(weekNumber == thisWeek){
                weekNumberString += "(This Week)";
            }


            data.add(new XYChart.Data<>(weekNumberString, weeksMap.get(weekNumber)));
        }

        return data;
    }

    public static ObservableList<XYChart.Data<String, Double>> getDailySalesData(){
        Map<LocalDate, Double> totalMap = new HashMap<>();

        ObservableList<XYChart.Data<String, Double>> data = FXCollections.observableArrayList();
        try {
            String query = "select sale_date, amount from sales";

            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();

            while (rs.next()) {
              LocalDate date = LocalDate.parse(rs.getDate("sale_date").toString());
              double total = rs.getDouble("amount");

              if(!totalMap.containsKey(date)) {
                  totalMap.put(date, total);
              }else{
                  totalMap.replace(date, totalMap.get(date) + total);
              }

            }

        }catch (SQLException sqlex){
            sqlex.printStackTrace();
        }

        System.out.println("Sales for today: " + totalMap.get(LocalDate.now()));

        for(LocalDate key: totalMap.keySet()){

            String dayString = key.toString();

            if(key.equals(LocalDate.now())){
                dayString += "(Today)";
            }

            data.add(new XYChart.Data<>(dayString, totalMap.get(key)));
        }

        return data;
    }


    //##############################################################################################################
    //Almost out of stock and almost expired stuff
    //##############################################################################################################

    public static ObservableList<Drug> getAlmostOutOfStockDrugs(boolean isAnalysisOpened){

        ObservableList<Drug> drugsList = FXCollections.observableArrayList();

        String query = "select * from drugs where quantity <= 40";
        try{

            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();
            while(rs.next()){
                int id = rs.getInt("id");
                int remainingQuanity = rs.getInt("quantity");
                String drugName = rs.getString("drug_name");
                String drugType = rs.getString("drug_type");
                String manufacturer= rs.getString("manufacturer");
                Drug outOfStockDrug = new Drug(id, remainingQuanity, drugName, DrugType.drugValueOf(drugType), manufacturer);

                if(isAnalysisOpened){

                    LocalDate manufactureDate = LocalDate.parse(rs.getDate("manufacture_date").toString());
                    LocalDate expireDate = LocalDate.parse(rs.getDate("expiration_date").toString());
                    double price = rs.getDouble("wholesale_price");
                    double retailPrice = rs.getDouble("retail_price");

                    outOfStockDrug.setManufactureDate(manufactureDate);
                    outOfStockDrug.setExpiringDate(expireDate);
                    outOfStockDrug.setPrice(price);
                    outOfStockDrug.setRetailPrice(retailPrice);

                }

                drugsList.add(outOfStockDrug);

            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }

        return drugsList;

    }



    public static ObservableList<Drug> getAlmostExpiredDrugs(){

        ObservableList<Drug> expiredDrugsList = FXCollections.observableArrayList();
        ObservableList<Drug> drugs = getDrugs();

        LocalDate today = LocalDate.now();
        int thisMonth = today.getMonthValue();
        int thisYear = today.getYear();

        for(Drug drug: drugs){

            LocalDate expireDate = drug.getExpiringDate();
            int month = expireDate.getMonthValue();
            int year = expireDate.getYear();

            if(drug.getQuantity() < 1){
                continue;
            }

            if(year ==  thisYear){

                if(expireDate.equals(today) ||
                        (month > thisMonth && month-thisMonth == 1) ||
                        month == thisMonth ||
                        month < thisMonth){
                  expiredDrugsList.add(drug);
                }

                continue;
            }

            if(year < thisYear){
                expiredDrugsList.add(drug);
            }

        }

        return expiredDrugsList;
    }
    //##########################################################################################################


    public static ObservableList<Drug> getSimpleDrugViewData(){
        Map<String, Drug> simpleDrugsMap = new HashMap<>();
        ObservableList<Drug> simpleDrugViewData = FXCollections.observableArrayList();

        String query = "select drug_name, drug_type, manufacturer, wholesale_price, retail_price, sum(quantity) as total_quantity from drugs " +
                "group by drug_name, drug_type, manufacturer";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()){

                String drugName = rs.getString("drug_name");
                String drugType = rs.getString("drug_type");
                String manufacturer = rs.getString("manufacturer");
                double wholesalePrice = rs.getDouble("wholesale_price");
                double retailPrice = rs.getDouble("retail_price");
                int quantity = rs.getInt("total_quantity");

                String key = String.format("%s||%s||%s", drugName, drugType, manufacturer);
                Drug dummyDrug = simpleDrugsMap.get(key);

                if(dummyDrug == null){
                    dummyDrug = new Drug(quantity, drugName, DrugType.drugValueOf(drugType), manufacturer);
                    dummyDrug.setPrice(wholesalePrice);
                    dummyDrug.setRetailPrice(retailPrice);
                    simpleDrugsMap.put(key, dummyDrug);
                }else{
                    dummyDrug.setQuantity(dummyDrug.getQuantity() + quantity);
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        simpleDrugViewData.setAll(simpleDrugsMap.values());
        return simpleDrugViewData;
    }


    //############################################################################################################
    //Totals
    //############################################################################################################
    public static Double getDailySalesTotal(){

        String query = "select sum(amount) as total from sales where sale_date = ? ";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();
            double total = rs.getDouble("total");
            return total;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static double getWeeklySalesTotal(){

        try{
            return thisYearWeeklySalesMap().get(Builder.getWeek(LocalDate.now()));
        }catch (NullPointerException npex){
            return 0;
        }

    }


    public static double getThisMonthSalesTotal(){
        try{
            return getThisYearMonthlySalesMap().get(LocalDate.now().getMonthValue());
        }catch (NullPointerException npex){
            return 0;
        }
    }

    //############################################################################################################
    //Maps
    //############################################################################################################

    private static Map<String, Integer> getDrugSalesMap() throws SQLException {

        int year = LocalDate.now().getYear();

        Map<String, Integer> drugsMap = new HashMap<>();

        String query = "select * from sales order by quantity";
        ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();

        while(rs.next()){

            String drugName = rs.getString("drug_name");
            int quantity = rs.getInt("amount");
            int sale_year = LocalDate.parse(rs.getDate("sale_date").toString()).getYear();

            if(sale_year == year){
                if(!drugsMap.containsKey(drugName)){
                    drugsMap.put(drugName, quantity);
                }else{
                    drugsMap.replace(drugName, drugsMap.get(drugName) + quantity);
                }
            }
        }

        return drugsMap;
    }


    public static Map<Date, Double> getSalesDates() throws SQLException{

        Map<Date, Double> dates = new HashMap<>();

        String query = "select sale_date, amount  from sales";
        ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();

        while (rs.next()) {

            Date sale_date = rs.getDate("sale_date");
            double totalAmount = rs.getDouble("amount");

            if(!dates.containsKey(sale_date)){
                dates.put(sale_date, totalAmount);
            }else{
                dates.replace(sale_date, dates.get(sale_date) + totalAmount);
            }
        }

        return dates;

    }

    public static Map<Integer, Double> getYearlySalesMap(){

        Map<Integer, Double> yearlySalesMap = new HashMap<>();

        String query = "select sale_date, amount from sales";
        try{
            ResultSet rs = DatabaseConnector.getInstance().getConnection().prepareStatement(query).executeQuery();
            while (rs.next()){
                LocalDate saleDate = LocalDate.parse(rs.getDate("sale_date").toString());
                Double amount = rs.getDouble("amount");
                int saleYear = saleDate.getYear();

                if(yearlySalesMap.containsKey(saleYear)){
                    yearlySalesMap.replace(saleYear, yearlySalesMap.get(saleYear) + amount);
                }else{
                    yearlySalesMap.put(saleYear, amount);
                }
            }

        }catch (SQLException sqlex){
            sqlex.printStackTrace();
        }


        return yearlySalesMap;
    }

    static Map<Integer, Double> getThisYearMonthlySalesMap(){

        Map<Integer, Double> monthlySalesMap = new HashMap<>();
        int currentYear = LocalDate.now().getYear();

        try {
            Map<Date, Double> saleDatesMap = getSalesDates();

            for(Date date: saleDatesMap.keySet()){
                LocalDate keyLocalDate = LocalDate.parse(date.toString());

                if(keyLocalDate.getYear() != currentYear){
                    continue;

                }

                int month = keyLocalDate.getMonthValue();
                double amount = saleDatesMap.get(date);

                if(!monthlySalesMap.containsKey(month)){
                    monthlySalesMap.put(month, amount);
                }else{
                    monthlySalesMap.replace(month, monthlySalesMap.get(month) + amount);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return monthlySalesMap;

    }

    static Map<Integer, Double> thisYearWeeklySalesMap(){

        Map<Integer, Double> weeksMap = new HashMap<>();

        Map<Date, Double> datesMap;
        try{
            datesMap = getSalesDates();
        }catch (SQLException sqlex){
            throw new RuntimeException(sqlex);
        }

        int currentYear = LocalDate.now().getYear();

        for(Date date: datesMap.keySet()){

            LocalDate keyLocalDate = LocalDate.parse(date.toString());

            if(keyLocalDate.getYear() != currentYear){
                continue;
            }

            //get the amounts from the dates
            int week = Builder.getWeek(LocalDate.parse(date.toString()));

            if(!weeksMap.containsKey(week)){
                weeksMap.put(week, datesMap.get(date));
            }else{
                weeksMap.replace(week, weeksMap.get(week) + datesMap.get(date));
            }

        }

        return weeksMap;
    }



    //############################################################################################################
    //Sales Averages
    //############################################################################################################

    public static double getYearlySalesAverage(){

        Map<Integer, Double> yearlySalesMap = getYearlySalesMap();

        double yearlySalesAverage = 0.00;

        for(int year: yearlySalesMap.keySet()){
            yearlySalesAverage = yearlySalesAverage + yearlySalesMap.get(year);
        }

        yearlySalesAverage = yearlySalesAverage / yearlySalesMap.size();

        return yearlySalesAverage;
    }


    public static double getMonthlySalesAverage(){

        Map<Integer, Double> monthlySalesMap = getThisYearMonthlySalesMap();

        double grossAmount = 0;

        for(Map.Entry<Integer, Double> entry: monthlySalesMap.entrySet()){
            grossAmount = grossAmount + entry.getValue();
        }

        return grossAmount/monthlySalesMap.size();
    }


    public static double getWeeklySalesAverage(){

        double weeklyGrossTotal = 0.0;

        Map<Integer, Double> weeklySalesMap = thisYearWeeklySalesMap();

        for(int week: weeklySalesMap.keySet()){
            weeklyGrossTotal = weeklyGrossTotal + weeklySalesMap.get(week);
        }

        return weeklyGrossTotal / weeklySalesMap.size();


    }


    public static double getDailySalesAverage(){

        try {

            double grossTotal = 0;
            int currentYear = LocalDate.now().getYear();
            int mapSize = 0;

            Map<Date, Double> dailySalesMap = getSalesDates();

            for(Date key: dailySalesMap.keySet()){
                LocalDate keyLocalDate = LocalDate.parse(key.toString());

                if(keyLocalDate.getYear() == currentYear){
                    mapSize += 1;
                    grossTotal = grossTotal + dailySalesMap.get(key);
                }

            }
            return grossTotal / mapSize;

        } catch (SQLException e) {
            return 0.00;
        }

    }


    //############################################################################################################
    //Sales Summary
    //############################################################################################################

    public static ObservableList<DailySalesSummary> getDailySalesSummary(){

        ObservableList<DailySalesSummary> summaryList = FXCollections.observableArrayList();
        int currentYear = LocalDate.now().getYear();
        try{

            Map<Date, Double> salesMap = getSalesDates();

            for(Date key: salesMap.keySet()){
                LocalDate keyLocalDate = LocalDate.parse(key.toString());
                if(keyLocalDate.getYear() == currentYear){
                    DailySalesSummary summary = new DailySalesSummary(keyLocalDate, salesMap.get(key));
                    summaryList.add(summary);
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return summaryList;
    }

    public static ObservableList<Summary.WeeklySalesSummary> getWeeklySalesSummary(){
        ObservableList<Summary.WeeklySalesSummary> summaryList = FXCollections.observableArrayList();

        Map<Integer, Double> weeklySalesMap = thisYearWeeklySalesMap();

        for(Map.Entry<Integer, Double> entry: weeklySalesMap.entrySet()){
            summaryList.add(new Summary.WeeklySalesSummary(entry.getKey(), entry.getValue()));
        }


        return summaryList;
    }

    public static ObservableList<Summary.MonthlySalesSummary> getMonthlySalesSummary(){

        ObservableList<Summary.MonthlySalesSummary> summaryList = FXCollections.observableArrayList();

        Map<Integer, Double> monthlySalesMap = getThisYearMonthlySalesMap();

        for(Map.Entry<Integer, Double> entry: monthlySalesMap.entrySet()){
            summaryList.add(new Summary.MonthlySalesSummary(entry.getKey(), entry.getValue()));
        }

        return summaryList;
    }

    public static ObservableList<Summary.YearlySalesSummary> getYearlySaleSummary(){

        ObservableList<Summary.YearlySalesSummary> summaryList = FXCollections.observableArrayList();

        Map<Integer, Double> yearlySalesMap = getYearlySalesMap();

        for(Map.Entry<Integer, Double> entry: yearlySalesMap.entrySet()){
            summaryList.add(new Summary.YearlySalesSummary(entry.getKey(), entry.getValue()));
        }

        return summaryList;
    }


    //############################################################################################################
    //Drug Rankings
    //#############################################################################################################

    public static String getMostSoldDrug(){

        String mostSoldDrug = null;
        try {
            Map<String, Integer> drugSalesMap = getDrugSalesMap();

            int maxValue = Integer.MIN_VALUE;

            for (Map.Entry<String, Integer> entry : drugSalesMap.entrySet()) {
                if (entry.getValue() > maxValue) {
                    mostSoldDrug = entry.getKey();
                    maxValue = entry.getValue();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return mostSoldDrug;
    }


    public static ObservableList<DrugSalesRanking> getDrugRankings(){

        ObservableList<DrugSalesRanking> rankings = FXCollections.observableArrayList();

        try {
            Map<String, Integer> drugSalesMap = getDrugSalesMap();
            for(Map.Entry<String, Integer> entry: drugSalesMap.entrySet()){
                String drugName = entry.getKey();
                Integer quantity = entry.getValue();
                DrugSalesRanking ranking = new DrugSalesRanking(drugName, quantity);
                rankings.add(ranking);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rankings;
    }


    //###########################################################################################################
    //Logs
    //###########################################################################################################
    public static boolean addLog(String title, String logDetails){
        String query = "insert into logs(log_date, title,details) values(?, ?, ?)";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, title);
            ps.setString(3, logDetails);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteLog(int id){
        String query = "delete from logs where id = ?";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean clearLogs(){
        String query = "delete from logs";
        try {
            PreparedStatement ps = DatabaseConnector.getInstance().getConnection().prepareStatement(query);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}