package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import game.engine.base.Wall;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InsufficientResourcesException;
import game.engine.exceptions.InvalidLaneException;
import game.engine.lanes.Lane;
import game.engine.titans.Titan;
import game.engine.titans.TitanRegistry;
import game.engine.weapons.Weapon;
import game.engine.weapons.factory.FactoryResponse;
import game.engine.weapons.factory.WeaponFactory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;


public class Battle
{
	public static final int[][] PHASES_APPROACHING_TITANS =
	{
		{ 1, 1, 1, 2, 1, 3, 4 },
		{ 2, 2, 2, 1, 3, 3, 4 },
		{ 4, 4, 4, 4, 4, 4, 4 } 
	}; // order of the types of titans (codes) during each phase
	private static final int WALL_BASE_HEALTH = 10000;

	private IntegerProperty numberOfTurns;
    private IntegerProperty resourcesGathered;
    private ObjectProperty<BattlePhase> battlePhase;
	private int numberOfTitansPerTurn; // initially equals to 1
    private IntegerProperty score;
	private int titanSpawnDistance;
	private final WeaponFactory weaponFactory;
	private final HashMap<Integer, TitanRegistry> titansArchives;
	private final ArrayList<Titan> approachingTitans; // treated as a Queue
	private final PriorityQueue<Lane> lanes;
	private final ArrayList<Lane> originalLanes;  

	public Battle(int numberOfTurns, int score, int titanSpawnDistance, int initialNumOfLanes,
			int initialResourcesPerLane) throws IOException
	{
		super();
        this.numberOfTurns = new SimpleIntegerProperty(numberOfTurns);
        this.battlePhase = new SimpleObjectProperty<>(BattlePhase.EARLY); // replace DEFAULT_PHASE with the default phase
		this.numberOfTitansPerTurn = 1;
        this.score = new SimpleIntegerProperty(0);
		this.titanSpawnDistance = titanSpawnDistance;
        this.resourcesGathered = new SimpleIntegerProperty(initialResourcesPerLane * initialNumOfLanes);
		this.weaponFactory = new WeaponFactory();
		this.titansArchives = DataLoader.readTitanRegistry();
		this.approachingTitans = new ArrayList<Titan>();
		this.lanes = new PriorityQueue<>();
		this.originalLanes = new ArrayList<>();
		this.initializeLanes(initialNumOfLanes);
	}

	public int getNumberOfTurns() {
        return numberOfTurns.get();
    }

    public IntegerProperty numberOfTurnsProperty() {
        return numberOfTurns;
    }

    public void setNumberOfTurns(int numberOfTurns) {
        this.numberOfTurns.set(numberOfTurns);
    }

    public int getResourcesGathered() {
        return resourcesGathered.get();
    }

    public IntegerProperty resourcesGatheredProperty() {
        return resourcesGathered;
    }

    public void setResourcesGathered(int resourcesGathered) {
        this.resourcesGathered.set(resourcesGathered);
    }

	public BattlePhase getBattlePhase() {
        return battlePhase.get();
    }

	public ObjectProperty<BattlePhase> battlePhaseProperty() {
    return battlePhase;
	}
	
    public void setBattlePhase(BattlePhase battlePhase) {
        this.battlePhase.set(battlePhase);
    }

	public int getNumberOfTitansPerTurn()
	{
		return numberOfTitansPerTurn;
	}

	public void setNumberOfTitansPerTurn(int numberOfTitansPerTurn)
	{
		this.numberOfTitansPerTurn = numberOfTitansPerTurn;
	}

	public int getScore()
	{
		return score.get();
	}
	public IntegerProperty scoreProperty() {
        return score;
    }
	public void setScore(int score)
	{
		this.score.set(score);
	}

	public int getTitanSpawnDistance()
	{
		return titanSpawnDistance;
	}

	public void setTitanSpawnDistance(int titanSpawnDistance)
	{
		this.titanSpawnDistance = titanSpawnDistance;
	}

	public WeaponFactory getWeaponFactory()
	{
		return weaponFactory;
	}

	public HashMap<Integer, TitanRegistry> getTitansArchives()
	{
		return titansArchives;
	}

	public ArrayList<Titan> getApproachingTitans()
	{
		return approachingTitans;
	}

	public PriorityQueue<Lane> getLanes()
	{
		return lanes;
	}

	public ArrayList<Lane> getOriginalLanes()
	{
		return originalLanes;
	}

