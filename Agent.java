// This class controls the agents.
// NOTE: Much of the spatial logic is contained in class Item, which this clas inherits from.

import java.io.FileInputStream;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.awt.*;  
import java.util.Random;
import java.util.Iterator;
import java.io.IOException;

public class Agent extends Item implements java.io.Serializable , Comparable<Agent>{
    double energy;
    ArrayList<Long> pedigree;
    transient Random R;
    transient World myworld;
    int num;
    double rotation;
    double speed, fight;
    boolean isUpdated;
    double heading;
    long age;
    double SIGMAFOOD = .2; double MULTFOOD = .1;
    double SIGMAOTHER = .2; double MULTOTHER = .1;
    double[][] w;
    double[] neury, neurx;
    int[] validneur;
    double dtdivtau , meannormw;

    Agent() // No-arg constructor for serialization
    {
        //super(ww);
        /*num = nb;
        pedigree = new ArrayList<Long>();
        R= null;
        dtdivtau = 1.0 / myw.TAU;
        validneur = new int[myworld.NBNEUR]; for (int ii=0; ii < myworld.NBNEUR; ii++) validneur[ii] = 0;
        neurx = new double[myworld.NBNEUR];
        neury = new double[myworld.NBNEUR];
        for (int ii=0; ii < myworld.NBNEUR; ii++){ 
            neury[ii] = 0.0; neurx[ii] = 0.0; 
        }
        w = new double[myworld.NBNEUR][myworld.NBNEUR];
        speed=0.0; fight=0;
        rotation=0.0;
        heading = R.nextDouble() * 2 * Math.PI;
        randomizeNet();
        initialize();*/
    }


    Agent(World ww, int nb)
    {
        super(ww);
        num = nb;
        pedigree = new ArrayList<Long>();
        myworld = ww;
        dtdivtau = 1.0 / myw.TAU;
        R = myworld.R;
        energy = 0; age=0;
        validneur = new int[myworld.NBNEUR]; for (int ii=0; ii < myworld.NBNEUR; ii++) validneur[ii] = 0;
        neurx = new double[myworld.NBNEUR];
        neury = new double[myworld.NBNEUR];
        for (int ii=0; ii < myworld.NBNEUR; ii++){ 
            neury[ii] = 0.0; neurx[ii] = 0.0; 
        }
        w = new double[myworld.NBNEUR][myworld.NBNEUR];
        speed=0.0; fight=0;
        rotation=0.0;
        heading = R.nextDouble() * 2 * Math.PI;
        randomizeNet();
        initialize();
    }
    public int compareTo(Agent a)
    {
        return (int)Math.signum(getEnergy() - a.getEnergy());
    }
    public void randomizeNet(){
        meannormw = 0;
        for (int ii=0; ii < myworld.NBNEUR; ii++)
            for (int jj=0; jj < myworld.NBNEUR; jj++){
                w[ii][jj] = (2.0 * R.nextDouble() - 1.0) ;
                //w[ii][jj] = (2.0 * R.nextDouble() - 1.0) / Math.sqrt((double)myworld.NBNEUR);
                meannormw += Math.abs(w[ii][jj]);
            }
        meannormw /= (myworld.NBNEUR * myworld.NBNEUR);
        SIGMAFOOD = R.nextDouble();
        MULTFOOD = R.nextDouble();
        SIGMAOTHER = R.nextDouble();
        MULTOTHER = R.nextDouble();
        for (int ii=0; ii < myworld.NBNEUR; ii++)
            validneur[ii]= R.nextInt(2);
    }
    public void resetNeurons() { 
        for (int n=0; n<myworld.NBNEUR; n++){
            neurx[n] = 0;
            neury[n] = 0;
        }
    }
    public void resetEnergy() { energy = myworld.INITENERGY; } 
    public void increaseEnergy() { energy ++; } 
    public void decreaseEnergy() { energy --; } 
    public void copyFrom(Agent A){
        energy = A.energy; // Sometimes this won't be wanted, but in these situations you will have to call initialize() after the copy anyway.
        pedigree = new ArrayList<Long>(A.pedigree);
        for (int ii=0; ii < myworld.NBNEUR; ii++){
            validneur[ii] = A.validneur[ii];
            for (int jj=0; jj < myworld.NBNEUR; jj++)
                w[ii][jj] = A.w[ii][jj];
        }
        meannormw = A.meannormw;
        SIGMAFOOD = A.SIGMAFOOD;
        MULTFOOD = A.MULTFOOD;
        SIGMAOTHER = A.SIGMAOTHER;
        MULTOTHER = A.MULTOTHER;
    }
    public void initialize()
    {
        age=0; speed=0; fight=0;
            randPos();
            resetNeurons();
            resetEnergy();
    }
    public double getEnergy() { return energy /*- meannormw * 15.0 */ ; }
    
