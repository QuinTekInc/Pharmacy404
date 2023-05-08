package data.models;

public enum DrugType {

    TABLET("Tablet"),
    CAPSULE("Capsule"),
    SYRUP("Syrup"),
    POWDER("Powder"),
    OIL("Oil"),
    OINTMENT("Ointment");

    final String typeName;

    private DrugType(String typeName){
        this.typeName = typeName;
    }

    public static DrugType drugValueOf(String typeName){
        for(DrugType drugType: DrugType.values()){
            if(drugType.typeName.equals(typeName)){
                return drugType;
            }
        }

        throw new IllegalArgumentException(typeName + " does not exist in " + DrugType.class.getName());
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
