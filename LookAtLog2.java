public class LookAtLog2 {
    public static void main(String[] args) {
        System.out.println("Wait, getSegmentCount() DOES exist. So it compiled.");
        System.out.println("So if the line vanished entirely, it must be because `indentCaret` or `openCaret` or `closeCaret` is null.");
        System.out.println("Why would it be null? Because `caretInfo` returned null, or `getSegmentCount()` returned 0, or `TextCell` wasn't found.");
        System.out.println("Why wouldn't `TextCell` be found? `editorCodeArea.lookupAll(\".content > *\")`.");
        System.out.println("Maybe `TextCell` nodes are not direct children of `.content` anymore?");
        System.out.println("Let's look at `TestLookup4.java` that dumps the structure.");
    }
}
