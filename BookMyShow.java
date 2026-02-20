import java.util.*; // import utilities
enum SeatStatus{AVAILABLE,RESERVED,BOOKED} // define seat states
class Seat{
    private String seatId; // unique seat id
    private SeatStatus status; // current status
    public Seat(String seatId){this.seatId=seatId;this.status=SeatStatus.AVAILABLE;} // constructor
    public String getSeatId(){return seatId;} // return seat id
    public SeatStatus getStatus(){return status;} // return seat status
    public void reserve(){this.status=SeatStatus.RESERVED;} // mark as reserved
    public void book(){this.status=SeatStatus.BOOKED;} // mark as booked
    public void makeAvailable(){this.status=SeatStatus.AVAILABLE;} // reset seat
}
class Show{
    private String showId; // show identifier
    private long timestamp; // show start time epoch
    private List<Seat> seats; // seats for this show
    private Screen screen; // screen reference
    public Show(String showId,long timestamp,List<Seat> seats,Screen screen){
        this.showId=showId; // assign id
        this.timestamp=timestamp; // assign timestamp
        this.seats=seats; // assign seats
        this.screen=screen; // assign screen
    }
    public String getShowId(){return showId;} // return id
    public long getTimestamp(){return timestamp;} // return timestamp
    public List<Seat> getSeats(){return seats;} // return seats
    public Screen getScreen(){return screen;} // return screen
}
class Screen{
    private String screenId; // screen id
    private List<Show> shows; // list of shows
    public Screen(String screenId){this.screenId=screenId;this.shows=new ArrayList<>();} // constructor
    public void addShow(Show show){shows.add(show);} // add show to screen
    public String getScreenId(){return screenId;} // return screen id
    public List<Show> getShows(){return shows;} // return shows
}
class Reservation{
    private String reservationId; // reservation id
    private String userId; // user id
    private Set<String> seatIds; // seat ids
    public Reservation(String reservationId,String userId,Set<String> seatIds){
        this.reservationId=reservationId; // assign id
        this.userId=userId; // assign user
        this.seatIds=seatIds; // assign seats
    }
    public String getReservationId(){return reservationId;} // return id
    public Set<String> getSeatIds(){return seatIds;} // return seat ids
}
interface SeatAllocationStrategy{
    List<Seat> allocate(List<Seat> seats,int count); // allocation contract
}
class FirstAvailableStrategy implements SeatAllocationStrategy{
    public List<Seat> allocate(List<Seat> seats,int count){ // O(N)
        List<Seat> allocated=new ArrayList<>(); // store allocated seats
        for(Seat seat:seats){ // iterate seats
            if(seat.getStatus()==SeatStatus.AVAILABLE){ // check availability
                allocated.add(seat); // add seat
                if(allocated.size()==count)break; // stop if enough
            }
        }
        if(allocated.size()!=count)return null; // return null if insufficient
        return allocated; // return allocated seats
    }
}
class BookMyShow{
    private Show show; // show reference
    private Map<String,Reservation> reservations; // active reservations
    private SeatAllocationStrategy strategy; // seat strategy
    public BookMyShow(Show show){
        this.show=show; // assign show
        this.reservations=new HashMap<>(); // initialize map
        this.strategy=new FirstAvailableStrategy(); // set default strategy
    }
    public synchronized String reserve(String userId,int seatCount){ // O(N)
        List<Seat> allocated=strategy.allocate(show.getSeats(),seatCount); // allocate seats
        if(allocated==null)return "Not enough seats"; // insufficient seats
        Set<String> seatIds=new HashSet<>(); // track seat ids
        for(Seat seat:allocated){seat.reserve();seatIds.add(seat.getSeatId());} // mark seats reserved
        String reservationId=UUID.randomUUID().toString(); // generate id
        reservations.put(reservationId,new Reservation(reservationId,userId,seatIds)); // store reservation
        return reservationId; // return id
    }
    public synchronized void confirm(String reservationId){ // O(K)
        Reservation reservation=reservations.get(reservationId); // fetch reservation
        if(reservation==null){System.out.println("Invalid reservation");return;} // validation
        for(Seat seat:show.getSeats()){ // iterate seats
            if(reservation.getSeatIds().contains(seat.getSeatId()))seat.book(); // mark booked
        }
        System.out.println("Booking confirmed"); // confirmation
    }
    public synchronized void cancel(String reservationId){ // O(K)
        Reservation reservation=reservations.get(reservationId); // fetch reservation
        if(reservation==null){System.out.println("Invalid reservation");return;} // validation
        for(Seat seat:show.getSeats()){ // iterate seats
            if(reservation.getSeatIds().contains(seat.getSeatId()))seat.makeAvailable(); // reset seat
        }
        reservations.remove(reservationId); // remove reservation
        System.out.println("Reservation cancelled"); // confirmation
    }
}
public class Main{
    public static void main(String[] args){
        Screen screen1=new Screen("Screen1"); // create screen

        List<Seat> seats=new ArrayList<>(); // create seat list
        for(int i=1;i<=8;i++){seats.add(new Seat("S"+i));} // create seats

        Show show=new Show("Show1",System.currentTimeMillis(),seats,screen1); // create show with timestamp
        screen1.addShow(show); // attach show to screen

        BookMyShow service=new BookMyShow(show); // create service for show

        String r1=service.reserve("User1",3); // reserve seats
        System.out.println("ReservationId:"+r1); // print id
        service.confirm(r1); // confirm booking

        String r2=service.reserve("User2",2); // reserve more seats
        System.out.println("ReservationId:"+r2); // print id
        service.cancel(r2); // cancel booking
    }
}
