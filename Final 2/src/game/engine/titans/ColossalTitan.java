package game.engine.titans;

public class ColossalTitan extends Titan
{
	public static final int TITAN_CODE = 4;

	public ColossalTitan(int baseHealth, int baseDamage, int heightInMeters, int distanceFromBase, int speed,
			int resourcesValue, int dangerLevel)
	{
		super(baseHealth, baseDamage, heightInMeters, distanceFromBase, speed, resourcesValue, dangerLevel);
	}
	@Override
public boolean move() {
    // Get the current distance from the wall
    int currentDistance = this.getDistance();

    // If the Titan is at distance 4 or less, set the distance to 1 and set hasReachedTarget to true
    if (currentDistance <= 4) {
        this.setDistance(1);
        return false; // Return false to indicate that the Titan has stopped moving
    }

    boolean moveResult = super.move();

    // Only increase the speed if the Titan is not at distance 4 or less
    if (currentDistance > 4) {
        this.setSpeed(this.getSpeed() + 1);
    }

    return moveResult;
}
}
