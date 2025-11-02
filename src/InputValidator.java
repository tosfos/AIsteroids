/**
 * Utility class for input validation and error handling.
 * 
 * <p>Provides defensive programming tools to validate inputs and prevent crashes.
 * All methods throw {@link IllegalArgumentException} with descriptive messages
 * when validation fails.</p>
 * 
 * <p>This class contains:
 * <ul>
 *   <li>Range validation for numbers</li>
 *   <li>Null checking for objects</li>
 *   <li>Coordinate validation</li>
 *   <li>Safe execution wrappers for exception handling</li>
 *   <li>Domain-specific validators (delta time, asteroid size)</li>
 * </ul>
 * </p>
 * 
 * <p>All methods are static and the class cannot be instantiated.</p>
 * 
 * @author AIsteroids Development Team
 * @version 1.0
 */
public final class InputValidator {

    private InputValidator() {} // Prevent instantiation

    /**
     * Validates that a number is within a specified range.
     * @param value The value to validate
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @param paramName Name of the parameter for error messages
     * @return The validated value
     * @throws IllegalArgumentException if value is outside range
     */
    public static double validateRange(double value, double min, double max, String paramName) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(paramName + " cannot be NaN");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException(paramName + " cannot be infinite");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(paramName + " must be between " + min + " and " + max + ", got: " + value);
        }
        return value;
    }

    /**
     * Validates that an integer is within a specified range.
     * @param value The value to validate
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @param paramName Name of the parameter for error messages
     * @return The validated value
     * @throws IllegalArgumentException if value is outside range
     */
    public static int validateRange(int value, int min, int max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(paramName + " must be between " + min + " and " + max + ", got: " + value);
        }
        return value;
    }

    /**
     * Validates that a value is positive (> 0).
     * @param value The value to validate
     * @param paramName Name of the parameter for error messages
     * @return The validated value
     * @throws IllegalArgumentException if value is not positive
     */
    public static double validatePositive(double value, String paramName) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(paramName + " cannot be NaN");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException(paramName + " cannot be infinite");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " must be positive, got: " + value);
        }
        return value;
    }

    /**
     * Validates that a value is non-negative (>= 0).
     * @param value The value to validate
     * @param paramName Name of the parameter for error messages
     * @return The validated value
     * @throws IllegalArgumentException if value is negative
     */
    public static double validateNonNegative(double value, String paramName) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(paramName + " cannot be NaN");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException(paramName + " cannot be infinite");
        }
        if (value < 0) {
            throw new IllegalArgumentException(paramName + " must be non-negative, got: " + value);
        }
        return value;
    }

    /**
     * Validates that an object is not null.
     * @param obj The object to validate
     * @param paramName Name of the parameter for error messages
     * @return The validated object
     * @throws IllegalArgumentException if object is null
     */
    public static <T> T validateNotNull(T obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
        return obj;
    }

    /**
     * Validates screen coordinates are within game bounds.
     * @param x X coordinate
     * @param y Y coordinate
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public static void validateScreenCoordinates(double x, double y) {
        validateRange(x, -GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_WIDTH * 2, "x coordinate");
        validateRange(y, -GameConfig.SCREEN_HEIGHT, GameConfig.SCREEN_HEIGHT * 2, "y coordinate");
    }

    /**
     * Validates that a string is not null or empty.
     * @param str The string to validate
     * @param paramName Name of the parameter for error messages
     * @return The validated string
     * @throws IllegalArgumentException if string is null or empty
     */
    public static String validateNotEmpty(String str, String paramName) {
        validateNotNull(str, paramName);
        if (str.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be empty");
        }
        return str;
    }

    /**
     * Safely executes a potentially throwing operation, logging errors.
     * @param operation The operation to execute
     * @param errorMessage Message to log if operation fails
     * @return true if operation succeeded, false if it failed
     */
    public static boolean safeExecute(Runnable operation, String errorMessage) {
        try {
            operation.run();
            return true;
        } catch (Exception e) {
            System.err.println(errorMessage + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Safely executes a potentially throwing operation that returns a value.
     * @param operation The operation to execute
     * @param defaultValue Default value to return if operation fails
     * @param errorMessage Message to log if operation fails
     * @return Result of operation or default value if operation failed
     */
    public static <T> T safeExecute(java.util.concurrent.Callable<T> operation, T defaultValue, String errorMessage) {
        try {
            return operation.call();
        } catch (Exception e) {
            System.err.println(errorMessage + ": " + e.getMessage());
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Validates asteroid size parameter.
     * @param size The asteroid size to validate (1-3)
     * @return The validated size
     * @throws IllegalArgumentException if size is invalid
     */
    public static int validateAsteroidSize(int size) {
        return validateRange(size, 1, 3, "asteroid size");
    }

    /**
     * Validates delta time parameter for game updates.
     * @param deltaTime The delta time to validate
     * @return The validated delta time
     * @throws IllegalArgumentException if delta time is invalid
     */
    public static double validateDeltaTime(double deltaTime) {
        return validateRange(deltaTime, 0.0, 1.0, "deltaTime");
    }
}
