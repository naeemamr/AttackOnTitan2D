package game.engine.interfaces;

public interface Mobil
{
	int getDistance();

	void setDistance(int distance);

	int getSpeed();

	void setSpeed(int speed);

	default boolean hasReachedTarget() // returns true if arrived at the intended target
	{
		System.out.println("hasReachedTarget called, Distance is "+this.getDistance()); // Add this line

		return getDistance() <= 1;
	}

	default boolean move() // returns true if arrived at the intended target
{
    System.out.println("Move method called, Distance and speed are "+this.getDistance() + this.getSpeed()); // Add this line
    setDistance(getDistance() - getSpeed());
    System.out.println("New distance: " + getDistance()); // Add this line
    return hasReachedTarget();
}

}
