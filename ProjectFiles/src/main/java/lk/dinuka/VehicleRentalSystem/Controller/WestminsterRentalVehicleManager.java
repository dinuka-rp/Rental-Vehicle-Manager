package lk.dinuka.VehicleRentalSystem.Controller;

import lk.dinuka.VehicleRentalSystem.Model.Car;
import lk.dinuka.VehicleRentalSystem.Model.Motorbike;
import lk.dinuka.VehicleRentalSystem.Model.RentalVehicleManager;
import lk.dinuka.VehicleRentalSystem.Model.Vehicle;
import lk.dinuka.VehicleRentalSystem.View.GUI;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

public class WestminsterRentalVehicleManager implements RentalVehicleManager {

    private static Scanner scanInput = new Scanner(System.in);

    protected static HashMap<String, Vehicle> allVehicles = new HashMap<>();          //used to check whether the plate No already exists in the system
    protected static List<Vehicle> vehiclesInSystem = new ArrayList<>();       //used for sorting and printing.    protected: making sure that customers can't modify the vehicles in the system
    public static HashMap<String, ArrayList> bookedVehicles = new HashMap<>();      //used to record booked vehicles   (plateNo, ArrayList of Schedules)

    public static List<Vehicle> getVehiclesInSystem() {         //accessed in GUI
        return vehiclesInSystem;
    }

    public static HashMap<String, Vehicle> getAllVehicles() {
        return allVehicles;
    }

    private static String plateNo;
    private static String make;
    private static String model;
    private static String engineCapacity;
    private static double dailyCostD;
    private static BigDecimal dailyCostBigD;
    private static String startType;
    private static double wheelSize;
    private static String transmission;
    private static boolean hasAirCon;
    private static String type;

    private static boolean replaceVeh;          //used to check whether vehicle data is being added or edited


    @Override
    public void addVehicle() {              //add vehicle into system

        //Pointless the getting the inputs in the console because edit option is in the add option (will have to repeat code)

        if (Vehicle.getCount() <= MAX_VEHICLES) {       //checking whether the vehicles existing in the system has occupied all the available parking lots

            int typeSelection;
            do {
                System.out.println("\nChoose the type of Vehicle to be added:");
                System.out.println("1) Car\n2) Motorbike");
                System.out.print(">");
                intInputValidation();
                typeSelection = scanInput.nextInt();
                scanInput.nextLine();              //to consume the rest of the line

            } while (!(typeSelection == 1 || typeSelection == 2));


            System.out.println("\nEnter Plate No:");
            System.out.print(">");
            plateNo = scanInput.nextLine();

            if (allVehicles.containsKey(plateNo)) {
                System.out.println("This Plate No. exists in the system.");
                System.out.println();           //to keep space for clarity

                replaceVeh = false;

                printListForEdit();         //display information of vehicle

                System.out.println();           //to keep space for clarity
                System.out.println("Do u want to edit information related to this vehicle?");
                System.out.print(">");

                boolean edit = yesOrNo();

                if (edit) {

                    replaceVeh = true;

                    //remove vehicle from db
                    DatabaseController.deleteFromSystemDB(plateNo);

                    addInfo(typeSelection);             //add information related to a Vehicle of identified plateNo.

                    deleteFile();       //deleting existing file
                    save();     //saving info in file

                    API.getAllVehiclesToFront();                //update vehicles in front end


                } else {
                    System.out.println();       //keeps space and goes back to main menu
                }
            } else {

                addInfo(typeSelection);             //add information related to a Vehicle of identified plateNo.

                save();     //saving info in file

                API.getAllVehiclesToFront();                //update vehicles in front end

            }


        } else {
            System.out.println("There are no available spaces. 50 vehicles have been added!");
        }
    }

