import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_44721a07f5cd2ed8eceaf49e95c8163aac29cdce {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAFdwQAAAAFc3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAKCgBzcQB+AAIAAAAAcQB+AAVxAH4ABXEAfgAFeA==");

    public static void main(String[] args) throws Throwable {
        Crash_44721a07f5cd2ed8eceaf49e95c8163aac29cdce.class.getClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = fuzzer.fuzzmodel.FuzzCercleStandalone.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = fuzzer.fuzzmodel.FuzzCercleStandalone.class.getMethod("fuzzerInitialize", String[].class);
                fuzzerInitialize.invoke(null, (Object) args);
            } catch (NoSuchMethodException ignored1) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
        com.code_intelligence.jazzer.api.CannedFuzzedDataProvider input = new com.code_intelligence.jazzer.api.CannedFuzzedDataProvider(base64Bytes);
        fuzzer.fuzzmodel.FuzzCercleStandalone.fuzzerTestOneInput(input);
    }
}