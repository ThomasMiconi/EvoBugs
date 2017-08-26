// java World VISUAL 1 SEED 5 FIGHTENERGY .0135 EATBONUS 400.0 FIGHTDAMAGE 3.0 SPEEDENERGY .003 > tmp7n.txt  : all sensor-other (with a few fluctuations of sensor-other)
// java World VISUAL 1 SEED 5 FIGHTENERGY .013 EATBONUS 1200.0 FIGHTDAMAGE 3.0 SPEEDENERGY .003 > tmp7m.txt  : all sensor-food ! 
// fightenergy++ favors sensor-other. eatbonus ++ has very little impac!


import java.awt.*;  
import java.util.Iterator;
import java.io.*;
import java.awt.event.*;  
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

// The World class is the overall controller.
// While it defines the overall algorithm, much of the actual logic occurs in
// classes Agent, Item, Agent and FoodItem.
public class World {
    // First, various definitions....
    String FILESUFFIX;
    String FILENAME = "";
    Random R;
    PrintWriter outputfilewriter;
    MyFrame mf; // MyFrame defines the graphical View/Controller.
    int delay=0, 
        nbagents=0,
        WSIZE = 2000,  
        POPSIZEMIN = 5,
        SEED = 3, // Random seed
        MINIMUMREPRODELAY = 1000,
        NBNEUR = 25; // Number of neurons
    double FOODSPEED = .5,  // The speed of the food/poison items. NOTE: it is possible that having higher food energy, with fewer food item, might favor 
           FOODENERGY = .3,
           INITENERGY = 1.0,
           SPEEDENERGY = .01,
           FIGHTDAMAGE = 1.0,
           MEANADDEDFOODPERSTEP = 4.0,
           DISTANCEENERGY = 0.0, // 3e-7
           FIGHTNOISE = 1.0,
           NEURENERGY = 1e-5,
           FIGHTENERGY = .001, //1.0,
           EATBONUS = 400.0, //100.0, 
           PROBAREPRO = 1.0 / 1000.0,
           ENERGYDECAY = .995,
           ENERGYDECREMENT = 1.0 / 500.0, 
           AGENTSPEED = 2.0,  // MAximum agent speed
           AGENTANGULARSPEED = .15,  // Maximum agent angular speed
           EATRADIUS = 10.0,  // How close must we be to be deemed 'eaten'?
           PROBAMUT = .05,      // Probability of mutation for each gene
           MUTATIONSIZE= .3,   // Size parameter of the Cauchy distribution used for the mutations
           TAU = 5.0,       // Time constant of the recurrent neural network
           MAXW = 10.0;     // Maximum weight 
    ArrayList<FoodBit>  food;
    int VISUAL = 0;  // Using graphics or not?
    Agent agent, bestagent;  // 'agent' is the agent being currently evaluated. 
    ArrayList<Agent>  population;  // The list of agents on which the genetic algorithm is performed
    int bestscore , bestscoreever;
    int numgen;
    protected Thread thrd;
    World(String args[]){ 
        // Reading the command line arguments...
        int numarg = 0;
        if (args.length % 2 != 0) { throw new RuntimeException("Each argument must be provided with its value"); }
        while (numarg < args.length) {
            if (args[numarg].equals( "FILENAME")) { VISUAL = 1 ; FILENAME  = args[numarg+1]; delay=0; }
            if (args[numarg].equals( "VISUAL")) { VISUAL  = Integer.parseInt(args[numarg+1]); if ((VISUAL !=0) && (VISUAL != 1)) throw new RuntimeException("VISUAL must be 0 or 1!");}
            if (args[numarg].equals( "SEED")) SEED = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "WSIZE")) WSIZE = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBNEUR")) NBNEUR = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "DISTANCEENERGY")) DISTANCEENERGY  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "TAU")) TAU  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "FOODENERGY")) FOODENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "EATBONUS")) EATBONUS  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "FIGHTDAMAGE")) FIGHTDAMAGE  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MEANADDEDFOODPERSTEP")) MEANADDEDFOODPERSTEP = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "FIGHTENERGY")) FIGHTENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "SPEEDENERGY")) SPEEDENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "NEURENERGY")) NEURENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MAXW")) MAXW  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "PROBAMUT")) PROBAMUT  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MUTATIONSIZE")) MUTATIONSIZE  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "ENERGYDECAY")) ENERGYDECAY = Double.parseDouble(args[numarg+1]);
            numarg += 2;
        }
        if (VISUAL == 1) delay = 0;
        // suffix for the output files (results and bestagent).
        FILESUFFIX = "_nodirectio_noglobalmut_cauchy_validneur_"+WSIZE+"_MUTSIZE"+MUTATIONSIZE+"_MAXW"+MAXW+"_FIGHTENERGY"+FIGHTENERGY+"_FIGHTDAMAGE"+FIGHTDAMAGE+"_EATBONUS" + 
                                EATBONUS+"_SPEEDENERGY"+SPEEDENERGY+"_ENERGYDECAY"+ENERGYDECAY+"_EATBONUS"+EATBONUS+"_DISTANCEENERGY"+DISTANCEENERGY+"_FOODENERGY"+FOODENERGY+"_MAF"+MEANADDEDFOODPERSTEP+"_NEURENERGY"+NEURENERGY+"_SEED"+SEED;
        if (VISUAL == 0) {
            try { outputfilewriter = new PrintWriter("results"+FILESUFFIX+".txt"); } catch(IOException e) {}
        }
        // Initializations and graphics setup...
        R = new Random(SEED);
        population = new ArrayList<Agent>();
        food = new ArrayList<FoodBit>();
        if (VISUAL == 1)
            mf = new MyFrame(this);
    }         

    public static void main(String[] args) {  
        World tf = new World(args);  
        tf.run();
    }  


    public void savePop()
    {
        String FNAME = "pop_"+FILESUFFIX+".ser";
        //ArrayList<Agent>  popfromfile; 
        try {
            FileOutputStream fileOut =
                new FileOutputStream(FNAME);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(population);
            out.close();
            fileOut.close();
            System.out.printf("Serialized population data is saved in "+FNAME);
        }catch(IOException i) {
            i.printStackTrace();
        }
    }
    public void readPop()
    {
        String FNAME = "pop_"+FILESUFFIX+".ser";
        ArrayList<Agent>  popfromfile = null; 
        try {
            FileInputStream fileIn = new FileInputStream(FNAME);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            popfromfile = (ArrayList<Agent>) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i) {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c) {
            System.out.println("Population / Agent class not found");
            c.printStackTrace();
            return;
        }
        population = new ArrayList<Agent>();
        for (int ii=0; ii < popfromfile.size(); ii++){
            Agent a1 = popfromfile.get(ii);
            Agent a2 = new Agent(this, ii);
            a2.copyFrom(a1);
            a2.num= a1.num;
            population.add(a2);
        }

    }

    public void run()
    {
        int FOODSIZEINIT = 20;
        long numstep = 0;
        for (int nn=0; nn < POPSIZEMIN; nn++)
            population.add(new Agent(this, nbagents++));
        for (int nn=0; nn < FOODSIZEINIT; nn++)
            food.add(new FoodBit(this));
        LinkedList<Agent> children = new LinkedList<Agent>();
        while (true)
        //for (int ttt=0; ttt < 10000; ttt++)
        {
            //System.out.println(ttt);
            // Crude approximation of Poisson distribution
            for (int nn=0; nn < 100; nn++)
                if (R.nextDouble() < MEANADDEDFOODPERSTEP / 100.0)
                    food.add(new FoodBit(this));
            for (FoodBit f: food)
                f.update();
            /*for (Iterator<Agent> iter = population.listIterator(); iter.hasNext(); ) // Iterator allows us to remove elements from the list within the loop... But not within update() !
            {
                Agent a = iter.next();
                if ((a.age > MINIMUMREPRODELAY) && (R.nextDouble() < PROBAREPRO)){
                    Agent child  = new Agent(this);
                    child.copyFrom(a);
                    if (R.nextDouble() < .9)
                        child.mutate();
                    child.initialize();
                    children.add(child);
                }
                a.update();
                if ((a.getEnergy() < 0) && (population.size() > POPSIZEMIN))
                    iter.remove();
            }*/

            // The difficulty here is that update() can remove agents from the
            // population. This would confuse both a for loop and an iterator.
            // So we need to do the looping ourselves.
            // NOTE: maybe a better way would be to just determine energy transfers in Agent.java's update(), and do all the actual removals here?...
            for (Agent a: population)
                a.isUpdated = false;
            while (true){
                // Find un-updated agent
                Agent a = null;
                for (Agent a2:population)
                    if (a2.isUpdated == false)
                        a = a2;
                if (a == null) break;
                // Update!
                if ((a.age > MINIMUMREPRODELAY) && (R.nextDouble() < PROBAREPRO)){
                    // Reproduction!
                    Agent child  = new Agent(this, nbagents++);
                    child.copyFrom(a);
                    child.pedigree.add(new Long(a.num));
                    if (R.nextDouble() < .75)
                        child.mutate();
                    child.initialize();
                    children.add(child);
                }
                a.update();
                a.isUpdated = true;
            }
            // Removing agents with energy <0 (that were not already removed due to fighting by update())
            for (Iterator<Agent> iter = population.listIterator(); iter.hasNext(); ) 
            {
                Agent a = iter.next();
                if ((a.getEnergy() < 0) && (population.size() > POPSIZEMIN))
                    iter.remove();
            }
            while (children.size() > 0)
                population.add(children.pop());



            if ((VISUAL > 0) && (delay > 0)){
                mf.cnv.repaint();
            }
            try{ Thread.sleep(delay); }
            catch ( InterruptedException e )   {
                System.out.println ( "Exception: " + e.getMessage() );
            }        
            long oldestage=0; Agent oldestagent=population.get(0); 
            for (Agent a: population) { if (a.age > oldestage) { oldestage = a.age; oldestagent = a; } }
            if (numstep % 10000 == 0){

                savePop();

                System.out.println(population.size()+" "+oldestage+" "+food.size()+" "+population.get(0).fight);
                double[] sensorparams = Utils.getSensorParams(population);
                System.out.println("Sigmaother mean, var: "+sensorparams[6]+", "+sensorparams[7]+". Multother mean, var: "+sensorparams[4]+", "+sensorparams[5]);
                System.out.println("Sigmafood mean, var: "+sensorparams[2]+", "+sensorparams[3]+". Multfood mean, var: "+sensorparams[0]+", "+sensorparams[1]);
                //oldestagent.saveAgent("oldestagent_"+FILESUFFIX+".txt");
                Agent.savePedigrees(this, "pedigrees_"+FILESUFFIX+".txt");
            }
            //System.out.println(population.get(1).speed+" "+population.get(1).fight);
            numstep++;
        }
        //System.exit(0);


    }
}  