    @Override
    public void deleteVehicle() {                  //delete vehicle by entering plate no. of vehicle
        System.out.println("Enter the plate number of the vehicle that u desire to delete:");
        System.out.print(">");              //get plateNo from user to choose vehicle to be deleted
        String searchNo = scanInput.nextLine();

        if (allVehicles.containsKey(searchNo)) {
            Vehicle vehicleToBeDeleted = allVehicles.get(searchNo);

            type = vehicleToBeDeleted.getType();

            System.out.println("\nA " + type + " has been deleted from the system.");
            System.out.println("The details of the vehicle that was deleted: " + vehicleToBeDeleted.toString());      //displaying information of deleted vehicle

            vehiclesInSystem.remove(vehicleToBeDeleted);
            allVehicles.remove(searchNo);
            Vehicle.count -= 1;          //decreasing the number of vehicles from the system by one

            //Deleting from noSQL Database
            DatabaseController.deleteFromSystemDB(searchNo);

            System.out.println("There are " + (MAX_VEHICLES - Vehicle.getCount()) + " parking lots left in the garage.");

            save();     //save changes to file

            API.getAllVehiclesToFront();                //update vehicles in front end

        } else {
            System.out.println("There's no vehicle related to the Plate No: " + searchNo);
        }
    }


    @Override
    public void printList() {       //prints list of vehicles in the system

        Collections.sort(vehiclesInSystem);     //sort vehicles alphabetically, according to make


        // print the plate number, the type of vehicle (car/ van/ motorbike).

        String leftAlignFormat = "| %-15s | %-12s |%n";

        System.out.format("+-----------------+--------------+%n");
        System.out.format("|   Plate ID      |   Type       |%n");
        System.out.format("+-----------------+--------------+%n");

        for (Vehicle item : vehiclesInSystem) {
            if (item instanceof Car) {
                System.out.format(leftAlignFormat, item.getPlateNo(), "Car");
            } else if (item instanceof Motorbike) {
                System.out.format(leftAlignFormat, item.getPlateNo(), "Motorbike");
            }
        }
        System.out.println("+--------------------------------+");
    }

