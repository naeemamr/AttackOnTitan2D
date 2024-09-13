package game.engine.lanes;

import java.util.ArrayList;
import java.util.PriorityQueue;

import game.engine.base.Wall;
import game.engine.titans.Titan;
import game.engine.weapons.Weapon;

public class Lane implements Comparable<Lane>
{
	private final Wall laneWall;
	private int dangerLevel;
	private final PriorityQueue<Titan> titans;
	private final ArrayList<Weapon> weapons;
	private int row;


	public Lane(int row, Wall laneWall)
	{
		super();
		this.laneWall = laneWall;
		this.dangerLevel = 0;
		this.titans = new PriorityQueue<>();
		this.weapons = new ArrayList<>();
		this.row = row;

	}

	public Wall getLaneWall()
	{
		return this.laneWall;
	}
	public int getRow() {
        return row;
    }

	public int getDangerLevel()
	{
		return this.dangerLevel;
	}

	public void setDangerLevel(int dangerLevel)
	{
		this.dangerLevel = dangerLevel;
	}

	public PriorityQueue<Titan> getTitans()
	{
		return this.titans;
	}

	public ArrayList<Weapon> getWeapons()
	{
		return this.weapons;
	}

	@Override
	public int compareTo(Lane o)
	{
		return this.dangerLevel - o.dangerLevel;
	}

	public void addTitan(Titan titan)
	{
		this.getTitans().add(titan);
	}

	public void addWeapon(Weapon weapon, int position) {
		// Ensure the weapons list is large enough to hold the weapon at the specified position
		while (this.getWeapons().size() <= position) {
			this.getWeapons().add(null);
		}
	
		// Add the weapon at the specified position
		this.getWeapons().set(position, weapon);
	}

	public void moveLaneTitans() {
	System.out.println("Moving " + this); // Add this line

    ArrayList<Titan> tmp = new ArrayList<>();
    ArrayList<Titan> movedTitans = new ArrayList<>();

    for (Titan t : this.getTitans())
    {
        if (!t.hasReachedTarget())
        {
            t.move();
            tmp.add(t);
            movedTitans.add(t);
        }
    }
    
    this.getTitans().removeAll(tmp);
    this.getTitans().addAll(tmp);

}

	public int performLaneTitansAttacks()
	{
		int resourcesGathered = 0;

		for (Titan t : this.getTitans())
		{
			if (t.hasReachedTarget())
			{
				resourcesGathered += t.attack(this.getLaneWall());
			}
		}

		return resourcesGathered;
	}

	public int performLaneWeaponsAttacks() {
		int resourcesGathered = 0;
	
		for (Weapon w : this.getWeapons()) {
			if (w != null) {
				int weaponDamage = w.turnAttack(this.getTitans());
				System.out.println("Damage from weapon: " + weaponDamage);
				resourcesGathered += weaponDamage;
			}
		}
	
		System.out.println("Resources gathered from lane: " + resourcesGathered);
		return resourcesGathered;
	}
	public boolean isLaneLost()
	{
		return this.getLaneWall().isDefeated();
	}

	public void updateLaneDangerLevel()
	{
		int newDanger = 0;

		for (Titan t : this.getTitans())
		{
			newDanger += t.getDangerLevel();
		}

		this.setDangerLevel(newDanger);
	}

}
