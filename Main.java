import java.util.*;

//Defining battery class
class Battery {
    public int ID;
    public int amountOfColumns;
    public String status;
    public int amountOfFloors;
    public int amountOfBasements;
    public int amountOfElevatorPerColumn;
    public List<Integer> servedFloors;
    public int columnID = 1;
    public int floorRequestButtonID = 1;
    public List<Column> columnsList;
    public List<FloorRequestButton> floorRequestButtonsList;

    public Battery(int ID, int amountOfColumns, String status, int amountOfFloors, int amountOfBasements, int amountOfElevatorPerColumn) {
        this.ID = ID;
        this.amountOfColumns = amountOfColumns;
        this.status = status;
        this.amountOfFloors = amountOfFloors;
        this.amountOfBasements = amountOfBasements;
        this.amountOfElevatorPerColumn = amountOfElevatorPerColumn;
        this.columnsList = new ArrayList<Column>();
        this.floorRequestButtonsList = new ArrayList<FloorRequestButton>();
        
        //Calls to create buttons and columns and elevators
        if (amountOfBasements > 0) {
            this.makeBasementFloorRequestButtons(amountOfBasements);
            this.makeBasementColumn(amountOfBasements, amountOfElevatorPerColumn);
            amountOfColumns--;
        }

    
        this.makeFloorRequestButtons(amountOfFloors);
        this.makeColumns(amountOfColumns, amountOfFloors, amountOfElevatorPerColumn);

        for (int i = 0; i < amountOfColumns + 1; i++) {
            columnsList.get(i).makeCallButtons(amountOfFloors, amountOfBasements, columnsList.get(i).isBasement);
        }

        for (int i = 0; i < amountOfColumns + 1; i++) {
            columnsList.get(i).makeElevators(columnsList.get(i).servedFloorsList, columnsList.get(i).amountOfElevators);
        }
    }

    //Method to create basement columns
    public void makeBasementColumn(int amountOfBasements, int amountOfElevatorPerColumn) {
        List<Integer> servedFloors = new ArrayList<Integer>();
        int floor = -1;
        for (int i = 0; i < (amountOfBasements + 1); i++) {
            if (i == 0) { //adding main floor to floor list
                servedFloors.add(1);
            } else {
                servedFloors.add(floor);
                floor--;
            }
        }
        var column = new Column(columnID, "online", amountOfElevatorPerColumn, servedFloors, true);
        columnsList.add(column);
        columnID++;
    }
        
    //Method to create columns
    public void makeColumns(int amountOfColumns, int amountOfFloors, int amountOfElevatorPerColumn) {
        int amountOfFloorsPerColumn = (int)Math.ceil((double)amountOfFloors / amountOfColumns);
        int floor = 1;
        for (int i = 0; i < amountOfColumns; i++) {
            List<Integer> servedFloors = new ArrayList<Integer>();
            for (int x = 0; x < amountOfFloorsPerColumn; x++) {
                if (i == 0) { //For first above ground column
                    servedFloors.add(floor);
                    floor++;
                } else { //For all columns after first above ground, to make sure main floor is included
                    if (x == 0) {
                        servedFloors.add(1);
                    }
                    servedFloors.add(floor);
                    floor++;
                }
            }
            var column = new Column(columnID, "online",amountOfElevatorPerColumn, servedFloors, false);
            columnsList.add(column);
            columnID++; 
        }
    }

    //Method to create basement floor request buttons
    public void makeBasementFloorRequestButtons(int amountOfBasements) {
        int buttonFloor = -1;
        for (int i = 0; i < amountOfBasements; i++) {
            var floorRequestButton = new FloorRequestButton(floorRequestButtonID, "off", buttonFloor);
            floorRequestButtonsList.add(floorRequestButton);
            buttonFloor--;
            floorRequestButtonID++;
        }
    }

    //Method to create buttons to request a floor
    public void makeFloorRequestButtons(int amountOfFloors) {
        int buttonFloor = 1;
        for (int i = 0; i < amountOfFloors; i++) {
            var floorRequestButton = new FloorRequestButton(floorRequestButtonID, "off", buttonFloor);
            floorRequestButtonsList.add(floorRequestButton);
            buttonFloor++;
            floorRequestButtonID++;
        }
    }