    @Override
    public void save() {        //saves the information of vehicles entered into the system
        //Rewrite the file every time a change is made.

        deleteFile();       //delete existing file

        try {       //creating the file
            File myFile = new File("allVehicles.txt");
            myFile.createNewFile();

//                System.out.println("\nFile created: " + myFile.getName());
            FileWriter soldFile = new FileWriter("allVehicles.txt", true);


            soldFile.write(String.format("+-----------------+---------------+--------------+----------------+---------------+-----------+--------------+--------+-----------------+------------+%n"));
            soldFile.write(String.format("|   Plate ID      |   Make        |   Model      | Engine Capacity| Daily Cost(£) |   Type    | transmission | AirCon | Start type      | Wheel Size |%n"));
            soldFile.write(String.format("+-----------------+---------------+--------------+----------------+---------------+-----------+--------------+--------+-----------------+------------+%n"));
//                soldFile.write(System.getProperty("line.separator"));       //line break


            String leftAlignFormat2 = "| %-15s | %-13s | %-12s | %-14s | %-13s | %-9s | %-12s | %-6s | %-15s | %-10s |%n";


            //writing into the file
            for (Vehicle veh : vehiclesInSystem) {
                if (veh instanceof Motorbike) {
                    soldFile.write(String.format(leftAlignFormat2, veh.getPlateNo(), veh.getMake(), veh.getModel(), veh.getEngineCapacity(),
                            veh.getDailyCost(), veh.getType(), "      -     ", "   -  ", ((Motorbike) veh).getStartType(), ((Motorbike) veh).getWheelSize()));
                } else {
                    soldFile.write(String.format(leftAlignFormat2, veh.getPlateNo(), veh.getMake(), veh.getModel(), veh.getEngineCapacity(),
                            veh.getDailyCost(), veh.getType(), ((Car) veh).getTransmission(), ((Car) veh).isHasAirCon(), "       -       ", "     -    "));
                }
                soldFile.write(System.getProperty("line.separator"));       //line break
            }
            soldFile.write(String.format("+-----------------+---------------+--------------+----------------+---------------+-----------+--------------+--------+-----------------+------------+%n"));

            soldFile.close();

        } catch (IOException e) {
            System.out.println("\nAn error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public void viewGUI() {         //opens a chosen GUI
        int guiSelection;

        do {
            System.out.println("\nChoose the required GUI:");
            System.out.println("1) Angular\n2) JavaFX");
            System.out.print(">");
            intInputValidation();
            guiSelection = scanInput.nextInt();
            scanInput.nextLine();              //to consume the rest of the line

        } while (!(guiSelection == 1 || guiSelection == 2));


        if (guiSelection == 1) {            // Angular GUI

            API.getAllVehiclesToFront();                //send vehicles to front end
            API.postBookingsFromFront();                //handle booking
            API.postAvailabilityFromFront();            //handle availability


            //Open Angular GUI in browser
            ProcessBuilder builder = new ProcessBuilder("explorer.exe", "http://localhost:4200/");

            builder.redirectErrorStream(true);

            Process p = null;
            try {
                p = builder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                try {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(line);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {                            //JavaFX GUI

            GUI.main(null);       //used to open javafx application

        }


    }


//    ---- repeated methods ----

    private static void addInfo(int typeSelection) {          //method to add information related to a Vehicle of identified plateNo.

        if (replaceVeh) {
            vehiclesInSystem.remove(allVehicles.get(plateNo));              //removing vehicle from ArrayList, if editing it's information
        }

        if (typeSelection == 1) {       //new Car chosen
            addCommonInfo();

            type = "Car";

            System.out.println("\nEnter the type of transmission:");
            System.out.print(">");
            transmission = scanInput.nextLine();


            System.out.println("\nDoes this car have A/C?");
            System.out.print(">");

            hasAirCon = yesOrNo();


            Vehicle newCar = new Car(plateNo, make, model, engineCapacity, dailyCostBigD, type, transmission, hasAirCon);

            allVehicles.put(plateNo, newCar);           //adding a car into the allVehicles hashMap
            vehiclesInSystem.add(newCar);

            //adding new Car to noSQL database
            DatabaseController.addToSystemDB(plateNo, make, model, engineCapacity, dailyCostD, type, transmission, hasAirCon);

            System.out.println(newCar);        //displaying added vehicle

        } else if (typeSelection == 2) {         //new Motorbike chosen
            addCommonInfo();

            type = "Motorbike";

            System.out.println("\nEnter start type:");
            System.out.print(">");
            startType = scanInput.nextLine();

            System.out.println("\nEnter wheel size:");
            System.out.print(">");
            doubleInputValidation();
            wheelSize = scanInput.nextDouble();
            scanInput.nextLine();           //to consume the rest of the line


            Vehicle newBike = new Motorbike(plateNo, make, model, engineCapacity, dailyCostBigD, type, startType, wheelSize);

            allVehicles.put(plateNo, newBike);           //adding a motorbike into the allVehicles hashMap
            vehiclesInSystem.add(newBike);

            //adding new Bike to noSQL database
            DatabaseController.addToSystemDB(plateNo, make, model, engineCapacity, dailyCostD, type, startType, wheelSize);

            System.out.println(newBike);        //displaying added vehicle
        }

        System.out.println("\nThere are " + (MAX_VEHICLES - Vehicle.getCount()) + " parking lots left, to park vehicles.");

    }


    private static void addCommonInfo() {       //common information related to Car & Motorbike in addVehicle


        System.out.println("\nEnter Make:");
        System.out.print(">");
        make = scanInput.nextLine();

        System.out.println("\nEnter Model:");
        System.out.print(">");
        model = scanInput.nextLine();


        System.out.println("\nEnter Engine Capacity (in CC):");
        System.out.print(">");
        engineCapacity = scanInput.nextLine();

        System.out.println("\nEnter Daily cost (in £):");
        System.out.print(">$");
        doubleInputValidation();
        dailyCostD = scanInput.nextDouble();

        dailyCostBigD = BigDecimal.valueOf(dailyCostD);     //converting double to BigDecimal, to use for calculations


        scanInput.nextLine();              //to consume the rest of the line

    }


    public static void printListForEdit() {
        //print information of vehicle when asked whether to edit
        System.out.println("Make: " + allVehicles.get(plateNo).getMake());
        System.out.println("Model: " + allVehicles.get(plateNo).getModel());
        System.out.println("Engine Capacity: " + allVehicles.get(plateNo).getEngineCapacity());
        System.out.println("Daily Cost (in £): " + allVehicles.get(plateNo).getDailyCost());
        System.out.println("Type: " + allVehicles.get(plateNo).getType());

        if (allVehicles.get(plateNo) instanceof Car) {
            System.out.println("Transmission: " + ((Car) allVehicles.get(plateNo)).getTransmission());
            System.out.println("Has Air Conditioning: " + ((Car) allVehicles.get(plateNo)).isHasAirCon());
        } else {
            System.out.println("Start Type: " + ((Motorbike) allVehicles.get(plateNo)).getStartType());
            System.out.println("Wheel Size: " + ((Motorbike) allVehicles.get(plateNo)).getWheelSize());
        }
    }


    private static boolean yesOrNo() {           //gets yes/ no input

        while (!scanInput.hasNextBoolean()) {                                            //check whether this works as expected!!!!!!!!!!!
            String inputYN = scanInput.nextLine().toLowerCase();
            if (inputYN.equals("y") || inputYN.equals("yes")) {
                return true;
            } else if (inputYN.equals("n") || inputYN.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input. Please try again.");
                System.out.print(">");
            }
        }
        return false;           //won't reach this point (added to get rid of the missing return statement error)
    }


    private static void intInputValidation() {                     //validating integer input

        while (!scanInput.hasNextInt()) {
            System.out.println("Only integer numbers are allowed! Please provide a valid input");              //error handling message for characters other than integers
            scanInput.next();                                                     //removing incorrect input entered
        }
    }

    private static void doubleInputValidation() {                     //validating double input

        while (!scanInput.hasNextDouble()) {
            System.out.println("Only numbers are allowed! Please provide a valid input");              //error handling message for characters other than integers
            scanInput.next();                                                     //removing incorrect input entered
        }
    }


    private static void deleteFile() {       //deleting file, if exists (When vehicle is added/ deleted/ edited)
        try {
            Files.deleteIfExists(Paths.get("C:\\Users\\Dell XPS15\\Documents\\IIT Work\\L5\\OOP\\Coursework 01\\OOP-CW\\OOP-CW+\\allVehicles.txt"));
        } catch (NoSuchFileException e) {
            System.out.println("No such file/directory exists");
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Directory is not empty.");
        } catch (IOException e) {
            System.out.println("Invalid permissions.");
        }
    }
}


/*
References:

Open URL in browser (Angular GUI)
https://alvinalexander.com/blog/post/java/how-open-read-url-java-url-class-example-code

Java Big Decimal
https://www.geeksforgeeks.org/bigdecimal-class-java/

https://stackoverflow.com/questions/27409718/java-reading-multiple-objects-from-a-file-as-they-were-in-an-array

replacing hashMap value
https://stackoverflow.com/questions/35297537/difference-between-replace-and-put-for-hashmap

-------

https://stackoverflow.com/questions/13102045/scanner-is-skipping-nextline-after-using-next-or-nextfoo

https://www.callicoder.com/java-arraylist/

https://stackoverflow.com/questions/48720936/java-enhanced-for-loop-for-arraylist-with-custom-object

To open GUI from console
https://stackoverflow.com/questions/2550310/can-a-main-method-of-class-be-invoked-from-another-class-in-java

File handling
https://www.w3schools.com/java/java_files.asp

Next line in file handling
https://stackoverflow.com/questions/17716192/insert-line-break-when-writing-to-file

File handling - table format
https://stackoverflow.com/questions/26229140/writing-data-to-text-file-in-table-format

Delete file
https://www.geeksforgeeks.org/delete-file-using-java/

Table display format for print list
https://stackoverflow.com/questions/15215326/how-can-i-create-table-using-ascii-in-a-console

Selling date/time
https://www.javatpoint.com/java-get-current-date

Search for object in ArrayList
https://stackoverflow.com/questions/17526608/how-to-find-an-object-in-an-arraylist-by-property
*/