/**
 * Base class for all projectile weapons in the game.
 * This includes bullets, laser beams, and any future weapon types.
 */
public abstract class Projectile extends GameObject {
    /** The type of damage this projectile will deal */
    protected int damageValue = 1;

    /**
     * Creates a new projectile at the specified position.
     *
     * @param x Starting X coordinate
     * @param y Starting Y coordinate
     */
    public Projectile(double x, double y) {
        super(x, y);
    }

    /**
     * Gets the damage value of this projectile.
     * @return Number of damage points the projectile deals
     */
    public int getDamage() {
        return damageValue;
    }

    /**
     * Sets the damage value for this projectile.
     * @param value Number of damage points the projectile should deal
     */
    public void setDamage(int value) {
        this.damageValue = value;
    }
}
