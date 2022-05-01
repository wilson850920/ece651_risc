/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.duke.ece651.risc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import edu.duke.ece651.risc.common.*;

public class Client {//extends Application{
//public class Client extends Application{
    public Player player;
    final BufferedReader inputReader;
    final PrintStream outputWriter;
    //public BufferedReader inputReader;
    //public PrintStream outputWriter;
    public String hostName;
    public int portNumber;
    public Socket clientSocket;
    public ObjectInputStream in; // review with swetha
    public ObjectOutputStream out;
    private HashMap<String, Integer> unitTypesToBonus = new HashMap<String, Integer>();

    public String uitest() {
	String str = "testing for ui";
	return str;
	
    }
 
    /**
     * This is the constructor for Client class
     *
     * @param player       is the player object that is playing the game
     * @param inputSource  is the type of input stream we are expecting
     * @param outputWriter will be used to display output
     * @param hostName     is the hostName of the server
     * @param portNumber   is the port number of the server
     */
    public Client(Player player,
                  BufferedReader inputSource,
                  PrintStream outputWriter,
                  String hostName,
                  int portNumber,
                  Socket clientSocket,
                  ObjectInputStream in,
                  ObjectOutputStream out) throws IOException, ClassNotFoundException {
        this.player = player;
        this.inputReader = inputSource;
        this.outputWriter = outputWriter;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
        this.unitTypesToBonus.put("A", 0);
        this.unitTypesToBonus.put("B", 1);
        this.unitTypesToBonus.put("C", 3);
        this.unitTypesToBonus.put("D", 5);
        this.unitTypesToBonus.put("E", 8);
        this.unitTypesToBonus.put("F", 11);
        this.unitTypesToBonus.put("G", 15);
    }

    public void setClientSocketStreams() throws IOException {
        this.clientSocket = createCilentSocket();
        setObjectInputStream();
        setObjectOutputStream();
    }

    private void setObjectInputStream() throws IOException {
        this.in = new ObjectInputStream(this.clientSocket.getInputStream());
    }

    private void setObjectOutputStream() throws IOException {
        this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
    }

    private Socket createCilentSocket() {
        try {
            String hostName = getHostName();
            //int portNumber = getPositiveInteger("Enter the port number of server:");
	    int portNumber = 1641;
            Socket socket = getClientSocket(hostName, portNumber);
            return socket;
        } catch (IOException e) {
            outputWriter
                    .println("Client socket creation failed. Make sure the server with entered hostname and port number is up.");
            return createCilentSocket();
        }
    }

    private String getHostName() throws IOException {
        outputWriter.println("Please enter the hostname of sever");
        //String hostName = inputReader.readLine();
	String hostName = "vcm-24503.vm.duke.edu";
        return hostName;
    }

