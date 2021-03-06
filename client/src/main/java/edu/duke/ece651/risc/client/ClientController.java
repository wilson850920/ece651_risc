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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import edu.duke.ece651.risc.common.*;

public class ClientController {
  public static HashMap<Integer, ObjectOutputStream> outMap = new HashMap<>();
  public static HashMap<Integer, ObjectInputStream> inMap = new HashMap<>();
  public static HashMap<Integer, NewClient> clientMap = new HashMap<>();
  public static HashMap<Integer, Integer> stageMap = new HashMap<>();
  public static int numOfGames = 0;

  public static void playGame(NewClient[] myClient, int stage, ObjectOutputStream out, ObjectInputStream in)
      throws ClassNotFoundException, IOException {
    NewClient thisClient = myClient[0];
    try {
      switch (stage) {
        case 1:
          thisClient.receivePlayer(in);
          System.out.println("recieve");
          thisClient.displayCommonMessage(in);
          thisClient.assignUnits();
          System.out.println("please wait for other player to finish");
          thisClient.sendPlayer(out);
          break;
        case 2:
          thisClient.displayCommonMessage(in);
          break;
        case 3:
          OrderList ordersList = thisClient.takeOrders();
          thisClient.sendOrders(ordersList, out);
          System.out.println("please wait for other player to finish issuing their orders");
          break;
        case 4:
          thisClient.receivePlayer(in);
          thisClient.recieveTurnSummary(in);
          break;
        case 5:
          thisClient.receivechecklosemap(in, out);
          thisClient.winner(in);
          break;
        case 6:
          thisClient.displayCommonMessage(in);
          break;
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public static void createMap(String hostname, int portNum) throws IOException {
    Socket thisSocket = new Socket(hostname, portNum);
    ObjectOutputStream out = new ObjectOutputStream(thisSocket.getOutputStream());
    ObjectInputStream in = new ObjectInputStream(thisSocket.getInputStream());
    Player testPlayer = new BasicPlayer("clientPlayer", 2000);
    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader inputReader = new BufferedReader(inputStreamReader);
    NewClient thisClient = new NewClient(testPlayer, inputReader, System.out);
    ++numOfGames;
    outMap.put(numOfGames, out);
    inMap.put(numOfGames, in);
    clientMap.put(numOfGames, thisClient);
    stageMap.put(numOfGames, 0);
  }

  public static String getActionInput() throws IOException {
    System.out.println(
        "What would you like to do?\n(S)tart a new game\n(P)lay current game \n(C)hange to other active game\n(Q)uit current game\n(R)estart historical game");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    return reader.readLine();
  }

  public static void performAction(String action) throws IOException, ClassNotFoundException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String[] parsedInput;
    int gameInd;
    int stage;
    ObjectOutputStream out;
    ObjectInputStream in;
    NewClient[] myClient = new NewClient[1];
    switch (action) {
      case "S":
        System.out.println("<hostname, port number>");
        parsedInput = reader.readLine().split(", ");
        createMap(parsedInput[0], Integer.parseInt(parsedInput[1]));
        System.out.println("Game " + numOfGames + " created");
        break;
      case "P":
        System.out.println("enter the game ID you would like to play. You have following game(s)");
        for (int i : stageMap.keySet()) {
          System.out.println("Game " + i + " at stage " + stageMap.get(i));
        }
        parsedInput = reader.readLine().split(", ");
        gameInd = Integer.parseInt(parsedInput[0]);
        stage = Integer.parseInt(parsedInput[1]);
        out = outMap.get(gameInd);
        in = inMap.get(gameInd);
        myClient[0] = clientMap.get(gameInd);
        playGame(myClient, stage, out, in);
        clientMap.replace(gameInd, myClient[0]);
        stageMap.replace(gameInd, stage);
        break;
      case "C":
        System.out.println("enter the game ID you would like to play. You have following game(s)");
        for (int i : stageMap.keySet()) {
          System.out.println("Game " + i + " at stage " + stageMap.get(i));
        }
        parsedInput = reader.readLine().split(", ");
        gameInd = Integer.parseInt(parsedInput[0]);
        stage = Integer.parseInt(parsedInput[1]);
        out = outMap.get(gameInd);
        in = inMap.get(gameInd);
        myClient = new NewClient[1];
        myClient[0] = clientMap.get(gameInd);
        playGame(myClient, stage, out, in);
        clientMap.replace(gameInd, myClient[0]);
        break;
      case "Q":
        System.out.println("enter the game ID you would like to quit. You have following game(s)");
        for (int i : stageMap.keySet()) {
          System.out.println("Game " + i + " at stage " + stageMap.get(i));
        }
        gameInd = Integer.parseInt(reader.readLine());
        outMap.get(gameInd).close();
        inMap.get(gameInd).close();
        stageMap.replace(gameInd, -1);
        break;
    }

  }

  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
    while (true) {
      String action = getActionInput(); // this bye bye
      performAction(action);
      System.out.println("back to main");
    }
  }
}
