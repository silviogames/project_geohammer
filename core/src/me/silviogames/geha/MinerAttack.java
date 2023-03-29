package me.silviogames.geha;

public enum MinerAttack
{
	// BASIC ATTACKS EVERY MINER DOES
	SIMPLE_HIT(5, 0),

	// NOT IMPLEMENTED RIGHT NOW,
	// TODO: 29.03.23 it could later when gamepad input is more sophisticated be performed by holding the
	//  swing button and then cost one crystal. and then maybe some of the miners have other secondaries
	ROUND_SWING(50, 0),

	IMPACT(0, 3),
	OROGENY(0, 3),
	TRANSFORM_FAULT(0, 3),

	BRAIDED_RIVER(0, 3),
	ALLUVIAL_FAN(0, 3),
	;

	public final int stamina_cost;
	public final int crystal_cost;

	MinerAttack(int sc, int cc)
	{
		this.stamina_cost = sc;
		this.crystal_cost = cc;
	}
}