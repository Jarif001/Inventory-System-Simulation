import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int I,M,P; // initial invLvl, no. of months, no. of policies
        int D; // no. of demand sizes
        float beta_D; // mean inter-demand time in month
        float K, i, h, pi; // setup, perUnitIncremental, holding, shortage cost
        float min_lag, max_lag; // min-max lag in month
        float[] cumulativeDemand; // D space separated values
        int[][] sSpair; // P lines

        // Read the input file
        String filePath = "in.txt";
        File inputFile = new File(filePath);
        Scanner scanner;
        try{
            scanner = new Scanner(inputFile);

            // I,M,P
            String theRow = scanner.nextLine();
            String[] words = theRow.split(" ");
            I = Integer.parseInt(words[0]);
            M = Integer.parseInt(words[1]);
            P = Integer.parseInt(words[2]);

            // D, beta_D
            theRow = scanner.nextLine();
            words = theRow.split(" ");
            D = Integer.parseInt(words[0]);
            beta_D = Float.parseFloat(words[1]);

            // K, i, h, pi
            theRow = scanner.nextLine();
            words = theRow.split(" ");
            K = Float.parseFloat(words[0]);
            i = Float.parseFloat(words[1]);
            h = Float.parseFloat(words[2]);
            pi = Float.parseFloat(words[3]);

            // min max lag
            theRow = scanner.nextLine();
            words = theRow.split(" ");
            min_lag = Float.parseFloat(words[0]);
            max_lag = Float.parseFloat(words[1]);

            // cumulative distribution
            cumulativeDemand = new float[D+1];
            theRow = scanner.nextLine();
            words = theRow.split(" ");
            for(int j = 1; j <= D; j++){
                cumulativeDemand[j] = Float.parseFloat(words[j-1]);
            }

            // s,S pair
            sSpair = new int[P][2];
            for(int j = 0; j < P; j++){
                theRow = scanner.nextLine();
                words = theRow.split(" ");
                sSpair[j][0] = Integer.parseInt(words[0]);
                sSpair[j][1] = Integer.parseInt(words[1]);
            }

            // Main functionality
            int numEvents = 4;
            File outFile = new File("output.txt");

            // Output file introductions
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile)); // creating an output file, not appending
            writer.write("------Single-Product Inventory System------\n\n");
            writer.write("Initial inventory level: " + I + " items\n\n");
            writer.write("Number of demand sizes: " + D + "\n\n");
            String str = String.format("%s %.2f %.2f %.2f %.2f\n\n", "Distribution function of demand sizes:", cumulativeDemand[1], cumulativeDemand[2], cumulativeDemand[3], cumulativeDemand[4]);
            writer.write(str);
            str = String.format("%s %.2f %s\n\n", "Mean inter-demand time:", beta_D, "months");
            writer.write(str);
            str = String.format("%s %.2f %s %.2f %s\n\n", "Delivery lag range:", min_lag, "to", max_lag, "months");
            writer.write(str);
            writer.write("Length of simulation: " + M + " months\n\n");
            writer.write("Costs:\n");
            str = String.format("%s %.2f\n", "K =", K);
            writer.write(str);
            str = String.format("%s %.2f\n", "i =", i);
            writer.write(str);
            str = String.format("%s %.2f\n", "h =", h);
            writer.write(str);
            str = String.format("%s %.2f\n\n", "pi =", pi);
            writer.write(str);
            writer.write("Number of policies: " + P + "\n\n");
            writer.write("Policies:\n");
            writer.write("--------------------------------------------------------------------------------------------------\n");
            str = String.format(" %-13s %-18s %-22s %-20s %-22s\n", "Policy", "Avg_total_cost", "Avg_ordering_cost", "Avg_holding_cost", "Avg_shortage_cost");
            writer.write(str);
            writer.write("--------------------------------------------------------------------------------------------------\n\n");
            writer.flush();
            // Output file introduction ends

            for(int itn = 0; itn < P; itn++){
                // setting up the (s, S) pair
                InventorySystem inventorySystem = new InventorySystem(I, M, D, beta_D, K, i, h, pi, min_lag, max_lag, cumulativeDemand, sSpair[itn], numEvents);
                boolean willContinue = true;
                do{
                   willContinue = inventorySystem.timing();
                   inventorySystem.updateTimeAvgStats(); // update the stats at beginning (at first the ivn can both be + or -. update the area)
                   if(inventorySystem.nextEventType == 1){
                       // order arrival
                       inventorySystem.orderArrival();
                   }
                   else if(inventorySystem.nextEventType == 2){
                       // demand
                       inventorySystem.demand();
                   }
                   else if(inventorySystem.nextEventType == 3){
                       // report
                       inventorySystem.report(outFile);
                       if(itn == P-1){
                           System.out.println("Generated report. Simulation stops!");
                       }
                   }
                   else{
                       // evaluate
                       inventorySystem.evaluate();
                   }
                }while(willContinue);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}