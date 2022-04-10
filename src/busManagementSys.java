import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.TST;
import edu.princeton.cs.algs4.DijkstraSP;

import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.Integer.parseInt;

public class busManagementSys {

    static ArrayList<String> stopTimes; // arraylist of integers used as corresponding matrix value for creation of
    static ArrayList<Integer> stops; // arraylist of integers used as corresponding matrix value for creation of
    static TST<String> TST; // empty ternary search tree
    static DijkstraSP SP;
    public static EdgeWeightedDigraph digraph;


    public static final String[] STREET_PREFIXES = new String[] {"FLAGSTOP", "WB", "NB", "SB", "EB"}; // keywords array for use in reformatting
    public static final int ONE_HUNDRED = 100;

    public static void main(String[] args){
        addStopsToArraylist("stops.txt"); // read in files and add data to arraylists and TST
        addEdgesFromTransfersFile("transfers.txt");
        addEdgesFromStopTimes("stop_times.txt");

        boolean isOver = false; // condition flag for user to exit program
        System.out.println("Welcome to the Vancouver Bus System Data program.");

        while(!isOver){
            System.out.println("If you would like to search for the shortest path between two bus stops of your choosing, please input 1 below.\n" +
                    "If you would like to search for a certain bus stop by name/prefix, please type in 2. \n" +
                    "If you would like to search by arrival time, please type in 3. \n" +
                    "Or if you would like to leave this program please enter 'exit'.\n");
            System.out.println("Choose 1,2 or 3:  ");

            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextInt()) {
                int userChoice = scanner.nextInt();
                switch(userChoice){
                    case 1:
                        findShortestPath();
                        break;
                    case 2:
                        searchBusStop();
                        break;
                    case 3:
                        searchByTime();
                        break;
                }
                System.out.println("\n\n");
            }
            else {
                if (scanner.hasNext() && scanner.next().equalsIgnoreCase("exit")){
                    isOver = true;
                    System.out.println("You have quit the program. Have a good day :) \n");
                }
                else{
                    System.out.println("Invalid input. Please enter 1, 2 or 3 to choose an option or quit by entering 'exit' \n");
                }
            }
        }
    }

    public static void searchBusStop(){
        String prefix;
        System.out.println("Please enter the name or the first few letters of the bus stop you are searching for: ");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()){
            prefix = scanner.next();
            int counter = 0;
            for (String s : TST.keysWithPrefix(prefix.toUpperCase())) {
                counter++; // count used to check if bus stops with this name were found
                System.out.println(s); // print out each bus stop name with inputted prefix
            }
            if (counter == 0) {
                System.out.println("There are no existing stops with this name/prefix.");
            }
        }
    }

    public static void searchByTime(){
        String userQuery;
        System.out.println("Please enter the arrival time you are searching for in 'hh:mm:ss' format: ");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
            userQuery = scanner.next();
            String queryTime = scanner.nextLine().trim(); // trim whitespace inputted by user
            String[] splitQueryTime = queryTime.split(":");
            // if user inputted time is valid and properly formatted
            if (    !splitQueryTime[0].equalsIgnoreCase("") &&
                    (parseInt(splitQueryTime[0]) < 23 && parseInt(splitQueryTime[0]) >= 0) &&
                    (parseInt(splitQueryTime[1]) < 59 && parseInt(splitQueryTime[1]) >= 0) &&
                    (parseInt(splitQueryTime[2]) < 59 && parseInt(splitQueryTime[2]) >= 0)
            ){
                int counter = 0;
                System.out.println("trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled\n");
                String[] arrivalTime;
                for (String s : stopTimes) {
                    arrivalTime = s.split(",");
                    if (arrivalTime[1].trim().equals(queryTime)) {
                        counter++;
                        System.out.println(s);
                    }
                }
                if (counter == 0) {
                    System.out.println("There are no existing stops with this arrival time.");
                }
            }
            // else invalid time/format
            else{
                System.out.println("You have entered either an invalid time or didn't format it as 'hh:mm:ss'.");
            }
        }
    }

    public static void findShortestPath(){
        System.out.println("Please enter the first bus stop:  ");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextInt()) {
            int sourceVertex = scanner.nextInt();
            System.out.println("Please enter the first bus stop:  ");
            if (scanner.hasNextInt()){
                int destinationVertex = scanner.nextInt();
                if (sourceVertex != destinationVertex){
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
                else {
                    System.out.println("You have entered the same bus stop twice. If you are looking for a path, please enter two different stops.");
                }
            }
            else{
                System.out.println("This bus stop is not a valid bus stop ID.");
            }
        }
        else{
            System.out.println("This bus stop is not a valid bus stop ID.");
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
            String stop_id,  stop_code, reformattedStop, reformattedLine;
            int index = 0;
            while (myReader.hasNextLine()) {
                String[] line = myReader.nextLine().split(","); // split line at comma
                stops.add(parseInt(line[0])); // add each bus stop ID to the bus stops arraylist
                reformattedStop = reformatStreetName(line[2]); // move the keywords to the back of the street name and store in variable
                stop_id = line[0]; // store this so it can be moved
                stop_code = line[1];
                line[0] = ""; // clear stop_id in line as it will be moved to the end of the line in the TST
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
                if (parseInt(lineData[2]) == 0){ // if transfer type is 0, weight is 2
                    weight = 2;
                }
                else{
                    weight = (parseInt(lineData[3]))/ ONE_HUNDRED; // else weight is minimum transfer time divided by 100
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
            int vertexIndexOne, vertexIndexTwo;
            DirectedEdge edge;
            String arrivalTime;
            String[] arrivalTimeSplit;
            stopTimes = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                String[] line2 = nextLine.split(",");
                arrivalTime = line2[1].trim();
                arrivalTimeSplit = arrivalTime.split(":");
                if (parseInt(arrivalTimeSplit[0]) >= 0 && parseInt(arrivalTimeSplit[0]) < 24)
                    stopTimes.add(nextLine);
                if (parseInt(line1[0]) == parseInt(line2[0])) {
                    // search the stops arraylist to get the position of where that bus id is in the arraylist
                    vertexIndexOne = Collections.binarySearch(stops, parseInt(line1[3]));
                    vertexIndexTwo = Collections.binarySearch(stops, parseInt(line2[3]));
                    edge = new DirectedEdge(vertexIndexOne, vertexIndexTwo, 1); // add edge between those stops using a weight of 1 as it comes from stop_times.txt
                    digraph.addEdge(edge);
                }
                line1 = line2; // go back as there could be an edge from second line to the next
            }
            Collections.sort(stopTimes);
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
            e.printStackTrace();
        }
    }

    public static String reformatStreetName(String streetName){
        String[] splitBySpaces = streetName.split(" ");
        String firstWordInStreetName = splitBySpaces[0];
        for (String prefix : STREET_PREFIXES){ // if one of the keywords is found at the start of a street name
            if (firstWordInStreetName.equals(prefix)){
                streetName = streetName.replace(firstWordInStreetName, "").trim(); // replace the keyword with nothing
                streetName = streetName + " " + firstWordInStreetName; // add the keyword back at the end of the street name
            }
        }
        return streetName;
    }




}
