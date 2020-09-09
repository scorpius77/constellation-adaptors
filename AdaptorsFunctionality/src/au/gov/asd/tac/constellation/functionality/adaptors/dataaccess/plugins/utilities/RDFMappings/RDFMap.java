/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.adaptors.dataaccess.plugins.utilities.RDFMappings;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Nova
 */
public class RDFMap extends AbstractRDFMap{
    
    private static final RDFMap instance = new RDFMap();

    private RDFMap() {
        this.NAME = "None";
        System.out.println("Singleton is Instantiated."); 
    }
    
    public static synchronized RDFMap getInstance(){
        return instance;
    }
    
    public Map<String, String> getPrefixes(){
        return new HashMap<String, String>();
    };
    
    public Map<String, String> getPredicateMap(){
        return new HashMap<String, String>();
    };
            
}