    public double explin(double x) { if (x<0) return Math.exp(x); else return 1.0+x; }
    public void runNetwork()
    {
        double tempx;
            int startcol;
        for (int row=0; row < myworld.NBNEUR; row++){
            if (validneur[row] == 0)
                continue; // Has little effect on performance. Apparently network running is not a major cost... Distance/angle computations are!
            tempx = 0;
            for (int col=0; col < myworld.NBNEUR; col++)
                if ((row > 2) || ( col > 10)) // The actuators can't receive direct sensor / actuator input! But note this prevent hard biases into the actuators too...
                    tempx += w[row][col] * neury[col];
            neurx[row] += dtdivtau * (tempx - neurx[row]);
        }
        for (int row=0; row < myworld.NBNEUR; row++){
            if ((row ==0) || (row == 2))
                neury[row] = explin(neurx[row]);
            else
                neury[row] = Math.tanh(neurx[row]);
        }
        //neury[0] = (1.0 + neury[0]) / 2.0;  // Speed is positive
        //neury[2] = (1.0 + neury[2]) / 2.0;  // So is fight
    }
    public void mutate()
    {
        meannormw = 0;
        for (int ii=0; ii < myworld.NBNEUR; ii++)
            if (R.nextDouble() < myworld.PROBAMUT)
                validneur[ii] = 1 - validneur[ii];
        for (int ii=0; ii < myworld.NBNEUR; ii++)
            for (int jj=0; jj < myworld.NBNEUR; jj++)
            {
                ///w[ii][jj] += myworld.MUTATIONSIZE * R.nextGaussian();
                //w[ii][jj] *= .99;
                if (R.nextDouble() < myworld.PROBAMUT){
                    double cauchy = Math.tan((R.nextDouble() - .5) * Math.PI);
                    w[ii][jj] += myworld.MUTATIONSIZE * cauchy;
                    w[ii][jj] *= .99;
                //    w[ii][jj] += myworld.myworld.MUTATIONSIZE * R.nextGaussian();
                //    w[ii][jj] *= .99;
                }
                if (w[ii][jj] > myworld.MAXW)
                    w[ii][jj] = myworld.MAXW;
                if (w[ii][jj] < -myworld.MAXW)
                    w[ii][jj] = -myworld.MAXW;
                meannormw += w[ii][jj];
            }
        if (R.nextDouble() < myworld.PROBAMUT)
            SIGMAFOOD += .2 * R.nextGaussian();
        if (R.nextDouble() < myworld.PROBAMUT)
            MULTFOOD += .2 * R.nextGaussian();
        if (R.nextDouble() < myworld.PROBAMUT)
            SIGMAOTHER += .2 * R.nextGaussian();
        if (R.nextDouble() < myworld.PROBAMUT)
            MULTOTHER += .2 * R.nextGaussian();
        if (SIGMAFOOD < .0001) SIGMAFOOD = .0001;
        if (MULTFOOD < .0001) MULTFOOD = .0001;
        if (SIGMAOTHER < .0001) SIGMAOTHER = .0001;
        if (MULTOTHER < .0001) MULTOTHER = .0001;
        meannormw /= (myworld.NBNEUR * myworld.NBNEUR);

    }



