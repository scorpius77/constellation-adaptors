package au.gov.asd.tac.constellation.functionality.adaptors.dataaccess.plugins.hopping;

/*
 * Copyright 2010-2019 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import au.gov.asd.tac.constellation.functionality.adaptors.dataaccess.plugins.DataAccessPluginAdaptorType;
import au.gov.asd.tac.constellation.functionality.adaptors.dataaccess.plugins.utilities.GDELTHoppingUtilities;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * RRead graph data from a GDELT file and add it to a graph.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"HOP"})
@Messages("HopFromGDELTPlugin=Hop From GDELT Knowledge Graph")
public class HopFromGDELTPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    // plugin parameters

    @Override
    public String getType() {
        return DataAccessPluginAdaptorType.HOP;
    }

    @Override
    public int getPosition() {
        return 500;
    }

    @Override
    public String getDescription() {
        return "Chain on graph entities to import from GDELT";
    }
    
    public static final String LOCAL_DATE_PARAMETER_ID = PluginParameter.buildId(HopFromGDELTPlugin.class, "local_date");
    public static final String CHOICE_PARAMETER_ID = PluginParameter.buildId(HopFromGDELTPlugin.class, "choice");
    public static final String LIMIT_PARAMETER_ID = PluginParameter.buildId(HopFromGDELTPlugin.class, "limit");
    

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        /**
         * The date to read from
         */
        final PluginParameter<LocalDateParameterType.LocalDateParameterValue> date = LocalDateParameterType.build(LOCAL_DATE_PARAMETER_ID);
        date.setName("Date");
        date.setDescription("Pick a day to import");
        date.setLocalDateValue(LocalDate.of(2020, Month.JANUARY, 1));
        params.addParameter(date);
        
        final PluginParameter<MultiChoiceParameterValue> choices = MultiChoiceParameterType.build(CHOICE_PARAMETER_ID);
        choices.setName("Relationship Options");
        choices.setDescription("Choose which relationship types to hop on");
        MultiChoiceParameterType.setOptions(choices, Arrays.asList(
                "Person - Person", 
                "Person - Organisation", 
                "Person - Theme", 
                "Person - Location",
                "Person - Source", 
                "Person - URL", 
                "Organisation - Organisation", 
                "Organisation - Theme", 
                "Organisation - Source", 
                "Organisation - URL"));
        final List<String> checked = new ArrayList<>();
        checked.add("Person - Theme");
        MultiChoiceParameterType.setChoices(choices, checked);
        params.addParameter(choices);
        
        final PluginParameter<IntegerParameterType.IntegerParameterValue> limit = IntegerParameterType.build(LIMIT_PARAMETER_ID);
        limit.setName("Limit");
        limit.setDescription("Maximum number of results to import");
        IntegerParameterType.setMinimum(limit, 1);
        IntegerParameterType.setMaximum(limit, 50000);
        limit.setIntegerValue(20000);
        params.addParameter(limit);
        
        return params;
    }

    @Override
    protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final RecordStore edgeRecords = new GraphRecordStore();

        interaction.setProgress(0, 0, "Hopping...", true);
        /**
         * Initialize variables
         */
        final LocalDate localDate = parameters.getLocalDateValue(LOCAL_DATE_PARAMETER_ID);
        final MultiChoiceParameterValue choices = parameters.getMultiChoiceValue(CHOICE_PARAMETER_ID);
        final List<String> options = choices.getChoices();
        final int limit = parameters.getIntegerValue(LIMIT_PARAMETER_ID);
        
        final List<String> labels = query.getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL);
        RecordStore results = null;
        
        if (localDate != null) {            
            try {
                results = GDELTHoppingUtilities.hopRelationships(localDate, options, limit, labels);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (results != null) {
            interaction.setProgress(1, 0, "Completed successfully - added " + results.size() + " entities.", true);
            return results;
        }
        else {
            interaction.setProgress(1, 0, "Something went wrong - added 0 entities.", true);
            return new GraphRecordStore();

        }
    }
}