	private void initializeLanes(int numOfLanes)
	{
		for (int i = 0; i < numOfLanes; i++)
		{
			Wall w = new Wall(WALL_BASE_HEALTH);
			Lane l = new Lane(i,w);

			this.getOriginalLanes().add(l);
			this.getLanes().add(l);
		}
	}
	public void refillApproachingTitans() // spawns titans of the specified code to lanes based on the current phase
	{
		int[] phaseApproachingTitans;

		switch (this.getBattlePhase())
		{
		case EARLY:
			phaseApproachingTitans = PHASES_APPROACHING_TITANS[0];
			break;
		case INTENSE:
			phaseApproachingTitans = PHASES_APPROACHING_TITANS[1];
			break;
		case GRUMBLING:
			phaseApproachingTitans = PHASES_APPROACHING_TITANS[2];
			break;
		default:
			phaseApproachingTitans = new int[0];
		}

		for (int code : phaseApproachingTitans)
		{
			Titan spawnedTitan = this.getTitansArchives().get(code).spawnTitan(this.getTitanSpawnDistance());
			this.getApproachingTitans().add(spawnedTitan);
		}
	}

	public void purchaseWeapon(int weaponCode, Lane lane, int position) throws InsufficientResourcesException, InvalidLaneException {
		if (!this.getLanes().contains(lane)) {
			throw new InvalidLaneException("Weapon purchase failed");
		}
	
		FactoryResponse factoryResponse = this.getWeaponFactory().buyWeapon(getResourcesGathered(), weaponCode);
		Weapon purchasedWeapon = factoryResponse.getWeapon();
	
		if (purchasedWeapon != null) {
			lane.addWeapon(purchasedWeapon, position);
			this.setResourcesGathered(factoryResponse.getRemainingResources());
			//performTurn();
		}
	}

	public void passTurn()
	{
		System.out.println("passTurn method called");

		performTurn();
	}
	public Lane getLeastDangerousLane() {
		Lane leastDangerousLane = null;
		int lowestDangerLevel = Integer.MAX_VALUE;
	
		for (Lane lane : lanes) {
			int totalDangerLevel = 0;
			for (Titan titan : lane.getTitans()) {
				totalDangerLevel += titan.getDangerLevel();
			}
	
			if (totalDangerLevel < lowestDangerLevel) {
				lowestDangerLevel = totalDangerLevel;
				leastDangerousLane = lane;
			}
		}
	
		return leastDangerousLane;
	}
	public void addTurnTitansToLane()
	{
		if(this.getLanes().peek() != null) {
			Lane leastDangerLane = this.getLanes().poll();

			for (int i = 0; i < this.getNumberOfTitansPerTurn(); i++)
			{
				if (this.getApproachingTitans().isEmpty())
				{
					this.refillApproachingTitans();
				}

				leastDangerLane.addTitan(this.getApproachingTitans().remove(0));
			}

			this.getLanes().add(leastDangerLane);
		}
	}

	public void moveTitans()
	{
		System.out.println("Battle moveTitans method called");
		for (Lane l : this.getLanes())
		{
			l.moveLaneTitans();
		}
	}

	public int performWeaponsAttacks()
	{
		int resourcesGathered = 0;

		for (Lane l : this.getLanes())
		{
			resourcesGathered += l.performLaneWeaponsAttacks();
		}

		this.setResourcesGathered(this.getResourcesGathered() + resourcesGathered);
		System.out.println("Score increased by " + resourcesGathered + " due to weapons attacks");

		this.setScore(this.getScore() + resourcesGathered);

		return resourcesGathered;
	}

	public int performTitansAttacks()
	{
		int resourcesGathered = 0;
		ArrayList<Lane> lostLanes = new ArrayList<>();

		for (Lane l : this.getLanes())
		{
			resourcesGathered += l.performLaneTitansAttacks();
			if (l.isLaneLost())
			{
				lostLanes.add(l);
			}
		}

		this.getLanes().removeAll(lostLanes);

		return resourcesGathered;
	}

	private void updateLanesDangerLevels()
	{
		ArrayList<Lane> tmp = new ArrayList<>();
		
		while(!this.getLanes().isEmpty())
		{
			Lane l = this.getLanes().poll();
			l.updateLaneDangerLevel();
			tmp.add(l);
		}
		
		this.getLanes().addAll(tmp);
		
	}
	public void finalizeTurns()
	{
		this.setNumberOfTurns(this.getNumberOfTurns() + 1);

		if (this.getNumberOfTurns() == 15)
		{
			this.setBattlePhase(BattlePhase.INTENSE);
		} else if (this.getNumberOfTurns() == 30)
		{
			this.setBattlePhase(BattlePhase.GRUMBLING);
		} else if (this.getNumberOfTurns() > 30 && this.getNumberOfTurns() % 5 == 0)
		{
			this.setNumberOfTitansPerTurn(this.getNumberOfTitansPerTurn() * 2);
		}
	}

	private void performTurn()
	{
		this.moveTitans();
		this.performWeaponsAttacks();
		this.performTitansAttacks();

		this.addTurnTitansToLane();
		this.updateLanesDangerLevels();

		this.finalizeTurns();
	}

	public boolean isGameOver() // checks if all lanes are destroyed
	{
		return this.getLanes().size() == 0;
	}

}
