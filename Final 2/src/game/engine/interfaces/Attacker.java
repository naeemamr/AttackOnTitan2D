package game.engine.interfaces;

public interface Attacker
{
	int getDamage(); // gets the damage value to be applied

	default int attack(Attackee target)
	{
		int damage = getDamage();
		return target.takeDamage(getDamage());
	}

}