    // This function controls the agent's behavior.
    public void update()
    {
        double dist, angle;
        int sensorR, sensorL;
        age++;
        neury[10] = 1.0;  // Bias
        neury[8] = 0.0;
        neury[9] = 0.0; // 2.0 * (myworld.POISONFIRSTHALF - .5);
         neury[4] = 0.0; neury[5] = 0.0;
         neury[6] = 0.0; neury[7] = 0.0;

         fight = neury[2];
        // Check where the food bits (and poison bits!) are, whether we have eaten one, and fill
        // the sensors with appropriate values:
        for (Iterator<FoodBit> iter = myworld.food.listIterator(); iter.hasNext(); ) // Iterator allows us to remove elements from the list within the loop
        {
            //if ( (n < myworld.FOODSIZE /2  && myworld.POISONFIRSTHALF == 1) || (n >= myworld.FOODSIZE /2  && myworld.POISONFIRSTHALF == 0))  { sensorL = 2; sensorR = 3; } else{ sensorL = 4; sensorR = 5;}
            //if ( n < myworld.FOODSIZE /2)  { sensorL = 2; sensorR = 3; } else{ sensorL = 4; sensorR = 5;}
            FoodBit fooditem = iter.next();
            sensorL = 4; sensorR = 5;
            dist = getDistanceFrom(fooditem); // getDistanceFrom and getAngleFrom are from ancestor class Item
            if (dist > 500)
                continue;
            if (dist < myworld.EATRADIUS)  // Eaten!
            {
                energy += myworld.FOODENERGY;
                    //neury[8] = 10.0;  // Would be interesting to know if this improves performance....
                    iter.remove();
            }
            else
            {
                angle = getAngleFrom(fooditem);
                if ((angle-heading < 3.0) && (angle-heading > 0))
                    neury[sensorL] += 1.0 / (SIGMAFOOD + MULTFOOD * dist);
                if ((angle-heading > -3.0) && (angle-heading < 0))
                    neury[sensorR] += 1.0 / (SIGMAFOOD + MULTFOOD * dist);
            }

        }
        double mindist = 10000.0;
        for (Iterator<Agent> iter = myworld.population.listIterator() ; iter.hasNext(); )
        {
            Agent other = iter.next();
            if (other == this)
                continue;
            dist = getDistanceFrom(other); // Note that this computes distance twice for each pair... inelegant.
            if (dist > 500)
                continue;
            // If you just use the sum of all the disances as a penalty, moving towards the closest agent will have very, very little impact on the total....
            if (dist < mindist)
                mindist =  dist;            
            sensorL = 6; sensorR = 7;
            angle = getAngleFrom(other);
            if ((angle-heading < 3.0) && (angle-heading > 0))
                neury[sensorL] += 1.0 / (SIGMAOTHER + MULTOTHER * dist);
            if ((angle-heading > -3.0) && (angle-heading < 0))
                neury[sensorR] += 1.0 / (SIGMAOTHER + MULTOTHER * dist);

            neury[9] += other.fight / (.2 + .1 * dist);  // You can hear the other's aggro

            if (dist < myworld.EATRADIUS)  // Sufficiently close to fight?
            {
                //neury[9] +=  myworld.FIGHTNOISE * other.fight; // You can "hear" other's aggro if it's close enough to hurt you
                other.energy -= myworld.FIGHTDAMAGE * (fight - other.fight);  // Note that the fighting is based on the previous step's activity... Some will already be updated, others not!
                if ((other.energy < 0) && (fight > other.fight) && (myworld.population.size() > myworld.POPSIZEMIN))
                {
                    iter.remove();  
                    energy += myworld.EATBONUS;
                }
                

                validneur = new int[myworld.NBNEUR]; for (int ii=0; ii < myworld.NBNEUR; ii++) validneur[ii] = 0;
                validneur[0]= 1; validneur[1]=1; validneur[2] = 1; 
            }
        }
        if (mindist > 9999)
            mindist = 0;
        energy -= myworld.DISTANCEENERGY * mindist  * mindist;  // Generally not used (myworld.DISTANCEENERGY = 0)
        // Energy cost grows with square of number of actually used neurons 
        int nbvalidneur=0; for (int ii=0; ii < myworld.NBNEUR; ii++) nbvalidneur += validneur[ii];
        energy -= myworld.NEURENERGY * nbvalidneur * nbvalidneur;
        // The speed/fight/damage is a multiple of the output of the fight/speed neuron, which is exponential-linear of its inputs
        // The cost of speed / fight is the square of this value
        energy -=  fight * fight * myworld.FIGHTENERGY;
        neury[8] = .1 * energy;
        
        for (int ii=0; ii < myworld.NBNEUR; ii++) {  if (validneur[ii] == 0) {neury[ii] = 0; neurx[ii] = 0; } }
        runNetwork();
        for (int ii=0; ii < myworld.NBNEUR; ii++) {  if (validneur[ii] == 0) {neury[ii] = 0; neurx[ii] = 0; } }
        
        for (int ii=0; ii < myworld.NBNEUR; ii++) {  
            assert ((validneur[ii] == 0)  || (validneur[ii] == 1));
        }
        
        // Determine motion based on neural network outputs:
        speed =  neury[0];  // Speed and fight are guaranteed to lie within [0,1]
        energy -= myworld.SPEEDENERGY * speed * speed;
        speed = myworld.AGENTSPEED * speed;
        rotation = myworld.AGENTANGULARSPEED * neury[1];
        heading += rotation;
        if (heading < 0) heading += 2 * Math.PI;
        if (heading > 2 * Math.PI) heading -= 2 * Math.PI;
        x += speed * Math.cos(heading);
        y -= speed * Math.sin(heading);
        if (y > myworld.WSIZE)
            y -= myworld.WSIZE;
        if (y < 0)
            y += myworld.WSIZE;
        if (x > myworld.WSIZE)
            x -= myworld.WSIZE;
        if (x < 0)
            x += myworld.WSIZE;
        energy -= myworld.ENERGYDECREMENT;
        energy *= myworld.ENERGYDECAY;
    }
    
