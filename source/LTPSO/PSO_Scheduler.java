package LTPSO;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Constants;
import utils.DatacenterCreator;
import utils.GenerateMatrices;

import java.text.DecimalFormat;
import java.util.*;

public class PSO_Scheduler {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static Datacenter[] datacenter;
    private static PSO PSOSchedularInstance;
    private static double mapping[];
    private static double[][] commMatrix;
    private static double[][] execMatrix;

    private static List<Vm> createVM(int userId, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(datacenter[i].getId(), userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

        //cloudlet parameters
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];
        long[]  length= {703436,598684,452458,890487,739165,1039481,67423,905202,361905,
                894703,628817,510410,1034139,841043,426611,647266,440829,939420,
                703648,784140,470769,735967,368867,422789,361224,714411,620431,
                780133,771498,416571,555264,598684,456121,391143,739165,884962,
                949522,667038,540160,559880,628817,591017,1034139,196999,275188,
                747699,525641,799482,703648,784140,476308,607045,1046088,422789,361224,714411,620431,373791,771498,877413,
                883667,894227,136224,144711,540436,859684,542458,390487,
                984703,268817,105410,2210139,804143,222611,476266,824409,429390,
                470836,841740,647079,573967,620431,539165,339481,37423,85202,461905,
                807133,74798,765411,333550,138451,910445,890487,637342,565745,900733,667038,694292,605271,628817,695416,847213,596066,570968,558013,434094,609329,703648,56076,446685,493707,368867,564616,434182,672599,495781,610636,412966,448419
        };
        int[] dcId={4,1,0,1,2,2,2,4,4,2,4,0,1,3,4,0,2,4,1,1,1,4,0,4,3,3,0,1,2,1,2,3,0,1,3,4,4,2,3,4,1,0,1,2,4,0,2,4,2,1,1,4,0,4,3,3,1,1,2,
                1,2,4,4,2,3,4,1,0,0,1,3,4,0,2,4,2,1,2,3,1,1,4,0,4,3,3,1,1,2,1,2,4,4,2,3,4,1,0,0,1,3,4,0,2,4,2,1,2,3,1,1,4,0,4,3,3,1,1,2,1};
        for (int i = 0; i < cloudlets; i++) {


            cloudlet[i] = new Cloudlet(idShift + i, length[i], pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            cloudlet[i].setVmId(dcId[i] + 2);
            list.add(cloudlet[i]);
        }


        return list;
    }
//    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
//        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
//
//        //cloudlet parameters
//        long fileSize = 300;
//        long outputSize = 300;
//        int pesNumber = 1;
//        UtilizationModel utilizationModel = new UtilizationModelFull();
//
//        Cloudlet[] cloudlet = new Cloudlet[cloudlets];
//
//        for (int i = 0; i < cloudlets; i++) {
//            int dcId = (int) (mapping[i]);
//            long length = (long) (1e3 * (commMatrix[i][dcId] + execMatrix[i][dcId]));
//            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
//            cloudlet[i].setUserId(userId);
//            list.add(cloudlet[i]);
//        }
//
//        return list;
//    }

    public static void main(String[] args) {
        Log.printLine("Starting PSO Scheduler...");

        new GenerateMatrices();
        commMatrix = GenerateMatrices.getCommMatrix();
        execMatrix = GenerateMatrices.getExecMatrix();
        PSOSchedularInstance = new PSO();
        mapping = PSOSchedularInstance.run();

        try {
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
            for (int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i);
            }

            //Third step: Create Broker
            PSODatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmList = createVM(brokerId, Constants.NO_OF_DATA_CENTERS);
            cloudletList = createCloudlet(brokerId, Constants.NO_OF_TASKS, 0);

            // mapping our dcIds to cloudsim dcIds
            HashSet<Integer> dcIds = new HashSet<>();
            HashMap<Integer, Integer> hm = new HashMap<>();
            for (Datacenter dc : datacenter) {
                if (!dcIds.contains(dc.getId()))
                    dcIds.add(dc.getId());
            }
            Iterator<Integer> it = dcIds.iterator();
            for (int i = 0; i < mapping.length; i++) {
                if (hm.containsKey((int) mapping[i])) continue;
                hm.put((int) mapping[i], it.next());
            }
            for (int i = 0; i < mapping.length; i++)
                mapping[i] = hm.containsKey((int) mapping[i]) ? hm.get((int) mapping[i]) : mapping[i];

            broker.submitVmList(vmList);
            broker.setMapping(mapping);
            broker.submitCloudletList(cloudletList);


            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine(PSO_Scheduler.class.getName() + " finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static PSODatacenterBroker createBroker(String name) throws Exception {
        return new PSODatacenterBroker(name);
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" +
                indent + "Data center ID" +
                indent + "VM ID" +
                indent + indent + "Time" +
                indent + "Start Time" +
                indent + "Finish Time");

        double mxFinishTime = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        dft.setMinimumIntegerDigits(2);
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + dft.format(cloudlet.getCloudletId()) + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + dft.format(cloudlet.getResourceId()) +
                        indent + indent + indent + dft.format(cloudlet.getVmId()) +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
            mxFinishTime = Math.max(mxFinishTime, cloudlet.getFinishTime());
        }
        Log.printLine(mxFinishTime);
        PSOSchedularInstance.printBestFitness();
    }
}