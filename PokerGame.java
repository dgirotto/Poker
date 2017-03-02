import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

class Card {
	char suit;
	char face; 
	int value; // A=1, ...,  J=11, Q=12, K=13
	
	public Card(char suit, char face, int value){
		this.suit = suit;
		this.face = face;
		this.value = value;
	}
}

class Player {
	ArrayList<Card> playersHand; /* contains the user's two cards */
	String name; /* the user's name */
	int chips; /* total number of chips a user has */
	int inPot[]; /* number of chips in pot on current street; reset after each street */
	boolean human; /* human or computer? */
	boolean toAct; /* does the player still have to act before the next street can begin? */
	boolean folded;
	boolean allIn;
	
	public Player(String name, boolean isHuman, int chips){ /* each player starts with two cards */
		this.playersHand = new ArrayList<Card>();
		this.name = name;
		this.chips = chips;
		this.inPot = new int[4]; 
		this.human = isHuman;
		this.toAct = false;
		this.folded = false;
		this.allIn = false;
	}
	
	public void showCards(){
		System.out.print(((Card)this.playersHand.get(0)).face + "" + ((Card)this.playersHand.get(0)).suit + " ");
		System.out.print(((Card)this.playersHand.get(1)).face + "" + ((Card)this.playersHand.get(1)).suit);
	}
}

class RankedHand{
	int type; /* 0: high card, 1: 1 pair, 2: 2 pair, 3: 3 of a kind, 4: straight, 5: flush, 6: full house, 7: 4 of a kind,
	 		  *  8: straight flush, 9: royal flush  */
	ArrayList<Card>[] count;
	
	@SuppressWarnings("unchecked")
	RankedHand(){
		this.type=-1;
		this.count = (ArrayList<Card>[])new ArrayList[13];
	}
}

class Hand{
	ArrayList<Player> thePlayers; // each hand has a unique set of players (in a specific order)
	ArrayList<Card> theBoard;
	ArrayList<Card> theDeck; // each hand has a unique deck
	ArrayList<Card> combined; /* a player's hand combined with the board (used to rank their hand) */
	int pot, smallBlind, bigBlind;
	
	public Hand(ArrayList<Player> players, int smallB, int bigB){
		this.thePlayers = players;
		this.smallBlind = smallB;
		this.bigBlind = bigB;
		this.pot = 0;
		playHand();		
	}
	
	public void printPositions(){
		/* dealer = players[0], SB = players[1], BB = players[2] */
		for(int i=0; i<this.thePlayers.size(); i++){
			if(i==(this.thePlayers.size()-1)){
				System.out.print("D:  ");
			}
			else if(i==0){ /* small blind acts first */
				System.out.print("SB: ");
			}
			else if (i==1){
				System.out.print("BB: ");
			}
			else{
				System.out.print("    ");
			}
			System.out.print(((Player) this.thePlayers.get(i)).name + "\t(" + ((Player) this.thePlayers.get(i)).chips + ")");
			if(((Player) this.thePlayers.get(i)).folded){
				System.out.print(" FOLDED");		
			}
			System.out.println();
		}
		System.out.println("POT: " + this.pot);
	}	
	
