# MYRA

MYRA is a collection of Ant Colony Optimization (ACO) algorithms for the data mining classification task. It includes popular rule induction and decision tree induction algorithms. The algorithms are ready to be used from the command line or can be easily called from your own Java code. They are implemented using a modular architecture, so they can be easily extended to incorporate different procedures and/or use different parameter values.

This repository contains a complete rewrite of the code (by the same author) from the MYRA project hosted at [sourceforge](http://sourceforge.net/projects/myra/). The computational time has been significantly improved &mdash; tasks that used to take minutes, now are done in seconds &mdash; although it was not possible to maintain backward compatibility. You will find that the overall architecture is very similar, but most of the data structures have changed.

While this repository is a fresh start, the versioning is maintained &mdash; version `4.x` is the new version of the refactored code. If you are interested in the hierarchical multi-label algorithms (`3.x` versions), check the [sourceforge](http://sourceforge.net/projects/myra/) repository. These algorithms will eventually be refactored into this repository.

###### Latest Release

* [MYRA 4.5 (jar file)](https://sourceforge.net/projects/myra/files/myra/4.5/myra-4.5.jar/download)

### Algorithms

The following algorithms are implemented:

##### Ant-Miner
```
Main class: myra.rule.irl.AntMiner
```

The first rule induction ACO classification algorithm. Ant-Miner uses a sequentical covering strategy combined with an ACO search to create a list of rules. Ant-Miner only supports categorical attributes, continuous attributes need to be discretised in a pre-processing step.

##### *c*Ant-Miner
```
Main class: myra.rule.irl.cAntMiner
```

An extension of Ant-Miner to cope with continuous attributes. It works essentially the same as Ant-Miner, but continuous attributes undergo a dynamic discretisation process when selected to be included in a rule.

##### *c*Ant-Miner<sub>PB</sub>
```
Main class: myra.rule.pittsburgh.cAntMinerPB
```

*c*Ant-Miner<sub>PB</sub> incorporates  a new strategy to discover a list of classification rules, which guides the search performed by the ACO algorithm using the quality of a candidate list of rules, instead of a single rule. The main motivation is to avoid the problem of rule interaction derived from the order in which the rules are discovered &mdash; i.e., the outcome of a rule affects the rules that can be discovered subsequently since the search space is modified due to the removal of examples covered by previous rules.

##### Unordered *c*Ant-Miner<sub>PB</sub>
```
Main class: myra.rule.pittsburgh.unordered.UcAntMinerPB
```

An extension to the cAnt-MinerPB in order to discover unordered rules to improve the interpretation of individual rules. In an ordered list of rules, the effect (meaning) of a rule depends on all previous rules in the list, since a rule is only used if all previous rules do not cover the example. On the other hand, in an unordered set of rules, an example is shown to all rules and, depending on the conflict resolution strategy, a single rule is used to make a prediction.

##### Ant-Tree-Miner
```
Main class: myra.tree.AntTreeMiner
```

A decision tree induction algorithm that uses an ACO procedure to creates decision trees. Trees are created in a top-down fashion, similar to C4.5 strategy, but instead of using a greedy procedure based on the information gain, it select decision nodes using an ACO procedure.

### Running the algorithms

All algorihtm can be used in the command line:

```
java -cp myra-<version>.jar <main class> -f <arff training file>
```

where `<version>` is MYRA version number (e.g., `4.0`), `<main class>` is the main class name of the algorithm and `<aff training file>` is the path to the ARFF file to be used as training data. The minimum requirement to run an algorihtm is a training file. If no training file is specified, a list of options is printed:

```
[febo@uok myra]$ java -cp myra-4.0.jar myra.rule.pittsburgh.cAntMinerPB

Usage: cAntMinerPB -f <arff_training_file> [-t <arff_test_file>] [options]

The minimum required parameter is a training file to build the model from. If a 
test file is specified, the model will be tested at the end of training. The 
results are presented in a confusion matrix. 

The following options are available:

  -c <size>             specify the size of the colony (default 5) 

  -d <method>           specify the discretisation [c45 | mdl] (default mdl) 

  -e <factor>           set the MAX-MIN evaporation factor (default 0.9) 

  -g                    enables the dynamic heuristic computation 

  -h <method>           specify the heuristic method [gain | none] (default 
                        gain) 

  -i <number>           set the maximum number of iterations (default 500) 

  -l <function>         specify the rule list quality function [accuracy | 
                        pessimistic] (default pessimistic) 

  -m <number>           set the minimum number of covered examples per rule 
                        (default 10) 

  -p <method>           specify the rule pruner method [backtrack | greedy] 
                        (default backtrack) 

  -r <function>         specify the rule quality function [laplace | sen_spe] 
                        (default sen_spe) 

  -s <seed>             Random seed value (default current time) 

  -u <percentage>       set the percentage of allowed uncovered examples 
                        (default 0.01) 

  -x <iterations>       set the number of iterations for convergence test 
                        (default 40) 

  --parallel <cores>    enable parallel execution in multiple cores; if no cores 
                        are specified, use all available cores 
```
Usinng command-line options you can tweak the parameters of an algorithm. Note that when running the algorithm in parallel (`--parallel` option), there is no guarantee that it will have the same behaviour even if the same seed value is used (`-s` option), since the thread allocation is not controlled by the code.

### Citation Policy

If you publish material based on algorithms from MYRA, please include a reference to the paper describing the algorithm. All papers are available [online](http://cs.kent.ac.uk/~febo).

There is no specific way to cite the MYRA repository. If you would like to make a referecence to the repository, please either include a note in your acknowledgements and/or a citation to:
```
@MISC{otero:myra,
    author = "F.E.B. Otero",
    year   = "2015",
    title  = "{MYRA}: an {ACO} framework for classification",
    url    = "https://github.com/febo/myra",
    note   = "Available online at: https://github.com/febo/myra"
}
```

##### Ant-Miner

* R.S. Parpinelli, H.S. Lopes and A.A. Freitas. Data Mining with an Ant Colony Optimization Algorithm. In: IEEE Transactions on Evolutionary Computation, Volume 6, Issue 4, pp. 321-332. IEEE, 2002.

```
@ARTICLE{Parpinelli02datamining,
    author  = {R.S. Parpinelli and H.S. Lopes and A.A. Freitas},
    title   = {Data Mining with an Ant Colony Optimization Algorithm},
    journal = {IEEE Transactions on Evolutionary Computation},
    year    = {2002},
    volume  = {6},
    number  = {4},
    pages   = {321--332}
}
```
##### *c*Ant-Miner

* F.E.B. Otero, A.A. Freitas and C.G. Johnson. cAnt-Miner: an ant colony classification algorithm to cope with continuous attributes. In: Ant Colony Optimization and Swarm Intelligence (Proc. ANTS 2008), Lecture Notes in Computer Science 5217, pp. 48-59. Springer, 2008.
```
@INPROCEEDINGS{Otero08datamining,
    author    = {F.E.B. Otero and A.A. Freitas and C.G. Johnson},
    title     = {\emph{c}{A}nt-{M}iner: an ant colony classification algorithm to cope with continuous attributes},
    booktitle = {Proceedings of the 6th International Conference on Swarm Intelligence (ANTS 2008), Lecture Notes in Computer Science 5217},
    editor    = {M. Dorigo and M. Birattari and C. Blum and M. Clerc and T. St{\" u}tzle and A.F.T. Winfield},
    publisher = {Springer-Verlag},
    pages     = {48--59},
    year      = {2008}
}
```

* F.E.B. Otero, A.A. Freitas and C.G. Johnson. Handling continuous attributes in ant colony classification algorithms. In: Proceedings of the 2009 IEEE Symposium on Computational Intelligence in Data Mining (CIDM 2009), pp. 225-231. IEEE, 2009.
```
@INPROCEEDINGS{Otero09datamining,
    author    = {F.E.B. Otero and A.A. Freitas and C.G. Johnson},
    title     = {Handling continuous attributes in ant colony classification algorithms},
    booktitle = {Proceedings of the 2009 IEEE Symposium on Computational Intelligence in Data Mining (CIDM 2009)},
    publisher = {IEEE},
    pages     = {225--231},
    year      = {2009}
}
```

##### *c*Ant-Miner<sub>PB</sub>

* F.E.B. Otero, A.A. Freitas and C.G. Johnson. A New Sequential Covering Strategy for Inducing Classification Rules with Ant Colony Algorithms. In: IEEE Transactions on Evolutionary Computation, Volume 17, Issue 1, pp. 64-74. IEEE, 2013.
```
@ARTICLE{Otero13covering,
    author  = {F.E.B. Otero and A.A. Freitas and C.G. Johnson},
    title   = {A New Sequential Covering Strategy for Inducing Classification Rules with Ant Colony Algorithms},
    journal = {IEEE Transactions on Evolutionary Computation},
    year    = {2013},
    volume  = {17},
    number  = {1},
    pages   = {64--74}
}
```

* M. Medland, F.E.B. Otero and A.A. Freitas. Improving the cAnt-MinerPB Classification Algorithm. In: Swarm Intelligence (Proc. ANTS 2012), Lecture Notes in Computer Science 7461, pp. 73-84. Springer, 2012.
```
@INPROCEEDINGS{Medland12datamining,
    author    = {M. Medland and F.E.B. Otero and A.A. Freitas},
    title     = {Improving the $c$Ant-Miner$_{\mathrm{PB}}$ Classification Algorithm},
    booktitle = {Swarm Intelligence, Lecture Notes in Computer Science 7461},
    editor    = {M. Dorigo and M. Birattari and C. Blum and A.L. Christensen and A.P. Engelbrecht and R. Gro{\ss} and T. St{\"u}tzle},
    publisher = {Springer-Verlag},
    pages     = {73–-84},
    year      = {2012}
}
```

##### Unordered *c*Ant-Miner<sub>PB</sub>

* F.E.B. Otero and A.A. Freitas. Improving the Interpretability of Classification Rules Discovered by an Ant Colony Algorithm: Extended Results. Evolutionary Computation (accepted for publication), MIT Press, 2015.
```
@ARTICLE{Otero15evco,
    author  = {F.E.B. Otero and A.A. Freitas},
    title   = {Improving the Interpretability of Classification Rules Discovered by an Ant Colony Algorithm: Extended Results},
    journal = {Evolutionary Computation},
    year    = {2015},
    note    = {Accepted for publication}
}
```

* F.E.B. Otero and A.A. Freitas. Improving the Interpretability of Classification Rules Discovered by an Ant Colony Algorithm. In: Proceedings of the Genetic and Evolutionary Computation Conference (GECCO '13), pp. 73-80, 2013.
```
@INPROCEEDINGS{Otero13unordered,
    author    = {F.E.B. Otero and A.A. Freitas},
    title     = {Improving the Interpretability of Classification Rules Discovered by an Ant Colony Algorithm},
    booktitle = {Proceedings of the Genetic and Evolutionary Computation Conference (GECCO'13)},
    publisher = {ACM Press},
    pages     = {73--80},
    year      = {2013}
}
```

##### Ant-Tree-Miner

* F.E.B. Otero, A.A. Freitas and C.G. Johnson. Inducing Decision Trees with An Ant Colony Optimization Algorithm. Applied Soft Computing, Volume 12, Issue 11, pp. 3615-3626, 2012.
```
@ARTICLE{Otero12tree,
    author  = {F.E.B. Otero, A.A. Freitas and C.G. Johnson},
    title   = {Inducing Decision Trees with An Ant Colony Optimization Algorithm},
    journal = {Applied Soft Computing},
    year    = {2012},
    volume  = {12},
    number  = {11},
    pages   = {3615--3626}
}
```
