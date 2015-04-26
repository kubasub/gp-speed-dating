package ec.select;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeSelector;
import ec.gp.GPTree;
import static ec.gp.koza.CrossoverPipeline.NO_SIZE_LIMIT;
import ec.gp.koza.KozaNodeSelector;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleProblemForm;

public class CrossoverDate implements Date {

  @Override
  public double match( Individual first, Individual second, EvolutionState state ) {
    double offspringFitness = this.produce( first, second, state, 0 );

    return 1.0 - offspringFitness;
  }

  public double produce( Individual first, Individual second,
          final EvolutionState state,
          final int thread ) {
    // how many individuals should we make?
    int n = 1;
    GPInitializer initializer = ( ( GPInitializer ) state.initializer );

    GPIndividual[] parents = new GPIndividual[ 2 ];
    parents[0] = ( GPIndividual ) first;
    parents[1] = ( GPIndividual ) second;
    for ( int q = 0; q < n; /* no increment */ ) // keep on going until we're filled up
    {
      // at this point, parents[] contains our two selected individuals
      int t1 = 0;
      int t2 = 0;
      do // pick random trees  -- their GPTreeConstraints must be the same
      {
        if ( parents[0].trees.length > 1 ) {
          t1 = state.random[thread].nextInt( parents[0].trees.length );
        }
        else {
          t1 = 0;
        }

        if ( parents[1].trees.length > 1 ) {
          t2 = state.random[thread].nextInt( parents[1].trees.length );
        }
        else {
          t2 = 0;
        }
      } while ( parents[0].trees[t1].constraints( initializer ) != parents[1].trees[t2].constraints( initializer ) );

      // validity results...
      boolean res1 = false;
      boolean res2 = false;

      // How the pipeline selects a node from the individuals
      GPNodeSelector nodeselect1 = new KozaNodeSelector();
      GPNodeSelector nodeselect2 = new KozaNodeSelector();

      // prepare the nodeselectors
      nodeselect1.reset();
      nodeselect2.reset();

      // pick some nodes
      GPNode p1 = null;
      GPNode p2 = null;

      // TODO use params
      int numTries = 1;
      boolean tossSecondParent = false;
      for ( int x = 0; x < numTries; x++ ) {
        // pick a node in individual 1
        p1 = nodeselect1.pickNode( state, 0, thread, parents[0], parents[0].trees[t1] );

        // pick a node in individual 2
        p2 = nodeselect2.pickNode( state, 0, thread, parents[1], parents[1].trees[t2] );

        // check for depth and swap-compatibility limits
        res1 = verifyPoints( initializer, p2, p1 );  // p2 can fill p1's spot -- order is important!
        if ( n - q < 2 || tossSecondParent ) {
          res2 = true;
        }
        else {
          res2 = verifyPoints( initializer, p1, p2 );  // p1 can fill p2's spot -- order is important!
        }
        // did we get something that had both nodes verified?
        // we reject if EITHER of them is invalid.  This is what lil-gp does.
        // Koza only has numTries set to 1, so it's compatible as well.
        if ( res1 && res2 ) {
          break;
        }
      }

      GPIndividual j1 = ( GPIndividual ) ( parents[0].lightClone() );
      GPIndividual j2 = null;
      if ( n - q >= 2 && !tossSecondParent ) {
        j2 = ( GPIndividual ) ( parents[1].lightClone() );
      }

      // Fill in various tree information that didn't get filled in there
      j1.trees = new GPTree[ parents[0].trees.length ];
      if ( n - q >= 2 && !tossSecondParent ) {
        j2.trees = new GPTree[ parents[1].trees.length ];
      }

      // at this point, p1 or p2, or both, may be null.
      // If not, swap one in.  Else just copy the parent.
      for ( int x = 0; x < j1.trees.length; x++ ) {
        if ( x == t1 && res1 ) // we've got a tree with a kicking cross position!
        {
          j1.trees[x] = ( GPTree ) ( parents[0].trees[x].lightClone() );
          j1.trees[x].owner = j1;
          j1.trees[x].child = parents[0].trees[x].child.cloneReplacing( p2, p1 );
          j1.trees[x].child.parent = j1.trees[x];
          j1.trees[x].child.argposition = 0;
          j1.evaluated = false;
        }  // it's changed
        else {
          j1.trees[x] = ( GPTree ) ( parents[0].trees[x].lightClone() );
          j1.trees[x].owner = j1;
          j1.trees[x].child = ( GPNode ) ( parents[0].trees[x].child.clone() );
          j1.trees[x].child.parent = j1.trees[x];
          j1.trees[x].child.argposition = 0;
        }
      }

      if ( n - q >= 2 && !tossSecondParent ) {
        for ( int x = 0; x < j2.trees.length; x++ ) {
          if ( x == t2 && res2 ) // we've got a tree with a kicking cross position!
          {
            j2.trees[x] = ( GPTree ) ( parents[1].trees[x].lightClone() );
            j2.trees[x].owner = j2;
            j2.trees[x].child = parents[1].trees[x].child.cloneReplacing( p1, p2 );
            j2.trees[x].child.parent = j2.trees[x];
            j2.trees[x].child.argposition = 0;
            j2.evaluated = false;
          } // it's changed
          else {
            j2.trees[x] = ( GPTree ) ( parents[1].trees[x].lightClone() );
            j2.trees[x].owner = j2;
            j2.trees[x].child = ( GPNode ) ( parents[1].trees[x].child.clone() );
            j2.trees[x].child.parent = j2.trees[x];
            j2.trees[x].child.argposition = 0;
          }
        }
      }

      SimpleProblemForm p = ( SimpleProblemForm ) SimpleEvaluator.instance.p_problem;
      ( ( ec.Problem ) SimpleEvaluator.instance.p_problem ).prepareToEvaluate( state, thread );
      p.evaluate( state, j1, 0, thread );
      return j1.fitness.fitness();
    }

    return n;
  }

  public boolean verifyPoints( final GPInitializer initializer,
          final GPNode inner1, final GPNode inner2 ) {
        // first check to see if inner1 is swap-compatible with inner2
    // on a type basis

    //TODO: replace with params
    int maxDepth = 17;
    int maxSize = -1;
    if ( !inner1.swapCompatibleWith( initializer, inner2 ) ) {
      return false;
    }

    // next check to see if inner1 can fit in inner2's spot
    if ( inner1.depth() + inner2.atDepth() > maxDepth ) {
      return false;
    }

    // check for size
    // NOTE: this is done twice, which is more costly than it should be.  But
    // on the other hand it allows us to toss a child without testing both times
    // and it's simpler to have it all here in the verifyPoints code.  
    if ( maxSize != NO_SIZE_LIMIT ) {
      // first easy check
      int inner1size = inner1.numNodes( GPNode.NODESEARCH_ALL );
      int inner2size = inner2.numNodes( GPNode.NODESEARCH_ALL );
      if ( inner1size > inner2size ) // need to test further
      {
        // let's keep on going for the more complex test
        GPNode root2 = ( ( GPTree ) ( inner2.rootParent() ) ).child;
        int root2size = root2.numNodes( GPNode.NODESEARCH_ALL );
        if ( root2size - inner2size + inner1size > maxSize ) // take root2, remove inner2 and swap in inner1.  Is it still small enough?
        {
          return false;
        }
      }
    }

    // checks done!
    return true;
  }
}