    public void readAgent(String fname)
    {   
        try{
            System.out.println("Reading from file "+fname);
            BufferedReader in
                = new BufferedReader(new FileReader(fname));

            for (int row=0; row < myworld.NBNEUR; row++)
            {   
                String strs[] = in.readLine().split(" ");
                if (strs.length != myworld.NBNEUR)
                    throw new RuntimeException("Data file has wrong number of neurons! (strs.length is "+strs.length+", strs[0] is "+strs[0]+", row "+row+")");
                for (int col=0; col < myworld.NBNEUR; col++)
                    this.w[row][col] = Double.parseDouble(strs[col]);
            }
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't read agent from file "+fname); System.exit(0);            
        }
    }
    
    public void saveAgent(String fname)
    {   
        try{
            PrintWriter writer = new PrintWriter(fname);
            for (int row=0; row < myworld.NBNEUR; row++)
            {   
                for (int col=0; col < myworld.NBNEUR; col++)
                    writer.print(Double.toString(this.w[row][col])+" "); 
                writer.print("\n");
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't save agent to file "+fname); System.exit(0);            
        }

    }
    
    public void draw(Graphics G){
                G.setColor(new Color((float)(Math.tanh(fight)) , (float)0.0, (float)0.0) );
            G.fillOval((int)(x/2.0)-3, (int)(y/2.0)-3, 7, 7);
            G.fillOval((int)((x/2.0) + 6 * Math.cos(heading))-1, 
                       (int)((y/2.0) - 6 * Math.sin(heading))-1, // minus to preserve counterclockwise (trigonometric) heading when y grows downwards..
                        2,2);
    }

    public static void savePedigrees(World w, String fname)
    {
        try{
            PrintWriter writer = new PrintWriter(fname);
            for (Agent a: w.population)
            {   
                for (int col=0; col < a.pedigree.size(); col++)
                    writer.print(Long.toString(a.pedigree.get(col))+" "); 
                writer.print("\n");
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace(); System.out.println("Couldn't save pedigree to file "+fname); System.exit(0);            
        }

    }


}