    //Method to find the appropriate elevator within the appropriate column to serve user
    public void assignElevator(int requestedFloor, String direction) {
        System.out.println("A request for an elevator is made from the lobby for floor " + requestedFloor + " going " + direction + ".");
        Column column = this.findBestColumn(requestedFloor); //returning column
        System.out.println("Column " + column.ID + " is the column that can handle this request.");
        Elevator elevator = column.findBestElevator(1, direction); //returning best elevator, 1 bescause this request is only made from lobby
        int stopFloor = elevator.floorRequestList.get(0);
        System.out.println("Elevator " + elevator.ID + " is the best elevator, so it is sent.");
        if (elevator.status == "moving") {
            elevator.moveElevator(stopFloor);
        }
        elevator.floorRequestList.add(requestedFloor);
        elevator.sortFloorList();
        System.out.println("Elevator is moving.");
        elevator.moveElevator(stopFloor);
        System.out.println("Elevator is " + elevator.status + ".");
        elevator.doorController();
        if (elevator.floorRequestList.size() == 0) {
            elevator.direction = null;
            elevator.status = "idle";
        }
        System.out.println("Elevator is " + elevator.status + ".");
    }

    //Method to find appropriate column to serve user
    public Column findBestColumn(int requestedFloor) {
        Column bestColumn = null;
        for (Column column : this.columnsList) {
            if (column.servedFloorsList.contains(requestedFloor)) {
                bestColumn = column;
            }
        }
        return bestColumn;
    }

}

//Defining column class
class Column {
    public int ID;
    public String status;
    public int amountOfElevators;
    public List<Integer> servedFloorsList;
    public boolean isBasement;
    public List<Elevator> elevatorsList;
    public List<CallButton> callButtonsList;

    public Column(int ID, String status, int amountOfElevators, List<Integer> servedFloors, boolean isBasement) {
        this.ID = ID;
        this.status = status;
        this.amountOfElevators = amountOfElevators;
        this.servedFloorsList = servedFloors;
        this.isBasement = isBasement;
        this.elevatorsList = new ArrayList<Elevator>();
        this.callButtonsList = new ArrayList<CallButton>();
    }

    //Method to create call buttons
    public void makeCallButtons(int floorsServed, int amountOfBasements, boolean isBasement) {
        int callButtonID = 1;
        if (isBasement) {
            int buttonFloor = -1;
            for (int i = 0; i < amountOfBasements; i++) {
                var callButton = new CallButton(callButtonID, "off", buttonFloor, "up");
                callButtonsList.add(callButton);
                buttonFloor--;
                callButtonID++;
            }
        } else {
            for (int floor : servedFloorsList) {
                var callButton = new CallButton(callButtonID, "off", floor, "down");
                callButtonsList.add(callButton);
                callButtonID++;
            }
        }
    }

    //Method to create elevators
    public void makeElevators(List<Integer> servedFloorsList, int amountOfElevators) {
        int elevatorID = 1;
        for (int i = 0; i < amountOfElevators; i++) {
            var elevator = new Elevator(elevatorID, "idle", servedFloorsList, 1);
            elevatorsList.add(elevator);
            elevatorID++;
        }
    }

    //When a user calls an elevator form a floor, not the lobby
    public void requestElevator(int userFloor, String direction) {
        System.out.println("A request for an elevator is made from floor " + userFloor + " going " + direction + " to the lobby.");
        Elevator elevator = this.findBestElevator(userFloor, direction);
        System.out.println("Elevator " + elevator.ID + " is the best elevator, so it is sent.");
        elevator.floorRequestList.add(1); //1 because elevator can only move to lobby from floors
        elevator.sortFloorList();
        System.out.println("Elevator is moving.");
        elevator.moveElevator(userFloor);
        System.out.println("Elevator is " + elevator.status + ".");
        elevator.doorController();
        if (elevator.floorRequestList.size() == 0) {
            elevator.direction = null;
            elevator.status = "idle";
        }
        System.out.println("Elevator is " + elevator.status + ".");
    }

