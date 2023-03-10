package me.silviogames.geha;

public enum MinerAttack
{
	SIMPLE_HIT(5, 0),
	ROUND_SWING(50, 0),

	IMPACT(0, 3),
	OROGENY(0, 3),
	TRANSFORM_FAULT(0, 5),
	;

	public final int stamina_cost;
	public final int crystal_cost;

	MinerAttack(int sc, int cc)
	{
		this.stamina_cost = sc;
		this.crystal_cost = cc;
	}
}