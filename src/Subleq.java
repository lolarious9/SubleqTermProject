import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * subleq
 */
public class Subleq {

    public static void main(String[] args) {

        try (Scanner s = new Scanner(new File("test.slq"))) {
            SubleqParser parser = new SubleqParser();

            while (s.hasNextLine()) {
                String tmp = s.nextLine();
                if (tmp != "" && !parser.add(tmp)) {
                    System.out.println(tmp + " failed to parse ");
                }
            }

            parser.build("test");
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}