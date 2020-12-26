/*
 * MixedAttributeAntMiner.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2020 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra.algorithm;

import static myra.Archive.ARCHIVE_SIZE;
import static myra.Archive.DEFAULT_Q;
import static myra.Archive.Q;
import static myra.Config.CONFIG;
import static myra.datamining.VariableArchive.CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.DEFAULT_CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.PRECISION;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.classification.ClassificationModel;
import myra.classification.rule.SinglePassPruner;
import myra.datamining.Dataset;
import myra.rule.Heuristic;
import myra.rule.Pruner;
import myra.rule.archive.ArchivePheromonePolicy;
import myra.rule.archive.ArchiveRuleFactory;
import myra.rule.archive.Graph;
import myra.rule.irl.SequentialCovering;

/**
 * Default executable class file for the <code>Mixed-Attribute Ant-Miner</code>
 * algorithm. The algorithms is based on the work:
 *
 * <pre>
 * &#64;INPROCEEDINGS{Helal2016GECCO,
 *    author    = {A. Helal and F.E.B. Otero},
 *    title     = {A Mixed-Attribute Approach in Ant-Miner Classification Rule Discovery Algorithm},
 *    booktitle = {Proceedings of the 2016 Annual Conference on Genetic and Evolutionary Computation (GECCO'16)},
 *    publisher = {ACM Press},
 *    pages     = {13--20},
 *    year      = {2016}
 * }
 * </pre>
 * 
 * The difference in this implementation is the use of a hybrid construction
 * graph, where vertices represent attributes and attribute tests are created
 * using individual solution archives.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class MixedAttributeAntMiner extends AntMiner {
    @Override
    protected void defaults() {
        super.defaults();

        // configuration not set via command line

        CONFIG.set(P_BEST, 0.05);
        CONFIG.set(DEFAULT_HEURISTIC, new Heuristic.None());
        CONFIG.set(DEFAULT_FACTORY, new ArchiveRuleFactory());
        CONFIG.set(DEFAULT_POLICY, new ArchivePheromonePolicy());
        CONFIG.set(PRECISION, 2.0);

        // default configuration values

        CONFIG.set(EVAPORATION_FACTOR, 0.9);
        CONFIG.set(DEFAULT_PRUNER, new SinglePassPruner());
        CONFIG.set(ARCHIVE_SIZE, 20);
        CONFIG.set(Q, DEFAULT_Q);
        CONFIG.set(CONVERGENCE_SPEED, DEFAULT_CONVERGENCE_SPEED);
    }

    @Override
    protected Collection<Option<?>> options() {
        ArrayList<Option<?>> options = new ArrayList<Option<?>>();

        for (Option<?> option : super.options()) {
            if (option.getKey() != DEFAULT_HEURISTIC
                    && option.getKey() != DYNAMIC_HEURISTIC) {
                options.add(option);
            }
        }

        // evaporation factor
        options.add(new DoubleOption(EVAPORATION_FACTOR,
                                     "e",
                                     "set the MAX-MIN evaporation %s",
                                     "factor"));

        // rule pruner
        Option<Pruner> pruner = new Option<Pruner>(DEFAULT_PRUNER,
                                                   "p",
                                                   "specify the rule pruner %s",
                                                   true,
                                                   "method");
        pruner.add("single-pass", CONFIG.get(DEFAULT_PRUNER));
        pruner.add("none", new Pruner.None());
        options.add(pruner);

        // archive size
        options.add(new IntegerOption(ARCHIVE_SIZE,
                                      "a",
                                      "set the archive %s",
                                      "size"));

        // convergence speed
        options.add(new DoubleOption(CONVERGENCE_SPEED,
                                     "v",
                                     "specify the converge speed %s",
                                     "value"));

        // weight calculation q
        options.add(new DoubleOption(Q,
                                     "q",
                                     "specify the influence of best quality solutions %s",
                                     "value"));

        return options;
    }

    @Override
    public ClassificationModel train(Dataset dataset) {
        SequentialCovering seco = new SequentialCovering();
        return new ClassificationModel(seco.train(dataset, new Graph(dataset)));
    }

    @Override
    public String description() {
        return "Mixed-Attribute Ant-Miner";
    }

    /**
     * <code>Ant-Miner<sub>MA</sub></code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
        MixedAttributeAntMiner algorithm = new MixedAttributeAntMiner();
        algorithm.run(args);
    }
}