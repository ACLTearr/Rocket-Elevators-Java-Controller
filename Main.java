import java.util.*;

public class Main {

    //Defining battery class
    public class battery {
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

        public void Battery(int ID, int amountOfColumns, String status, int amountOfFloors, int amountOfBasements, int amountOfElevatorPerColumn) {
            this.ID = ID;
            this.amountOfColumns = amountOfColumns;
            this.status = status;
            this.amountOfFloors = amountOfFloors;
            this.amountOfBasements = amountOfBasements;
            this.amountOfElevatorPerColumn = amountOfElevatorPerColumn;
            this.columnsList = new ArrayList<Column>();
            this.floorRequestButtonsList = new ArrayList<FloorRequestButton>();
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
            int stopFloor = 1000; //Set to 1000 because it is not used here, but is used when moveElevator is called from requestElevator
            System.out.println("A request for an elevator is made from the lobby for floor {requestedFloor}, going {direction}.");
            Column column = this.findBestColumn(requestedFloor); //returning column
            System.out.println("Column {column.ID} is the column that can handle this request.");
            Elevator elevator = column.findBestElevator(1, direction); //returning best elevator, 1 bescause this request is only made from lobby
            System.out.println("Elevator {elevator.ID} is the best elevator, so it is sent.");
            elevator.currentFloor = 1;
            elevator.doorController();
            elevator.floorRequestList.add(requestedFloor);
            // elevator.sortFloorList(); BREAKS SCENARIO, REMOVE BEFORE DELIVERING IF NOT FIXED
            System.out.println("Elevator is moving.");
            elevator.moveElevator(stopFloor);
            System.out.println("Elevator is {elevator.status}.");
            elevator.doorController();
            if (elevator.floorRequestList.size() == 0) {
                elevator.direction = null;
                elevator.status = "idle";
            }
            System.out.println("Elevator is {elevator.status}.");
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

    public class Column {
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
                // int buttonFloor = 1;
                for (int floor : servedFloorsList) {
                    var callButton = new CallButton(callButtonID, "off", floor, "down");
                    callButtonsList.add(callButton);
                    // buttonFloor++;
                    callButtonID++;
                }
            }
        }

        public void makeElevators(int[] servedFloorsList, int amountOfElevators) {
            int elevatorID = 1;
            for (int i = 0; i < amountOfElevators; i++) {
                var elevator = new Elevator(elevatorID, "idle", servedFloorsList, 1);
                elevatorsList.add(elevator);
                elevatorID++;
            }
        }

