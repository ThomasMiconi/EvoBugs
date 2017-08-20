// java World VISUAL 1 SEED 5 FIGHTENERGY .0135 EATBONUS 400.0 FIGHTDAMAGE 3.0 SPEEDENERGY .003 > tmp7n.txt  : all sensor-other (with a few fluctuations of sensor-other)
// java World VISUAL 1 SEED 5 FIGHTENERGY .013 EATBONUS 1200.0 FIGHTDAMAGE 3.0 SPEEDENERGY .003 > tmp7m.txt  : all sensor-food ! 
// fightenergy++ favors sensor-other. eatbonus ++ has very little impac!


import java.awt.*;  
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.event.*;  
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

// The World class is the overall controller.
// While it defines the overall algorithm, much of the actual logic occurs in
// classes Agent, Item, Agent and FoodItem.
public class World extends Frame {
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
           DISTANCEENERGY = 1e-3,
           FIGHTNOISE = 1.0,
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
            if (args[numarg].equals( "FILENAME")) { VISUAL = 1 ; FILENAME  = args[numarg+1]; delay=40; }
            if (args[numarg].equals( "VISUAL")) { VISUAL  = Integer.parseInt(args[numarg+1]); if ((VISUAL !=0) && (VISUAL != 1)) throw new RuntimeException("VISUAL must be 0 or 1!");}
            if (args[numarg].equals( "SEED")) SEED = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "WSIZE")) WSIZE = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "NBNEUR")) NBNEUR = Integer.parseInt(args[numarg+1]);
            if (args[numarg].equals( "DISTANCEENERGY")) DISTANCEENERGY  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "TAU")) TAU  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "FOODENERGY")) FOODENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "EATBONUS")) EATBONUS  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "FIGHTDAMAGE")) FIGHTDAMAGE  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "FIGHTENERGY")) FIGHTENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "SPEEDENERGY")) SPEEDENERGY= Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MAXW")) MAXW  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "PROBAMUT")) PROBAMUT  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "MUTATIONSIZE")) MUTATIONSIZE  = Double.parseDouble(args[numarg+1]);
            if (args[numarg].equals( "ENERGYDECAY")) ENERGYDECAY = Double.parseDouble(args[numarg+1]);
            numarg += 2;
        }
        if (VISUAL == 1) delay = 40;
        // suffix for the output files (results and bestagent).
        FILESUFFIX = "_nodirectio_noglobalmut_cauchy_NoSelfEnergySensor_WSIZE"+WSIZE+"_MUTATIONSIZE"+MUTATIONSIZE+"_MAXW"+MAXW+"_FIGHTENERGY"+FIGHTENERGY+"_FIGHTDAMAGE"+FIGHTDAMAGE+"_EATBONUS" + 
                                EATBONUS+"_SPEEDENERGY"+SPEEDENERGY+"_ENERGYDECAY"+ENERGYDECAY+"_EATBONUS"+EATBONUS+"_DISTANCEENERGY"+DISTANCEENERGY+"_SEED"+SEED;
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
        {
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
                System.out.println(population.size()+" "+oldestage+" "+food.size()+" "+population.get(0).fight);
                
                double meansigmafood=0, meanmultfood=0, meansigmafoodsq=0, meanmultfoodsq=0;
                for (Agent a: population)
                {
                    meansigmafood += a.SIGMAFOOD; meanmultfood += a.MULTFOOD;
                }
                meansigmafood /= (double)population.size();
                meanmultfood /= (double)population.size();
                for (Agent a: population)
                {
                    meansigmafoodsq += (a.SIGMAFOOD - meansigmafood) * (a.SIGMAFOOD - meansigmafood) ; 
                    meanmultfoodsq += (a.MULTFOOD - meanmultfood) * (a.MULTFOOD - meanmultfood) ;
                }
                meansigmafoodsq /= (double)population.size();
                meanmultfoodsq /= (double)population.size();

                double meansigma=0, meanmult=0, meansigmasq=0, meanmultsq=0;
                for (Agent a: population)
                {
                    meansigma += a.SIGMAOTHER; meanmult += a.MULTOTHER;
                }
                meansigma /= (double)population.size();
                meanmult /= (double)population.size();
                for (Agent a: population)
                {
                    meansigmasq += (a.SIGMAOTHER - meansigma) * (a.SIGMAOTHER - meansigma) ; 
                    meanmultsq += (a.MULTOTHER - meanmult) * (a.MULTOTHER - meanmult) ;
                }
                meansigmasq /= (double)population.size();
                meanmultsq /= (double)population.size();

                System.out.println("Sigmaother mean, std: "+meansigma+", "+meansigmasq+". Multother mean, std: "+meanmult+", "+meanmultsq);
                System.out.println("Sigmafood mean, std: "+meansigmafood+", "+meansigmafoodsq+". Multfood mean, std: "+meanmultfood+", "+meanmultfoodsq);
                oldestagent.saveAgent("oldestagent_"+FILESUFFIX+".txt");
                Agent.savePedigrees(this, "pedigrees_"+FILESUFFIX+".txt");
            }
            //System.out.println(population.get(1).speed+" "+population.get(1).fight);
            numstep++;
        }


    }
    /*
       public void runOld()
       {
       numgen = 0; bestscoreever = 0;
       for (int i=0; i<POPSIZE; i++) population.get(i).randomizeNet();
    //while (numgen < 10)
    while (true)
    {
    // Note that we re-evaluate the champion agents at each generation, even though we already know their score! OTOH, score evaluation is quite noisy, so it's probably worth it.
    for (int numagent=0; numagent < POPSIZE; numagent++)
    {
    // Each agent is evaluated in turn by putting it into the 'active' agent.
    agent.copyFrom(population.get(numagent));
    // If we are currently visualizing an agent saved in a 'bestagent' file (provided as command line argument), we only ever show this one.
    if (FILENAME.length() > 0) 
    agent.readAgent(FILENAME);
    agent.initialize();
    // Which is food, which is poison? Randomly set.
    POISONFIRSTHALF = R.nextInt(2);

    // Evaluation :
    for (int numstep=0; numstep < NBSTEPSPEREVAL; numstep++)
    {
    // Food and poison switch at mid-trial !
    if (numstep == (int)(NBSTEPSPEREVAL / 2))
    POISONFIRSTHALF = 1 - POISONFIRSTHALF;
    for (int n=0; n < food.length; n++)
    food[n].update();
    agent.update(); // Takes care of sensors, network update, score update, motion, etc.

    // If using graphics, 
    // we need a delay between refreshes if we want to see what's going on...
    // But we can set it to 0 (with the buttons) if we just want
    // the algorithm to proceed fast.
    try{ Thread.sleep(delay); }
    catch ( InterruptedException e )   {
    System.out.println ( "Exception: " + e.getMessage() );
    }        
    if (VISUAL > 0){
    mf.cnv.repaint();
    mf.scorelabel.setText("Score: "+agent.getScore());
    }
    }
    population.get(numagent).copyFrom(agent); // Mostly to get back the total score.
    if (FILENAME.length() > 0) System.out.println(agent.getScore());
    }
    Collections.sort(population); // This will sort population by ascending order of the scores, because Agent implements Comparable.
    Collections.reverse(population); // We want descending order.
    bestscore = population.get(0).getScore();
    //System.out.println("Gen "+numgen+": "+bestscore+" "+population.get(1).getScore());
    System.out.println(bestscore);
    if (FILENAME.length() == 0){
    outputfilewriter.println(bestscore); outputfilewriter.flush();
    population.get(0).saveAgent("bestagent"+FILESUFFIX+".txt");
    if (population.get(0).getScore() > bestscoreever)
    {
    bestscoreever = population.get(0).getScore();
    population.get(0).saveAgent("besteveragent"+FILESUFFIX+".txt");
    }
    }

    for (int n=NBBEST; n<POPSIZE; n++)
    {
    population.get(n).copyFrom(population.get(R.nextInt(NBBEST)));
    population.get(n).mutate();
    }
    numgen ++;
    }
    //System.exit(0);
       }
       */
}  
