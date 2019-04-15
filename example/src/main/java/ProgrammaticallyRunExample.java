import com.artglorin.mai.diplom.core.Application;
import com.artglorin.mai.diplom.core.DefaultModuleLoaderFactory;
import com.artglorin.mai.diplom.core.MultiplyModuleLoaderImpl;
import com.artglorin.mai.diplom.synoptic.modules.datahandlers.Mathematician;
import com.artglorin.mai.diplom.synoptic.modules.datahandlers.Optimist;
import com.artglorin.mai.diplom.synoptic.modules.datahandlers.Pessimist;
import com.artglorin.mai.diplom.synoptic.modules.datahandlers.Synoptic;
import com.artglorin.mai.diplom.synoptic.modules.datalisteners.ConsoleOutputModule;
import com.artglorin.mai.diplom.synoptic.modules.datalisteners.FileOutputModule;
import com.artglorin.mai.diplom.synoptic.modules.datasources.JsonDataSource;
import com.artglorin.mai.diplom.synoptic.modules.solutions.WeightsSolutionModule;
import com.artglorin.mai.diplom.synoptic.modules.taskmanagers.SimpleTaskManger;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 19/05/2018
 */
public class ProgrammaticallyRunExample {
    public static void main(String[] args) {

        new Application(new MultiplyModuleLoaderImpl(new DefaultModuleLoaderFactory()))
                .startWithModules(
                        new SimpleTaskManger(),
                        Collections.singletonList(new JsonDataSource()),
                        Arrays.asList(new Optimist(), new Pessimist(), new Mathematician(), new Synoptic()),
                        Arrays.asList(new ConsoleOutputModule(), new FileOutputModule()),
                        new WeightsSolutionModule()
                );

    }
}