        //When a user calls an elevator form a floor, not the lobby
        public void requestElevator(int userFloor, String direction) {
            System.out.println("A request for an elevator is made from floor {userFloor}, going {direction} to the lobby.");
            Elevator elevator = this.findBestElevator(userFloor, direction);
            System.out.println("Elevator {elevator.ID} is the best elevator, so it is sent.");
            elevator.floorRequestList.add(1); //1 because elevator can only move to lobby from floors
            elevator.sortFloorList();
            elevator.moveElevator(userFloor);
            elevator.doorController();
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

    public class Elevator {
        public int ID;
        public String status;
        public int[] servedFloorsList;
        public int currentFloor;
        public String direction;
        public List<Door> door;
        public List<Integer> floorRequestList;

        public Elevator(int elevatorID, String status, int[] servedFloorsList, int currentFloor) {
            this.ID = elevatorID;
            this.status = status;
            this.servedFloorsList = servedFloorsList;
            this.currentFloor = currentFloor;
            this.direction = "";
            var door = new Door(elevatorID, "closed");
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
                            System.out.println("Elevator is at floor: {this.currentFloor}");
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
                            System.out.println("Elevator is at floor: {this.currentFloor}");
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
            String status = "opened";
            System.out.printf("Elevator doors are %s.", this.status);
            System.out.println("Waiting for occupant(s) to transition.");
            //Wait 5 seconds
            if (!overweight) {
                status = "closing";
                System.out.println("Elevator doors are {status}.");
                if (!obstruction) {
                    status = "closed";
                    System.out.println("Elevator doors are {status}.");
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

    public class BestElevatorInfo {
        public Elevator bestElevator;
        public int bestScore;
        public int referenceGap;

        public BestElevatorInfo(Elevator bestElevator, int bestScore, int referenceGap) {
            this.bestElevator = bestElevator;
            this.bestScore = bestScore;
            this.referenceGap = referenceGap;
        }
    }

    public class CallButton {
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

    public class FloorRequestButton {
        public int ID;
        public String status;
        public int floor;

        public FloorRequestButton(int ID, String status, int floor) {
            this.ID = ID;
            this.status = status;
            this.floor = floor;
        }

    }

    public class Door {
        public int ID;
        public String status;

        public Door(int ID, String status) {
            this.ID = ID;
            this.status = status;
        }

    }

    //Uncomment this function, along with the appropriate call to run this scenario
    // void scenario1() {

    //     main.battery.columnsList[1].elevatorsList[0].currentFloor = 20;
    //     main.battery.columnsList[1].elevatorsList[0].direction = "down";
    //     main.battery.columnsList[1].elevatorsList[0].status = "moving";
    //     main.battery.columnsList[1].elevatorsList[0].floorRequestList.Add(5);
        
    //     main.battery.columnsList[1].elevatorsList[1].currentFloor = 3;
    //     main.battery.columnsList[1].elevatorsList[1].direction = "up";
    //     main.battery.columnsList[1].elevatorsList[1].status = "moving";
    //     main.battery.columnsList[1].elevatorsList[1].floorRequestList.Add(15);
        
    //     main.battery.columnsList[1].elevatorsList[2].currentFloor = 13;
    //     main.battery.columnsList[1].elevatorsList[2].direction = "down";
    //     main.battery.columnsList[1].elevatorsList[2].status = "moving";
    //     main.battery.columnsList[1].elevatorsList[2].floorRequestList.Add(1);
        
    //     main.battery.columnsList[1].elevatorsList[3].currentFloor = 15;
    //     main.battery.columnsList[1].elevatorsList[3].direction = "down";
    //     main.battery.columnsList[1].elevatorsList[3].status = "moving";
    //     main.battery.columnsList[1].elevatorsList[3].floorRequestList.Add(2);
        
    //     main.battery.columnsList[1].elevatorsList[4].currentFloor = 6;
    //     main.battery.columnsList[1].elevatorsList[4].direction = "down";
    //     main.battery.columnsList[1].elevatorsList[4].status = "moving";
    //     main.battery.columnsList[1].elevatorsList[4].floorRequestList.Add(1);

    //     main.battery.assignElevator(20, "up");
    // }

    // //Uncomment this function, along with the appropriate call to run this scenario
    // void scenario2() {

    //     main.battery.columnsList[2].elevatorsList[0].currentFloor = 1;
    //     main.battery.columnsList[2].elevatorsList[0].direction = "up";
    //     main.battery.columnsList[2].elevatorsList[0].status = "stopped";
    //     main.battery.columnsList[2].elevatorsList[0].floorRequestList.Add(21);
        
    //     main.battery.columnsList[2].elevatorsList[1].currentFloor = 23;
    //     main.battery.columnsList[2].elevatorsList[1].direction = "up";
    //     main.battery.columnsList[2].elevatorsList[1].status = "moving";
    //     main.battery.columnsList[2].elevatorsList[1].floorRequestList.Add(28);
        
    //     main.battery.columnsList[2].elevatorsList[2].currentFloor = 33;
    //     main.battery.columnsList[2].elevatorsList[2].direction = "down";
    //     main.battery.columnsList[2].elevatorsList[2].status = "moving";
    //     main.battery.columnsList[2].elevatorsList[2].floorRequestList.Add(1);
        
    //     main.battery.columnsList[2].elevatorsList[3].currentFloor = 40;
    //     main.battery.columnsList[2].elevatorsList[3].direction = "down";
    //     main.battery.columnsList[2].elevatorsList[3].status = "moving";
    //     main.battery.columnsList[2].elevatorsList[3].floorRequestList.Add(24);
        
    //     main.battery.columnsList[2].elevatorsList[4].currentFloor = 39;
    //     main.battery.columnsList[2].elevatorsList[4].direction = "down";
    //     main.battery.columnsList[2].elevatorsList[4].status = "moving";
    //     main.battery.columnsList[2].elevatorsList[4].floorRequestList.Add(1);

    //     main.battery.assignElevator(36, "up");
    // }

    // //Uncomment this function, along with the appropriate call to run this scenario
    // void scenario3() {

    //     main.battery.columnsList[3].elevatorsList[0].currentFloor = 58;
    //     main.battery.columnsList[3].elevatorsList[0].direction = "down";
    //     main.battery.columnsList[3].elevatorsList[0].status = "moving";
    //     main.battery.columnsList[3].elevatorsList[0].floorRequestList.Add(1);
        
    //     main.battery.columnsList[3].elevatorsList[1].currentFloor = 50;
    //     main.battery.columnsList[3].elevatorsList[1].direction = "up";
    //     main.battery.columnsList[3].elevatorsList[1].status = "moving";
    //     main.battery.columnsList[3].elevatorsList[1].floorRequestList.Add(60);
    
    //     main.battery.columnsList[3].elevatorsList[2].currentFloor = 46;
    //     main.battery.columnsList[3].elevatorsList[2].direction = "up";
    //     main.battery.columnsList[3].elevatorsList[2].status = "moving";
    //     main.battery.columnsList[3].elevatorsList[2].floorRequestList.Add(58);
        
    //     main.battery.columnsList[3].elevatorsList[3].currentFloor = 1;
    //     main.battery.columnsList[3].elevatorsList[3].direction = "up";
    //     main.battery.columnsList[3].elevatorsList[3].status = "moving";
    //     main.battery.columnsList[3].elevatorsList[3].floorRequestList.Add(54);
        
    //     main.battery.columnsList[3].elevatorsList[4].currentFloor = 60;
    //     main.battery.columnsList[3].elevatorsList[4].direction = "down";
    //     main.battery.columnsList[3].elevatorsList[4].status = "moving";
    //     main.battery.columnsList[3].elevatorsList[4].floorRequestList.Add(1);

    //     main.battery.columnsList[3].requestElevator(54, "down");
    // }

    // //Uncomment this function, along with the appropriate call to run this scenario
    // void scenario4() {

    //     main.battery.columnsList[0].elevatorsList[0].currentFloor = -4;
        
    //     main.battery.columnsList[0].elevatorsList[1].currentFloor = 1;
        
    //     main.battery.columnsList[0].elevatorsList[2].currentFloor = -3;
    //     main.battery.columnsList[0].elevatorsList[2].direction = "down";
    //     main.battery.columnsList[0].elevatorsList[2].status = "moving";
    //     main.battery.columnsList[0].elevatorsList[2].floorRequestList.Add(-5);
        
    //     main.battery.columnsList[0].elevatorsList[3].currentFloor = -6;
    //     main.battery.columnsList[0].elevatorsList[3].direction = "up";
    //     main.battery.columnsList[0].elevatorsList[3].status = "moving";
    //     main.battery.columnsList[0].elevatorsList[3].floorRequestList.Add(1);
        
    //     main.battery.columnsList[0].elevatorsList[4].currentFloor = -1;
    //     main.battery.columnsList[0].elevatorsList[4].direction = "down";
    //     main.battery.columnsList[0].elevatorsList[4].status = "moving";
    //     main.battery.columnsList[0].elevatorsList[4].floorRequestList.Add(-6);

    //     main.battery.columnsList[0].requestElevator(-3, "up");
    // }

    public static void main(String[] args) {
        
            Battery battery = new Battery(1, 4, "online", 60, 6, 5);

            if (battery.amountOfBasements > 0) {
                battery.makeBasementFloorRequestButtons(battery.amountOfBasements);
                battery.makeBasementColumn(battery.amountOfBasements, battery.amountOfElevatorPerColumn);
                battery.amountOfColumns--;
            }

        
            battery.makeFloorRequestButtons(battery.amountOfFloors);
            battery.makeColumns(battery.amountOfColumns, battery.amountOfFloors, battery.amountOfElevatorPerColumn);

            for (int i = 0; i < battery.amountOfColumns + 1; i++) {
                battery.columnsList[i].makeCallButtons(battery.amountOfFloors, battery.amountOfBasements, battery.columnsList[i].isBasement);
            }

            for (int i = 0; i < battery.amountOfColumns + 1; i++) {
                battery.columnsList[i].makeElevators(battery.columnsList[i].servedFloorsList, battery.columnsList[i].amountOfElevators);
            }

            //Uncomment this call and the appropriate function to run scenario 1
            // scenario1();

            //Uncomment this call and the appropriate function to run scenario 2
            // main.scenario2();

            //Uncomment this call and the appropriate function to run scenario 3
            // main.scenario3();

            //Uncomment this call and the appropriate function to run scenario 4
            // main.scenario4();

    }
}