public class LookAtTestLookup45 {
    public static void main(String[] args) {
        System.out.println("Wait... what if we just use the `boundsInParent.getMinY()` to sort them?");
        System.out.println("And what if `index = getIndexMethod.invoke(cell)` THROWS AN EXCEPTION?");
        System.out.println("Wait... does `invoke` ALWAYS throw an exception if the package is internal?");
        System.out.println("Yes, unless we `--add-exports` or `--add-opens` at runtime!");
        System.out.println("In the Maven configuration, are we adding exports?");
        System.out.println("Let's look at `pom.xml`!");
    }
}
