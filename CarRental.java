import java.util.*; // import utilities

class Car{
    String licensePlate; // unique id
    int costPerDay; // daily cost
    int freeKmsPerDay; // free kms per day
    int costPerKm; // extra km cost
    List<Booking> bookings; // bookings list
    public Car(String licensePlate,int costPerDay,int freeKmsPerDay,int costPerKm){
        this.licensePlate=licensePlate; // assign id
        this.costPerDay=costPerDay; // assign cost
        this.freeKmsPerDay=freeKmsPerDay; // assign free kms
        this.costPerKm=costPerKm; // assign per km cost
        this.bookings=new ArrayList<>(); // init booking list
    }
}
class Booking{
    String orderId; // order id
    int fromDay; // start day
    int tillDay; // end day
    int startOdometer; // start km
    boolean tripStarted; // flag
    public Booking(String orderId,int fromDay,int tillDay){
        this.orderId=orderId; // assign id
        this.fromDay=fromDay; // assign start
        this.tillDay=tillDay; // assign end
        this.tripStarted=false; // not started
    }
}
class CarRentalService{
    private Map<String,Car> cars; // cars map
    private Map<String,Booking> bookings; // order map

    public CarRentalService(){
        this.cars=new HashMap<>(); // init cars
        this.bookings=new HashMap<>(); // init bookings
    }

    public void addCar(String licensePlate,int costPerDay,int freeKmsPerDay,int costPerKm){ // O(1)
        if(cars.containsKey(licensePlate))return; // ignore duplicate
        cars.put(licensePlate,new Car(licensePlate,costPerDay,freeKmsPerDay,costPerKm)); // add car
    }

    public boolean bookCar(String orderId,String licensePlate,String fromDate,String tillDate){ // O(N)
        if(bookings.containsKey(orderId))return false; // unique orderId
        Car car=cars.get(licensePlate); // fetch car
        if(car==null)return false; // invalid car
        int fromDay=parseDay(fromDate); // parse start day
        int tillDay=parseDay(tillDate); // parse end day
        for(Booking b:car.bookings){ // check overlap
            if(overlaps(fromDay,tillDay,b.fromDay,b.tillDay))return false; // overlap fail
        }
        Booking booking=new Booking(orderId,fromDay,tillDay); // create booking
        car.bookings.add(booking); // store in car
        bookings.put(orderId,booking); // store in map
        return true; // success
    }

    public void startTrip(String orderId,int odometerReading){ // O(1)
        Booking booking=bookings.get(orderId); // fetch booking
        booking.startOdometer=odometerReading; // store start km
        booking.tripStarted=true; // mark started
    }

    public int endTrip(String orderId,int finalOdometer,String endDate){ // O(N)
        Booking booking=bookings.get(orderId); // fetch booking
        Car car=findCarByBooking(orderId); // fetch car
        int endDay=parseDay(endDate); // parse end day
        int effectiveEnd=Math.max(booking.tillDay,endDay); // effective end date
        int days=1+(effectiveEnd-booking.fromDay); // inclusive days
        int totalFreeKms=days*car.freeKmsPerDay; // total free kms
        int totalKms=finalOdometer-booking.startOdometer; // distance
        int extraKms=Math.max(0,totalKms-totalFreeKms); // extra kms
        int totalCost=(days*car.costPerDay)+(extraKms*car.costPerKm); // total cost
        booking.tillDay=endDay; // update booking to actual end
        return totalCost; // return cost
    }

    private boolean overlaps(int a,int b,int c,int d){ // inclusive overlap
        return a<=d && c<=b; // overlap rule
    }

    private int parseDay(String date){ // parse yyyy-mm-dd
        String[] parts=date.split("-"); // split
        return Integer.parseInt(parts[2]); // return day
    }

    private Car findCarByBooking(String orderId){ // find car
        for(Car car:cars.values()){ // iterate cars
            for(Booking b:car.bookings){
                if(b.orderId.equals(orderId))return car; // match
            }
        }
        return null; // fallback
    }
}
public class Main{
    public static void main(String[] args){
        CarRentalService service=new CarRentalService(); // create service
        service.addCar("KA01",1000,100,10); // add car
        service.bookCar("O1","KA01","2025-08-06","2025-08-12"); // book
        service.startTrip("O1",1000); // start trip
        int cost=service.endTrip("O1",1300,"2025-08-09"); // end early
        System.out.println(cost); // print cost
    }
}
