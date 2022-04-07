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
import static java.lang.Integer.parseInt;

public class busManagementSys {

    static ArrayList<Integer> stops; // arraylist of integers used as corresponding matrix value for creation of
    static TST<String> TST; // empty ternary search tree
    static DijkstraSP SP;
    static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    public static final String[] STREET_PREFIXES = new String[] {"FLAGSTOP", "WB", "NB", "SB", "EB"};


    public static final int ONE_HUNDRED = 100;
    public static EdgeWeightedDigraph digraph;


    public static void main(String[] args){
        addStopsToArraylist("stops.txt");
        addEdgesFromTransfersFile("transfers.txt");
        addEdgesFromStopTimes("stop_times.txt");
        findShortestPath(7, 2);

        String test = "FLAGSTOP NB MILLER RD AT DEEP BAY";
        System.out.println(reformatStreetName(test));
        searchBusStop("Has");
    }

    public static void searchBusStop(String prefix){
        int counter = 0;
        for (String s : TST.keysWithPrefix(prefix.toUpperCase())) {
            counter++; // count used to check if bus stops with this name were found
            System.out.println(s); // print out each bus stop name with inputted prefix
        }
        if (counter == 0) {
            System.out.println("There are no existing stops with this prefix.");
        }
    }

    public static void findShortestPath(int sourceVertex, int destinationVertex){

        int sourceVIndex = Collections.binarySearch(stops, sourceVertex);
        int destVIndex = Collections.binarySearch(stops, destinationVertex);

        System.out.println("Finding shortest path from bus stop " + sourceVertex + " to bus stop " + destinationVertex);

        SP = new DijkstraSP(digraph, sourceVIndex);

        if(SP.hasPathTo(destVIndex)) {
            double lengthOfPath = SP.distTo(destVIndex);
            System.out.println("Cost: " + lengthOfPath);

            Iterable<DirectedEdge> stopList = SP.pathTo(destVIndex);
            System.out.println("List of stops in this path (and associated costs):");
            for(DirectedEdge stop: SP.pathTo(destVIndex)) {
                System.out.println("Stop ID: " + stops.get(stop.to()) + "\t Cost (from last stop ): " + stop.weight());
            }
        }else{
            System.out.println("No path exists between these stops. ");
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
            TST = new TST<>();
            String stop_id;
            String stop_code;
            String reformattedStop;
            String reformattedLine;
            int index = 0;
            while (myReader.hasNextLine()) {
                String[] line = myReader.nextLine().split(","); // split line at comma
                stops.add(parseInt(line[0])); // add each bus stop ID to the bus stops arraylist
                reformattedStop = reformatStreetName(line[2]);
                stop_id = line[0];
                stop_code = line[1];
                line[0] = "";
                line[1] = "";
                reformattedLine = reformattedStop + ", " + line[3] + ", " + line[4] + ", " + line[5] + ", " +  line[6] + ", " +  line[7] + ", " +  line[8] + ", " +  stop_id + ", " +  stop_code;
                TST.put(reformattedLine, Integer.toString(index));
                index++;
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

    public static void addEdgesFromStopTimes(String filename) {
        try {
            if (filename == null) {
                return;
            }
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); // skip line0 as it has no data
            String line = scanner.nextLine();
            String[] line1 = line.split(",");
            int vertexIndexOne;
            int vertexIndexTwo;
            DirectedEdge edge;
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                String[] line2 = nextLine.split(",");
                if (parseInt(line1[0]) == parseInt(line2[0])) {
                    // search the stops arraylist to get the position of where that bus id is in the arraylist
                    vertexIndexOne = Collections.binarySearch(stops, parseInt(line1[3]));
                    vertexIndexTwo = Collections.binarySearch(stops, parseInt(line2[3]));
                    edge = new DirectedEdge(vertexIndexOne, vertexIndexTwo, 1); // add edge between those stops using a weight of 1 as it comes from stop_times.txt
                    digraph.addEdge(edge);
                }
                line1 = line2; // go back as there could be an edge from second line to the next
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
            e.printStackTrace();
        }
    }

    public static String reformatStreetName(String streetName){
        String[] splitBySpaces = streetName.split(" ");
        String firstWordInStreetName = splitBySpaces[0];
        for (String prefix : STREET_PREFIXES){
            if (firstWordInStreetName.equals(prefix)){
                streetName = streetName.replace(firstWordInStreetName, "").trim();
                streetName = streetName + " " + firstWordInStreetName;
            }
        }
        return streetName;
    }




}
