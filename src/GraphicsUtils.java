import java.awt.*;

/**
 * Utility class for common graphics operations and color manipulations.
 * Provides helper methods to reduce code duplication across rendering code.
 * 
 * <p>This class contains static methods for:
 * <ul>
 *   <li>Color manipulation with alpha transparency</li>
 *   <li>Color clamping to valid RGB ranges</li>
 *   <li>Composite/alpha operations</li>
 * </ul>
 * </p>
 * 
 * @author AIsteroids Development Team
 * @version 1.0
 */
public final class GraphicsUtils {
    
    private GraphicsUtils() {} // Prevent instantiation
    
    /**
     * Creates a color with the specified RGB values and alpha transparency.
     * Clamps all values to valid ranges (0-255).
     * 
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @param alpha Alpha component (0.0-1.0)
     * @return Color with the specified RGBA values
     */
    public static Color colorWithAlpha(int r, int g, int b, float alpha) {
        int alphaValue = (int)(255 * Math.max(0, Math.min(1, alpha)));
        return new Color(
            clampColorComponent(r),
            clampColorComponent(g),
            clampColorComponent(b),
            alphaValue
        );
    }
    
    /**
     * Creates a color with alpha transparency from an existing color.
     * 
     * @param color Base color
     * @param alpha Alpha component (0.0-1.0)
     * @return New color with the same RGB as base color but with specified alpha
     */
    public static Color colorWithAlpha(Color color, float alpha) {
        return colorWithAlpha(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    /**
     * Clamps a color component value to the valid range 0-255.
     * 
     * @param value Color component value
     * @return Clamped value between 0 and 255
     */
    public static int clampColorComponent(int value) {
        return Math.max(0, Math.min(255, value));
    }
    
    /**
     * Clamps a color component with a variation offset.
     * Useful for creating color variations while ensuring valid RGB range.
     * 
     * @param baseValue Base color component value
     * @param variation Variation to add (can be negative)
     * @return Clamped value between 0 and 255
     */
    public static int clampColorComponent(int baseValue, int variation) {
        return clampColorComponent(baseValue + variation);
    }
    
    /**
     * Creates an AlphaComposite with the specified transparency.
     * 
     * @param alpha Alpha value (0.0-1.0)
     * @return AlphaComposite instance
     */
    public static Composite createAlphaComposite(float alpha) {
        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
            Math.max(0, Math.min(1, alpha)));
    }
}

