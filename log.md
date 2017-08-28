With exclsens:

Enormous EatBonus, because the exponential decay require it to extend life time by any significant amount... Also slower enery decay, and higher fightE, SpeedE
java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 100000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .02 ENERGYDECAY .998 WSIZE 2005 > tmp28.txt
Now some clear divergences in pedigrees... mostly between some apparent 'immortals' with very short pedigrees and everyone else...
r classes of individuals: sessile individuals with no motion or aggro, and highly-mobile, high-aggro individuals. Not clear if the immortals are former or latter.

Yes, the immortals are the high-mobility, high-aggro.
Massive lotkerra-volta cycles? "immortals" at one point majority, crash to small minority, the rest of the population re-expands...

Possibility that 'sens-other' guided predators do emerge, but are so efficient that they basically almost-destroy the population - and themselves?...

Eventually the strong diversity dies out and all lineages converge (at least to 30 generations). But still some diversity...

 Big swings in population size, by factor of 5 or more..
 
Diversity is re-created after homogenization - the high-aggro, high-mobility phenotype is so useful it re-appears, and diversity resurfaces... then population crashes again!

"Wild Evolution"

exclsens:
java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 100000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .02 ENERGYDECAY .998 WSIZE 2005 > tmp28.txt leads to durable, self-recreating diversity. With exclsense!

The final, long-term situation is mostly sessile individuals, with a few
looping, high-aggro individuals (with or without 'hiccups'). Why do they not
use sesnor-other to just zero-in on the sessile ones? There seems to be a lot
of sensor-others, though always in the minority - are loopers sensor-other?...

Without exclsens, java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 2000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .01 ENERGYDECAY .998 WSIZE 2005 > out.txt leads to convergence to single type (though with some complexity?)

Trying java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 100000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .02 ENERGYDECAY .998 WSIZE 2005 >  out.txt (same that workd to produce diversity with exclsens...) devolves into all-sessile!
Hmm, so the excl-sens was crucial in variety?...


NOTE: in a separate experiment (close), setting Eatbonu to zero, there were still a lot of aggro!... Benefits of aggro even without eating? As defense against unavoidable stray aggro, or as "spite"?...

==

I can get phenotypic diversity, despite lineages coalescing within ~20 lifetimes...

After a while (pedigrees reach length 800+) I get coalescence back to 50 lifetimes for java World WSIZE 2500 FIGHTENERGY .03 FIGHTDAMAGE 4.0 EATBONUS 100000.0 ENERGYDECAY .995 DISTANCEENERGY 0.0 VISUAL 1 > out3.txt

However, java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 100000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .02 ENERGYDECAY .995 WSIZE 3005 MEANADDEDFOODPERSTEP 7 > out6.txt gets persistent diversity, with one lineage coalescing 150+ lifetimes! 
And several more coalescing within 40-80 lifetimes...
But the diversity is not easy to see - seems mostly sessile, with apparently a few high-aggro high-speed darting through, rarely... Note that only a small portion of the space can be seen... but still...
Need to be able to save the population, and show half-sized workld for visual!

== 

(With valid-neur mask...)

java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 100000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .003 ENERGYDECAY .995 WSIZE 3000 NEURENERGY 1e-6 FOODENERGY .6 

Produces durable diversity in both pedigrees and visually....

Apparently boring sessile/hunters, but ACTUALLY some of the "sessiles" turn out to become mobile + high-aggro when a hunter touches them!

== 

Fixed some bugs (fighting could result in positive energy increase to the other if the diff. between fight values was negative... Also some stray validneur statements).

Also simplified the update loop... and randomizedupdate order!

== 

Massive bug fixing!

java World VISUAL 1 SEED 5 FIGHTENERGY .03 EATBONUS 100000.0 FIGHTDAMAGE 4.0 SPEEDENERGY .003 ENERGYDECAY .995 WSIZE 3000 NEURENERGY 1e-6 FOODENERGY .6 
Mostly similar : highly eficient food catchers, with non trivial, but rare aggro behaviors. When too close to another, they do a sharp move with aggo++, not clear if defensive of aggressive (or both?)
Apparently some are aversive, and others hunting, during their close-by encounters!? B but all are mostly food-catchers...
There is some diversity (60 lifetimes back, in a 800 lifetimes pedigree) but it's not clearly visible in phenotype: they all look very similar in behavior

With NEURENERGY 2e-7, much more diversity in phenotypes (clearly high-aggro and low-aggro behaviors, though not clear if it's not different phases of the same individuals!) But genetic diversity is similar, ~60 lifetimes! Also population somewhat smaller(??)

Note: the 1e-7 creates STRONG SPECIATION (after a lot of time, around 2 days,
1100 savings, so ~1100 * 10K timesteps) ! Hunters vs herbivores, with massive,
durable split in the pedigrees over hundreds of ancestors. HOWEVER, it seems
unstable - the number of hunters (and perhaps herbivores) oscillates, and
occasionally reaches just 1 !
Note that the hunters seem to live *much* longer than the herbivores. 

Also note that the hunters are not immediately obvious - only when they get near a prey do they attack!

