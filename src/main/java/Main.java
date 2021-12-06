import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String measuringType = args[0];
        switch (measuringType) {
            case "time":
                MainTimeMeasures.main(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "access":
                MainMonitorAccesses.main(Arrays.copyOfRange(args, 1, args.length));
                break;
        }
    }

}
