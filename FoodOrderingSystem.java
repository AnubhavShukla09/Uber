import java.util.*; // import utility classes

class RatingAggregate{
    int total; // total rating sum
    int count; // rating count
    public void addRating(int rating){total+=rating;count++;} // add rating
    public double getAverage(){if(count==0)return 0.0;return (double)total/count;} // average
}

class Restaurant{
    String restaurantId; // id
    RatingAggregate overallRating; // overall rating
    Map<String,RatingAggregate> foodRatings; // food specific ratings
    public Restaurant(String id){
        this.restaurantId=id; // assign id
        this.overallRating=new RatingAggregate(); // init overall
        this.foodRatings=new HashMap<>(); // init food ratings
    }
}

class Order{
    String orderId; // id
    String restaurantId; // restaurant
    String foodItemId; // food
    boolean rated; // rated flag
    public Order(String orderId,String restaurantId,String foodItemId){
        this.orderId=orderId; // assign id
        this.restaurantId=restaurantId; // assign restaurant
        this.foodItemId=foodItemId; // assign food
        this.rated=false; // initially not rated
    }
}

class FoodDeliverySystem{
    private Map<String,Restaurant> restaurants; // restaurant map
    private Map<String,Order> orders; // order map
    private Map<String,TreeSet<Restaurant>> foodRanking; // per food ranking
    private TreeSet<Restaurant> overallRanking; // overall ranking

    public FoodDeliverySystem(){
        this.restaurants=new HashMap<>(); // init restaurant map
        this.orders=new HashMap<>(); // init order map
        this.foodRanking=new HashMap<>(); // init food ranking map
        this.overallRanking=new TreeSet<>((a,b)->{ // comparator
            double avgA=a.overallRating.getAverage(); // avg A
            double avgB=b.overallRating.getAverage(); // avg B
            if(avgA!=avgB)return Double.compare(avgB,avgA); // desc
            return a.restaurantId.compareTo(b.restaurantId); // lex tie
        });
    }

    public synchronized void orderFood(String orderId,String restaurantId,String foodItemId){ // O(log R)
        restaurants.putIfAbsent(restaurantId,new Restaurant(restaurantId)); // ensure restaurant
        Restaurant r=restaurants.get(restaurantId); // fetch restaurant
        overallRanking.add(r); // ensure present in ranking
        foodRanking.putIfAbsent(foodItemId,new TreeSet<>((a,b)->{ // ensure food ranking
            double avgA=a.foodRatings.containsKey(foodItemId)?a.foodRatings.get(foodItemId).getAverage():0.0; // avg A
            double avgB=b.foodRatings.containsKey(foodItemId)?b.foodRatings.get(foodItemId).getAverage():0.0; // avg B
            if(avgA!=avgB)return Double.compare(avgB,avgA); // desc
            return a.restaurantId.compareTo(b.restaurantId); // lex
        }));
        foodRanking.get(foodItemId).add(r); // add restaurant to food ranking
        orders.put(orderId,new Order(orderId,restaurantId,foodItemId)); // store order
    }

    public synchronized void rateOrder(String orderId,int rating){ // O(log R)
        Order order=orders.get(orderId); // fetch order
        if(order==null||order.rated)return; // ignore invalid
        Restaurant r=restaurants.get(order.restaurantId); // fetch restaurant

        overallRanking.remove(r); // remove before update
        TreeSet<Restaurant> foodSet=foodRanking.get(order.foodItemId); // fetch food set
        if(foodSet!=null)foodSet.remove(r); // remove from food ranking

        r.overallRating.addRating(rating); // update overall
        r.foodRatings.putIfAbsent(order.foodItemId,new RatingAggregate()); // ensure food rating
        r.foodRatings.get(order.foodItemId).addRating(rating); // update food rating

        overallRanking.add(r); // reinsert
        if(foodSet!=null)foodSet.add(r); // reinsert into food ranking

        order.rated=true; // mark rated
    }

    public synchronized List<String> getTopRatedRestaurants(){ // O(K)
        List<String> result=new ArrayList<>(); // result
        int count=0; // counter
        for(Restaurant r:overallRanking){ // iterate sorted
            if(count==20)break; // limit
            result.add(r.restaurantId); // add id
            count++; // inc
        }
        return result; // return
    }

    public synchronized List<String> getTopRestaurantsByFood(String foodItemId){ // O(K)
        List<String> result=new ArrayList<>(); // result
        TreeSet<Restaurant> set=foodRanking.get(foodItemId); // get ranking
        if(set==null)return result; // empty
        int count=0; // counter
        for(Restaurant r:set){ // iterate sorted
            if(count==20)break; // limit
            result.add(r.restaurantId); // add id
            count++; // inc
        }
        return result; // return
    }
}

public class Main{
    public static void main(String[] args){
        FoodDeliverySystem system=new FoodDeliverySystem(); // create system
        system.orderFood("O1","R1","Burger"); // order
        system.orderFood("O2","R2","Burger"); // order
        system.orderFood("O3","R1","Pizza"); // order
        system.rateOrder("O1",5); // rate
        system.rateOrder("O2",4); // rate
        system.rateOrder("O3",3); // rate
        System.out.println(system.getTopRestaurantsByFood("Burger")); // top burger
        System.out.println(system.getTopRatedRestaurants()); // top overall
    }
}
