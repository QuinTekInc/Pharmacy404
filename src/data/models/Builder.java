package data.models;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Builder {

    public static String defaultAppImage = "/alert/icons/icons8-pharmacy-shop-96.png";

    public static String buildDatabasePath(){

        String userProperty = System.getProperty("user.home");
        String dbDir = String.format("%s/DB_P404", userProperty);

        boolean isCreated = new File(dbDir).mkdir();

        return dbDir;

    }

    public static String buildDrugName(String name, String type, String manufacturer){
        return String.format("%s %s by %s", name, type, manufacturer);
    }

    public static String buildSaleUpdateLog(int sale_id, int oldQuantity, int newQuantity, String oldDrugName,
                            String newDrugName, double oldDrugAmount, double newDrugAmount){

        StringBuilder logStringBuilder = new StringBuilder("Sale has been updated\n");
        logStringBuilder.append(String.format("Sale ID: %d\n", sale_id));

        if(oldQuantity==newQuantity){
            logStringBuilder.append(String.format("Quantity: %d", oldQuantity));
        }else{
            logStringBuilder.append(String.format("Previous Quantity: %d\nNew Quantity: %d", oldQuantity, newQuantity));
        }

        logStringBuilder.append("\n");

        if(oldDrugName.equals(newDrugName)){
            logStringBuilder.append(String.format("Drug Name: %s", oldDrugName));
        }else{
            logStringBuilder.append(String.format("Previous Drug Name: %s\nNew Drug Name: %s", oldDrugName, newDrugName));
        }

        logStringBuilder.append("\n");

        if(oldDrugAmount == newDrugAmount){
            logStringBuilder.append(String.format("Amount: %.2f", oldDrugAmount));
        }else{
            logStringBuilder.append(String.format("Previous Amount: %.2f\nNew Amount: %.2f", oldDrugAmount, newDrugAmount));
        }

        return  logStringBuilder.toString();

    }


    public static String buildDateString(LocalDate selectedDate){

        String dayOfWeek = selectedDate.getDayOfWeek().toString();
        int dayOfMonth = selectedDate.getDayOfMonth();
        String month = selectedDate.getMonth().toString();
        int year = selectedDate.getYear();

        return String.format("%s, %d %s, %d", dayOfWeek, dayOfMonth, month, year);

    }

    public static int getWeek(LocalDate localDate){
        // Change this to the date you want to calculate the week of
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int week = localDate.get(weekFields.weekOfWeekBasedYear());

        return week;

    }

}
