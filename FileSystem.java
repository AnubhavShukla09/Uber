import java.util.*; // import utility classes

class Directory{
    private String name; // directory name
    Directory parent; // parent directory reference
    TreeMap<String,Directory> children; // children stored in lexicographic order
    public Directory(String name){this.name=name;this.children=new TreeMap<>();} // constructor
    public String getName(){return name;} // return directory name
    public Directory getChild(String name){return children.get(name);} // fetch child by name
    public void addChild(Directory dir){dir.parent=this;children.put(dir.getName(),dir);} // add child directory
}
class FileSystem{
    private static volatile FileSystem instance; // singleton instance
    private Directory root; // root directory
    private Directory current; // current working directory
    private final Object lock=new Object(); // lock for thread safety
    private FileSystem(){
        this.root=new Directory("/"); // create root
        this.root.parent=root; // root parent points to itself
        this.current=root; // initialize current to root
    }
    public static FileSystem getInstance(){ // double checked locking singleton
        if(instance==null){
            synchronized(FileSystem.class){
                if(instance==null){
                    instance=new FileSystem();
                }
            }
        }
        return instance;
    }
    private String[] parsePath(String path){ // split path into components
        if(path==null||path.isEmpty())return new String[0]; // handle empty
        String[] parts=path.split("/"); // split by /
        List<String> result=new ArrayList<>(); // list to hold valid parts
        for(String part:parts){ // iterate parts
            if(!part.isEmpty())result.add(part); // ignore empty segments
        }
        return result.toArray(new String[0]); // convert to array
    }
    private Directory navigateTo(Directory start,String[] parts,boolean createIfNotExist){ // navigate helper
        Directory temp=start; // start traversal from given directory
        for(String part:parts){ // iterate path parts
            if(".".equals(part))continue; // stay in current
            if("..".equals(part)){temp=temp.parent;continue;} // go to parent
            if("*".equals(part)){ // wildcard case
                if(!temp.children.isEmpty())temp=temp.children.firstEntry().getValue(); // move to smallest child
                continue; // continue to next part
            }
            Directory next=temp.getChild(part); // get child directory
            if(next==null){ // if child does not exist
                if(createIfNotExist){ // if mkdir mode
                    Directory newDir=new Directory(part); // create new directory
                    temp.addChild(newDir); // add as child
                    temp=newDir; // move to new directory
                }else{
                    return null; // fail for cd if not exist
                }
            }else{
                temp=next; // move to existing directory
            }
        }
        return temp; // return final directory
    }
    public boolean mkdir(String path){ // O(depth)
        synchronized(lock){
            if(path==null||path.isEmpty())return false; // invalid path
            Directory start=path.startsWith("/")?root:current; // determine start
            String[] parts=parsePath(path); // parse path
            Directory result=navigateTo(start,parts,true); // navigate and create
            return result!=null; // return success
        }
    }
    public boolean cd(String path){ // O(depth)
        synchronized(lock){
            if(path==null||path.isEmpty())return false; // invalid path
            if("/".equals(path)){current=root;return true;} // go to root
            Directory start=path.startsWith("/")?root:current; // determine start
            String[] parts=parsePath(path); // parse path
            Directory result=navigateTo(start,parts,false); // navigate without create
            if(result==null)return false; // if invalid path
            current=result; // update current directory
            return true; // success
        }
    }
    public String pwd(){ // O(depth)
        synchronized(lock){
            if(current==root)return "/"; // root case
            Deque<String> stack=new ArrayDeque<>(); // stack to build path
            Directory temp=current; // start from current
            while(temp!=root){ // traverse up to root
                stack.push(temp.getName()); // push directory name
                temp=temp.parent; // move to parent
            }
            StringBuilder path=new StringBuilder("/"); // build absolute path
            while(!stack.isEmpty()){ // pop stack
                path.append(stack.pop()); // append name
                if(!stack.isEmpty())path.append("/"); // add separator
            }
            return path.toString(); // return path
        }
    }
}
public class Main{
    public static void main(String[] args){
        FileSystem fs=FileSystem.getInstance(); // get singleton instance
        fs.mkdir("/a/b/c"); // create nested directories
        fs.mkdir("/a/b/d"); // create another branch
        fs.cd("/a/b"); // change directory
        System.out.println(fs.pwd()); // print current path
        fs.cd("*"); // go to smallest lexicographic child (c)
        System.out.println(fs.pwd()); // print path
        fs.cd(".."); // go to parent
        fs.cd("d"); // go to d
        System.out.println(fs.pwd()); // print path
        fs.cd("/"); // go back to root
        System.out.println(fs.pwd()); // print root
    }
}
