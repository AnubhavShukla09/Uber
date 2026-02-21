import java.util.*; // import utility classes
class User{
    String userId; // user identifier
    Set<String> playerIds; // players in team
    int score; // current team score
    public User(String userId,Set<String> playerIds,int score){this.userId=userId;this.playerIds=playerIds;this.score=score;} // constructor
}
class Leaderboard{
    private Map<String,Integer> playerScores; // map of player to cumulative score
    private Map<String,User> users; // map of userId to User
    private Map<String,Set<String>> playerToUsers; // reverse mapping player -> users
    private TreeSet<User> ranking; // sorted leaderboard
    public Leaderboard(){
        this.playerScores=new HashMap<>(); // initialize player score map
        this.users=new HashMap<>(); // initialize user map
        this.playerToUsers=new HashMap<>(); // initialize reverse map
        this.ranking=new TreeSet<>((a,b)->{ // custom comparator
            if(a.score!=b.score)return Integer.compare(b.score,a.score); // score descending
            return a.userId.compareTo(b.userId); // tie-break lex ascending
        });
    }
    public synchronized void addUser(String userId,List<String> playerIds){ // O(P log N)
        if(users.containsKey(userId))return; // ignore duplicate user
        Set<String> team=new HashSet<>(playerIds); // create team set
        int totalScore=0; // initialize team score
        for(String playerId:team){ // iterate players
            int playerScore=playerScores.getOrDefault(playerId,0); // fetch existing score
            totalScore+=playerScore; // accumulate
            playerToUsers.putIfAbsent(playerId,new HashSet<>()); // ensure reverse map exists
            playerToUsers.get(playerId).add(userId); // map player to this user
        }
        User user=new User(userId,team,totalScore); // create user
        users.put(userId,user); // store user
        ranking.add(user); // insert into leaderboard
    }
    public synchronized void addScore(String playerId,int delta){ // O(U log N)
        int newScore=playerScores.getOrDefault(playerId,0)+delta; // update player score
        playerScores.put(playerId,newScore); // store updated score
        Set<String> affectedUsers=playerToUsers.getOrDefault(playerId,Collections.emptySet()); // get affected users
        for(String userId:affectedUsers){ // update each affected user
            User user=users.get(userId); // fetch user
            ranking.remove(user); // remove before modifying score
            user.score+=delta; // update team score
            ranking.add(user); // reinsert to maintain ordering
        }
    }
    public synchronized List<String> getTopK(int k){ // O(K)
        List<String> result=new ArrayList<>(); // result list
        int count=0; // counter
        for(User user:ranking){ // iterate sorted leaderboard
            if(count==k)break; // stop at k
            result.add(user.userId); // add userId
            count++; // increment counter
        }
        return result; // return result
    }
}
public class Main{
    public static void main(String[] args){
        Leaderboard lb=new Leaderboard(); // create leaderboard
        lb.addUser("Alice",Arrays.asList("P1","P2")); // add user Alice
        lb.addUser("Bob",Arrays.asList("P2","P3")); // add user Bob
        lb.addUser("Charlie",Arrays.asList("P3")); // add user Charlie
        lb.addScore("P1",10); // update player P1
        lb.addScore("P2",5); // update player P2
        lb.addScore("P3",20); // update player P3
        System.out.println(lb.getTopK(3)); // print top 3 users
        lb.addScore("P2",-3); // negative update
        System.out.println(lb.getTopK(2)); // print top 2 users
    }
}
