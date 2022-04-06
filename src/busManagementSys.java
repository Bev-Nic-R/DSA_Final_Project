import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.TST;
import edu.princeton.cs.algs4.DijkstraSP;

import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.lang.Integer.parseInt;

public class busManagementSys {
    private static String stopFile, stopTimeFile , transfersFile;

    static ArrayList<Integer> stops; // arraylist of integers used as corresponding matrix value for creation of
    static TST<String> TST; // empty ternary search tree
    static DijkstraSP SP;
    static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    public static final int ONE_HUNDRED = 100;
    public static EdgeWeightedDigraph digraph;
    public int indexOfMap = 0;


    busManagementSys(String stopFile, String stopTimeFile, String transfersFile) {
        this.stopTimeFile = stopTimeFile;
        this.stopFile = stopFile;
        this.transfersFile = transfersFile;
    }

    public static void main(String[] args){
        busManagementSys bus = new busManagementSys("stops.txt", "stop_times.txt", "transfers.txt");
        addStopsToArraylist(stopFile);
        addEdgesFromStopTimes(stopTimeFile);
        addEdgesFromTransfersFile(transfersFile);
        findShortestPath(7, 2);
    }

    public static void findShortestPath(int sourceVertex, int destinationVertex){
        System.out.println("Finding shortest path");

        int sourceVIndex = Collections.binarySearch(stops, sourceVertex);
        int destVIndex = Collections.binarySearch(stops, destinationVertex);

        SP = new DijkstraSP(digraph, sourceVIndex);

        if(SP.hasPathTo(destVIndex)) {
            double lengthOfPath = SP.distTo(destVIndex);
            System.out.println("Cost: " + lengthOfPath);

            Iterable<DirectedEdge> stopList = SP.pathTo(destVIndex);
            System.out.println("List of stops in this path (and associated costs):");
            for(DirectedEdge stop: stopList) {
                System.out.println("Stop ID: " + stop.to() + "\t Cost (from previous): " + stop.weight());
            }
        }
    }


    public static void addStopsToArraylist(String filename) {
        try {
            if (filename == null) {
                return;
            }
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            myReader.nextLine(); // skip the first line
            stops = new ArrayList<>();
            while (myReader.hasNextLine()) {
                String[] line = myReader.nextLine().split(","); // split line at comma
                stops.add(parseInt(line[0])); // add each bus stop ID to the bus stops arraylist
            }
            Collections.sort(stops); // sort Bus Stop arraylist from small->big
            digraph = new EdgeWeightedDigraph(stops.size()); // create graph with number of vertices from bus stops arraylist
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File was not found.");
            e.printStackTrace();
        }
    }


    public static void addEdgesFromTransfersFile(String filename){
        if(filename == null) {
            return;
        }
        File file = new File(filename);
        try {
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); // skip the first line as it has no data
            int weight = 0;
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineData = line.split(",");
                if (parseInt(lineData[2]) == 0){
                    weight = 2;
                }
                else{
                    weight = (parseInt(lineData[3]))/ ONE_HUNDRED;
                }
                // get the two arraylist ids of the bus stops and then add an edge between them to the graph
                int index1 = Collections.binarySearch(stops, parseInt(lineData[0]));
                int index2 =  Collections.binarySearch(stops, parseInt(lineData[1]));
                DirectedEdge newEdge = new DirectedEdge(index1, index2, weight); // create new edge with
                digraph.addEdge(newEdge);
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: File was not found.");
            e.printStackTrace();
        }
        return;
    }

    public static void addEdgesFromStopTimes(String filename){
        if(filename == null) {
            return;
        }
        File file = new File(filename);
        try {
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); // skip the first line as it has no data
            int tripID;
            while(scanner.hasNextLine()) {
                String line1 = scanner.nextLine();
                String[] lineData1 = line1.split(",");
                tripID = parseInt(lineData1[0]);
                int vertex1 = Collections.binarySearch(stops, parseInt(lineData1[3]));  // get the stop ID from bus stop arraylist
                int vertex2;
                String line2;
                String[] lineData2;
                //int seq = 0;
                while (parseInt(lineData1[0]) == tripID){
                    // while the trip ID is the same, add edges
                    if(scanner.hasNextLine()) {
                        line2 = scanner.nextLine();
                        lineData2 = line2.split(",");
                        vertex2 = Collections.binarySearch(stops, parseInt(lineData2[3])); // get the stop ID from bus stop arraylist
                        // System.out.println("Adding an edge from " + stops.get(vertex1) + " to " + stops.get(vertex2));
                        DirectedEdge newEdge = new DirectedEdge(vertex1, vertex2, 1); // create new edge with
                        digraph.addEdge(newEdge);
                        line1 = line2;
                        lineData1 = line1.split(",");
                        vertex1 = Collections.binarySearch(stops, parseInt(lineData1[3]));
                    }
                    else{
                        tripID = -1;
                    }
                }
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: File was not found.");
            e.printStackTrace();
        }
        return;
    }

}
