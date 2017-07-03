// This class controls the agents.
// NOTE: Much of the spatial logic is contained in class Item, which this clas inherits from.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.awt.*;  
import java.util.Random;
import java.util.Iterator;
import java.io.IOException;

public class Agent extends Item implements Comparable<Agent>{
    double energy;
    Random R;
    World myworld;
    double rotation;
    double speed, fight;
    boolean isUpdated;
    double heading;
    int NBNEUR, WSIZE;
    long age;
    double MUTATIONSIZE, MAXW, INITENERGY, FOODENERGY, ENERGYDECREMENT, SPEEDENERGY, FIGHTNOISE, FIGHTDAMAGE, FIGHTENERGY, EATBONUS;
    double[][] w;
    double[] neury, neurx;
    double dtdivtau , meannormw;
    
    Agent(World ww)
    {
        super(ww);
        myworld = ww;
        dtdivtau = 1.0 / myw.TAU;
        R = myworld.R;
        energy = 0; age=0;
        NBNEUR = myworld.NBNEUR; 
        WSIZE = myworld.WSIZE;
        MUTATIONSIZE = myworld.MUTATIONSIZE; MAXW = myworld.MAXW;
        FOODENERGY = myworld.FOODENERGY;
        INITENERGY = myworld.INITENERGY;
        ENERGYDECREMENT = myworld.ENERGYDECREMENT;
        SPEEDENERGY = myworld.SPEEDENERGY;
        FIGHTNOISE = myworld.FIGHTNOISE; FIGHTENERGY = myworld.FIGHTENERGY; FIGHTDAMAGE = myworld.FIGHTDAMAGE; EATBONUS = myworld.EATBONUS;
        neurx = new double[NBNEUR];
        neury = new double[NBNEUR];
        for (int ii=0; ii < NBNEUR; ii++){ 
            neury[ii] = 0.0; neurx[ii] = 0.0; 
        }
        w = new double[NBNEUR][NBNEUR];
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
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++){
                w[ii][jj] = (2.0 * R.nextDouble() - 1.0) ;
                //w[ii][jj] = (2.0 * R.nextDouble() - 1.0) / Math.sqrt((double)NBNEUR);
                meannormw += Math.abs(w[ii][jj]);
            }
        meannormw /= (NBNEUR * NBNEUR);
    }
    public void resetNeurons() { 
        for (int n=0; n<NBNEUR; n++){
            neurx[n] = 0;
            neury[n] = 0;
        }
    }
    public void resetEnergy() { energy = INITENERGY; } 
    public void increaseEnergy() { energy ++; } 
    public void decreaseEnergy() { energy --; } 
    public void copyFrom(Agent A){
        energy = A.energy;
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++)
                w[ii][jj] = A.w[ii][jj];
        meannormw = A.meannormw;
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
        for (int row=0; row < NBNEUR; row++){
            tempx = 0;
            for (int col=0; col < NBNEUR; col++)
                if ((row > 2) || ( col > 10)) // The actuators can't receive direct sensor input! But note this prevent hard biases into the actuators too...
                //if ((row > 2) || (col > 9)) // The actuators can't receive direct sensor or actuator input!
                    tempx += w[row][col] * neury[col];
            neurx[row] += dtdivtau * (tempx - neurx[row]);
        }
        for (int row=0; row < NBNEUR; row++)
            if ((row ==0) || (row == 2))
                neury[row] = explin(neurx[row]);
            else
                neury[row] = Math.tanh(neurx[row]);
        //neury[0] = (1.0 + neury[0]) / 2.0;  // Speed is positive
        //neury[2] = (1.0 + neury[2]) / 2.0;  // So is fight
    }
    public void mutate()
    {
        meannormw = 0;
        for (int ii=0; ii < NBNEUR; ii++)
            for (int jj=0; jj < NBNEUR; jj++)
            {
                ///w[ii][jj] += MUTATIONSIZE * R.nextGaussian();
                //w[ii][jj] *= .99;
                if (R.nextDouble() < myworld.PROBAMUT){
                    double cauchy = Math.tan((R.nextDouble() - .5) * Math.PI);
                    w[ii][jj] += MUTATIONSIZE * cauchy;
                    w[ii][jj] *= .99;
                //    w[ii][jj] += myworld.MUTATIONSIZE * R.nextGaussian();
                //    w[ii][jj] *= .99;
                }
                if (w[ii][jj] > MAXW)
                    w[ii][jj] = MAXW;
                if (w[ii][jj] < -MAXW)
                    w[ii][jj] = -MAXW;
                meannormw += w[ii][jj];
            }
        meannormw /= (NBNEUR * NBNEUR);
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
            if (dist < myworld.EATRADIUS)  // Eaten!
            {
                energy += FOODENERGY;
                    //neury[8] = 10.0;  // Would be interesting to know if this improves performance....
                    iter.remove();
            }
            else
            {
                angle = getAngleFrom(fooditem);
                if ((angle-heading < 3.0) && (angle-heading > 0))
                    neury[sensorL] += 1.0 / (1.0 + .1 * dist);
                if ((angle-heading > -3.0) && (angle-heading < 0))
                    neury[sensorR] += 1.0 / (1.0 + .1 * dist);
            }

        }
        for (Iterator<Agent> iter = myworld.population.listIterator() ; iter.hasNext(); )
        {
            Agent other = iter.next();
            dist = getDistanceFrom(other); // Note that this computes distance twice for each pair... inelegant.
            sensorL = 6; sensorR = 7;
            angle = getAngleFrom(other);
            if ((angle-heading < 3.0) && (angle-heading > 0))
                neury[sensorL] += 1.0 / (1.0 + .1 * dist);
            if ((angle-heading > -3.0) && (angle-heading < 0))
                neury[sensorR] += 1.0 / (1.0 + .1 * dist);

            neury[9] += other.fight / (1.0 + .1 * dist);  // You can hear the other's aggro

            if ((angle-heading > -3.0) && (angle-heading < 0))
            if (dist < myworld.EATRADIUS)  // Sufficiently close to fight?
            {
                //neury[9] +=  FIGHTNOISE * other.fight; // You can "hear" other's aggro if it's close enough to hurt you
                other.energy -= FIGHTDAMAGE * (fight - other.fight);  // Note that the fighting is based on the previous step's activity... Some will already be updated, others not!
                if ((other.energy < 0) && (fight > other.fight) && (myworld.population.size() > myworld.POPSIZEMIN))
                {
                    iter.remove();  
                    energy += EATBONUS;
                }

            }
        }
        // The speed/fight/damage is a multiple of the output of the fight/speed neuron, which is exponential-linear of its inputs
        // The cost of speed / fight is the square of this value
        energy -=  fight * fight * FIGHTENERGY;
        neury[8] = .1 * energy;
        runNetwork();
        // Determine motion based on neural network outputs:
        speed =  neury[0];  // Speed and fight are guaranteed to lie within [0,1]
        energy -= SPEEDENERGY * speed * speed;
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
        energy -= ENERGYDECREMENT;
        energy *= .995;
    }
    
    public void readAgent(String fname)
    {   
        try{
            System.out.println("Reading from file "+fname);
            BufferedReader in
                = new BufferedReader(new FileReader(fname));

            for (int row=0; row < NBNEUR; row++)
            {   
                String strs[] = in.readLine().split(" ");
                if (strs.length != NBNEUR)
                    throw new RuntimeException("Data file has wrong number of neurons! (strs.length is "+strs.length+", strs[0] is "+strs[0]+", row "+row+")");
                for (int col=0; col < NBNEUR; col++)
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
            for (int row=0; row < NBNEUR; row++)
            {   
                for (int col=0; col < NBNEUR; col++)
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
            G.fillOval((int)(x)-3, (int)y-3, 7, 7);
            G.fillOval((int)(x + 6 * Math.cos(heading))-1, 
                       (int)(y - 6 * Math.sin(heading))-1, // minus to preserve counterclockwise (trigonometric) heading when y grows downwards..
                        2,2);
    }


}
