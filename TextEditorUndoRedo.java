import java.util.*; // import utilities
interface Command{
    void execute(); // apply operation
    void undo(); // revert operation
}
class TextEditor{
    private List<StringBuilder> rows; // document rows
    private Stack<Command> undoStack; // undo history
    private Stack<Command> redoStack; // redo history
    public TextEditor(){
        this.rows=new ArrayList<>(); // initialize rows
        this.undoStack=new Stack<>(); // initialize undo
        this.redoStack=new Stack<>(); // initialize redo
    }
    public synchronized void addText(int row,int column,String text){ // O(N)
        if(row==rows.size())rows.add(new StringBuilder()); // append new row if needed
        AddCommand cmd=new AddCommand(row,column,text); // create command
        cmd.execute(); // execute operation
        undoStack.push(cmd); // push to undo
        redoStack.clear(); // clear redo history
    }
    public synchronized void deleteText(int row,int startColumn,int length){ // O(N)
        DeleteCommand cmd=new DeleteCommand(row,startColumn,length); // create command
        cmd.execute(); // execute operation
        undoStack.push(cmd); // push to undo
        redoStack.clear(); // clear redo history
    }
    public synchronized void undo(){ // O(N)
        if(undoStack.isEmpty())return; // no-op if empty
        Command cmd=undoStack.pop(); // get last command
        cmd.undo(); // revert
        redoStack.push(cmd); // push to redo
    }
    public synchronized void redo(){ // O(N)
        if(redoStack.isEmpty())return; // no-op
        Command cmd=redoStack.pop(); // get command
        cmd.execute(); // reapply
        undoStack.push(cmd); // push back to undo
    }
    public synchronized String readLine(int row){ // O(1)
        if(row<0||row>=rows.size())return ""; // invalid row
        return rows.get(row).toString(); // return content
    }
    private class AddCommand implements Command{
        int row; // target row
        int column; // insertion column
        String text; // text to insert
        public AddCommand(int row,int column,String text){
            this.row=row; // assign row
            this.column=column; // assign column
            this.text=text; // assign text
        }
        public void execute(){
            rows.get(row).insert(column,text); // insert text
        }
        public void undo(){
            rows.get(row).delete(column,column+text.length()); // remove inserted text
        }
    }
    private class DeleteCommand implements Command{
        int row; // target row
        int start; // start column
        int length; // delete length
        String deletedText; // store deleted text
        public DeleteCommand(int row,int start,int length){
            this.row=row; // assign row
            this.start=start; // assign start
            this.length=length; // assign length
        }
        public void execute(){
            StringBuilder sb=rows.get(row); // fetch row
            deletedText=sb.substring(start,start+length); // store deleted text
            sb.delete(start,start+length); // delete text
        }
        public void undo(){
            rows.get(row).insert(start,deletedText); // restore deleted text
        }
    }
}
public class Main{
    public static void main(String[] args){
        TextEditor editor=new TextEditor(); // create editor
        editor.addText(0,0,"Hello"); // add text
        editor.addText(0,5," World"); // append
        System.out.println(editor.readLine(0)); // print
        editor.deleteText(0,5,6); // delete
        System.out.println(editor.readLine(0)); // print
        editor.undo(); // undo delete
        System.out.println(editor.readLine(0)); // print
        editor.undo(); // undo add
        System.out.println(editor.readLine(0)); // print
        editor.redo(); // redo
        System.out.println(editor.readLine(0)); // print
    }
}