    //Find best elevator to send
    public Elevator findBestElevator(int floor, String direction) {
        int requestedFloor = floor;
        String requestedDirection = direction;
        var bestElevatorInfo = new BestElevatorInfo(null, 6, 1000000);

        if (requestedFloor == 1) {
            for (Elevator elevator : this.elevatorsList) {
                //Elevator is at lobby with some requests, and about to leave but has not yet
                if (1 == elevator.currentFloor && elevator.status == "stopped") {
                    this.checkBestElevator(1, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is at lobby with no requests
                } else if (1 == elevator.currentFloor && elevator.status == "idle") {
                    this.checkBestElevator(2, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is lower than user and moving up. Shows user is requesting to go to basement, and elevator is moving to them.
                } else if (1 > elevator.currentFloor && elevator.direction == "up") {
                    this.checkBestElevator(3, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is higher than user and moving down. Shows user is requesting to go to a floor, and elevator is moving to them.
                } else if (1 < elevator.currentFloor && elevator.direction == "down") {
                    this.checkBestElevator(3, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is not at lobby floor, but has no requests
                } else if (elevator.status == "idle") {
                    this.checkBestElevator(4, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is last resort
                } else {
                    this.checkBestElevator(5, elevator, bestElevatorInfo, requestedFloor);
                }
            }
        } else {
            for (Elevator elevator : this.elevatorsList) {
                //Elevator is at floor going to lobby
                if (requestedFloor == elevator.currentFloor && elevator.status == "stopped" && requestedDirection == elevator.direction) {
                    this.checkBestElevator(1, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is lower than user and moving through them to destination
                } else if (requestedFloor > elevator.currentFloor && elevator.direction == "up" && requestedDirection == "up") {
                    this.checkBestElevator(2, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is higher than user and moving through them to destination
                } else if (requestedFloor < elevator.currentFloor && elevator.direction == "down" && requestedDirection == "down") {
                    this.checkBestElevator(2, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is idle
                } else if (elevator.status == "idle") {
                    this.checkBestElevator(3, elevator, bestElevatorInfo, requestedFloor);
                //Elevator is last resort
                } else {
                    this.checkBestElevator(4, elevator, bestElevatorInfo, requestedFloor);
                }
            }
        }
        return bestElevatorInfo.bestElevator;

    }

    //Comparing elevator to previous best
    public BestElevatorInfo checkBestElevator(int scoreToCheck, Elevator newElevator, BestElevatorInfo bestElevatorInfo, int floor) {
        //If elevators situation is more favourable, set to best elevator
        if (scoreToCheck < bestElevatorInfo.bestScore) {
            bestElevatorInfo.bestScore = scoreToCheck;
            bestElevatorInfo.bestElevator = newElevator;
            bestElevatorInfo.referenceGap = Math.abs(newElevator.currentFloor - floor);
        //If elevators are in a similar situation, set the closest one to the best elevator
        } else if (bestElevatorInfo.bestScore == scoreToCheck) {
            int gap = Math.abs(newElevator.currentFloor - floor);
            if (bestElevatorInfo.referenceGap > gap) {
                bestElevatorInfo.bestScore = scoreToCheck;
                bestElevatorInfo.bestElevator = newElevator;
                bestElevatorInfo.referenceGap = gap;
            }
        }
        return bestElevatorInfo;
    }
}

class Elevator {
    public int ID;
    public String status;
    public List<Integer> servedFloorsList;
    public int currentFloor;
    public String direction;
    public List<Door> door;
    public List<Integer> floorRequestList;

    public Elevator(int elevatorID, String status, List<Integer> servedFloorsList, int currentFloor) {
        this.ID = elevatorID;
        this.status = status;
        this.servedFloorsList = servedFloorsList;
        this.currentFloor = currentFloor;
        this.direction = "";
        this.floorRequestList = new ArrayList<Integer>();
    }

    //Moving elevator
    public void moveElevator(int stopFloor) {
        while (this.floorRequestList.size() != 0) {
            int destination = this.floorRequestList.get(0);
            this.status = "moving";
            if (this.currentFloor < destination) {
                this.direction = "up";
                while (this.currentFloor < destination) {
                    if (this.currentFloor == stopFloor) {
                        this.status = "stopped";
                        this.doorController();
                        this.currentFloor++;
                    } else {
                        this.currentFloor++;
                    }
                    if (this.currentFloor == 0) {
                        //Do nothing, so that moving from basement to/from 1 doesnt show 0
                    } else {
                        System.out.println("Elevator is at floor: " + this.currentFloor);
                    }
                }
            } else if (this.currentFloor > destination) {
                this.direction = "down";
                while (this.currentFloor > destination) {
                    if (this.currentFloor == stopFloor) {
                        this.status = "stopped";
                        this.doorController();
                        this.currentFloor--;
                    } else {
                        this.currentFloor--;
                    }
                    if (this.currentFloor == 0) {
                        //Do nothing, so that moving from basement to/from 1 doesnt show 0
                    } else {
                        System.out.println("Elevator is at floor: " + this.currentFloor);
                    }
                }
            }
            this.status = "stopped";
            this.floorRequestList.remove(0);
        }
    }

    public void sortFloorList() {
        if (this.direction == "up") {
            Collections.sort(this.floorRequestList);
        } else {
            Collections.sort(this.floorRequestList, Collections.reverseOrder());
        }
        
    }

    //Door operation controller
    public void doorController() {
        boolean overweight = false;
        boolean obstruction = false;
        this.status = "opened";
        System.out.println("Elevator doors are " + this.status);
        System.out.println("Waiting for occupant(s) to transition.");
        //Wait 5 seconds
        if (!overweight) {
            this.status = "closing";
            System.out.println("Elevator doors are " + this.status);
            if (!obstruction) {
                this.status = "closed";
                System.out.println("Elevator doors are " + this.status);
            } else {
                //Wait for obstruction to clear
                obstruction = false;
                doorController();
            }
        } else {
            while (overweight) {
                //Ring alarm and wait until not overweight
                overweight = false;
            }
            doorController();
        }
    }

}

//Defining best elevator info class
class BestElevatorInfo {
    public Elevator bestElevator;
    public int bestScore;
    public int referenceGap;

    public BestElevatorInfo(Elevator bestElevator, int bestScore, int referenceGap) {
        this.bestElevator = bestElevator;
        this.bestScore = bestScore;
        this.referenceGap = referenceGap;
    }
}

//Defining call button class
class CallButton {
    public int ID;
    public String status;
    public int floor;
    public String direction;

    public CallButton(int ID, String status, int floor, String direction) {
        this.ID = ID;
        this.status = status;
        this.floor = floor;
        this.direction = direction;
    }

}

//Defining floor request button class
class FloorRequestButton {
    public int ID;
    public String status;
    public int floor;

    public FloorRequestButton(int ID, String status, int floor) {
        this.ID = ID;
        this.status = status;
        this.floor = floor;
    }

}

//Defining door class
class Door {
    public int ID;
    public String status;

    public Door(int ID, String status) {
        this.ID = ID;
        this.status = status;
    }

}

public class Main {

    public static void main(String[] args) {

        Tests test = new Tests();
        
        // Uncomment to run scenario 1
        // test.scenario1();

        // Uncomment to run scenario 2
        // test.scenario2();

        // Uncomment to run scenario 3
        // test.scenario3();

        // Uncomment to run scenario 4
        // test.scenario4();

    }
}

class Tests {
    
    public void scenario1() {

        Battery battery = new Battery(1, 4, "online", 60, 6, 5);

        battery.columnsList.get(1).elevatorsList.get(0).currentFloor = 20;
        battery.columnsList.get(1).elevatorsList.get(0).direction = "down";
        battery.columnsList.get(1).elevatorsList.get(0).status = "moving";
        battery.columnsList.get(1).elevatorsList.get(0).floorRequestList.add(5);
        
        battery.columnsList.get(1).elevatorsList.get(1).currentFloor = 3;
        battery.columnsList.get(1).elevatorsList.get(1).direction = "up";
        battery.columnsList.get(1).elevatorsList.get(1).status = "moving";
        battery.columnsList.get(1).elevatorsList.get(1).floorRequestList.add(15);
        
        battery.columnsList.get(1).elevatorsList.get(2).currentFloor = 13;
        battery.columnsList.get(1).elevatorsList.get(2).direction = "down";
        battery.columnsList.get(1).elevatorsList.get(2).status = "moving";
        battery.columnsList.get(1).elevatorsList.get(2).floorRequestList.add(1);
        
        battery.columnsList.get(1).elevatorsList.get(3).currentFloor = 15;
        battery.columnsList.get(1).elevatorsList.get(3).direction = "down";
        battery.columnsList.get(1).elevatorsList.get(3).status = "moving";
        battery.columnsList.get(1).elevatorsList.get(3).floorRequestList.add(2);
        
        battery.columnsList.get(1).elevatorsList.get(4).currentFloor = 6;
        battery.columnsList.get(1).elevatorsList.get(4).direction = "down";
        battery.columnsList.get(1).elevatorsList.get(4).status = "moving";
        battery.columnsList.get(1).elevatorsList.get(4).floorRequestList.add(1);

        battery.assignElevator(20, "up");
    }

    
    void scenario2() {

        Battery battery = new Battery(1, 4, "online", 60, 6, 5);

        battery.columnsList.get(2).elevatorsList.get(0).currentFloor = 1;
        battery.columnsList.get(2).elevatorsList.get(0).direction = "up";
        battery.columnsList.get(2).elevatorsList.get(0).status = "stopped";
        battery.columnsList.get(2).elevatorsList.get(0).floorRequestList.add(21);
        
        battery.columnsList.get(2).elevatorsList.get(1).currentFloor = 23;
        battery.columnsList.get(2).elevatorsList.get(1).direction = "up";
        battery.columnsList.get(2).elevatorsList.get(1).status = "moving";
        battery.columnsList.get(2).elevatorsList.get(1).floorRequestList.add(28);
        
        battery.columnsList.get(2).elevatorsList.get(2).currentFloor = 33;
        battery.columnsList.get(2).elevatorsList.get(2).direction = "down";
        battery.columnsList.get(2).elevatorsList.get(2).status = "moving";
        battery.columnsList.get(2).elevatorsList.get(2).floorRequestList.add(1);
        
        battery.columnsList.get(2).elevatorsList.get(3).currentFloor = 40;
        battery.columnsList.get(2).elevatorsList.get(3).direction = "down";
        battery.columnsList.get(2).elevatorsList.get(3).status = "moving";
        battery.columnsList.get(2).elevatorsList.get(3).floorRequestList.add(24);
        
        battery.columnsList.get(2).elevatorsList.get(4).currentFloor = 39;
        battery.columnsList.get(2).elevatorsList.get(4).direction = "down";
        battery.columnsList.get(2).elevatorsList.get(4).status = "moving";
        battery.columnsList.get(2).elevatorsList.get(4).floorRequestList.add(1);

        battery.assignElevator(36, "up");
    }

    void scenario3() {

        Battery battery = new Battery(1, 4, "online", 60, 6, 5);

        battery.columnsList.get(3).elevatorsList.get(0).currentFloor = 58;
        battery.columnsList.get(3).elevatorsList.get(0).direction = "down";
        battery.columnsList.get(3).elevatorsList.get(0).status = "moving";
        battery.columnsList.get(3).elevatorsList.get(0).floorRequestList.add(1);
        
        battery.columnsList.get(3).elevatorsList.get(1).currentFloor = 50;
        battery.columnsList.get(3).elevatorsList.get(1).direction = "up";
        battery.columnsList.get(3).elevatorsList.get(1).status = "moving";
        battery.columnsList.get(3).elevatorsList.get(1).floorRequestList.add(60);
    
        battery.columnsList.get(3).elevatorsList.get(2).currentFloor = 46;
        battery.columnsList.get(3).elevatorsList.get(2).direction = "up";
        battery.columnsList.get(3).elevatorsList.get(2).status = "moving";
        battery.columnsList.get(3).elevatorsList.get(2).floorRequestList.add(58);
        
        battery.columnsList.get(3).elevatorsList.get(3).currentFloor = 1;
        battery.columnsList.get(3).elevatorsList.get(3).direction = "up";
        battery.columnsList.get(3).elevatorsList.get(3).status = "moving";
        battery.columnsList.get(3).elevatorsList.get(3).floorRequestList.add(54);
        
        battery.columnsList.get(3).elevatorsList.get(4).currentFloor = 60;
        battery.columnsList.get(3).elevatorsList.get(4).direction = "down";
        battery.columnsList.get(3).elevatorsList.get(4).status = "moving";
        battery.columnsList.get(3).elevatorsList.get(4).floorRequestList.add(1);

        battery.columnsList.get(3).requestElevator(54, "down");
    }

    void scenario4() {

        Battery battery = new Battery(1, 4, "online", 60, 6, 5);

        battery.columnsList.get(0).elevatorsList.get(0).currentFloor = -4;
        
        battery.columnsList.get(0).elevatorsList.get(1).currentFloor = 1;
        
        battery.columnsList.get(0).elevatorsList.get(2).currentFloor = -3;
        battery.columnsList.get(0).elevatorsList.get(2).direction = "down";
        battery.columnsList.get(0).elevatorsList.get(2).status = "moving";
        battery.columnsList.get(0).elevatorsList.get(2).floorRequestList.add(-5);
        
        battery.columnsList.get(0).elevatorsList.get(3).currentFloor = -6;
        battery.columnsList.get(0).elevatorsList.get(3).direction = "up";
        battery.columnsList.get(0).elevatorsList.get(3).status = "moving";
        battery.columnsList.get(0).elevatorsList.get(3).floorRequestList.add(1);
        
        battery.columnsList.get(0).elevatorsList.get(4).currentFloor = -1;
        battery.columnsList.get(0).elevatorsList.get(4).direction = "down";
        battery.columnsList.get(0).elevatorsList.get(4).status = "moving";
        battery.columnsList.get(0).elevatorsList.get(4).floorRequestList.add(-6);

        battery.columnsList.get(0).requestElevator(-3, "up");
    }
}