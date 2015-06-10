# MYRA

MYRA is a collection of Ant Colony Optimization (ACO) algorithms for the data mining classification task. It includes popular rule induction and decision tree induction algorithms. The algorithms are ready to be used from the command line or can be easily called from your own Java code. They are build using a modular architecture, so they can be easily extended to incorporate different procedures and/or use different parameter values.

This repository contains a complete rewrite of the code from the MYRA project hosted at [sourceforge](http://sourceforge.net/projects/myra/). The computational time has been significantly improved – tasks that used to take minutes, now are done in seconds – although it was not possible to maintain backward compatibility. You will find that the overall architecture is very similar, but most of the data structures have changed.

### Algorithms

The following algorithms are implemented:

######Ant-Miner
`Main class: myra.rule.irl.AntMiner`

The first rule induction ACO classification algorithm. Ant-Miner uses a sequentical covering strategy combined with an ACO search to create a list of rules. Ant-Miner only supports categorical attributes, continuous attributes need to be discretised in a pre-processing step.

######*c*Ant-Miner
`Main class: myra.rule.irl.cAntMiner`

An extension of Ant-Miner to cope with continuous attributes. It works essentially the same as Ant-Miner, but continuous attributes undergo a dynamic discretisation process when selected to be included in a rule.

###### *c*Ant-Miner<sub>PB</sub>
`Main class: myra.rule.pittsburgh.cAntMinerPB`

*c*Ant-Miner<sub>PB</sub> incorporates  a new strategy to discover a list of classification rules, which guides the search performed by the ACO algorithm using the quality of a candidate list of rules, instead of a single rule. The main motivation is to avoid the problem of rule interaction derived from the order in which the rules are discovered – i.e., the outcome of a rule affects the rules that can be discovered subsequently since the search space is modified due to the removal of examples covered by previous rules.

###### Unordered *c*Ant-Miner<sub>PB</sub>
`Main class: myra.rule.pittsburgh.unordered.UcAntMinerPB`

An extension to the cAnt-MinerPB in order to discover unordered rules to improve the interpretation of individual rules. In an ordered list of rules, the effect (meaning) of a rule depends on all previous rules in the list, since a rule is only used if all previous rules do not cover the example. On the other hand, in an unordered set of rules, an example is shown to all rules and, depending on the conflict resolution strategy, a single rule is used to make a prediction.

###### Ant-Tree-Miner
`Main class: myra.tree.AntTreeMiner`

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

