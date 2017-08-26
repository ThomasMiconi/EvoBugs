import java.util.ArrayList;
public class Utils
{

    public static double[] getSensorParams(ArrayList<Agent> population)
    {
        // Get the mean and variance of the sigma and multiplier parameters for the divisors in the sensors
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
        
        double meansigmaother=0, meanmultother=0, meansigmaothersq=0, meanmultothersq=0;
        for (Agent a: population)
        {
            meansigmaother += a.SIGMAOTHER; meanmultother += a.MULTOTHER;
        }
        meansigmaother /= (double)population.size();
        meanmultother /= (double)population.size();
        for (Agent a: population)
        {
            meansigmaothersq += (a.SIGMAOTHER - meansigmaother) * (a.SIGMAOTHER - meansigmaother) ; 
            meanmultothersq += (a.MULTOTHER - meanmultother) * (a.MULTOTHER - meanmultother) ;
        }
        meansigmaothersq /= (double)population.size();
        meanmultothersq /= (double)population.size();

        double[] result = new double[8];
        result[0] = meanmultfood;
        result[1] = meanmultfoodsq;
        result[2] = meansigmafood;
        result[3] = meansigmafoodsq;
        result[4] = meanmultother;
        result[5] = meanmultothersq;
        result[6] = meansigmaother;
        result[7] = meansigmaothersq;
        return result;
    }
}
