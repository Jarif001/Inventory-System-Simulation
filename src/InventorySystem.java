import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InventorySystem {
    public int inventoryLvl, numberOfMonths; // initial invLvl, no. of months, no. of policies
    public int D; // no. of demand sizes
    public float meanInterDemand; // mean inter-demand time in month
    public float setupCost, incrementalCost, holdingCost, shortageCost; // setup, perUnitIncremental, holding, shortage cost
    public float min_lag, max_lag; // min-max lag in month
    public float[] cumulativeDemand; // D space separated values
    int smalls, bigs; // s, S pair

    // simulations variables
    float simTime;
    float timeLastEvent;
    float[] timeNextEvent;
    int nextEventType, numberOfEvents;
    int amount;

    // stats
    float totalOrderingCost, areaHolding, areaShortage;


    public InventorySystem(int i, int m, int d, float beta_D, float k, float i1, float h, float pi, float min_lag, float max_lag, float[] cumulativeDemand, int[] sSpair, int noOfEvents) {
        inventoryLvl = i;
        numberOfMonths = m;
        D = d;
        this.meanInterDemand = beta_D;
        setupCost = k;
        this.incrementalCost = i1;
        this.holdingCost = h;
        this.shortageCost = pi;
        this.min_lag = min_lag;
        this.max_lag = max_lag;
        this.cumulativeDemand = cumulativeDemand;
        this.smalls = sSpair[0]; // reorder point
        this.bigs = sSpair[1]; // max order level

        // time
        simTime = 0;
        timeLastEvent = 0;
        numberOfEvents = noOfEvents;
        timeNextEvent = new float[noOfEvents+1]; // from 1-4
        timeNextEvent[1] = Float.MAX_VALUE; // arrival is out of consideration
        timeNextEvent[2] = simTime + exponential(meanInterDemand);
        timeNextEvent[3] = numberOfMonths; // number of months. 3 --> stopping condition
        timeNextEvent[4] = 0; // inventory evaluation at the start
        amount = 0;

        // Stats
        totalOrderingCost = 0;
        areaHolding = 0;
        areaShortage = 0;

    }

    private float exponential(float meanInter) {
        return (float) (-meanInter * Math.log(Math.random()));
    }

    public boolean timing(){
        float minTime = Float.MAX_VALUE;
        // determine the event that occurs in the nearest time
        for(int i = 1; i <= numberOfEvents; i++){
            if(timeNextEvent[i] < minTime){
                minTime = timeNextEvent[i];
                nextEventType = i; // 1 for arrival, 2 for departure
            }
        }
        if(nextEventType == 3){// end of total months
            // end the simulation
            return false;
        }
        // simulation continues
        simTime = minTime; // simulation clock is taken to the event time
        return true;
    }

    public void updateTimeAvgStats(){
        float timeSinceLastEvent = simTime - timeLastEvent;
        timeLastEvent = simTime;
        if(inventoryLvl < 0){ // negative --> shortage
            areaShortage -= (inventoryLvl * timeSinceLastEvent);
        }
        else if(inventoryLvl > 0){
            areaHolding += (inventoryLvl * timeSinceLastEvent);
        }
    }

    public void orderArrival(){
        inventoryLvl += amount;
        timeNextEvent[1] = Float.MAX_VALUE; // next arrival set
    }

    private int randomInteger(float[] probDist){
        float u = (float) Math.random();
        int number = 1;
        while(u >= probDist[number]){
            number++;
        }
        return number;
    }

    public void demand(){ // its like client ordering
        inventoryLvl -= randomInteger(cumulativeDemand); // random demand size
        timeNextEvent[2] = simTime + exponential(meanInterDemand);
    }

    private float uniform(float min, float max){
        return min + (float)(Math.random() * (max - min)); // returns a float between min and max
    }

    public void evaluate(){
        if(inventoryLvl < smalls){ // reorder
            amount = bigs - inventoryLvl;
            totalOrderingCost += (setupCost + incrementalCost*amount);
            timeNextEvent[1] = simTime + uniform(min_lag, max_lag); // scheduling the order arrival time withing min and max lag
        }
        timeNextEvent[4] = simTime + 1; // set the next evaluation on next month
    }

    public void report(File outfile){
        float avgHoldingCost, avgOrderingCost, avgShortageCost;
        avgOrderingCost = totalOrderingCost / numberOfMonths;
        avgHoldingCost = holdingCost * areaHolding / numberOfMonths;
        avgShortageCost = shortageCost * areaShortage / numberOfMonths;
        float avgTotalCost = avgHoldingCost + avgOrderingCost + avgShortageCost;
        // write to file
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outfile, true));
            String policy = "(" + smalls + ", " + bigs + ")";
            String str = String.format("%-13s \t%-18.2f \t%-22.2f \t%-20.2f \t%-22.2f\n\n", policy, avgTotalCost, avgOrderingCost, avgHoldingCost, avgShortageCost);
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
