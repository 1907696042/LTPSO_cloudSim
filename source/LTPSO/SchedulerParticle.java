package LTPSO;

import net.sourceforge.jswarm_pso.Particle;
import utils.Constants;

import java.util.Random;

public class SchedulerParticle extends Particle {
    SchedulerParticle() {
        super(Constants.NO_OF_TASKS);
        double[] position = new double[Constants.NO_OF_TASKS];
        double[] velocity = new double[Constants.NO_OF_TASKS];
        double chx[] = new double[1000];
        int n=Constants.NO_OF_TASKS;
        chx=createChx(0.55,4);
        double pmax = 0,pmin=1000,vmax=0,vmin=1000;
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            Random randObj = new Random();
            position[i] = randObj.nextInt(Constants.NO_OF_DATA_CENTERS);
            if(position[i]>pmax)
                pmax=position[i];
            if(position[i]<pmin)
                pmin=position[i];
            velocity[i] = Math.random();
            if(velocity[i]>vmax)
                vmax=velocity[i];
            if(velocity[i]<vmin)
                vmin=velocity[i];

        }
        for (int i = 0; i < n; i++) {
            position[i]=pmax-(pmax-pmin)*chx[i];
            velocity[i]=vmax-(vmax-vmin)*chx[i];
        }
        setPosition(position);
        setVelocity(velocity);

    }
    public double[] createChx(double x,double mu)
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

    @Override
    public String toString() {
        String output = "";
        for (int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
            String tasks = "";
            int no_of_tasks = 0;
            for (int j = 0; j < Constants.NO_OF_TASKS; j++) {
                if (i == (int) getPosition()[j]) {
                    tasks += (tasks.isEmpty() ? "" : " ") + j;
                    ++no_of_tasks;
                }
            }
            if (tasks.isEmpty()) output += "There is no tasks associated to Data Center " + i + "\n";
            else
                output += "There are " + no_of_tasks + " tasks associated to Data Center " + i + " and they are " + tasks + "\n";
        }
        return output;
    }
}
