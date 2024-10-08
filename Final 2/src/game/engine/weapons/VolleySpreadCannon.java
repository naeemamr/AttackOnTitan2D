package game.engine.weapons;

import java.util.ArrayList;
import java.util.PriorityQueue;

import game.engine.titans.Titan;

public class VolleySpreadCannon extends Weapon
{
	public static final int WEAPON_CODE = 2;

	private int minRange;
	private int maxRange;

	public VolleySpreadCannon(int baseDamage, int minRange, int maxRange)
	{
		super(baseDamage);
		this.minRange = minRange;
		this.maxRange = maxRange;
	}

	public int getMinRange()
	{
		return minRange;
	}

	public int getMaxRange()
	{
		return maxRange;
	}

	@Override
	public int turnAttack(PriorityQueue<Titan> laneTitans)
	{
		System.err.println("VolleySpreadCannon turnAttack");
		ArrayList<Titan> tmp = new ArrayList<>();
		int attackRes = 0;

		while (!laneTitans.isEmpty() && laneTitans.peek().getDistance() <= this.getMaxRange())
		{
			Titan nextTitan = laneTitans.poll();
			if (nextTitan.getDistance() >= getMinRange())
			{
				attackRes += this.attack(nextTitan);
			}

			if (!nextTitan.isDefeated())
			{
				tmp.add(nextTitan);
			}
		}

		laneTitans.addAll(tmp);

		return attackRes;
	}

	public void setMaxRange(int gridMaxRange) {
		this.maxRange = gridMaxRange;
	}

    public void setMinRange(int gridMinRange) {
		this.minRange = gridMinRange;
	}
}
