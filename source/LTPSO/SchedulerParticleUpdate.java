package LTPSO;

import net.sourceforge.jswarm_pso.Neighborhood;
import net.sourceforge.jswarm_pso.Particle;
import net.sourceforge.jswarm_pso.ParticleUpdate;
import net.sourceforge.jswarm_pso.Swarm;
import utils.Constants;

import java.lang.reflect.Array;
import java.util.Arrays;

public class SchedulerParticleUpdate extends ParticleUpdate {
    private static final double W = 0.445;
    private static final double C = 2;
    private double alpha =0.67;
    private static double varMin = 0.0001;

    private static double[] pPreBest = new double[Constants.NO_OF_TASKS];
    private static double[] gPreBest = new double[Constants.NO_OF_TASKS];
//    private boolean tag=true;
    SchedulerParticleUpdate(Particle particle) {
        super(particle);
    }

    @Override
    public void update(Swarm swarm, Particle particle) {
        double[] v = particle.getVelocity();
        double[] x = particle.getPosition();
        double[] pbest = particle.getBestPosition();
        double[] gbest = swarm.getBestPosition();
        double[] chx=createChx(0.57,4);
        for (int i = 0; i < Constants.NO_OF_TASKS; ++i) {
            v[i] = W * v[i] + C * chx[i] * (pbest[i] - x[i]) + C * chx[i] * (gbest[i] - x[i]);
            x[i] =(int)(x[i] + v[i]);
        }
        double xmax= Arrays.stream(x).max().getAsDouble();
        double xmin= Arrays.stream(x).min().getAsDouble();
//        System.out.printf("%f,%f\n",xmax,xmin);
        double[] delta=new double[Constants.NO_OF_TASKS];
        for (int i = 0; i < Constants.NO_OF_TASKS; ++i)
        {
//            double var=(Arrays.stream(pbest).max().getAsDouble()-Arrays.stream(gbest).max().getAsDouble())*(Arrays.stream(pbest).max().getAsDouble()-Arrays.stream(gbest).max().getAsDouble())+(Arrays.stream(pPreBest).max().getAsDouble()-Arrays.stream(gPreBest).max().getAsDouble())*(Arrays.stream(pPreBest).max().getAsDouble()-Arrays.stream(gPreBest).max().getAsDouble());
//            System.out.println(var);
            double var=(gbest[i]-pbest[i])*(gbest[i]-pbest[i])+(gPreBest[i]-pPreBest[i])*(gPreBest[i]-pPreBest[i]);
            if(var<varMin)
            {
                double[] thx=createThx(0.6,0.5);
//                for(int j=0;j<thx.length;j++)
//                {
//                    thx[j]=alpha*thx[j];
                    delta[i]=(gbest[i]-alpha*thx[i])/(1-alpha);
                    x[i]=xmin+delta[i]*(xmax-xmin);
//                    System.out.printf("%f,%f,%f\n",delta[j],tmp,x[j]);
                    pbest[i]=x[i];
                    particle.setBestPosition(pbest);
//                  swarm.evaluate();
            }
        }
        pPreBest=pbest;
        gPreBest=gbest;
    }
    private double[] createThx(double x,double fai)
    {
        double[] thx =new double[Constants.NO_OF_TASKS];
        thx[0]=x;
        for (int i=1;i<Constants.NO_OF_TASKS;++i)
        {
            if(0<thx[i-1]&&thx[i-1]<fai)
                thx[i]=thx[i-1]/fai;
            else
                thx[i]=(1-thx[i-1])/(1-fai);
        }
        return thx;
    }
    private double[] createChx(double x,double mu)
    {
        double chx[] = new double[1000];
        for(int i=0;i<Constants.NO_OF_TASKS;i++)
        {
            if(i==0)
                chx[i]=mu*x*(1-x);
            else
                chx[i]=mu*chx[i-1]*(1-chx[i-1]);
        }

        return chx;
    }
}