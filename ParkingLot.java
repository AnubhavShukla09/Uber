import java.util.*; // import utility classes
enum VehicleType{CAR,BIKE,TRUCK} // define vehicle types
enum SpotSize{SMALL,MEDIUM,LARGE} // define spot sizes
class Vehicle{
    private String vehicleNumber; // store vehicle number
    private VehicleType type; // store vehicle type
    public Vehicle(String vehicleNumber,VehicleType type){this.vehicleNumber=vehicleNumber;this.type=type;} // constructor
    public VehicleType getType(){return type;} // return vehicle type
    public String getVehicleNumber(){return vehicleNumber;} // return vehicle number
    public SpotSize getRequiredSize(){ // determine required spot size for this vehicle
        if(type==VehicleType.BIKE)return SpotSize.SMALL; // bike requires small
        if(type==VehicleType.CAR)return SpotSize.MEDIUM; // car requires medium
        if(type==VehicleType.TRUCK)return SpotSize.LARGE; // truck requires large
        return null; // fallback
    }
}
class ParkingSpot{
    private int spotId; // unique spot id
    private SpotSize size; // size of the spot
    private Vehicle vehicle; // vehicle parked in this spot
    public ParkingSpot(int spotId,SpotSize size){this.spotId=spotId;this.size=size;} // constructor
    public boolean isFree(){return vehicle==null;} // check if spot is empty
    public void park(Vehicle vehicle){this.vehicle=vehicle;} // assign vehicle to spot
    public void unpark(){this.vehicle=null;} // remove vehicle from spot
    public SpotSize getSize(){return size;} // return size of spot
    public int getSpotId(){return spotId;} // return spot id
}
class ParkingFloor{
    private int floorNumber; // floor identifier
    private List<ParkingSpot> spots; // list of spots on floor
    public ParkingFloor(int floorNumber,List<ParkingSpot> spots){this.floorNumber=floorNumber;this.spots=spots;} // constructor
    public int getFloorNumber(){return floorNumber;} // return floor number
    public List<ParkingSpot> getSpots(){return spots;} // return spots list
}
class Ticket{
    private String ticketId; // unique ticket id
    private ParkingSpot spot; // allocated parking spot
    private int floorNumber; // floor number where vehicle parked
    public Ticket(String ticketId,ParkingSpot spot,int floorNumber){this.ticketId=ticketId;this.spot=spot;this.floorNumber=floorNumber;} // constructor
    public String getTicketId(){return ticketId;} // return ticket id
    public ParkingSpot getSpot(){return spot;} // return allocated spot
    public int getFloorNumber(){return floorNumber;} // return floor number
}
interface SlotAllocationStrategy{
    ParkingSpot allocate(List<ParkingFloor> floors,Vehicle vehicle); // allocation method
}
class FirstFitStrategy implements SlotAllocationStrategy{
    public ParkingSpot allocate(List<ParkingFloor> floors,Vehicle vehicle){ // iterate floors and spots to find first compatible
        SpotSize requiredSize=vehicle.getRequiredSize(); // get required size from vehicle
        for(ParkingFloor floor:floors){ // iterate through each floor
            for(ParkingSpot spot:floor.getSpots()){ // iterate through each spot
                if(spot.isFree()&&spot.getSize()==requiredSize)return spot; // return first free matching spot
            }
        }
        return null; // return null if no spot found
    }
}
class ParkingLot{
    private List<ParkingFloor> floors; // list of floors
    private Map<String,Ticket> activeTickets; // map of active tickets
    private SlotAllocationStrategy strategy; // allocation strategy
    public ParkingLot(List<ParkingFloor> floors){
        this.floors=floors; // initialize floors
        this.activeTickets=new HashMap<>(); // initialize ticket map
        this.strategy=new FirstFitStrategy(); // set default strategy
    }
    public synchronized String park(Vehicle vehicle){ // O(F × S)
        ParkingSpot spot=strategy.allocate(floors,vehicle); // allocate spot based on strategy
        if(spot==null)return "Parking Full"; // return message if no spot available
        spot.park(vehicle); // assign vehicle to spot
        int floorNumber=findFloorNumber(spot); // determine floor number
        String ticketId=UUID.randomUUID().toString(); // generate unique ticket id
        Ticket ticket=new Ticket(ticketId,spot,floorNumber); // create ticket object
        activeTickets.put(ticketId,ticket); // store ticket
        return ticketId; // return generated ticket id
    }
    public synchronized void unpark(String ticketId){ // O(1)
        Ticket ticket=activeTickets.get(ticketId); // fetch ticket
        if(ticket==null){System.out.println("Invalid Ticket");return;} // handle invalid case
        ticket.getSpot().unpark(); // free the spot
        activeTickets.remove(ticketId); // remove ticket from map
        System.out.println("Unparked Successfully"); // confirmation
    }
    private int findFloorNumber(ParkingSpot spot){ // helper method to find floor of spot
        for(ParkingFloor floor:floors){ // iterate floors
            if(floor.getSpots().contains(spot))return floor.getFloorNumber(); // return floor number if spot found
        }
        return -1; // fallback
    }
}

public class Main{
    public static void main(String[] args){
        List<ParkingFloor> floors=new ArrayList<>(); // create list of floors
        List<ParkingSpot> floor1Spots=new ArrayList<>(); // create floor1 spots
        floor1Spots.add(new ParkingSpot(1,SpotSize.SMALL)); // add small spot
        floor1Spots.add(new ParkingSpot(2,SpotSize.MEDIUM)); // add medium spot
        floor1Spots.add(new ParkingSpot(3,SpotSize.LARGE)); // add large spot
        floors.add(new ParkingFloor(1,floor1Spots)); // add floor1
        List<ParkingSpot> floor2Spots=new ArrayList<>(); // create floor2 spots
        floor2Spots.add(new ParkingSpot(1,SpotSize.SMALL)); // add small spot
        floor2Spots.add(new ParkingSpot(2,SpotSize.MEDIUM)); // add medium spot
        floor2Spots.add(new ParkingSpot(3,SpotSize.LARGE)); // add large spot
        floors.add(new ParkingFloor(2,floor2Spots)); // add floor2
        ParkingLot lot=new ParkingLot(floors); // initialize parking lot
        Vehicle bike=new Vehicle("B1",VehicleType.BIKE); // create bike
        Vehicle car=new Vehicle("C1",VehicleType.CAR); // create car
        Vehicle truck=new Vehicle("T1",VehicleType.TRUCK); // create truck
        String bikeTicket=lot.park(bike); // park bike
        System.out.println("Bike Ticket:"+bikeTicket); // print bike ticket
        String carTicket=lot.park(car); // park car
        System.out.println("Car Ticket:"+carTicket); // print car ticket
        String truckTicket=lot.park(truck); // park truck
        System.out.println("Truck Ticket:"+truckTicket); // print truck ticket
        lot.unpark(bikeTicket); // unpark bike
        lot.unpark(carTicket); // unpark car
        lot.unpark(truckTicket); // unpark truck
    }
}
