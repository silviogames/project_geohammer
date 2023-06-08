package me.silviogames.geha;

public enum MinerAttack
{
   // BASIC ATTACKS EVERY MINER DOES
   SIMPLE_HIT(5, 0),

   //  NOT IMPLEMENTED RIGHT NOW,
   //  it could later when gamepad input is more sophisticated be performed by holding the
   //  swing button and then cost one crystal. and then maybe some of the miners have other secondaries
   ROUND_SWING(50, 0),

   // STRUCTURAL
   IMPACT(0, 3),
   OROGENY(0, 3),
   TRANSFORM_FAULT(0, 3),

   // SEDIMENTOLOGY
   BRAIDED_RIVER(0, 3),
   EROSION(0, 3),
   GLACIER(0, 3),

   // MINERALOGY
   MELT(0, 2),

   GROWTH(0, 2),

   TSCHERMAK(0, 2),

   // GEOPHYSICS
   LANDSLIDE(0, 2),
   MAGNETISM(0, 2),
   RAYLEIGH(0, 2),
   ;

   public final int stamina_cost;
   // the crystal_cost right now is ignored since all attacks have the same
   public final int crystal_cost;

   MinerAttack(int sc, int cc)
   {
      this.stamina_cost = sc;
      this.crystal_cost = cc;
   }
}