    private Socket getClientSocket(String hostName, int portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);
        return socket;
    }

    /**
     * A method that returns us the player object
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * A method that close all the existing connections and input/output stream
     */
    public void close() throws IOException {

        this.in.close();
        this.out.close();
        this.clientSocket.close();

    }

    /**
     * A method that sends player object to the server
     */
    public void sendPlayer() throws IOException {
        this.out.writeObject(this.player);
    }

    /**
     * A method that send order obkect to the server
     */
    private void sendOrders(OrderList ordersList) throws IOException {
        this.out.writeObject(ordersList);
    }

    /**
     * A method that receives player object to the server
     */
    public void receivePlayer() throws IOException, ClassNotFoundException {
        //System.out.println("receivePlayer");
        Player p = (Player) this.in.readObject();
        this.player = p;
    }

    /**
     * A method that displays player's territories at the beginning of the game
     * (this may be discarded)
     * <p>
     * NOTE: for text display only as of now
     *
     * @throws IOException if the territory display info cannot be written to the
     *                     output
     */
    public void displayBeginningInfo() throws IOException {
        outputWriter.println("You are " + this.player.getName());
        outputWriter.println("You own following territories:\n");
        for (Iterator<Territory> it = this.player.getTerritories(); it.hasNext(); ) {
            Territory curTerr = it.next();
            curTerr.getDisplayInfo().displayTerritory(outputWriter);
        }

        outputWriter.println("\nYou have " + this.player.getAvailableUnits() +
                " units available");
        outputWriter.println("How would you like to distribute them?");
    }

    /**
     * A method that displays entire map received from server
     */
    public void displayCommonMessage() throws IOException, ClassNotFoundException {
        //System.out.println("displayCommonMessage");
        MapDisplayInfo myDisplay = (MapDisplayInfo) this.in.readObject();
        myDisplay.displayMap(outputWriter);
    }

    /**
     * A method that allows user to assign units to its territory
     */
    public void assignUnits() throws IOException {
        Iterator<Territory> it = player.getTerritories();
        int assignedUnits = 0;
        outputWriter.println("You are " + this.player.getName() +
                ". Please assign units to your territory");
        int i = 0;
        while (it.hasNext()) {
            Territory t = it.next();
            assignedUnits = assignUnitsToTerritory(t, assignedUnits, player.numberOfTerritories() - i - 1);
            i++;
        }
    }

    /**
     * A methond to assign unit to a territory for Client class,
     *
     * @param T             is the territory object
     * @param assignedUnits is the number of units to be assigned (0 <
     *                      assignedUnits)
     */
    public int assignUnitsToTerritory(Territory T, int assignedUnits, int territoriesLeft)
            throws IOException {
        outputWriter.println(
                "You have " + (this.player.getAvailableUnits() - assignedUnits) +
                        " units available. Enter the number of units to be assigned to Territory " +
                        T.getName());
        String units = this.inputReader.readLine();
        if (!validateAndAssignUnits(units, T, assignedUnits, territoriesLeft)) {
            assignUnitsToTerritory(T, assignedUnits, territoriesLeft);
        }
        return assignedUnits + T.getNumberOfUnits();
    }

    /**
     * A methond to validate if the input of unit assignment is valid
     *
     * @param units         is the number of units input by user
     * @param T             is the territoy object that the assigned unit goes to
     * @param assignedUnits is the number of units that is available for assignment
     */
    public boolean validateAndAssignUnits(String units,
                                          Territory T,
                                          int assignedUnits,
                                          int territoriesLeft) {
        try {
            int u = Integer.parseInt(units);
            if ((u > this.player.getAvailableUnits() - assignedUnits) || u <= 0 ||
                    (this.player.getAvailableUnits() - assignedUnits - u) < territoriesLeft) { // should be able to accept 0
                outputWriter.println(
                        "Entered number of units unavailable or No units will be available for remaining territories.");
                return false;
            }
            while (u > 0) {
                T.addUnit(0);
                u--;
            }
            return true;
        } catch (NumberFormatException e) {
            outputWriter.println("Please enter Integer number of units");
            return false;
        }
    }

    /**
     * this function will ask the player which action do they want to do
     * the choices are move, attack, and done
     *
     * @return a list with orders
     */
    public OrderList takeOrders() throws IOException {
        OrderList list = new BasicOrderList();
        if (this.player.numberOfTerritories() == 0) {
            return list;
        }
        outputWriter.println("Please enter the orders you wish to play:");
        boolean ordersRemaining = true;
        while (ordersRemaining) {
            Order order = getSingleOrder();
            if (order == null) {
                ordersRemaining = false;
            } else {
                list.addOrder(order);
            }
        }
        return list;
    }

    /**
     * get a single order
     *
     * @return a order representing a single order, after getting ordertype
     * [action, source, target, units]
     */
    public Order getSingleOrder() throws IOException {
        try {
            String orderType = getOrderType();
            if (orderType.equalsIgnoreCase("D"))
                return null;
            Order order = null;
            if (orderType.equalsIgnoreCase("M")) {
                order = createSingleMoveOrder();
            } else if (orderType.equalsIgnoreCase("A")) {
                order = createSingleAttackOrder();
            } else if (orderType.equalsIgnoreCase("UU")) {
                order = createSingleUnitUpgradeOrder();
            } else if (orderType.equalsIgnoreCase("TU")) {
                order = createTechnologyUpgradeOrder();
            }
            return order;
        } catch (IllegalArgumentException e) {
            return getSingleOrder();
        }
    }

    /**
     * by inputting source, target, and the units to actions
     * the function will return a move order with all the information that was sent into
     * it
     */
    public Order createSingleMoveOrder() {
        try {
            Territory source = getTerritory("Source");
            Territory target = getTerritory("Target");
            HashMap<Integer, Integer> units = getUnitsMap();
            BasicOrder order = new MoveOrder(this.player, source, target, units);
            return order;
        } catch (IllegalArgumentException | IOException e) {
            return createSingleMoveOrder();
        }
    }

    /**
     * by inputting source, target, and the units to actions
     * the function will return an attack order with all the information that was sent into
     * it
     */
    public Order createSingleAttackOrder() {
        try {
            Territory source = getTerritory("Source");
            Territory target = getTerritory("Target");
            HashMap<Integer, Integer> units = getUnitsMap();
            BasicOrder order = new AttackOrder(this.player, source, target, units);
            return order;
        } catch (IllegalArgumentException | IOException e) {
            return createSingleMoveOrder();
        }
    }

    /**
     * by inputting source, current type of unit and target type of unit
     * the function will return an unit upgrade order with all the information that was sent into
     * it
     */
    public Order createSingleUnitUpgradeOrder() {
        try {
            Territory source = getTerritory("Source");
            String currentUnitType = getUnitType("Enter type of unit that you wish to upgrade:");
            String targetUnitType = getUnitType("Enter type to which you wish to upgrade your unit:");
            BasicOrder order = new UnitUpgradeOrder(this.player, source, this.unitTypesToBonus.get(currentUnitType), this.unitTypesToBonus.get(targetUnitType));
            return order;
        } catch (IllegalArgumentException e) {
            return createSingleUnitUpgradeOrder();
        }
    }

    /**
     * the function will return a technology upgrade order with all the information
     */
    public Order createTechnologyUpgradeOrder() {
        try {
            outputWriter.println("Only one technology upgrade will be applied per a turn");
            BasicOrder order = new TechnologyUpgradeOrder(this.player);
            return order;
        } catch (IllegalArgumentException e) {
            return createTechnologyUpgradeOrder();
        }
    }

    /**
     * get the type of the action
     * which in this part, there's only going to be move and attack
     *
     * @return a string that represent the type of the order
     */
    public String getOrderType() throws IOException {
        outputWriter.println("You are " + this.player.getName() +
                ", what would you like to do?\n(M)ove\n(A)ttack\n(UU) Unit Upgrade\n(TU) Player Technology Upgrade\n(D)one");
        try {
            String type = inputReader.readLine();
            type = type.toUpperCase();
            if ((!type.equals("M") && !type.equals("A") && !type.equals("D") && !type.equals("UU") && !type.equals("TU") &&
                    !type.equals(null))) {
                outputWriter.println("Invalid order type.");
                return getOrderType();
            }
            return type;
        } catch (NullPointerException e) {
            return getOrderType();
        }
    }

    /**
     * by inputting a source or target string
     * if is source, then check whether do a player own the territory or not
     * if is target, then check whether the territory is a neighbor of the player of
     * not
     *
     * @throw if the condition isn't valid
     */
    public Territory getTerritory(String sourceOrTarget) {
        outputWriter.println("Enter name of " + sourceOrTarget +
                ". If it's a source, territory must be owned by you.");
        try {
            String s = inputReader.readLine();
            Territory T;
            if (sourceOrTarget.equals("Source")) {
                T = findSourceTerritory(s);
                if (T == null) {
                    outputWriter.println(
                            "Invalid source territory. You either don't own the entered territory or the territory doesn't exist.");
                    return getTerritory(sourceOrTarget);
                }
                return T;
            } else if (sourceOrTarget.equals("Target")) {
                T = findTargetTerritory(s);
                if (T == null) {
                    outputWriter.println(
                            "Invalid target territory. The territory may not exist or can not be a target as there is no path to the territory.");
                    return getTerritory(sourceOrTarget);
                }
                return T;
            }
        } catch (IOException e) {
            outputWriter.println(e);
            return getTerritory(sourceOrTarget);
        }
        return null;
    }

    /**
     * this function will check if the input string is a territory of a player
     *
     * @return a territory object correspond to the input string
     */
    public Territory findSourceTerritory(String s) {
        Iterator<Territory> it = this.player.getTerritories();
        while (it.hasNext()) {
            Territory T = it.next();
            if ((T.getName()).equalsIgnoreCase(s))
                return T;
        }
        return null;
    }

    /**
     * this function will check if the input string is a territory of a player
     *
     * @return a territory object correspond to the input string
     * if the action is move, the target would be a territory own by a
     * player
     * if the action is attack, the target would be a territory that is a
     * neighbor of the player's territory
     * but not own by the player
     */
    public Territory findTargetTerritory(String s) {
        Iterator<Territory> it = this.player.getTerritories();
        Territory firstTerritory = it.next();
        Stack<Territory> tracker = new Stack<Territory>();
        HashSet<Territory> visited = new HashSet<Territory>();
        visited.add(firstTerritory);
        tracker.push(firstTerritory);
        while (!tracker.empty()) {
            Territory current = tracker.pop();
            if ((current.getName()).equalsIgnoreCase(s)) {
                return current;
            }
            visited.add(current);
            // Getting neighbours that are only owned by the sender
            // TODO - does not check this? ^^^
            Iterator<Territory> neighbors = current.getNeighbors();
            while (neighbors.hasNext()) {
                Territory t = neighbors.next();
                if (!visited.contains(t) && !tracker.contains(t)) {
                    tracker.push(t);
                }
            }
        }
        return null;
    }

    public int getPositiveInteger(String s) throws IOException {
        outputWriter.println(s);
        try {
            String c = inputReader.readLine();
            int count = Integer.parseInt(c);
            if (count <= 0) {
                outputWriter.println("Invalid. Please enter a positive integer.");
                return getPositiveInteger(s);
            }
            return count;
        } catch (NumberFormatException e) {
            outputWriter.println("Please enter a positive integer");
            return getPositiveInteger(s);
        }
    }


    public String getUnitType(String s) {
        outputWriter.println(s +
                " \n(A) 0\n(B) 1\n(C) 3\n(D) 5\n(E) 8\n(F) 11\n(G) 15");

        try {
            String type = inputReader.readLine();
            type = type.toUpperCase();
            if (!unitTypesToBonus.containsKey(type) && !type.equalsIgnoreCase("O")) {
                outputWriter.println("Invalid unit type.");
                return getUnitType(s);
            }
            return type;
        } catch (NullPointerException | IOException e) {
            return getUnitType(s);
        }
    }

    public HashMap<Integer, Integer> getUnitsMap() throws IOException {
        outputWriter.println("Enter the type and number of units to be used in order:");
        HashMap<Integer, Integer> unitsMap = new HashMap<Integer, Integer>();
        while(true){
            String type = getUnitType("Enter Unit type. Enter (O)ver to finish. Following are the available unit types:");
            if(type.equalsIgnoreCase("O")){
                break;
            }
            int count = getPositiveInteger("Enter the number of units to be used.");
            unitsMap.put(this.unitTypesToBonus.get(type), count);
        }
        return unitsMap;
    }

    /**
     * after a player loses, we inform the player to input a choice bewteen watching
     * the game continue or the disconnect from the game
     *
     * @throw IllegalArgumentException if the loser input a incorrect type or
     * character
     */
    public String informloser() throws IOException, ClassNotFoundException {
        try {
            outputWriter.println(
                    "Boo you lose~ \nYou can either choose to stay in the game and <watch!> or you can disconnect and Goodbye\n(W) Continuing watch the game\n(E) Leave the game");
            String loser = inputReader.readLine();
            loser = loser.toUpperCase();
            if ((!loser.equals("W") && !loser.equals("E")) || loser == null) {
                outputWriter.println("Wrong input loser");
                throw new IllegalArgumentException();
            }
            return loser;
        } catch (IllegalArgumentException e) {
            return informloser();
        }
    }

    /**
     * disconnect the socket from the loser
     */

    /**
     * receive the hashmap from server which contains the player and the boolean
     * status which represent the status of each player
     * an iterator will then loop through the map and see whether the loser is a new
     * loser or a old loser
     */
    public void receivechecklosemap() throws IOException, ClassNotFoundException {
        //System.out.println("receivechecklosemap");
        HashMap<String, Boolean> losemap = (HashMap<String, Boolean>) this.in.readObject();
        for (String p : losemap.keySet()) {
            if (!p.equals(this.player.getName())) {
                continue;
            }
            String status = "lost";
            if (losemap.get(p)) {
                status = "are still playing please wait as server is processing";
            }
            System.out.println("You " + status);
            if ((losemap.get(p) == false) && (this.player.getIngameOrLoseFlag() == true)) {
                this.player.setIngameOrLoseFlag(false);
                String loserinput = informloser();
                if (loserinput.equals("E")) {
                    this.out.writeObject("disconnect");
                    System.out.println("disconnect");
                    close();
                } else {
                    this.out.writeObject("lost and watching");
                }
            } else {
                if (this.player.getIngameOrLoseFlag()) {
                    this.out.writeObject("still playing");
                } else {
                    this.out.writeObject("lost and watching");
                }
            }
        }
    }


    /**
     * check if the message from the server is true
     * if the messages is true, that means there is a winner in the game
     * and the client should close all the socket
     */
    public void winner() throws IOException, ClassNotFoundException {
        // this.in = new ObjectInputStream(this.clientSocket.getInputStream());
        String winner = (String) in.readObject();
        if (winner.equals("true") && this.player.getIngameOrLoseFlag()) {
            System.out.println("You win!");
            close();
        }
    }

    public void recieveTurnSummary() throws IOException, ClassNotFoundException {
        //System.out.println("receive turn summary");
        String summary = (String) this.in.readObject();
        outputWriter.println("Summary of latest turn executed:");
        outputWriter.println(summary);
    }
  
    /**
     * receive information from server side
     * send information to server side
     * make sure the game connects correctly
     * close if disconnected or a winner
     */

  public static void main(String[] args) throws IOException, ClassNotFoundException {
        Player testPlayer = new BasicPlayer("clientPlayer", 500);
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader inputReader = new BufferedReader(inputStreamReader);
        //String hostname = "vcm-25468.vm.duke.edu";
        String hostname = "vcm-24503.vm.duke.edu";
        // String hostname = "10.197.127.234";
        int portNum = 1641;
        Client thisClient = new Client(
                testPlayer, inputReader, System.out, hostname, portNum, null, null, null);
        //launch();
        //thisClient.runUI();
        thisClient.setClientSocketStreams();
        thisClient.receivePlayer();
        thisClient.displayCommonMessage();
        thisClient.assignUnits();
        System.out.println("please wait for other player to finish");
        thisClient.sendPlayer();
        thisClient.displayCommonMessage();

        while (true) {
            try {
                OrderList ordersList = thisClient.takeOrders();
                thisClient.sendOrders(ordersList);
                System.out.println("please wait for other player to finish issuing their orders");
                thisClient.receivePlayer();
                thisClient.recieveTurnSummary();
                thisClient.receivechecklosemap();
                thisClient.winner();
                thisClient.displayCommonMessage();
                //thisClient.winner();
            } catch (Exception e) {
                break;
            }
        }
        System.out.println("Game Over!");
    }
}