	public void generateDeck(){
		this.theDeck = new ArrayList<Card>();
		this.theBoard = new ArrayList<Card>();
		this.combined = new ArrayList<Card>();
		char suits[] = {'C','D','H','S'}; /* clubs, diamonds, hearts, spades */
		int value = 0;
		char face = 0;
		
		for(int j=0; j<4; j++){
			for(int k=1; k<14; k++){
				switch(k){
					case 1: 
						face = 'A';
						value = 14; // (k also = 1)
						break;
					case 10: 
						face = 'T';
						value = 10;
						break;
					case 11: 
						face = 'J';
						value = 11;
						break;
					case 12: 
						face = 'Q';
						value = 12;
						break;
					case 13: 
						face = 'K';
						value = 13;
						break;
					default: 
						face = (char)(k + '0');
						value = k;
						break;
				}
				theDeck.add(new Card(suits[j],face,value));
			}
		}
	}
	

	
	public Card chooseCard(){ // randomly selects a card from the deck
		int randomNum = 0;
		randomNum = (int)(Math.random() * this.theDeck.size()); /* choose random number from 0 to the size of the deck */
		return((Card) this.theDeck.remove(randomNum));
	}
	
	
	public int getInput(){
		String theInput = null;
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		
		try{
				theInput = br.readLine();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		
		return Integer.parseInt(theInput);		
	}
	
	public int getCompInput(){
		int theInput=0;
		
		
		return theInput;
	}
	
	public boolean checkBoard(int toCall, int theStreet){
		boolean status = true;
		
		for(int i=0; i < this.thePlayers.size(); i++){
			if(((Player)this.thePlayers.get(i)).inPot[theStreet] != toCall && ((Player)this.thePlayers.get(i)).folded != true){
				status = false;
				break;
			}	
		}
		return status;
	}
	
	public void resetToAct(int theRaiser){ /* set all other players' (who haven't folded) toAct variable to TRUE (aside from "theRaiser") */
		for(int i=0; i < this.thePlayers.size(); i++){
			if(i != theRaiser){
				((Player) this.thePlayers.get(i)).toAct = true;
			}
		}
	}
	
	public int rankHand(ArrayList<Card> combined){
		RankedHand theHand = new RankedHand();
		boolean trips=false;		
		int temp=0;
		int pairs=0;
		int highestVal=0; /* keeps track of the highest valued card */
		int straightCtr=0;
		char flushTemp=0; /* C: clubs, D: diamonds, H: hearts, S: spades */
		char suit = 0;
		
		int flushCtr[] = new int[4]; /* 0: clubs, 1: diamonds, 2: hearts, 3: spades */
		int suitHigh[] = new int[4];
		
		
		for(int i=0; i<combined.size(); i++){
			/* record the suit for flush purposes */
			suit = combined.get(i).suit;
			
			if(suit == 'C'){
				flushCtr[0]++;
				if(combined.get(i).value > suitHigh[0]){
					suitHigh[0] = combined.get(i).value;
				}
			}
			else if(suit == 'D'){
				flushCtr[1]++;
				if(combined.get(i).value > suitHigh[1]){
					suitHigh[1] = combined.get(i).value;
				}
			}
			else if(suit == 'H'){
				flushCtr[2]++;
				if(combined.get(i).value > suitHigh[2]){
					suitHigh[2] = combined.get(i).value;
				}
			}
			else{
				flushCtr[3]++;
				if(combined.get(i).value > suitHigh[3]){
					suitHigh[3] = combined.get(i).value;
				}
			}

			temp = (combined.get(i).value - 2);
			
			if(theHand.count[temp] == null){
				theHand.count[temp] = new ArrayList<Card>(); /* initialize an array list at index "temp" (it is no longer "null" anymore */
			}
			
			theHand.count[temp].add(combined.get(i));
		}
		
		for(int i=0; i<13; i++){
			if(theHand.count[i] != null){
				if(straightCtr==0){
					straightCtr++;
				}
				else{
					if((temp + 1) == i){
						straightCtr++;
						if(straightCtr >= 5){
							//System.out.println("Found a straight! Highest card in straight is: " + theHand.count[i].get(0).face);
							if(theHand.type < 4){
								theHand.type = 4;
							}
						}
					}
					else{
						straightCtr = 0; /* restart straight counter */
					}
				}
				
				temp = i;
				
				if(theHand.count[i].get(0).value > highestVal){
					highestVal = theHand.count[i].get(0).value;
					if(theHand.type < 0){
						theHand.type = 0;
					}
				}
				
				switch(theHand.count[i].size()){
					case 1:{
						/*
						if(theHand.count[i].get(0).value > theHand.topKicker){
							theHand.topKicker = theHand.count[i].get(0).value;
						}
						*/
						break;
					}
					case 2:{ /* found a pair */
						pairs++;
						if(theHand.type < 1){
							theHand.type = 1;
						}
						break;
					}
					case 3:{ /* found trips */
						trips = true;
						if(theHand.type < 3){
							theHand.type = 3;
						}
						break;
					}
					case 4:{ /* found quads */
						if(theHand.type < 7){
							theHand.type = 7;
						}
						break;
					}
					default:{
						System.err.println("Invalid outcome.");
						break;
					}
				}
			}
		}
		
		if((pairs > 0) && trips){ /* full house */
			if(theHand.type < 6){
				theHand.type = 6;
			}
		}
		else if(pairs == 2){ /* two pair */
			if(theHand.type < 2){
				theHand.type = 2;
			}
		}
		else if(pairs > 2){ /* more than two pairs; record the highest two pairs */
			if(theHand.type < 2){
				theHand.type = 2;
			}			
		}

		for(int i=0; i<flushCtr.length; i++){
			if(flushCtr[i] >= 5){
				//System.out.println("Highest card in flush: " + suitHigh[i]);
				if(theHand.type < 5){
					theHand.type = 5;
				}
			}
		}

		return theHand.type;
	}
	
	
	
	public void playHand(){
		Player currPlayer;
		boolean skipBlinds = true;
		int toCall = 0;
		int theInput = 0;
		int theWinner = -1;
		int bestHand = 0;
		
		/* generate deck */
		generateDeck();
		
		/* deal the cards */
		for(int i=0; i<this.thePlayers.size(); i++){
			/* deal two cards to the selected player */
			currPlayer = (Player) this.thePlayers.get(i);
			currPlayer.playersHand.add(chooseCard());
			currPlayer.playersHand.add(chooseCard());
			
			/* print out the player's two cards */
			System.out.print(currPlayer.name + " ");
			currPlayer.showCards();
			System.out.println();
		}
		
		/* theStreet: 0 = preflop, 1 = flop, 2 = turn, 3 = river */
		for(int theStreet=0; theStreet<4; theStreet++){
		
			/* print the positions and chip stacks of each player */
			System.out.println("---------------------------");
			printPositions();
			System.out.println("---------------------------");
			
			/* print the board */
			if(theStreet > 0){
				System.out.print("BOARD: ");
				for(int i=0; i<this.theBoard.size(); i++){
					System.out.print(((Card)this.theBoard.get(i)).face + "" + ((Card)this.theBoard.get(i)).suit + " ");
				}
				System.out.println();				
			}
			
			/* start of a new street; set "toAct" to TRUE, and reset "status" to 0 for all players */
			for(int i=0; i<this.thePlayers.size(); i++){
				((Player)this.thePlayers.get(i)).toAct = true;
			}
				
			if(theStreet == 0){ /* this only runs once! */
				/* pay small blind */
				this.pot += this.smallBlind;
				((Player)this.thePlayers.get(0)).chips -= this.smallBlind;
				((Player)this.thePlayers.get(0)).inPot[theStreet] = this.smallBlind;
					
				/* pay big blind */
				this.pot += this.bigBlind;
				((Player)this.thePlayers.get(1)).chips -= this.bigBlind;
				((Player)this.thePlayers.get(1)).inPot[theStreet] = this.bigBlind;
					
				toCall = this.bigBlind;
			}
				
			do{ /* run this loop until all players have either called/checked or folded */
					
				for(int i=0; i<this.thePlayers.size(); i++){	
					
					if(skipBlinds){
						i=2; /* action begins at thePlayers.get(2) -- the player after the BB position */
						skipBlinds = false;
					}
						
					while(true){ /* loop runs until valid input is entered for a particular user */
						
						/* status: check = 1, call = 2, raise = 3, fold = 4 */
						if(((Player)this.thePlayers.get(i)).folded == true || ((Player)this.thePlayers.get(i)).toAct == false){
							/* don't need input from player */
							//System.out.println("Don't need input from " + ((Player)this.thePlayers.get(i)).name);
							break;
						}
						else{
							/* get player's input */
							if(((Player)this.thePlayers.get(i)).human == true){
								System.out.println(((Player)this.thePlayers.get(i)).name + "'s turn. " + (toCall - ((Player)this.thePlayers.get(i)).inPot[theStreet]) + 
								" to call. Min raise: " + toCall*2);
								theInput = getInput();
							}
							else{
								System.out.println("Take AI's input...");
								try {
									/* 5 second pause */
								    Thread.sleep(5000);
								}
								catch(InterruptedException ex) {
									Thread.currentThread().interrupt(); 
								}
								theInput = getCompInput(); /* get the AI's input */
							}
						}
							
						/* validate the player's input */
						if((theInput == 0) && (((Player) this.thePlayers.get(i)).inPot[theStreet] == toCall)){ /* the player checks */
							System.out.println(((Player) this.thePlayers.get(i)).name + " checks.");
							
							((Player) this.thePlayers.get(i)).toAct = false;
							break;
						}
						else if((((Player) this.thePlayers.get(i)).inPot[theStreet] + theInput) == toCall){ /* the player calls */
							
							if(((((Player) this.thePlayers.get(i)).inPot[theStreet] + theInput) > ((Player) this.thePlayers.get(i)).chips) && (((Player) this.thePlayers.get(i)).chips > 0)){
								System.out.println(((Player) this.thePlayers.get(i)).name + " calls and is now \"all in\".");
								this.pot += ((Player) this.thePlayers.get(i)).chips;
								((Player) this.thePlayers.get(i)).chips = 0;
								break;
							}
							else{
								System.out.println(((Player) this.thePlayers.get(i)).name + " calls.");
								this.pot += theInput;
								((Player) this.thePlayers.get(i)).chips -= theInput;
								((Player) this.thePlayers.get(i)).toAct = false;
								((Player) this.thePlayers.get(i)).inPot[theStreet] = toCall;
								break;
							}
							
						}
						else if(theInput >= (toCall*2)){ /* the player raises */
							
							if(((Player) this.thePlayers.get(i)).chips < theInput){
								System.err.println(((Player) this.thePlayers.get(i)).name + " doesn't have enough chips.");
							}
							else{
								System.out.println(((Player) this.thePlayers.get(i)).name + " raises to " + theInput);
								
								/* reset all other players' "toAct" variable (if they haven't folded) to "toAct = TRUE" */
								resetToAct(i);
	
								toCall = theInput;
								this.pot += (theInput - ((Player) this.thePlayers.get(i)).inPot[theStreet]);
								
								((Player) this.thePlayers.get(i)).chips -= (theInput - ((Player) this.thePlayers.get(i)).inPot[theStreet]);
								((Player) this.thePlayers.get(i)).toAct = false;
								((Player) this.thePlayers.get(i)).inPot[theStreet] = theInput;
								break;	
							}
						}
						else if((theInput == 0) && (((Player) this.thePlayers.get(i)).inPot[theStreet] < toCall)){ /* player folds */
							System.out.println(((Player) this.thePlayers.get(i)).name + " folds.");
							
							((Player) this.thePlayers.get(i)).toAct = false;
							((Player) this.thePlayers.get(i)).folded = true;
							break;
						}
						else{
							System.err.println("Invalid input entered.");
						}
					}	
					/* check if all players have either called or folded */		
				}

			}while(checkBoard(toCall,theStreet) == false);
				 
			if(theStreet == 0){
				/* draw three cards */
				this.theBoard.add(chooseCard());
				this.theBoard.add(chooseCard());
				this.theBoard.add(chooseCard());
			}
			else if(theStreet < 3){ /* if theStreet == 1 or 2 */
				/* draw only one card */
				this.theBoard.add(chooseCard());
			}
			
			/* reset local variables */
			theInput = 0;
			toCall = 0;
		}
		
		int temp=0;
		
		for(int i=0; i<this.thePlayers.size(); i++){
			if(((Player) this.thePlayers.get(i)).folded == false){
				/* combine the user's hand with the board */
				combined.addAll(((Player) this.thePlayers.get(i)).playersHand);
				combined.addAll(theBoard);	
				
				temp = rankHand(combined);
				if(temp > bestHand){
					bestHand = temp;
					theWinner = i;
				}
			}
			((Player) this.thePlayers.get(i)).playersHand.clear(); /* clear the player's hand for the next game */
			combined.clear(); /* clear the array list for the next player */
		}	
		
		if((theWinner >= 0) && (theWinner < this.thePlayers.size())){
			System.out.println(((Player) this.thePlayers.get(theWinner)).name + " wins the pot!");
			((Player) this.thePlayers.get(theWinner)).chips += this.pot;
		}
		else{
			System.err.println("Error with determining winner!");
		}
		
		/* reset all of the fields */
		for(int i=0; i<this.thePlayers.size(); i++){
			((Player)this.thePlayers.get(i)).toAct = true;
			((Player)this.thePlayers.get(i)).folded = false;
			
			for(int j=0; j<((Player)this.thePlayers.get(i)).inPot.length; j++){
				//System.out.println("Resetting " + ((Player)this.thePlayers.get(i)).inPot[j] + " to 0");
				((Player)this.thePlayers.get(i)).inPot[j] = 0;
			}
		}
	
	}
	
}

public class PokerGame {

	ArrayList<Hand> hands = new ArrayList<Hand>();
	ArrayList<Player> players = new ArrayList<Player>();
	
	public static void main(String[] args){
		Player temp;
		
		/* initialize new game */
		PokerGame newGame = new PokerGame();
		
		/* create the players */	
		newGame.players.add(new Player("Andy",true,500)); /* starts in first position (SB) */
		newGame.players.add(new Player("Brad",true,500)); 
		newGame.players.add(new Player("Cody",true,500));
		newGame.players.add(new Player("Daniel",true,500));
		/*
		newGame.players.add(new Player("Computer1",false,500));
		newGame.players.add(new Player("Computer2",false,500));
		newGame.players.add(new Player("Computer3",false,500));
		*/
		
		while(true){
			for(int i=0; i<4; i++){ /* play 5 hands */
				newGame.hands.add(new Hand(newGame.players,20,40)); /* Hand(ArrayList players, int sBlind, int bBlind) */

				/* move the player in first position to last position */
				temp = (Player) newGame.players.remove(0);
				newGame.players.add(temp);
				System.out.println();
			}
			break;
		}
	}
}
